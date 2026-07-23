package com.tang.prm.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.Anniversary
import com.tang.prm.domain.model.AnniversaryType
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneId

class GetAnniversaryDisplayUseCaseTest {

    private val useCase = GetAnniversaryDisplayUseCase()
    private val zoneId = ZoneId.systemDefault()

    private fun dateMillis(year: Int, month: Int, day: Int): Long =
        LocalDate.of(year, month, day).atStartOfDay(zoneId).toInstant().toEpochMilli()

    @Nested
    @DisplayName("invoke 单个纪念日")
    inner class InvokeTest {

        @Test
        fun `birthday uses next birthday date`() {
            val ann = Anniversary(id = 1, name = "生日", type = AnniversaryType.BIRTHDAY,
                date = dateMillis(1990, 6, 15), isRepeat = true)
            val info = useCase(ann)
            assertThat(info.effectiveDate).isGreaterThan(0L)
            assertThat(info.daysUntil).isAtLeast(0)
        }

        @Test
        fun `repeating anniversary uses next repeat date`() {
            val ann = Anniversary(id = 1, name = "纪念日", type = AnniversaryType.ANNIVERSARY,
                date = dateMillis(2020, 1, 1), isRepeat = true)
            val info = useCase(ann)
            assertThat(info.effectiveDate).isGreaterThan(0L)
        }

        @Test
        fun `non repeating anniversary uses original date`() {
            val date = dateMillis(2025, 12, 25)
            val ann = Anniversary(id = 1, name = "一次性", type = AnniversaryType.ANNIVERSARY,
                date = date, isRepeat = false)
            val info = useCase(ann)
            assertThat(info.effectiveDate).isEqualTo(date)
        }
    }

    @Nested
    @DisplayName("categorizeAnniversaries 分类")
    inner class CategorizeTest {

        @Test
        fun `returns categorized lists`() {
            val anns = listOf(
                Anniversary(id = 1, name = "A", type = AnniversaryType.BIRTHDAY,
                    date = dateMillis(1990, 1, 1), isRepeat = true)
            )
            val result = useCase.categorizeAnniversaries(anns)
            assertThat(result.all.size + result.upcoming.size + result.past.size).isAtLeast(1)
        }

        @Test
        fun `empty list returns empty categories`() {
            val result = useCase.categorizeAnniversaries(emptyList())
            assertThat(result.all).isEmpty()
            assertThat(result.upcoming).isEmpty()
            assertThat(result.past).isEmpty()
        }

        @Test
        fun `all list contains all anniversaries`() {
            val anns = listOf(
                Anniversary(id = 1, name = "A", type = AnniversaryType.BIRTHDAY,
                    date = dateMillis(1990, 1, 1), isRepeat = true),
                Anniversary(id = 2, name = "B", type = AnniversaryType.ANNIVERSARY,
                    date = dateMillis(2020, 6, 15), isRepeat = true)
            )
            val result = useCase.categorizeAnniversaries(anns)
            assertThat(result.all).hasSize(2)
        }
    }
}
