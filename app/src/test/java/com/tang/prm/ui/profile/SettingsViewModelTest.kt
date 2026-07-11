package com.tang.prm.ui.profile

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.ThemeMode
import com.tang.prm.domain.repository.AiRepository
import com.tang.prm.domain.repository.SettingsRepository
import com.tang.prm.feature.profile.SettingsViewModel
import com.tang.prm.feature.profile.TestConnectionState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/** Fake AiRepository to avoid MockK's Result double-wrapping issue */
private class FakeAiRepository(
    private val testConnectionResult: Result<String> = Result.success("OK")
) : AiRepository {
    override fun streamChat(systemPrompt: String, userPrompt: String): Flow<String> = flowOf()
    override suspend fun testConnection(): Result<String> = testConnectionResult
}

@ExtendWith(MockKExtension::class)
class SettingsViewModelTest {

    @MockK
    private lateinit var settingsRepository: SettingsRepository

    private lateinit var viewModel: SettingsViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        every { settingsRepository.themeMode } returns flowOf(ThemeMode.SYSTEM)
        every { settingsRepository.tabletModeEnabled } returns flowOf(false)
        every { settingsRepository.aiApiKey } returns flowOf("")
        every { settingsRepository.aiBaseUrl } returns flowOf("https://api.deepseek.com")
        every { settingsRepository.aiModel } returns flowOf("deepseek-v4-flash")
        coEvery { settingsRepository.setThemeMode(any()) } returns Unit
        coEvery { settingsRepository.setTabletModeEnabled(any()) } returns Unit
        coEvery { settingsRepository.setAiApiKey(any()) } returns Unit
        coEvery { settingsRepository.setAiBaseUrl(any()) } returns Unit
        coEvery { settingsRepository.setAiModel(any()) } returns Unit

        viewModel = SettingsViewModel(settingsRepository, FakeAiRepository())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun themeModeReflectsRepository() = runTest {
        viewModel.themeMode.test {
            assertThat(awaitItem()).isEqualTo(ThemeMode.SYSTEM)
        }
    }

    @Test
    fun setThemeModeCallsRepository() = runTest {
        viewModel.setThemeMode(ThemeMode.DARK)
        coVerify { settingsRepository.setThemeMode(ThemeMode.DARK) }
    }

    @Test
    fun testConnectionSuccess() = runTest {
        val vm = SettingsViewModel(settingsRepository, FakeAiRepository(Result.success("OK")))
        vm.testConnection()
        advanceUntilIdle()
        assertThat(vm.testState.value).isInstanceOf(TestConnectionState.Success::class.java)
    }

    @Test
    fun testConnectionError() = runTest {
        val vm = SettingsViewModel(settingsRepository, FakeAiRepository(Result.failure(Exception("fail"))))
        vm.testConnection()
        advanceUntilIdle()
        assertThat(vm.testState.value).isInstanceOf(TestConnectionState.Error::class.java)
    }

    @Test
    fun resetTestStateSetsIdle() = runTest {
        viewModel.resetTestState()
        assertThat(viewModel.testState.value).isInstanceOf(TestConnectionState.Idle::class.java)
    }
}
