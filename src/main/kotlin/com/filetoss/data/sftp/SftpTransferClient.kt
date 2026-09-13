package com.filetoss.data.sftp

import com.filetoss.domain.client.TransferClient
import com.filetoss.domain.model.AuthCredentials
import com.filetoss.domain.model.AuthType
import com.filetoss.domain.model.RemoteFileInfo
import com.filetoss.domain.model.TransferProfile
import com.filetoss.domain.model.TransferProgress
import com.filetoss.domain.model.TransferProtocol
import com.filetoss.domain.model.TransferResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.common.StreamCopier
import net.schmizz.sshj.sftp.SFTPClient
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import net.schmizz.sshj.xfer.FileSystemFile
import net.schmizz.sshj.xfer.TransferListener
import java.io.File
import java.nio.file.Path
import kotlin.math.max

class SftpTransferClient : TransferClient {
    override val protocol: TransferProtocol = TransferProtocol.SFTP

    private var sshClient: SSHClient? = null
    private var sftpClient: SFTPClient? = null
    private var currentProfile: TransferProfile? = null

    override val isConnected: Boolean
        get() = sshClient?.isConnected == true && sshClient?.isAuthenticated == true

    override suspend fun connect(profile: TransferProfile, credentials: AuthCredentials): Unit = withContext(Dispatchers.IO) {
        disconnect()
        val client = SSHClient()
        client.addHostKeyVerifier(PromiscuousVerifier())
        client.connect(profile.host, profile.port)

        when (profile.authType) {
            AuthType.PASSWORD -> {
                val pwd = credentials.password ?: ""
                client.authPassword(profile.username, pwd)
            }
            AuthType.PRIVATE_KEY -> {
                val keyPath = profile.privateKeyPath ?: throw IllegalArgumentException("秘密鍵パスが指定されていません")
                val keyFile = File(keyPath)
                if (!keyFile.exists()) {
                    throw IllegalArgumentException("指定された秘密鍵ファイルが存在しません: $keyPath")
                }
                val keyProvider = if (!credentials.passphrase.isNullOrEmpty()) {
                    client.loadKeys(keyFile.absolutePath, credentials.passphrase.toCharArray())
                } else {
                    client.loadKeys(keyFile.absolutePath)
                }
                client.authPublickey(profile.username, keyProvider)
            }
        }

        sshClient = client
        sftpClient = client.newSFTPClient()
        currentProfile = profile
    }

    override suspend fun disconnect(): Unit = withContext(Dispatchers.IO) {
        try {
            sftpClient?.close()
        } catch (_: Exception) {
        } finally {
            sftpClient = null
        }

        try {
            sshClient?.disconnect()
            sshClient?.close()
        } catch (_: Exception) {
        } finally {
            sshClient = null
            currentProfile = null
        }
    }

    override suspend fun upload(
        localPath: Path,
        remoteDirectory: String,
        onProgress: (TransferProgress) -> Unit
    ): TransferResult = withContext(Dispatchers.IO) {
        val sftp = sftpClient ?: return@withContext TransferResult.Failure(
            IllegalStateException("SFTPクライアントが未接続です"),
            "サーバーに接続されていません"
        )

        val localFile = localPath.toFile()
        if (!localFile.exists()) {
            return@withContext TransferResult.Failure(
                IllegalArgumentException("ローカルファイルが存在しません: $localPath"),
                "ローカルファイルが見つかりません"
            )
        }

        val startTime = System.currentTimeMillis()
        var totalBytesCalculated = 0L
        var totalFilesCount = 0

        fun scan(file: File) {
            if (file.isDirectory) {
                file.listFiles()?.forEach { scan(it) }
            } else {
                totalBytesCalculated += file.length()
                totalFilesCount++
            }
        }
        scan(localFile)

        var totalTransferredSoFar = 0L

        try {
            ensureRemoteDirectory(sftp, remoteDirectory)

            val listener = object : TransferListener {
                private var fileStartBytes = 0L
                private var currentFileTotal = 0L
                private var lastReportTime = 0L
                private var lastReportBytes = 0L

                override fun directory(name: String?): TransferListener = this

                override fun file(name: String?, size: Long): StreamCopier.Listener {
                    currentFileTotal = size
                    fileStartBytes = totalTransferredSoFar
                    lastReportTime = System.currentTimeMillis()
                    lastReportBytes = totalTransferredSoFar

                    return StreamCopier.Listener { transferredInThisFile: Long ->
                        val currentTotal = fileStartBytes + transferredInThisFile
                        val now = System.currentTimeMillis()
                        val elapsed = max(1L, now - lastReportTime)
                        val bytesDiff = currentTotal - lastReportBytes

                        val speed = if (elapsed > 0) (bytesDiff * 1000) / elapsed else 0L

                        if (now - lastReportTime >= 100 || currentTotal >= totalBytesCalculated) {
                            onProgress(
                                TransferProgress(
                                    currentFileName = name ?: localFile.name,
                                    transferredBytes = currentTotal,
                                    totalBytes = totalBytesCalculated,
                                    bytesPerSecond = speed,
                                    isComplete = false
                                )
                            )
                            lastReportTime = now
                            lastReportBytes = currentTotal
                        }
                    }
                }
            }

            sftp.fileTransfer.transferListener = listener
            val targetRemotePath = remoteDirectory.trimEnd('/') + "/" + localFile.name

            if (localFile.isDirectory) {
                uploadDirectoryRecursive(sftp, localFile, remoteDirectory.trimEnd('/'), listener) { added ->
                    totalTransferredSoFar += added
                }
            } else {
                sftp.put(FileSystemFile(localFile), targetRemotePath)
            }

            val duration = System.currentTimeMillis() - startTime
            onProgress(
                TransferProgress(
                    currentFileName = localFile.name,
                    transferredBytes = totalBytesCalculated,
                    totalBytes = totalBytesCalculated,
                    bytesPerSecond = 0,
                    isComplete = true
                )
            )

            TransferResult.Success(
                transferredFiles = max(1, totalFilesCount),
                totalBytes = totalBytesCalculated,
                durationMs = duration
            )
        } catch (e: Throwable) {
            TransferResult.Failure(e, e.message ?: "転送中にエラーが発生しました")
        }
    }

    private fun uploadDirectoryRecursive(
        sftp: SFTPClient,
        localDir: File,
        remoteParent: String,
        listener: TransferListener,
        onFileDone: (Long) -> Unit
    ) {
        val targetRemoteDir = "$remoteParent/${localDir.name}"
        ensureRemoteDirectory(sftp, targetRemoteDir)

        val children = localDir.listFiles() ?: return
        for (child in children) {
            if (child.isDirectory) {
                uploadDirectoryRecursive(sftp, child, targetRemoteDir, listener, onFileDone)
            } else {
                val remoteFilePath = "$targetRemoteDir/${child.name}"
                sftp.put(FileSystemFile(child), remoteFilePath)
                onFileDone(child.length())
            }
        }
    }

    private fun ensureRemoteDirectory(sftp: SFTPClient, remoteDir: String) {
        val normalized = remoteDir.replace('\\', '/').trimEnd('/')
        if (normalized.isEmpty() || normalized == "/") return

        val parts = normalized.split('/').filter { it.isNotEmpty() }
        var current = if (normalized.startsWith("/")) "" else "."

        for (part in parts) {
            current = if (current == "/" || current.isEmpty()) "/$part" else "$current/$part"
            try {
                sftp.stat(current)
            } catch (_: Exception) {
                try {
                    sftp.mkdir(current)
                } catch (_: Exception) {
                }
            }
        }
    }

    override suspend fun download(
        remoteFilePath: String,
        localDirectory: Path,
        onProgress: (TransferProgress) -> Unit
    ): TransferResult = withContext(Dispatchers.IO) {
        val sftp = sftpClient ?: return@withContext TransferResult.Failure(
            IllegalStateException("SFTPクライアントが未接続です"),
            "サーバーに接続されていません"
        )

        try {
            val localDirFile = localDirectory.toFile()
            if (!localDirFile.exists()) {
                localDirFile.mkdirs()
            }

            val stat = sftp.stat(remoteFilePath)
            val fileName = remoteFilePath.substringAfterLast('/')
            val localTargetFile = File(localDirFile, fileName)
            val startTime = System.currentTimeMillis()

            var lastReportTime = startTime
            var lastReportBytes = 0L

            sftp.fileTransfer.transferListener = object : TransferListener {
                override fun directory(name: String?): TransferListener = this
                override fun file(name: String?, size: Long): StreamCopier.Listener {
                    return StreamCopier.Listener { transferred: Long ->
                        val now = System.currentTimeMillis()
                        val elapsed = max(1L, now - lastReportTime)
                        val bytesDiff = transferred - lastReportBytes
                        val speed = if (elapsed > 0) (bytesDiff * 1000) / elapsed else 0L

                        if (now - lastReportTime >= 100 || transferred >= size) {
                            onProgress(
                                TransferProgress(
                                    currentFileName = fileName,
                                    transferredBytes = transferred,
                                    totalBytes = size,
                                    bytesPerSecond = speed,
                                    isComplete = false
                                )
                            )
                            lastReportTime = now
                            lastReportBytes = transferred
                        }
                    }
                }
            }

            sftp.get(remoteFilePath, FileSystemFile(localTargetFile))
            val duration = System.currentTimeMillis() - startTime

            onProgress(
                TransferProgress(
                    currentFileName = fileName,
                    transferredBytes = stat.size,
                    totalBytes = stat.size,
                    bytesPerSecond = 0,
                    isComplete = true
                )
            )

            TransferResult.Success(
                transferredFiles = 1,
                totalBytes = stat.size,
                durationMs = duration
            )
        } catch (e: Throwable) {
            TransferResult.Failure(e, e.message ?: "ダウンロード中にエラーが発生しました")
        }
    }

    override suspend fun listRemoteFiles(remotePath: String): List<RemoteFileInfo> = withContext(Dispatchers.IO) {
        val sftp = sftpClient ?: throw IllegalStateException("SFTPクライアントが未接続です")
        val entries = sftp.ls(remotePath)
        entries.filterNot { it.name == "." || it.name == ".." }.map { entry ->
            RemoteFileInfo(
                name = entry.name,
                fullPath = entry.path,
                size = entry.attributes.size,
                isDirectory = entry.isDirectory,
                modifiedTime = entry.attributes.mtime * 1000L
            )
        }.sortedWith(compareByDescending<RemoteFileInfo> { it.isDirectory }.thenBy { it.name.lowercase() })
    }

    override suspend fun testConnection(profile: TransferProfile, credentials: AuthCredentials): Boolean = withContext(Dispatchers.IO) {
        val testClient = SSHClient()
        testClient.addHostKeyVerifier(PromiscuousVerifier())
        try {
            testClient.connect(profile.host, profile.port)
            when (profile.authType) {
                AuthType.PASSWORD -> {
                    testClient.authPassword(profile.username, credentials.password ?: "")
                }
                AuthType.PRIVATE_KEY -> {
                    val keyPath = profile.privateKeyPath ?: return@withContext false
                    val keyFile = File(keyPath)
                    if (!keyFile.exists()) return@withContext false
                    val provider = if (!credentials.passphrase.isNullOrEmpty()) {
                        testClient.loadKeys(keyFile.absolutePath, credentials.passphrase.toCharArray())
                    } else {
                        testClient.loadKeys(keyFile.absolutePath)
                    }
                    testClient.authPublickey(profile.username, provider)
                }
            }
            testClient.isConnected && testClient.isAuthenticated
        } catch (_: Throwable) {
            false
        } finally {
            try {
                testClient.disconnect()
                testClient.close()
            } catch (_: Throwable) {
            }
        }
    }

    override fun close() {
        try {
            sftpClient?.close()
        } catch (_: Throwable) {
        }
        try {
            sshClient?.disconnect()
            sshClient?.close()
        } catch (_: Throwable) {
        }
    }
}
