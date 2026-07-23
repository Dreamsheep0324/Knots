package com.tang.prm.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.Circle
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.repository.CircleRepository
import com.tang.prm.domain.repository.ContactRepository
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class ContactListAggregationUseCaseTest {

    private lateinit var contactRepository: ContactRepository
    private lateinit var circleRepository: CircleRepository
    private lateinit var useCase: ContactListAggregationUseCase

    private val contact1 = Contact(id = 1, name = "张三", intimacyScore = 80)
    private val contact2 = Contact(id = 2, name = "李四", intimacyScore = 50)
    private val contact3 = Contact(id = 3, name = "王五", intimacyScore = 30)
    private val allContacts = listOf(contact1, contact2, contact3)

    private val circle1 = Circle(id = 10, name = "朋友圈A", memberIds = listOf(1L, 2L))
    private val circle2 = Circle(id = 20, name = "朋友圈B", memberIds = listOf(3L))
    private val allCircles = listOf(circle1, circle2)

    @BeforeEach
    fun setUp() {
        contactRepository = mockk()
        circleRepository = mockk()

        every { contactRepository.getAllContacts() } returns flowOf(allContacts)
        every { circleRepository.getAllCircles() } returns flowOf(allCircles)

        useCase = ContactListAggregationUseCase(contactRepository, circleRepository)
    }

    @Test
    fun `invoke maps circles with their members`() = runTest {
        useCase().test {
            val result = awaitItem()
            assertThat(result.contacts).hasSize(3)
            assertThat(result.circles).hasSize(2)

            val circleA = result.circles.find { it.circle.id == 10L }
            assertThat(circleA?.members?.map { it.id }).containsExactly(1L, 2L).inOrder()

            val circleB = result.circles.find { it.circle.id == 20L }
            assertThat(circleB?.members?.map { it.id }).containsExactly(3L)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke handles circle with no matching contacts`() = runTest {
        val emptyCircle = Circle(id = 30, name = "空圈", memberIds = listOf(99L))
        every { circleRepository.getAllCircles() } returns flowOf(listOf(emptyCircle))

        useCase().test {
            val result = awaitItem()
            val emptyCircleResult = result.circles.first()
            assertThat(emptyCircleResult.members).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAvailableContacts returns contacts not in circle`() {
        val result = useCase.getAvailableContacts(allContacts, circle1)

        assertThat(result.map { it.id }).containsExactly(3L)
    }

    @Test
    fun `getSortedCircles sorts by member count ascending`() {
        val circles = listOf(
            CircleWithMembers(circle1, listOf(contact1, contact2)),
            CircleWithMembers(circle2, listOf(contact3))
        )

        val sorted = useCase.getSortedCircles(circles, CircleSortMode.MEMBER_COUNT_ASC)

        assertThat(sorted.map { it.circle.id }).containsExactly(20L, 10L).inOrder()
    }

    @Test
    fun `getSortedCircles sorts by member count descending`() {
        val circles = listOf(
            CircleWithMembers(circle1, listOf(contact1, contact2)),
            CircleWithMembers(circle2, listOf(contact3))
        )

        val sorted = useCase.getSortedCircles(circles, CircleSortMode.MEMBER_COUNT_DESC)

        assertThat(sorted.map { it.circle.id }).containsExactly(10L, 20L).inOrder()
    }

    @Test
    fun `getSortedCircles sorts by intimacy ascending`() {
        val circles = listOf(
            CircleWithMembers(circle1, listOf(contact1, contact2)),  // avg = 65
            CircleWithMembers(circle2, listOf(contact3))              // avg = 30
        )

        val sorted = useCase.getSortedCircles(circles, CircleSortMode.INTIMACY_ASC)

        assertThat(sorted.map { it.circle.id }).containsExactly(20L, 10L).inOrder()
    }

    @Test
    fun `getSortedCircles sorts by intimacy descending`() {
        val circles = listOf(
            CircleWithMembers(circle1, listOf(contact1, contact2)),  // avg = 65
            CircleWithMembers(circle2, listOf(contact3))              // avg = 30
        )

        val sorted = useCase.getSortedCircles(circles, CircleSortMode.INTIMACY_DESC)

        assertThat(sorted.map { it.circle.id }).containsExactly(10L, 20L).inOrder()
    }

    @Test
    fun `getSortedCircles default returns unchanged order`() {
        val circles = listOf(
            CircleWithMembers(circle1, listOf(contact1)),
            CircleWithMembers(circle2, listOf(contact3))
        )

        val sorted = useCase.getSortedCircles(circles, CircleSortMode.DEFAULT)

        assertThat(sorted.map { it.circle.id }).containsExactly(10L, 20L).inOrder()
    }
}
