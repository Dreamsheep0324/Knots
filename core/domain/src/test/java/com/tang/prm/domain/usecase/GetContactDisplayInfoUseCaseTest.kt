package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.util.Zodiac
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import com.google.common.truth.Truth.assertThat

class GetContactDisplayInfoUseCaseTest {
    private lateinit var useCase: GetContactDisplayInfoUseCase

    @BeforeEach
    fun setup() {
        useCase = GetContactDisplayInfoUseCase()
    }

    @Test
    fun `returns zodiac for birthday`() {
        val contact = Contact(id = 1, name = "A", birthday = createBirthday(4, 15))
        val result = useCase(contact)
        assertThat(result.zodiac).isEqualTo(Zodiac.ARIES)
    }

    @Test
    fun `returns null zodiac when no birthday`() {
        val contact = Contact(id = 1, name = "A", birthday = null)
        val result = useCase(contact)
        assertThat(result.zodiac).isNull()
    }

    @Test
    fun `returns days known text`() {
        val knowingDate = System.currentTimeMillis() - 400L * 24 * 60 * 60 * 1000
        val contact = Contact(id = 1, name = "A", knowingDate = knowingDate)
        val result = useCase(contact)
        assertThat(result.daysKnownText).contains("年")
    }

    @Test
    fun `returns null days known when no knowingDate`() {
        val contact = Contact(id = 1, name = "A", knowingDate = null)
        val result = useCase(contact)
        assertThat(result.daysKnownText).isNull()
    }

    @Test
    fun `returns correct intimacy level and color`() {
        val contact = Contact(id = 1, name = "A", intimacyScore = 90)
        val result = useCase(contact)
        assertThat(result.intimacyLevel).isEqualTo("至亲")
    }

    private fun createBirthday(month: Int, day: Int): Long {
        val cal = java.util.Calendar.getInstance()
        cal.set(2000, month - 1, day, 0, 0, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
