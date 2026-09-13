package com.filetoss.data.security

import com.sun.jna.Platform
import com.sun.jna.platform.win32.Crypt32Util
import java.nio.charset.StandardCharsets
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

interface CredentialStore {
    fun encrypt(plainText: String): String
    fun decrypt(cipherText: String): String
}

class WindowsDpapiCredentialStore : CredentialStore {
    private val isWindows = Platform.isWindows()

    // 非Windows環境向けフォールバックキー
    private val fallbackKey = SecretKeySpec("FileTossSecret21".toByteArray(StandardCharsets.UTF_8), "AES")

    override fun encrypt(plainText: String): String {
        if (plainText.isEmpty()) return ""
        return try {
            if (isWindows) {
                val encrypted = Crypt32Util.cryptProtectData(plainText.toByteArray(StandardCharsets.UTF_8))
                Base64.getEncoder().encodeToString(encrypted)
            } else {
                encryptAes(plainText)
            }
        } catch (e: Throwable) {
            encryptAes(plainText)
        }
    }

    override fun decrypt(cipherText: String): String {
        if (cipherText.isEmpty()) return ""
        return try {
            val bytes = Base64.getDecoder().decode(cipherText)
            if (isWindows) {
                val decrypted = Crypt32Util.cryptUnprotectData(bytes)
                String(decrypted, StandardCharsets.UTF_8)
            } else {
                decryptAes(cipherText)
            }
        } catch (e: Throwable) {
            try {
                decryptAes(cipherText)
            } catch (e2: Throwable) {
                ""
            }
        }
    }

    private fun encryptAes(plainText: String): String {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, fallbackKey)
        val encrypted = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
        return Base64.getEncoder().encodeToString(encrypted)
    }

    private fun decryptAes(cipherText: String): String {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, fallbackKey)
        val decrypted = cipher.doFinal(Base64.getDecoder().decode(cipherText))
        return String(decrypted, StandardCharsets.UTF_8)
    }
}
