package com.tang.prm.data.repository

import android.content.Context
import com.google.common.truth.Truth.assertThat
import com.tang.prm.data.local.database.TangDatabase
import com.tang.prm.domain.model.BackupResult
import com.tang.prm.domain.model.RestoreResult
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

    private lateinit var repository: BackupRepository

    @BeforeEach
    fun setUp() {
        repository = BackupRepository(context, database)
    }

    @Test
    fun generateBackupFileName_startsCorrectly() {
        val fileName = repository.generateBackupFileName()

        assertThat(fileName).startsWith("tang_backup_")
        assertThat(fileName).endsWith(".zip")
    }
}
