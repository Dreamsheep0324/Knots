package com.tang.prm.data.repository

import android.content.Context
import android.net.Uri
import com.google.common.truth.Truth.assertThat
import com.tang.prm.data.local.database.TangDatabase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
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
    private lateinit var uri: Uri

    private lateinit var repository: BackupRepository

    @BeforeEach
    fun setUp() {
        repository = BackupRepository(context, database)
    }

    @Test
    fun backup_returnsErrorWhenOutputStreamNull() = runTest {
        coEvery { database.checkpoint() } returns Unit
        every { context.getDatabasePath("tang_database") } returns File("/tmp/test.db")
        every { context.contentResolver.openOutputStream(uri) } returns null

        val result = repository.backupToUri(uri).first()

        assertThat(result).isInstanceOf(BackupResult.Error::class.java)
    }

    @Test
    fun restore_returnsErrorWhenInputStreamNull() = runTest {
        coEvery { database.close() } returns Unit
        every { context.getDatabasePath("tang_database") } returns File("/tmp/test.db")
        every { context.filesDir } returns File("/tmp/files")
        every { context.contentResolver.openInputStream(uri) } returns null

        val result = repository.restoreFromUri(uri).first()

        assertThat(result).isInstanceOf(RestoreResult.Error::class.java)
    }

    @Test
    fun generateBackupFileName_startsCorrectly() {
        val fileName = repository.generateBackupFileName()

        assertThat(fileName).startsWith("tang_backup_")
        assertThat(fileName).endsWith(".zip")
    }
}
