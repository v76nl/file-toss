package com.filetoss.domain

import com.filetoss.domain.model.TransferProgress
import kotlin.test.Test
import kotlin.test.assertEquals

class TransferProgressTest {

    @Test
    fun testPercentageCalculation() {
        val progress = TransferProgress(
            currentFileName = "test.txt",
            transferredBytes = 50,
            totalBytes = 100,
            bytesPerSecond = 10
        )
        assertEquals(0.5f, progress.percentage)
    }

    @Test
    fun testZeroTotalBytesPercentage() {
        val progress = TransferProgress(
            currentFileName = "empty.txt",
            transferredBytes = 0,
            totalBytes = 0,
            bytesPerSecond = 0
        )
        assertEquals(0f, progress.percentage)
    }

    @Test
    fun testFormatBytes() {
        assertEquals("500 B", TransferProgress.formatBytes(500))
        assertEquals("1.0 KB", TransferProgress.formatBytes(1024))
        assertEquals("1.5 MB", TransferProgress.formatBytes((1.5 * 1024 * 1024).toLong()))
        assertEquals("2.00 GB", TransferProgress.formatBytes((2L * 1024 * 1024 * 1024)))
    }
}
