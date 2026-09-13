package com.filetoss.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class TransferProtocol {
    SFTP,
    SCP
}

@Serializable
enum class AuthType {
    PASSWORD,
    PRIVATE_KEY
}

@Serializable
enum class BauhausColor {
    RED,
    BLUE,
    YELLOW
}

@Serializable
data class TransferProfile(
    val id: String,
    val name: String,
    val protocol: TransferProtocol = TransferProtocol.SFTP,
    val host: String,
    val port: Int = 22,
    val username: String,
    val authType: AuthType,
    val privateKeyPath: String? = null,
    val remoteDirectory: String,
    val localDirectory: String,
    val colorTag: BauhausColor = BauhausColor.RED,
    val encryptedPassword: String? = null,
    val encryptedPassphrase: String? = null
)

data class AuthCredentials(
    val password: String? = null,
    val passphrase: String? = null
)

data class TransferProgress(
    val currentFileName: String,
    val transferredBytes: Long,
    val totalBytes: Long,
    val bytesPerSecond: Long,
    val isComplete: Boolean = false
) {
    val percentage: Float
        get() = if (totalBytes > 0) (transferredBytes.toFloat() / totalBytes).coerceIn(0f, 1f) else 0f

    val formattedTransferred: String
        get() = formatBytes(transferredBytes)

    val formattedTotal: String
        get() = formatBytes(totalBytes)

    val formattedSpeed: String
        get() = "${formatBytes(bytesPerSecond)}/s"

    companion object {
        fun formatBytes(bytes: Long): String {
            if (bytes < 1024) return "$bytes B"
            val kb = bytes / 1024.0
            if (kb < 1024) return String.format("%.1f KB", kb)
            val mb = kb / 1024.0
            if (mb < 1024) return String.format("%.1f MB", mb)
            val gb = mb / 1024.0
            return String.format("%.2f GB", gb)
        }
    }
}

data class RemoteFileInfo(
    val name: String,
    val fullPath: String,
    val size: Long,
    val isDirectory: Boolean,
    val modifiedTime: Long
) {
    val formattedSize: String
        get() = if (isDirectory) "DIR" else TransferProgress.formatBytes(size)
}

sealed interface TransferResult {
    data class Success(
        val transferredFiles: Int,
        val totalBytes: Long,
        val durationMs: Long
    ) : TransferResult

    data class Failure(
        val error: Throwable,
        val message: String
    ) : TransferResult
}
