package com.filetoss.data.security

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class CredentialStoreTest {

    private val store = WindowsDpapiCredentialStore()

    @Test
    fun testEncryptionAndDecryption() {
        val original = "SuperSecretPassword123!"
        val encrypted = store.encrypt(original)

        assertNotEquals(original, encrypted)
        val decrypted = store.decrypt(encrypted)
        assertEquals(original, decrypted)
    }

    @Test
    fun testEmptyString() {
        val original = ""
        val encrypted = store.encrypt(original)
        val decrypted = store.decrypt(encrypted)
        assertEquals("", decrypted)
    }
}
