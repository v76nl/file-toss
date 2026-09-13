package com.filetoss.data.repository

import com.filetoss.domain.model.AuthType
import com.filetoss.domain.model.BauhausColor
import com.filetoss.domain.model.TransferProfile
import com.filetoss.domain.model.TransferProtocol
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class JsonProfileRepositoryTest {

    @Test
    fun testSaveAndLoadProfile(@TempDir tempDir: File) = runBlocking {
        val storageFile = File(tempDir, "test_profiles.json")
        val repository = JsonProfileRepository(storageFile, initDefault = false)

        val profile = TransferProfile(
            id = "test-1",
            name = "Test Server",
            protocol = TransferProtocol.SFTP,
            host = "10.0.0.1",
            port = 2222,
            username = "testuser",
            authType = AuthType.PASSWORD,
            remoteDirectory = "/var/data",
            localDirectory = "/tmp",
            colorTag = BauhausColor.BLUE,
            encryptedPassword = "encrypted_pwd_abc"
        )

        repository.saveProfile(profile)
        repository.setActiveProfileId("test-1")

        val loadedProfiles = repository.getProfiles()
        val active = repository.getActiveProfile()

        assertEquals(1, loadedProfiles.size)
        assertEquals("Test Server", loadedProfiles[0].name)
        assertEquals("test-1", repository.getActiveProfileId())
        assertNotNull(active)
        assertEquals("10.0.0.1", active.host)

        // 削除テスト
        repository.deleteProfile("test-1")
        val emptyProfiles = repository.getProfiles()
        assertEquals(0, emptyProfiles.size)
    }
}
