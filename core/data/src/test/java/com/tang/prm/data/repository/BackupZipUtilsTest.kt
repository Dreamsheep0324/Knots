package com.tang.prm.data.repository

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class BackupZipUtilsTest {

    @TempDir
    lateinit var tempDir: File

    private fun createZipStream(content: String): ZipInputStream {
        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zipOut ->
            zipOut.putNextEntry(java.util.zip.ZipEntry("entry"))
            zipOut.write(content.toByteArray())
            zipOut.closeEntry()
        }
        return ZipInputStream(ByteArrayInputStream(baos.toByteArray()))
    }

    @Test
    fun writeFileFromZip_writesFileWhenInsideAllowedDir() {
        val targetFile = File(tempDir, "inside.txt")
        val zipIn = createZipStream("hello")

        BackupZipUtils.writeFileFromZip(zipIn, targetFile, tempDir)

        assertThat(targetFile.exists()).isTrue()
        assertThat(targetFile.readText()).isEqualTo("hello")
    }

    @Test
    fun writeFileFromZip_blocksPathTraversalWithDotDot() {
        val targetFile = File(tempDir, "../../evil.txt")
        val zipIn = createZipStream("evil")

        BackupZipUtils.writeFileFromZip(zipIn, targetFile, tempDir)

        // 路径遍历被拦截，解析后的目标文件不应被创建
        assertThat(File(targetFile.canonicalPath).exists()).isFalse()
    }

    @Test
    fun writeFileFromZip_writesWhenTargetEqualsAllowedDir() {
        val targetFile = File(tempDir, "target.dat")
        targetFile.createNewFile()
        val zipIn = createZipStream("data")

        // targetFile == allowedDir，不应被路径遍历拦截
        BackupZipUtils.writeFileFromZip(zipIn, targetFile, targetFile)

        assertThat(targetFile.readText()).isEqualTo("data")
    }

    @Test
    fun writeFileFromZip_blocksAbsoluteOutsidePath() {
        val outsideFile = File(System.getProperty("java.io.tmpdir"), "outside_evil_${System.currentTimeMillis()}.txt")
        val zipIn = createZipStream("outside")

        BackupZipUtils.writeFileFromZip(zipIn, outsideFile, tempDir)

        assertThat(outsideFile.exists()).isFalse()
        outsideFile.delete()
    }
}
