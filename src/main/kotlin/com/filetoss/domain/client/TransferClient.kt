package com.filetoss.domain.client

import com.filetoss.domain.model.AuthCredentials
import com.filetoss.domain.model.RemoteFileInfo
import com.filetoss.domain.model.TransferProfile
import com.filetoss.domain.model.TransferProgress
import com.filetoss.domain.model.TransferProtocol
import com.filetoss.domain.model.TransferResult
import java.nio.file.Path

interface TransferClient : AutoCloseable {
    val protocol: TransferProtocol
    val isConnected: Boolean

    suspend fun connect(profile: TransferProfile, credentials: AuthCredentials)
    suspend fun disconnect()

    suspend fun upload(
        localPath: Path,
        remoteDirectory: String,
        onProgress: (TransferProgress) -> Unit
    ): TransferResult

    suspend fun download(
        remoteFilePath: String,
        localDirectory: Path,
        onProgress: (TransferProgress) -> Unit
    ): TransferResult

    suspend fun listRemoteFiles(remotePath: String): List<RemoteFileInfo>

    suspend fun testConnection(profile: TransferProfile, credentials: AuthCredentials): Boolean
}
