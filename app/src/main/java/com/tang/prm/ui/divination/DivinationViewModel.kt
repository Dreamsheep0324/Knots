package com.tang.prm.ui.divination

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.divination.model.DivinationRecord
import com.tang.prm.domain.divination.model.LiuyaoData
import com.tang.prm.domain.divination.model.MeihuaData
import com.tang.prm.domain.divination.repository.DivinationRepository
import com.tang.prm.engine.divination.liuyao.LiuyaoEngine
import com.tang.prm.engine.divination.meihua.MeihuaEngine
import com.tang.prm.engine.divination.data.ExternalOmenOption
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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

@HiltViewModel
class DivinationViewModel @Inject constructor(
    private val repository: DivinationRepository
) : ViewModel() {

    private val _selectedMethod = MutableStateFlow<DivinationMethod>(DivinationMethod.Meihua)
    val selectedMethod: StateFlow<DivinationMethod> = _selectedMethod.asStateFlow()

    private val _selectedMeihuaMethod = MutableStateFlow<MeihuaMethodType>(MeihuaMethodType.Time)
    val selectedMeihuaMethod: StateFlow<MeihuaMethodType> = _selectedMeihuaMethod.asStateFlow()

    private val _liuyaoData = MutableStateFlow<LiuyaoData?>(null)
    val liuyaoData: StateFlow<LiuyaoData?> = _liuyaoData.asStateFlow()

    private val _meihuaData = MutableStateFlow<MeihuaData?>(null)
    val meihuaData: StateFlow<MeihuaData?> = _meihuaData.asStateFlow()

    private val _currentYaoIndex = MutableStateFlow(0)
    val currentYaoIndex: StateFlow<Int> = _currentYaoIndex.asStateFlow()

    private val _yaoResults = MutableStateFlow<List<Int>>(emptyList())
    val yaoResults: StateFlow<List<Int>> = _yaoResults.asStateFlow()

    private val _lastCoinFlips = MutableStateFlow<List<Int>>(emptyList())
    val lastCoinFlips: StateFlow<List<Int>> = _lastCoinFlips.asStateFlow()

    private val _numberInput = MutableStateFlow("")
    val numberInput: StateFlow<String> = _numberInput.asStateFlow()

    private val _numberInputB = MutableStateFlow("")
    val numberInputB: StateFlow<String> = _numberInputB.asStateFlow()

    private val _externalSelections = MutableStateFlow<Map<String, ExternalOmenOption>>(emptyMap())
    val externalSelections: StateFlow<Map<String, ExternalOmenOption>> = _externalSelections.asStateFlow()

    private val _externalCount = MutableStateFlow("")
    val externalCount: StateFlow<String> = _externalCount.asStateFlow()

    private val _isCasting = MutableStateFlow(false)
    val isCasting: StateFlow<Boolean> = _isCasting.asStateFlow()

    private val _currentQuestion = MutableStateFlow("")
    val currentQuestion: StateFlow<String> = _currentQuestion.asStateFlow()

    private val _currentAiAnalysis = MutableStateFlow("")
    val currentAiAnalysis: StateFlow<String> = _currentAiAnalysis.asStateFlow()

    private val _lastSavedRecordId = MutableStateFlow(0L)

    private val gson = Gson()

    fun selectMethod(method: DivinationMethod) {
        _selectedMethod.value = method
    }

    fun selectMeihuaMethod(method: MeihuaMethodType) {
        _selectedMeihuaMethod.value = method
    }

    fun updateNumberInput(input: String) {
        _numberInput.value = input
    }

    fun updateNumberInputB(input: String) {
        _numberInputB.value = input
    }

    fun updateExternalSelection(category: String, option: ExternalOmenOption?) {
        _externalSelections.value = if (option != null) {
            _externalSelections.value + (category to option)
        } else {
            _externalSelections.value - category
        }
    }

    fun updateExternalCount(input: String) {
        if (input.isEmpty() || input.all { it.isDigit() }) {
            _externalCount.value = input
        }
    }

    fun updateQuestion(q: String) {
        _currentQuestion.value = q
    }

    fun updateAiAnalysis(analysis: String) {
        _currentAiAnalysis.value = analysis
    }

    fun saveAiAnalysis(analysis: String) {
        _currentAiAnalysis.value = analysis
        if (_lastSavedRecordId.value > 0) {
            viewModelScope.launch {
                repository.updateAiAnalysis(_lastSavedRecordId.value, analysis)
            }
        }
    }

    fun castNextYao() {
        if (_currentYaoIndex.value >= 6) return
        val flips = mutableListOf<Int>()
        var total = 0
        repeat(3) {
            val flip = if ((0..1).random() == 0) 2 else 3
            flips.add(flip)
            total += flip
        }
        _lastCoinFlips.value = flips
        _yaoResults.value = _yaoResults.value + total
        _currentYaoIndex.value += 1
    }

    fun generateLiuyaoResult() {
        if (_yaoResults.value.size != 6) return
        _liuyaoData.value = LiuyaoEngine.generate(yaoArray = _yaoResults.value)
    }

    private val _inputError = MutableStateFlow("")
    val inputError: StateFlow<String> = _inputError.asStateFlow()

    fun generateMeihuaResult() {
        _isCasting.value = true
        _inputError.value = ""
        _meihuaData.value = when (_selectedMeihuaMethod.value) {
            MeihuaMethodType.Time -> MeihuaEngine.generate(method = MeihuaEngine.Method.TIME)
            MeihuaMethodType.Number -> {
                val numA = _numberInput.value.toIntOrNull()
                val numB = _numberInputB.value.toIntOrNull()
                if (numA == null || numB == null || numA <= 0 || numB <= 0) {
                    _inputError.value = "请输入大于0的数字"
                    _isCasting.value = false
                    return
                }
                MeihuaEngine.generate(method = MeihuaEngine.Method.NUMBER, number = numA, numberB = numB)
            }
            MeihuaMethodType.Random -> MeihuaEngine.generate(method = MeihuaEngine.Method.RANDOM)
            MeihuaMethodType.External -> {
                val count = _externalCount.value.toIntOrNull()
                if (count == null || count <= 0) {
                    _inputError.value = "外应数必须大于0"
                    _isCasting.value = false
                    return
                }
                MeihuaEngine.generate(
                    method = MeihuaEngine.Method.EXTERNAL,
                    externalSelections = _externalSelections.value,
                    externalCount = count
                )
            }
        }
        _isCasting.value = false
    }

    fun resetLiuyao() {
        _currentYaoIndex.value = 0
        _yaoResults.value = emptyList()
        _liuyaoData.value = null
        _lastCoinFlips.value = emptyList()
        _currentQuestion.value = ""
        _currentAiAnalysis.value = ""
        _lastSavedRecordId.value = 0
    }

    fun resetMeihua() {
        _meihuaData.value = null
        _numberInput.value = ""
        _numberInputB.value = ""
        _externalSelections.value = emptyMap()
        _externalCount.value = ""
        _currentQuestion.value = ""
        _currentAiAnalysis.value = ""
        _lastSavedRecordId.value = 0
    }

    fun saveResult() {
        val data = _liuyaoData.value ?: _meihuaData.value ?: return
        val method = when (_selectedMethod.value) {
            is DivinationMethod.Liuyao -> "liuyao"
            is DivinationMethod.Meihua -> "meihua"
        }
        val json = gson.toJson(data)
        viewModelScope.launch {
            _lastSavedRecordId.value = repository.saveRecord(
                DivinationRecord(
                    method = method,
                    question = _currentQuestion.value,
                    resultJson = json,
                    createdAt = System.currentTimeMillis(),
                    aiAnalysis = _currentAiAnalysis.value
                )
            )
        }
    }
}
