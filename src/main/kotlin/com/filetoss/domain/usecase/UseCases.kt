package com.filetoss.domain.usecase

import com.filetoss.domain.client.TransferClient
import com.filetoss.domain.model.RemoteFileInfo
import com.filetoss.domain.model.TransferProgress
import com.filetoss.domain.model.TransferResult
import java.io.File
import java.nio.file.Path

class TossFilesUseCase(private val client: TransferClient) {
    suspend operator fun invoke(
        localFiles: List<File>,
        remoteDirectory: String,
        onProgress: (TransferProgress) -> Unit
    ): TransferResult {
        var totalTransferredFiles = 0
        var totalBytes = 0L
        val startTime = System.currentTimeMillis()

        for (file in localFiles) {
            val result = client.upload(file.toPath(), remoteDirectory, onProgress)
            when (result) {
                is TransferResult.Success -> {
                    totalTransferredFiles += result.transferredFiles
                    totalBytes += result.totalBytes
                }
                is TransferResult.Failure -> {
                    return result
                }
            }
        }

        return TransferResult.Success(
            transferredFiles = totalTransferredFiles,
            totalBytes = totalBytes,
            durationMs = System.currentTimeMillis() - startTime
        )
    }
}

class CatchFilesUseCase(private val client: TransferClient) {
    suspend operator fun invoke(
        remoteFilePath: String,
        localDirectory: Path,
        onProgress: (TransferProgress) -> Unit
    ): TransferResult {
        return client.download(remoteFilePath, localDirectory, onProgress)
    }
}

class ListRemoteFilesUseCase(private val client: TransferClient) {
    suspend operator fun invoke(remotePath: String): List<RemoteFileInfo> {
        return client.listRemoteFiles(remotePath)
    }
}
