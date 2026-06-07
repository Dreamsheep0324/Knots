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
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class SettingsViewModelTest {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var aiRepository: AiRepository
    private lateinit var viewModel: SettingsViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        settingsRepository = mockk()
        aiRepository = mockk()

        every { settingsRepository.themeMode } returns flowOf(ThemeMode.SYSTEM)
        every { settingsRepository.aiApiKey } returns flowOf("")
        every { settingsRepository.aiBaseUrl } returns flowOf("https://api.deepseek.com")
        every { settingsRepository.aiModel } returns flowOf("deepseek-v4-flash")
        coEvery { settingsRepository.setThemeMode(any()) } returns Unit
        coEvery { settingsRepository.setAiApiKey(any()) } returns Unit
        coEvery { settingsRepository.setAiBaseUrl(any()) } returns Unit
        coEvery { settingsRepository.setAiModel(any()) } returns Unit
        coEvery { aiRepository.testConnection() } returns Result.success("OK")

        viewModel = SettingsViewModel(settingsRepository, aiRepository)
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
    fun aiConfigReflectsRepository() = runTest {
        viewModel.aiApiKey.test {
            assertThat(awaitItem()).isEmpty()
        }
        viewModel.aiBaseUrl.test {
            assertThat(awaitItem()).isEqualTo("https://api.deepseek.com")
        }
        viewModel.aiModel.test {
            assertThat(awaitItem()).isEqualTo("deepseek-v4-flash")
        }
    }

    @Test
    fun testConnectionSuccess() = runTest {
        viewModel.testConnection()
        viewModel.testState.test {
            var state = awaitItem()
            while (state is TestConnectionState.Testing) {
                state = awaitItem()
            }
            assertThat(state).isInstanceOf(TestConnectionState.Success::class.java)
            assertThat((state as TestConnectionState.Success).message).isEqualTo("OK")
        }
    }

    @Test
    fun testConnectionError() = runTest {
        coEvery { aiRepository.testConnection() } returns Result.failure(Exception("fail"))
        viewModel.testConnection()
        viewModel.testState.test {
            val state = awaitItem()
            assertThat(state).isInstanceOf(TestConnectionState.Error::class.java)
        }
    }

    @Test
    fun resetTestStateSetsIdle() = runTest {
        viewModel.testState.test {
            viewModel.testConnection()
            var state = awaitItem()
            while (state !is TestConnectionState.Success && state !is TestConnectionState.Error) {
                state = awaitItem()
            }
        }
        viewModel.resetTestState()
        assertThat(viewModel.testState.value).isInstanceOf(TestConnectionState.Idle::class.java)
    }
}
