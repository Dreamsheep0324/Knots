package com.tang.prm.engine.meihua

import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.divination.model.*
import com.tang.prm.engine.divination.meihua.MeihuaEngine
import com.tang.prm.engine.divination.meihua.MeihuaEngine.Method
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Calendar
import java.util.GregorianCalendar

class MeihuaEngineTest {

    private val fixedDate = GregorianCalendar(2024, Calendar.JUNE, 15, 10, 0).time
    private val fixedDate2 = GregorianCalendar(2024, Calendar.JANUARY, 1, 0, 0).time

    @Test
    fun `generate_timeMethod_returnsValidMeihuaData`() {
        val result = MeihuaEngine.generate(method = Method.TIME, customDate = fixedDate)

        assertThat(result.originalName).isNotEmpty()
        assertThat(result.changedName).isNotNull()
        assertThat(result.interName).isNotNull()
        assertThat(result.ganzhi).isNotNull()
        assertThat(result.timestamp).isGreaterThan(0L)
        assertThat(result.tiGua).isNotNull()
        assertThat(result.yongGua).isNotNull()
        assertThat(result.movingYao).isNotNull()
        assertThat(result.analysis).isNotNull()
        assertThat(result.mainHexagram).isNotNull()
        assertThat(result.yaosDetail).isNotEmpty()
        assertThat(result.calculation).isNotNull()
    }

    @Test
    fun `generate_numberMethod_returnsValidMeihuaData`() {
        val result = MeihuaEngine.generate(
            method = Method.NUMBER, number = 3, numberB = 5, customDate = fixedDate
        )

        assertThat(result.originalName).isNotEmpty()
        assertThat(result.ganzhi).isNotNull()
        assertThat(result.timestamp).isGreaterThan(0L)
        assertThat(result.tiGua).isNotNull()
        assertThat(result.yongGua).isNotNull()
        assertThat(result.movingYao).isNotNull()
        assertThat(result.analysis).isNotNull()
        assertThat(result.mainHexagram).isNotNull()
        assertThat(result.yaosDetail).isNotEmpty()
        assertThat(result.calculation).isNotNull()
    }

    @Test
    fun `generate_numberMethod3and5_upperIs离_lowerIs巽`() {
        val result = MeihuaEngine.generate(
            method = Method.NUMBER, number = 3, numberB = 5, customDate = fixedDate
        )

        assertThat(result.mainHexagram.upper).isEqualTo("离")
        assertThat(result.mainHexagram.lower).isEqualTo("巽")
    }

    @Test
    fun `generate_numberMethod_movingYaoCorrect`() {
        val result = MeihuaEngine.generate(
            method = Method.NUMBER, number = 3, numberB = 5, customDate = fixedDate
        )

        assertThat(result.movingYao.position).isEqualTo(2)
    }

    @Test
    fun `generate_numberMethod8and8_upperIs坤_lowerIs坤`() {
        val result = MeihuaEngine.generate(
            method = Method.NUMBER, number = 8, numberB = 8, customDate = fixedDate
        )

        assertThat(result.mainHexagram.upper).isEqualTo("坤")
        assertThat(result.mainHexagram.lower).isEqualTo("坤")
    }

    @Test
    fun `generate_numberMethod6and6_movingYaoIs6`() {
        val result = MeihuaEngine.generate(
            method = Method.NUMBER, number = 6, numberB = 6, customDate = fixedDate
        )

        assertThat(result.movingYao.position).isEqualTo(6)
    }

    @Test
    fun `generate_randomMethod_returnsValidMeihuaData`() {
        val result = MeihuaEngine.generate(method = Method.RANDOM, customDate = fixedDate)

        assertThat(result.originalName).isNotEmpty()
        assertThat(result.ganzhi).isNotNull()
        assertThat(result.timestamp).isGreaterThan(0L)
        assertThat(result.tiGua).isNotNull()
        assertThat(result.yongGua).isNotNull()
        assertThat(result.movingYao).isNotNull()
        assertThat(result.movingYao.position).isIn(1..6)
        assertThat(result.analysis).isNotNull()
        assertThat(result.mainHexagram).isNotNull()
        assertThat(result.yaosDetail).hasSize(6)
        assertThat(result.calculation).isNotNull()
    }

    @Test
    fun `generate_timeMethod_tiYongCorrectWhenMovingAbove3`() {
        val result = MeihuaEngine.generate(method = Method.TIME, customDate = fixedDate)

        assumeTrue(result.movingYao.position > 3)

        assertThat(result.tiGua.name).isEqualTo(result.mainHexagram.lower)
        assertThat(result.yongGua.name).isEqualTo(result.mainHexagram.upper)
    }

    @Test
    fun `generate_timeMethod_tiYongCorrectWhenMovingBelow4`() {
        val result = MeihuaEngine.generate(method = Method.TIME, customDate = fixedDate2)

        assumeTrue(result.movingYao.position <= 3)

        assertThat(result.tiGua.name).isEqualTo(result.mainHexagram.upper)
        assertThat(result.yongGua.name).isEqualTo(result.mainHexagram.lower)
    }

    @Test
    fun `generate_numberMethod_interHexagramNotNull`() {
        val result = MeihuaEngine.generate(
            method = Method.NUMBER, number = 3, numberB = 5, customDate = fixedDate
        )

        assertThat(result.interHexagram).isNotNull()
        assertThat(result.interHexagram!!.name).isNotEmpty()
    }

    @Test
    fun `generate_numberMethod_changedHexagramNotNull`() {
        val result = MeihuaEngine.generate(
            method = Method.NUMBER, number = 3, numberB = 5, customDate = fixedDate
        )

        assertThat(result.changedHexagram).isNotNull()
        assertThat(result.changedHexagram!!.name).isNotEmpty()
    }

    @Test
    fun `generate_numberMethod_yaosDetailHas6Items`() {
        val result = MeihuaEngine.generate(
            method = Method.NUMBER, number = 3, numberB = 5, customDate = fixedDate
        )

        assertThat(result.yaosDetail).hasSize(6)
        assertThat(result.yaosDetail[0].position).isEqualTo(1)
        assertThat(result.yaosDetail[5].position).isEqualTo(6)
    }

    @Test
    fun `generate_numberMethod_analysisFieldsNonEmpty`() {
        val result = MeihuaEngine.generate(
            method = Method.NUMBER, number = 3, numberB = 5, customDate = fixedDate
        )

        assertThat(result.analysis.season).isNotEmpty()
        assertThat(result.analysis.tiYongRelation).isNotEmpty()
        assertThat(result.analysis.tiSeasonState).isNotEmpty()
        assertThat(result.analysis.yongSeasonState).isNotEmpty()
    }

    @Test
    fun `generate_numberMethodZeroInput_throwsException`() {
        assertThrows<IllegalArgumentException> {
            MeihuaEngine.generate(method = Method.NUMBER, number = 0, numberB = 5, customDate = fixedDate)
        }
    }

    @Test
    fun `generate_numberMethodNegativeInput_throwsException`() {
        assertThrows<IllegalArgumentException> {
            MeihuaEngine.generate(method = Method.NUMBER, number = -1, numberB = 5, customDate = fixedDate)
        }
    }
}
