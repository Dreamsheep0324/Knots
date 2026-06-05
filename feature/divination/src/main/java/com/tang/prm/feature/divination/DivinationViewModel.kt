package com.tang.prm.feature.divination

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.divination.model.DivinationRecord
import com.tang.prm.engine.divination.model.LiuyaoData
import com.tang.prm.engine.divination.model.MeihuaData
import com.tang.prm.domain.divination.repository.DivinationRepository
import com.tang.prm.engine.divination.liuyao.LiuyaoEngine
import com.tang.prm.engine.divination.meihua.MeihuaEngine
import com.tang.prm.engine.divination.data.ExternalOmenOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

sealed class DivinationMethod {
    object Liuyao : DivinationMethod()
    object Meihua : DivinationMethod()
}

sealed class MeihuaMethodType {
    object Time : MeihuaMethodType()
    object Number : MeihuaMethodType()
    object Random : MeihuaMethodType()
    object External : MeihuaMethodType()
}

data class DivinationUiState(
    val selectedMethod: DivinationMethod = DivinationMethod.Meihua,
    val selectedMeihuaMethod: MeihuaMethodType = MeihuaMethodType.Time,
    val liuyaoData: LiuyaoData? = null,
    val meihuaData: MeihuaData? = null,
    val currentYaoIndex: Int = 0,
    val yaoResults: List<Int> = emptyList(),
    val lastCoinFlips: List<Int> = emptyList(),
    val numberInput: String = "",
    val numberInputB: String = "",
    val externalSelections: Map<String, ExternalOmenOption> = emptyMap(),
    val externalCount: String = "",
    val isCasting: Boolean = false,
    val currentQuestion: String = "",
    val currentAiAnalysis: String = "",
    val inputError: String = ""
)

@HiltViewModel
class DivinationViewModel @Inject constructor(
    private val repository: DivinationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DivinationUiState())
    val uiState: StateFlow<DivinationUiState> = _uiState.asStateFlow()

    private val _lastSavedRecordId = MutableStateFlow(0L)

    fun selectMethod(method: DivinationMethod) {
        _uiState.update { it.copy(selectedMethod = method) }
    }

    fun selectMeihuaMethod(method: MeihuaMethodType) {
        _uiState.update { it.copy(selectedMeihuaMethod = method) }
    }

    fun updateNumberInput(input: String) {
        _uiState.update { it.copy(numberInput = input) }
    }

    fun updateNumberInputB(input: String) {
        _uiState.update { it.copy(numberInputB = input) }
    }

    fun updateExternalSelection(category: String, option: ExternalOmenOption?) {
        _uiState.update { state ->
            val updated = if (option != null) {
                state.externalSelections + (category to option)
            } else {
                state.externalSelections - category
            }
            state.copy(externalSelections = updated)
        }
    }

    fun updateExternalCount(input: String) {
        if (input.isEmpty() || input.all { it.isDigit() }) {
            _uiState.update { it.copy(externalCount = input) }
        }
    }

    fun updateQuestion(q: String) {
        _uiState.update { it.copy(currentQuestion = q) }
    }

    fun updateAiAnalysis(analysis: String) {
        _uiState.update { it.copy(currentAiAnalysis = analysis) }
    }

    fun saveAiAnalysis(analysis: String) {
        _uiState.update { it.copy(currentAiAnalysis = analysis) }
        if (_lastSavedRecordId.value > 0) {
            viewModelScope.launch {
                repository.updateAiAnalysis(_lastSavedRecordId.value, analysis)
            }
        }
    }

    fun castNextYao() {
        val state = _uiState.value
        if (state.currentYaoIndex >= 6) return
        val flips = mutableListOf<Int>()
        var total = 0
        repeat(3) {
            val flip = if ((0..1).random() == 0) 2 else 3
            flips.add(flip)
            total += flip
        }
        _uiState.update { it.copy(
            lastCoinFlips = flips,
            yaoResults = it.yaoResults + total,
            currentYaoIndex = it.currentYaoIndex + 1
        ) }
    }

    fun generateLiuyaoResult() {
        val state = _uiState.value
        if (state.yaoResults.size != 6) return
        _uiState.update { it.copy(liuyaoData = LiuyaoEngine.generate(yaoArray = state.yaoResults)) }
    }

    fun generateMeihuaResult() {
        val state = _uiState.value
        _uiState.update { it.copy(isCasting = true, inputError = "") }
        val result = when (state.selectedMeihuaMethod) {
            MeihuaMethodType.Time -> MeihuaEngine.generate(method = MeihuaEngine.Method.TIME)
            MeihuaMethodType.Number -> {
                val numA = state.numberInput.toIntOrNull()
                val numB = state.numberInputB.toIntOrNull()
                if (numA == null || numB == null || numA <= 0 || numB <= 0) {
                    _uiState.update { it.copy(inputError = "请输入大于0的数字", isCasting = false) }
                    return
                }
                MeihuaEngine.generate(method = MeihuaEngine.Method.NUMBER, number = numA, numberB = numB)
            }
            MeihuaMethodType.Random -> MeihuaEngine.generate(method = MeihuaEngine.Method.RANDOM)
            MeihuaMethodType.External -> {
                val count = state.externalCount.toIntOrNull()
                if (count == null || count <= 0) {
                    _uiState.update { it.copy(inputError = "外应数必须大于0", isCasting = false) }
                    return
                }
                MeihuaEngine.generate(
                    method = MeihuaEngine.Method.EXTERNAL,
                    externalSelections = state.externalSelections,
                    externalCount = count
                )
            }
        }
        _uiState.update { it.copy(meihuaData = result, isCasting = false) }
    }

    fun resetLiuyao() {
        _uiState.update { it.copy(
            currentYaoIndex = 0,
            yaoResults = emptyList(),
            liuyaoData = null,
            lastCoinFlips = emptyList(),
            currentQuestion = "",
            currentAiAnalysis = ""
        ) }
        _lastSavedRecordId.value = 0
    }

    fun resetMeihua() {
        _uiState.update { it.copy(
            meihuaData = null,
            numberInput = "",
            numberInputB = "",
            externalSelections = emptyMap(),
            externalCount = "",
            currentQuestion = "",
            currentAiAnalysis = ""
        ) }
        _lastSavedRecordId.value = 0
    }

    fun saveResult() {
        val state = _uiState.value
        val method = when (state.selectedMethod) {
            is DivinationMethod.Liuyao -> "liuyao"
            is DivinationMethod.Meihua -> "meihua"
        }
        val json = when (val d = state.liuyaoData ?: state.meihuaData) {
            is LiuyaoData -> Json.encodeToString(LiuyaoData.serializer(), d)
            is MeihuaData -> Json.encodeToString(MeihuaData.serializer(), d)
            else -> return
        }
        viewModelScope.launch {
            _lastSavedRecordId.value = repository.saveRecord(
                DivinationRecord(
                    method = method,
                    question = state.currentQuestion,
                    resultJson = json,
                    createdAt = System.currentTimeMillis(),
                    aiAnalysis = state.currentAiAnalysis
                )
            )
        }
    }
}
