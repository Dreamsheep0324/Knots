package com.tang.prm.data.repository

import com.tang.prm.domain.model.FileEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * [SyncFileDiffCalculator] 表驱动单测。
 *
 * 覆盖：同名同 mtime / 同名不同 mtime（本地新/远端新）/ 仅本地 / 仅远端 /
 * 双空 / 空本地非空远端 / 非空本地空远端 / mtime 边界（相等）。
 */
class SyncFileDiffCalculatorTest {

    private val calculator = SyncFileDiffCalculator()

    private fun entry(name: String, modified: Long = 0L, size: Long = 0L) =
        FileEntry(name = name, size = size, modified = modified, uploadedAt = 0)

    // ===== computeUploadDiff =====

    @Test
    fun `同名同 mtime 不需要上传也不删除远端`() {
        val local = listOf(entry("a.jpg", modified = 100L))
        val remote = mapOf("a.jpg" to entry("a.jpg", modified = 100L))

        val (toUpload, toDeleteRemote) = calculator.computeUploadDiff(local, remote)

        assertTrue(toUpload.isEmpty())
        assertTrue(toDeleteRemote.isEmpty())
    }

    @Test
    fun `本地 mtime 大于远端需要上传`() {
        val local = listOf(entry("a.jpg", modified = 200L))
        val remote = mapOf("a.jpg" to entry("a.jpg", modified = 100L))

        val (toUpload, toDeleteRemote) = calculator.computeUploadDiff(local, remote)

        assertEquals(1, toUpload.size)
        assertEquals("a.jpg", toUpload[0].name)
        assertTrue(toDeleteRemote.isEmpty())
    }

    @Test
    fun `本地 mtime 等于远端不上传`() {
        val local = listOf(entry("a.jpg", modified = 100L))
        val remote = mapOf("a.jpg" to entry("a.jpg", modified = 100L))

        val (toUpload, _) = calculator.computeUploadDiff(local, remote)

        assertTrue(toUpload.isEmpty())
    }

    @Test
    fun `仅本地有需要上传且不删远端`() {
        val local = listOf(entry("a.jpg", modified = 100L))
        val remote = emptyMap<String, FileEntry>()

        val (toUpload, toDeleteRemote) = calculator.computeUploadDiff(local, remote)

        assertEquals(1, toUpload.size)
        assertTrue(toDeleteRemote.isEmpty())
    }

    @Test
    fun `仅远端有需要删除远端`() {
        val local = emptyList<FileEntry>()
        val remote = mapOf("a.jpg" to entry("a.jpg", modified = 100L))

        val (toUpload, toDeleteRemote) = calculator.computeUploadDiff(local, remote)

        assertTrue(toUpload.isEmpty())
        assertEquals(1, toDeleteRemote.size)
        assertEquals("a.jpg", toDeleteRemote[0])
    }

    @Test
    fun `双空无操作`() {
        val (toUpload, toDeleteRemote) = calculator.computeUploadDiff(emptyList(), emptyMap())

        assertTrue(toUpload.isEmpty())
        assertTrue(toDeleteRemote.isEmpty())
    }

    @Test
    fun `混合场景部分上传部分删远端`() {
        val local = listOf(
            entry("keep.jpg", modified = 100L),      // 远端同 mtime，不上传
            entry("new.jpg", modified = 100L),        // 远端无，上传
            entry("updated.jpg", modified = 200L)     // 本地新，上传
        )
        val remote = mapOf(
            "keep.jpg" to entry("keep.jpg", modified = 100L),
            "updated.jpg" to entry("updated.jpg", modified = 100L),
            "orphan.jpg" to entry("orphan.jpg", modified = 100L)  // 本地无，删远端
        )

        val (toUpload, toDeleteRemote) = calculator.computeUploadDiff(local, remote)

        assertEquals(2, toUpload.size)
        val uploadNames = toUpload.map { it.name }.toSet()
        assertTrue("new.jpg" in uploadNames)
        assertTrue("updated.jpg" in uploadNames)
        assertEquals(1, toDeleteRemote.size)
        assertEquals("orphan.jpg", toDeleteRemote[0])
    }

    // ===== computeDownloadDiff =====

    @Test
    fun `同名同 mtime 不需要下载也不删除本地`() {
        val local = listOf(entry("a.jpg", modified = 100L))
        val remote = mapOf("a.jpg" to entry("a.jpg", modified = 100L))

        val (toDownload, toDeleteLocal) = calculator.computeDownloadDiff(local, remote)

        assertTrue(toDownload.isEmpty())
        assertTrue(toDeleteLocal.isEmpty())
    }

    @Test
    fun `远端 mtime 大于本地需要下载`() {
        val local = listOf(entry("a.jpg", modified = 100L))
        val remote = mapOf("a.jpg" to entry("a.jpg", modified = 200L))

        val (toDownload, toDeleteLocal) = calculator.computeDownloadDiff(local, remote)

        assertEquals(1, toDownload.size)
        assertEquals("a.jpg", toDownload[0].name)
        assertTrue(toDeleteLocal.isEmpty())
    }

    @Test
    fun `远端 mtime 等于本地不下载`() {
        val local = listOf(entry("a.jpg", modified = 100L))
        val remote = mapOf("a.jpg" to entry("a.jpg", modified = 100L))

        val (toDownload, _) = calculator.computeDownloadDiff(local, remote)

        assertTrue(toDownload.isEmpty())
    }

    @Test
    fun `仅远端有需要下载且不删本地`() {
        val local = emptyList<FileEntry>()
        val remote = mapOf("a.jpg" to entry("a.jpg", modified = 100L))

        val (toDownload, toDeleteLocal) = calculator.computeDownloadDiff(local, remote)

        assertEquals(1, toDownload.size)
        assertTrue(toDeleteLocal.isEmpty())
    }

    @Test
    fun `仅本地有需要删除本地`() {
        val local = listOf(entry("a.jpg", modified = 100L))
        val remote = emptyMap<String, FileEntry>()

        val (toDownload, toDeleteLocal) = calculator.computeDownloadDiff(local, remote)

        assertTrue(toDownload.isEmpty())
        assertEquals(1, toDeleteLocal.size)
        assertEquals("a.jpg", toDeleteLocal[0])
    }

    @Test
    fun `空本地非空远端全下载`() {
        val local = emptyList<FileEntry>()
        val remote = mapOf(
            "a.jpg" to entry("a.jpg", modified = 100L),
            "b.jpg" to entry("b.jpg", modified = 200L)
        )

        val (toDownload, toDeleteLocal) = calculator.computeDownloadDiff(local, remote)

        assertEquals(2, toDownload.size)
        assertTrue(toDeleteLocal.isEmpty())
    }

    @Test
    fun `非空本地空远端全删本地`() {
        val local = listOf(
            entry("a.jpg", modified = 100L),
            entry("b.jpg", modified = 200L)
        )
        val remote = emptyMap<String, FileEntry>()

        val (toDownload, toDeleteLocal) = calculator.computeDownloadDiff(local, remote)

        assertTrue(toDownload.isEmpty())
        assertEquals(2, toDeleteLocal.size)
    }

    @Test
    fun `下载混合场景部分下载部分删本地`() {
        val local = listOf(
            entry("keep.jpg", modified = 100L),      // 远端同 mtime，不下载
            entry("stale.jpg", modified = 100L),      // 远端无，删本地
            entry("older.jpg", modified = 100L)        // 远端新，下载
        )
        val remote = mapOf(
            "keep.jpg" to entry("keep.jpg", modified = 100L),
            "older.jpg" to entry("older.jpg", modified = 200L),
            "new.jpg" to entry("new.jpg", modified = 100L)  // 本地无，下载
        )

        val (toDownload, toDeleteLocal) = calculator.computeDownloadDiff(local, remote)

        assertEquals(2, toDownload.size)
        val downloadNames = toDownload.map { it.name }.toSet()
        assertTrue("older.jpg" in downloadNames)
        assertTrue("new.jpg" in downloadNames)
        assertEquals(1, toDeleteLocal.size)
        assertEquals("stale.jpg", toDeleteLocal[0])
    }
}
