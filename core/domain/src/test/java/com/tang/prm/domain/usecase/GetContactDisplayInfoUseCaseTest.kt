package com.tang.prm.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.Anniversary
import com.tang.prm.domain.model.AnniversaryType
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.util.DateCalcUtils
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneId

class GetContactDisplayInfoUseCaseTest {

    private val useCase = GetContactDisplayInfoUseCase()
    private val zoneId = ZoneId.systemDefault()

    @Nested
    @DisplayName("zodiac 计算")
    inner class ZodiacTest {

        @Test
        fun contactWithBirthday_returnsCorrectZodiac() {
            val millis = LocalDate.of(2000, 3, 25)
                .atStartOfDay(zoneId).toInstant().toEpochMilli()
            val contact = Contact(id = 1L, name = "Alice", birthday = millis)
            val info = useCase(contact)
            assertThat(info.zodiac).isNotNull()
            assertThat(info.zodiac!!.displayName).isEqualTo("白羊座")
        }

        @Test
        fun contactWithoutBirthday_returnsNullZodiac() {
            val contact = Contact(id = 1L, name = "Alice", birthday = null)
            val info = useCase(contact)
            assertThat(info.zodiac).isNull()
        }
    }

    @Nested
    @DisplayName("daysKnownText 计算")
    inner class DaysKnownTest {

        @Test
        fun contactWithKnowingDate_returnsDaysText() {
            val daysAgo = 400
            val knowingDate = System.currentTimeMillis() - daysAgo * 86_400_000L
            val contact = Contact(id = 1L, name = "Alice", knowingDate = knowingDate)
            val info = useCase(contact)
            assertThat(info.daysKnownText).isNotNull()
            assertThat(info.daysKnownText).contains("天")
        }

        @Test
        fun contactWithKnowingDateOverYear_returnsYearsAndDays() {
            val daysAgo = 800
            val knowingDate = System.currentTimeMillis() - daysAgo * 86_400_000L
            val contact = Contact(id = 1L, name = "Alice", knowingDate = knowingDate)
            val info = useCase(contact)
            assertThat(info.daysKnownText).contains("年")
            assertThat(info.daysKnownText).contains("天")
        }

        @Test
        fun contactWithoutKnowingDate_returnsNull() {
            val contact = Contact(id = 1L, name = "Alice", knowingDate = null)
            val info = useCase(contact)
            assertThat(info.daysKnownText).isNull()
        }
    }

    @Nested
    @DisplayName("intimacyLevel 和 intimacyColorValue")
    inner class IntimacyTest {

        @Test
        fun returnsCorrectIntimacyLevel() {
            val contact = Contact(id = 1L, name = "Alice", intimacyScore = 50)
            val info = useCase(contact)
            assertThat(info.intimacyLevel).isEqualTo("朋友")
        }

        @Test
        fun returnsNonZeroColorValue() {
            val contact = Contact(id = 1L, name = "Alice", intimacyScore = 50)
            val info = useCase(contact)
            assertThat(info.intimacyColorValue).isNotEqualTo(0L)
        }
    }
}
