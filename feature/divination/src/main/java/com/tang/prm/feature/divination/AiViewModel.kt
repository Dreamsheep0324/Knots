package com.tang.prm.feature.divination

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.repository.AiRepository
import com.tang.prm.domain.repository.SettingsRepository
import com.tang.prm.engine.divination.prompt.LiuyaoPromptBuilder
import com.tang.prm.engine.divination.prompt.MeihuaPromptBuilder
import com.tang.prm.engine.divination.model.LiuyaoData
import com.tang.prm.engine.divination.model.MeihuaData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

sealed class AiDeepState {
    object Idle : AiDeepState()
    object Loading : AiDeepState()
    data class Streaming(val content: String) : AiDeepState()
    data class Result(val content: String) : AiDeepState()
    data class Error(val message: String) : AiDeepState()
}

@HiltViewModel
class AiViewModel @Inject constructor(
    private val aiRepository: AiRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow<AiDeepState>(AiDeepState.Idle)
    val state: StateFlow<AiDeepState> = _state.asStateFlow()

    var onAnalysisComplete: ((String) -> Unit)? = null

    private val _apiKeyConfigured = MutableStateFlow(false)
    val apiKeyConfigured: StateFlow<Boolean> = _apiKeyConfigured.asStateFlow()

    val savedGender: StateFlow<String> = settingsRepository.aiGender
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "男")

    val savedBirthDate: StateFlow<String> = settingsRepository.aiBirthDate
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    init {
        viewModelScope.launch {
            _apiKeyConfigured.value = settingsRepository.aiApiKey.first().isNotBlank()
        }
    }

    fun refreshApiKeyStatus() {
        viewModelScope.launch {
            _apiKeyConfigured.value = settingsRepository.aiApiKey.first().isNotBlank()
        }
    }

    fun saveGender(gender: String) {
        viewModelScope.launch { settingsRepository.setAiGender(gender) }
    }

    fun saveBirthDate(date: String) {
        viewModelScope.launch { settingsRepository.setAiBirthDate(date) }
    }

    fun startLiuyaoAnalysis(data: LiuyaoData, gender: String, birthDate: String, question: String) {
        viewModelScope.launch {
            val apiKey = settingsRepository.aiApiKey.first()
            if (apiKey.isBlank()) {
                _state.value = AiDeepState.Error("请先在设置中配置AI API密钥")
                return@launch
            }

            _state.value = AiDeepState.Loading

            val systemPrompt = LiuyaoPromptBuilder.getSystemPrompt()
            val userPrompt = LiuyaoPromptBuilder.buildUserPrompt(data, gender, birthDate, question)

            streamAnalysis(systemPrompt, userPrompt)
        }
    }

    fun startMeihuaAnalysis(data: MeihuaData, gender: String, birthDate: String, question: String) {
        viewModelScope.launch {
            val apiKey = settingsRepository.aiApiKey.first()
            if (apiKey.isBlank()) {
                _state.value = AiDeepState.Error("请先在设置中配置AI API密钥")
                return@launch
            }

            _state.value = AiDeepState.Loading

            val systemPrompt = MeihuaPromptBuilder.getSystemPrompt()
            val userPrompt = MeihuaPromptBuilder.buildUserPrompt(data, gender, birthDate, question)

            streamAnalysis(systemPrompt, userPrompt)
        }
    }

    private suspend fun streamAnalysis(systemPrompt: String, userPrompt: String) {
        val contentBuilder = StringBuilder()

        try {
            withTimeout(60_000L) {
                aiRepository.streamChat(systemPrompt, userPrompt).collect { chunk ->
                    if (chunk.startsWith("ERROR:")) {
                        val errorType = chunk.removePrefix("ERROR:")
                        _state.value = AiDeepState.Error(
                            when (errorType) {
                                "NO_API_KEY" -> "请先在设置中配置AI API密钥"
                                "INVALID_API_KEY" -> "API密钥无效，请检查设置"
                                "RATE_LIMIT" -> "请求过于频繁，请稍后重试"
                                "SERVER_ERROR" -> "AI服务暂时不可用，请稍后重试"
                                "NETWORK" -> "网络不可用，请检查网络连接"
                                "EMPTY_RESPONSE" -> "AI服务返回空响应，请稍后重试"
                                else -> "解读失败：$errorType"
                            }
                        )
                        return@collect
                    }

                    contentBuilder.append(chunk)
                    _state.value = AiDeepState.Streaming(contentBuilder.toString())
                }
            }

            if (_state.value is AiDeepState.Streaming) {
                val content = contentBuilder.toString()
                _state.value = AiDeepState.Result(content)
                onAnalysisComplete?.invoke(content)
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            _state.value = AiDeepState.Error("请求超时，请检查网络后重试")
        } catch (e: Exception) {
            _state.value = AiDeepState.Error("解读失败：${e.message ?: "未知错误"}")
        }
    }

    fun reset() {
        _state.value = AiDeepState.Idle
    }
}
