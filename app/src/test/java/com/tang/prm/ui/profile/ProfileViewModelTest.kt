package com.tang.prm.ui.profile

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.repository.SettingsRepository
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
class ProfileViewModelTest {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var viewModel: ProfileViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        settingsRepository = mockk()

        every { settingsRepository.userName } returns flowOf("TestUser")
        every { settingsRepository.userSignature } returns flowOf("TestSignature")
        coEvery { settingsRepository.setUserName(any()) } returns Unit
        coEvery { settingsRepository.setUserSignature(any()) } returns Unit

        viewModel = ProfileViewModel(settingsRepository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initLoadsUserInfo() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.userName).isEqualTo("TestUser")
            assertThat(state.userSignature).isEqualTo("TestSignature")
        }
    }

    @Test
    fun updateUserNameCallsRepository() = runTest {
        viewModel.updateProfile("NewName", "NewSignature")
        coVerify { settingsRepository.setUserName("NewName") }
        coVerify { settingsRepository.setUserSignature("NewSignature") }
    }
}
