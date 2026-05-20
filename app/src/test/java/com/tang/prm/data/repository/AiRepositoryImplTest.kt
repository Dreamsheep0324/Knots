package com.tang.prm.data.repository

import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Response
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class AiRepositoryImplTest {

    @MockK
    private lateinit var okHttpClient: OkHttpClient

    @MockK
    private lateinit var settingsRepository: SettingsRepository

    private lateinit var repository: AiRepositoryImpl

    @BeforeEach
    fun setUp() {
        repository = AiRepositoryImpl(okHttpClient, settingsRepository)
    }

    @Test
    fun streamChat_returnsFlow() = runTest {
        coEvery { settingsRepository.getAiApiKey() } returns ""

        val result = repository.streamChat("system", "user")

        assertThat(result).isNotNull()
    }

    @Test
    fun streamChat_emitsNoApiKeyWhenBlank() = runTest {
        coEvery { settingsRepository.getAiApiKey() } returns ""
        coEvery { settingsRepository.getAiBaseUrl() } returns "https://api.test.com"
        coEvery { settingsRepository.getAiModel() } returns "test-model"

        val result = repository.streamChat("system", "user").first()

        assertThat(result).isEqualTo("ERROR:NO_API_KEY")
    }

    @Test
    fun testConnection_returnsFailureWhenNoApiKey() = runTest {
        coEvery { settingsRepository.getAiApiKey() } returns ""

        val result = repository.testConnection()

        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun testConnection_returnsSuccessOnValidResponse() = runTest {
        coEvery { settingsRepository.getAiApiKey() } returns "sk-test"
        coEvery { settingsRepository.getAiBaseUrl() } returns "https://api.test.com"
        coEvery { settingsRepository.getAiModel() } returns "test-model"

        val call = io.mockk.mockk<Call>()
        val response = io.mockk.mockk<Response>()
        every { okHttpClient.newCall(any()) } returns call
        every { call.execute() } returns response
        every { response.isSuccessful } returns true
        every { response.close() } returns Unit

        val result = repository.testConnection()

        assertThat(result.isSuccess).isTrue()
    }
}
