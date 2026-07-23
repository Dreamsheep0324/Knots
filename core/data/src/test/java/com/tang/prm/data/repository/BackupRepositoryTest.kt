package com.tang.prm.data.repository

import android.content.Context
import com.google.common.truth.Truth.assertThat
import com.tang.prm.data.local.database.TangDatabase
import com.tang.prm.domain.repository.AppRestarter
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File

@ExtendWith(MockKExtension::class)
class BackupRepositoryTest {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var database: TangDatabase

    @MockK
    private lateinit var appRestarter: AppRestarter

    private lateinit var repository: BackupRepositoryImpl

    @BeforeEach
    fun setUp() {
        repository = BackupRepositoryImpl(context, database, appRestarter)
    }

    @Test
    fun generateBackupFileName_startsCorrectly() {
        val fileName = repository.generateBackupFileName()

        assertThat(fileName).startsWith("tang_backup_")
        assertThat(fileName).endsWith(".zip")
    }

    @Test
    fun rollbackDatabase_restoresFromSnapshotAndCleansUp() {
        val tmpDir = File(System.getProperty("java.io.tmpdir"))
        val stamp = System.currentTimeMillis()
        val dbFile = File(tmpDir, "test_rollback_${stamp}.db")
        val snapshot = File(tmpDir, "test_rollback_${stamp}.snapshot")
        dbFile.writeText("original-db-content")
        snapshot.writeText("snapshot-content")

        val result = invokeRollback(dbFile, snapshot, null)

        assertThat(result).isEqualTo("Restored")
        assertThat(dbFile.readText()).isEqualTo("snapshot-content")
        assertThat(snapshot.exists()).isFalse()
        dbFile.delete()
    }

    @Test
    fun rollbackDatabase_cleansHalfWrittenDbWhenNoSnapshot() {
        val tmpDir = File(System.getProperty("java.io.tmpdir"))
        val stamp = System.currentTimeMillis()
        val dbFile = File(tmpDir, "test_rollback_nosnap_${stamp}.db")
        val snapshot = File(tmpDir, "test_rollback_nosnap_${stamp}.snapshot")
        dbFile.writeText("original-db-content")
        // snapshot 不存在：B-12 修复后应删除半写 DB 并返回 Cleaned

        val result = invokeRollback(dbFile, snapshot, null)

        assertThat(result).isEqualTo("Cleaned")
        assertThat(dbFile.exists()).isFalse()
    }

    private fun invokeRollback(dbFile: File, snapshot: File, datastoreSnapshot: File?): String {
        val method = BackupRepositoryImpl::class.java.getDeclaredMethod(
            "rollbackDatabase", File::class.java, File::class.java, File::class.java
        )
        method.isAccessible = true
        val result = method.invoke(repository, dbFile, snapshot, datastoreSnapshot)
        return result!!.javaClass.simpleName
    }
}
