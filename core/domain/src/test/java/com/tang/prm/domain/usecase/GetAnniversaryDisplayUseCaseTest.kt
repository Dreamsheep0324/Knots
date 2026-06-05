package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.Anniversary
import com.tang.prm.domain.model.AnniversaryType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import com.google.common.truth.Truth.assertThat

class GetAnniversaryDisplayUseCaseTest {
    private lateinit var useCase: GetAnniversaryDisplayUseCase

    @BeforeEach
    fun setup() {
        useCase = GetAnniversaryDisplayUseCase()
    }

    @Test
    fun `invoke returns display info with effective date`() {
        val anniversary = Anniversary(id = 1, name = "Test", date = System.currentTimeMillis(), type = AnniversaryType.HOLIDAY, isRepeat = false)
        val result = useCase(anniversary)
        assertThat(result.anniversary).isEqualTo(anniversary)
    }

    @Test
    fun `categorizeAnniversaries returns triple`() {
        val now = System.currentTimeMillis()
        val past = Anniversary(id = 1, name = "Past", date = now - 100000L, type = AnniversaryType.HOLIDAY, isRepeat = false)
        val anniversaries = listOf(past)
        val (all, upcoming, pastList) = useCase.categorizeAnniversaries(anniversaries)
        assertThat(all).isNotEmpty()
    }
}
