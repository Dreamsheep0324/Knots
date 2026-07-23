package com.tang.prm.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.Circle
import com.tang.prm.domain.repository.CircleRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class CircleManageUseCaseTest {

    private lateinit var circleRepository: CircleRepository
    private lateinit var useCase: CircleManageUseCase

    private val circle1 = Circle(id = 10, name = "朋友圈A", memberIds = listOf(1L, 2L))
    private val circle2 = Circle(id = 20, name = "朋友圈B", memberIds = listOf(3L))

    @BeforeEach
    fun setUp() {
        circleRepository = mockk()
        useCase = CircleManageUseCase(circleRepository)
    }

    @Test
    fun `createCircle delegates to repository`() = runTest {
        coEvery { circleRepository.insertCircle(any()) } returns 42L

        val id = useCase.createCircle("新圈", "描述", "#FF0000", "wave1")

        assertThat(id).isEqualTo(42L)
        coVerify { circleRepository.insertCircle(match { it.name == "新圈" && it.color == "#FF0000" }) }
    }

    @Test
    fun `updateCircle delegates to repository with updated fields`() = runTest {
        coEvery { circleRepository.updateCircle(any()) } returns Unit

        useCase.updateCircle(circle1, "改名", null, "#00FF00", "wave2")

        coVerify {
            circleRepository.updateCircle(match {
                it.id == 10L && it.name == "改名" && it.color == "#00FF00" && it.waveform == "wave2"
            })
        }
    }

    @Test
    fun `deleteCircle delegates to repository`() = runTest {
        coEvery { circleRepository.deleteCircleWithChildren(any()) } returns Unit

        useCase.deleteCircle(10L)

        coVerify { circleRepository.deleteCircleWithChildren(10L) }
    }

    @Test
    fun `addMemberToCircle adds contact id when not already member`() = runTest {
        coEvery { circleRepository.updateCircle(any()) } returns Unit

        useCase.addMemberToCircle(circle2, 1L)

        coVerify {
            circleRepository.updateCircle(match { it.memberIds.contains(1L) && it.memberIds.contains(3L) })
        }
    }

    @Test
    fun `addMemberToCircle skips when already member`() = runTest {
        coEvery { circleRepository.updateCircle(any()) } returns Unit

        useCase.addMemberToCircle(circle1, 1L)

        coVerify(exactly = 0) { circleRepository.updateCircle(any()) }
    }

    @Test
    fun `removeMemberFromCircle removes contact id`() = runTest {
        coEvery { circleRepository.updateCircle(any()) } returns Unit

        useCase.removeMemberFromCircle(circle1, 1L)

        coVerify {
            circleRepository.updateCircle(match {
                !it.memberIds.contains(1L) && it.memberIds.contains(2L)
            })
        }
    }
}
