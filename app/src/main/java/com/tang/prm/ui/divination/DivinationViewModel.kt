package com.tang.prm.ui.divination

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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

    var selectedMethod by mutableStateOf<DivinationMethod>(DivinationMethod.Meihua)
        private set

    var selectedMeihuaMethod by mutableStateOf<MeihuaMethodType>(MeihuaMethodType.Time)
        private set

    var liuyaoData by mutableStateOf<LiuyaoData?>(null)
        private set

    var meihuaData by mutableStateOf<MeihuaData?>(null)
        private set

    var currentYaoIndex by mutableStateOf(0)
        private set

    var yaoResults by mutableStateOf<List<Int>>(emptyList())
        private set

    var lastCoinFlips by mutableStateOf<List<Int>>(emptyList())
        private set

    var numberInput by mutableStateOf("")
        private set

    var numberInputB by mutableStateOf("")
        private set

    var externalSelections by mutableStateOf<Map<String, ExternalOmenOption>>(emptyMap())
        private set

    var externalCount by mutableStateOf("")
        private set

    var isCasting by mutableStateOf(false)
        private set

    var currentQuestion by mutableStateOf("")
        private set

    var currentAiAnalysis by mutableStateOf("")
        private set

    private var lastSavedRecordId by mutableStateOf(0L)

    private val gson = Gson()

    fun selectMethod(method: DivinationMethod) {
        selectedMethod = method
    }

    fun selectMeihuaMethod(method: MeihuaMethodType) {
        selectedMeihuaMethod = method
    }

    fun updateNumberInput(input: String) {
        numberInput = input
    }

    fun updateNumberInputB(input: String) {
        numberInputB = input
    }

    fun updateExternalSelection(category: String, option: ExternalOmenOption?) {
        externalSelections = if (option != null) {
            externalSelections + (category to option)
        } else {
            externalSelections - category
        }
    }

    fun updateExternalCount(input: String) {
        if (input.isEmpty() || input.all { it.isDigit() }) {
            externalCount = input
        }
    }

    fun updateQuestion(q: String) {
        currentQuestion = q
    }

    fun updateAiAnalysis(analysis: String) {
        currentAiAnalysis = analysis
    }

    fun saveAiAnalysis(analysis: String) {
        currentAiAnalysis = analysis
        if (lastSavedRecordId > 0) {
            viewModelScope.launch {
                repository.updateAiAnalysis(lastSavedRecordId, analysis)
            }
        }
    }

    fun castNextYao() {
        if (currentYaoIndex >= 6) return
        val flips = mutableListOf<Int>()
        var total = 0
        repeat(3) {
            val flip = if ((0..1).random() == 0) 2 else 3
            flips.add(flip)
            total += flip
        }
        lastCoinFlips = flips
        yaoResults = yaoResults + total
        currentYaoIndex += 1
    }

    fun generateLiuyaoResult() {
        if (yaoResults.size != 6) return
        liuyaoData = LiuyaoEngine.generate(yaoArray = yaoResults)
    }

    var inputError by mutableStateOf("")
        private set

    fun generateMeihuaResult() {
        isCasting = true
        inputError = ""
        meihuaData = when (selectedMeihuaMethod) {
            MeihuaMethodType.Time -> MeihuaEngine.generate(method = MeihuaEngine.Method.TIME)
            MeihuaMethodType.Number -> {
                val numA = numberInput.toIntOrNull()
                val numB = numberInputB.toIntOrNull()
                if (numA == null || numB == null || numA <= 0 || numB <= 0) {
                    inputError = "请输入大于0的数字"
                    isCasting = false
                    return
                }
                MeihuaEngine.generate(method = MeihuaEngine.Method.NUMBER, number = numA, numberB = numB)
            }
            MeihuaMethodType.Random -> MeihuaEngine.generate(method = MeihuaEngine.Method.RANDOM)
            MeihuaMethodType.External -> {
                val count = externalCount.toIntOrNull()
                if (count == null || count <= 0) {
                    inputError = "外应数必须大于0"
                    isCasting = false
                    return
                }
                MeihuaEngine.generate(
                    method = MeihuaEngine.Method.EXTERNAL,
                    externalSelections = externalSelections,
                    externalCount = count
                )
            }
        }
        isCasting = false
    }

    fun resetLiuyao() {
        currentYaoIndex = 0
        yaoResults = emptyList()
        liuyaoData = null
        lastCoinFlips = emptyList()
        currentQuestion = ""
        currentAiAnalysis = ""
        lastSavedRecordId = 0
    }

    fun resetMeihua() {
        meihuaData = null
        numberInput = ""
        numberInputB = ""
        externalSelections = emptyMap()
        externalCount = ""
        currentQuestion = ""
        currentAiAnalysis = ""
        lastSavedRecordId = 0
    }

    fun saveResult() {
        val data = liuyaoData ?: meihuaData ?: return
        val method = when (selectedMethod) {
            is DivinationMethod.Liuyao -> "liuyao"
            is DivinationMethod.Meihua -> "meihua"
        }
        val json = gson.toJson(data)
        viewModelScope.launch {
            lastSavedRecordId = repository.saveRecord(
                DivinationRecord(
                    method = method,
                    question = currentQuestion,
                    resultJson = json,
                    createdAt = System.currentTimeMillis(),
                    aiAnalysis = currentAiAnalysis
                )
            )
        }
    }
}
