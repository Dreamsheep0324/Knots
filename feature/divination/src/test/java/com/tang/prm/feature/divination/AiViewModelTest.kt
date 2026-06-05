package com.tang.prm.feature.divination

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tang.prm.engine.divination.model.MeihuaData
import com.tang.prm.domain.repository.AiRepository
import com.tang.prm.domain.repository.SettingsRepository
import com.tang.prm.engine.divination.prompt.MeihuaPromptBuilder
import io.mockk.coEvery
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
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
class AiViewModelTest {

    private lateinit var aiRepository: AiRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var viewModel: AiViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        aiRepository = mockk()
        settingsRepository = mockk()

        every { settingsRepository.aiGender } returns flowOf("男")
        every { settingsRepository.aiBirthDate } returns flowOf("")
        every { settingsRepository.aiApiKey } returns flowOf("test-api-key")

        mockkObject(MeihuaPromptBuilder)
        every { MeihuaPromptBuilder.getSystemPrompt() } returns "system"
        every { MeihuaPromptBuilder.buildUserPrompt(any(), any(), any(), any()) } returns "user"

        viewModel = AiViewModel(aiRepository, settingsRepository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkObject(MeihuaPromptBuilder)
    }

    @Test
    fun initialStateIsIdle() = runTest {
        viewModel.state.test {
            assertThat(awaitItem()).isInstanceOf(AiDeepState.Idle::class.java)
        }
    }

    @Test
    fun startMeihuaAnalysisSetsLoadingThenResult() = runTest {
        coEvery { aiRepository.streamChat(any(), any()) } returns flowOf("chunk1", "chunk2")

        viewModel.startMeihuaAnalysis(
            data = mockk(),
            gender = "男",
            birthDate = "",
            question = "test"
        )

        viewModel.state.test {
            val state = awaitItem()
            assertThat(state).isInstanceOf(AiDeepState.Result::class.java)
            assertThat((state as AiDeepState.Result).content).isEqualTo("chunk1chunk2")
        }
    }

    @Test
    fun apiKeyNotConfiguredSetsError() = runTest {
        every { settingsRepository.aiApiKey } returns flowOf("")

        val errorViewModel = AiViewModel(aiRepository, settingsRepository)
        errorViewModel.startMeihuaAnalysis(
            data = mockk(),
            gender = "男",
            birthDate = "",
            question = "test"
        )

        errorViewModel.state.test {
            val state = awaitItem()
            assertThat(state).isInstanceOf(AiDeepState.Error::class.java)
        }
    }

    @Test
    fun resetSetsIdleState() = runTest {
        coEvery { aiRepository.streamChat(any(), any()) } returns flowOf("chunk")
        viewModel.startMeihuaAnalysis(
            data = mockk(),
            gender = "男",
            birthDate = "",
            question = "test"
        )
        viewModel.reset()
        viewModel.state.test {
            assertThat(awaitItem()).isInstanceOf(AiDeepState.Idle::class.java)
        }
    }
}
