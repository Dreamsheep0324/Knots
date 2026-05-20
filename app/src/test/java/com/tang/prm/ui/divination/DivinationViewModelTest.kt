package com.tang.prm.ui.divination

import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.divination.model.MeihuaData
import com.tang.prm.domain.divination.model.LiuyaoData
import com.tang.prm.domain.divination.repository.DivinationRepository
import com.tang.prm.engine.divination.liuyao.LiuyaoEngine
import com.tang.prm.engine.divination.meihua.MeihuaEngine
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class DivinationViewModelTest {

    private lateinit var repository: DivinationRepository
    private lateinit var viewModel: DivinationViewModel

    private val mockMeihuaData = mockk<MeihuaData>()
    private val mockLiuyaoData = mockk<LiuyaoData>()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repository = mockk()
        coEvery { repository.saveRecord(any()) } returns 1L

        mockkObject(MeihuaEngine)
        every { MeihuaEngine.generate(method = MeihuaEngine.Method.TIME) } returns mockMeihuaData
        every { MeihuaEngine.generate(method = MeihuaEngine.Method.NUMBER, number = any(), numberB = any()) } returns mockMeihuaData
        every { MeihuaEngine.generate(method = MeihuaEngine.Method.RANDOM) } returns mockMeihuaData

        mockkObject(LiuyaoEngine)
        every { LiuyaoEngine.generate(yaoArray = any()) } returns mockLiuyaoData

        viewModel = DivinationViewModel(repository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkObject(MeihuaEngine)
        unmockkObject(LiuyaoEngine)
    }

    @Test
    fun castMeihuaByTimeGeneratesData() = runTest {
        viewModel.selectMethod(DivinationMethod.Meihua)
        viewModel.selectMeihuaMethod(MeihuaMethodType.Time)
        viewModel.generateMeihuaResult()
        assertThat(viewModel.meihuaData.value).isNotNull()
        assertThat(viewModel.meihuaData.value).isEqualTo(mockMeihuaData)
    }

    @Test
    fun castMeihuaByNumberGeneratesData() = runTest {
        viewModel.selectMethod(DivinationMethod.Meihua)
        viewModel.selectMeihuaMethod(MeihuaMethodType.Number)
        viewModel.updateNumberInput("3")
        viewModel.updateNumberInputB("5")
        viewModel.generateMeihuaResult()
        assertThat(viewModel.meihuaData.value).isNotNull()
    }

    @Test
    fun castMeihuaByNumberWithInvalidInputShowsError() = runTest {
        viewModel.selectMeihuaMethod(MeihuaMethodType.Number)
        viewModel.updateNumberInput("0")
        viewModel.updateNumberInputB("0")
        viewModel.generateMeihuaResult()
        assertThat(viewModel.inputError.value).isNotEmpty()
        assertThat(viewModel.meihuaData.value).isNull()
    }

    @Test
    fun castLiuyaoGeneratesData() = runTest {
        viewModel.selectMethod(DivinationMethod.Liuyao)
        repeat(6) { viewModel.castNextYao() }
        viewModel.generateLiuyaoResult()
        assertThat(viewModel.liuyaoData.value).isNotNull()
        assertThat(viewModel.liuyaoData.value).isEqualTo(mockLiuyaoData)
    }

    @Test
    fun saveRecordCallsInsert() = runTest {
        viewModel.selectMethod(DivinationMethod.Meihua)
        viewModel.selectMeihuaMethod(MeihuaMethodType.Time)
        viewModel.generateMeihuaResult()
        viewModel.saveResult()
        coVerify { repository.saveRecord(any()) }
    }

    @Test
    fun resetMeihuaClearsState() = runTest {
        viewModel.selectMeihuaMethod(MeihuaMethodType.Time)
        viewModel.generateMeihuaResult()
        viewModel.resetMeihua()
        assertThat(viewModel.meihuaData.value).isNull()
        assertThat(viewModel.numberInput.value).isEmpty()
        assertThat(viewModel.currentQuestion.value).isEmpty()
    }
}
