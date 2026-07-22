package com.tang.prm.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.*
import com.tang.prm.domain.repository.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class ContactDetailAggregationUseCaseTest {

    private lateinit var contactRepository: ContactRepository
    private lateinit var eventRepository: EventRepository
    private lateinit var anniversaryRepository: AnniversaryRepository
    private lateinit var giftRepository: GiftRepository
    private lateinit var thoughtRepository: ThoughtRepository
    private lateinit var customTypeRepository: CustomTypeRepository
    private lateinit var observeFavoritesUseCase: ObserveFavoritesUseCase
    private lateinit var useCase: ContactDetailAggregationUseCase

    private val testContact = Contact(id = 1, name = "测试联系人", phone = "13800138000")
    private val testEvent = Event(id = 1, title = "事件", type = EventType.MEETUP, time = 1000L, participants = listOf(testContact))
    private val testConversation = Event(id = 2, title = "对话", type = EventType.CONVERSATION, time = 2000L, participants = listOf(testContact))
    private val testAnniversary = Anniversary(id = 1, contactId = 1, name = "纪念日", type = AnniversaryType.ANNIVERSARY, date = 1000L)
    private val testGift = Gift(id = 1, contactId = 1, giftName = "礼物", date = 1000L, isSent = true)
    private val testThought = Thought(id = 1, contactId = 1, content = "感悟")
    private val testCustomType = CustomType(id = 1, category = CustomCategories.HOBBY, name = "阅读")

    @BeforeEach
    fun setUp() {
        contactRepository = mockk()
        eventRepository = mockk()
        anniversaryRepository = mockk()
        giftRepository = mockk()
        thoughtRepository = mockk()
        customTypeRepository = mockk()
        observeFavoritesUseCase = mockk()

        every { contactRepository.getContactById(any()) } returns flowOf(testContact)
        every { eventRepository.getEventsByContact(any()) } returns flowOf(listOf(testEvent, testConversation))
        every { anniversaryRepository.getAnniversariesByContact(any()) } returns flowOf(listOf(testAnniversary))
        every { giftRepository.getGiftsByContactId(any()) } returns flowOf(listOf(testGift))
        every { thoughtRepository.getThoughtsByContact(any()) } returns flowOf(listOf(testThought))
        every { customTypeRepository.getAllTypesGroupedByCategory() } returns flowOf(
            mapOf(CustomCategories.HOBBY to listOf(testCustomType))
        )
        every { observeFavoritesUseCase.getFavoriteIds(SourceTypes.THOUGHT) } returns flowOf(setOf(1L))

        useCase = ContactDetailAggregationUseCase(
            contactRepository, eventRepository, anniversaryRepository,
            giftRepository, thoughtRepository, customTypeRepository, observeFavoritesUseCase
        )
    }

    @Test
    fun `getContactDetail aggregates all data sources`() = runTest {
        useCase.getContactDetail(1L).test {
            val result = awaitItem()
            assertThat(result.contact).isEqualTo(testContact)
            assertThat(result.events).hasSize(1)
            assertThat(result.conversations).hasSize(1)
            assertThat(result.anniversaries).hasSize(1)
            assertThat(result.gifts).hasSize(1)
            assertThat(result.thoughts).hasSize(1)
            assertThat(result.favoriteIds).containsExactly(1L)
            assertThat(result.hobbyOptions).hasSize(1)
            assertThat(result.isLoading).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getContactDetail separates conversations from events`() = runTest {
        useCase.getContactDetail(1L).test {
            val result = awaitItem()
            assertThat(result.events.none { it.type == EventType.CONVERSATION }).isTrue()
            assertThat(result.conversations.all { it.type == EventType.CONVERSATION }).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getContactDetail maps custom types by category`() = runTest {
        useCase.getContactDetail(1L).test {
            val result = awaitItem()
            assertThat(result.hobbyOptions).containsExactly(testCustomType)
            assertThat(result.habitOptions).isEmpty()
            assertThat(result.dietOptions).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getContactDetail returns null contact when not found`() = runTest {
        every { contactRepository.getContactById(any()) } returns flowOf(null)

        useCase.getContactDetail(999L).test {
            val result = awaitItem()
            assertThat(result.contact).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteContact delegates to repository`() = runTest {
        // REP-A-1 修复后：UseCase 协调跨聚合操作，先清理礼物（含照片），再删除联系人
        coEvery { contactRepository.deleteContact(any()) } returns Unit
        coEvery { giftRepository.deleteGiftsByContactId(any()) } returns Unit

        useCase.deleteContact(1L)

        coVerify { giftRepository.deleteGiftsByContactId(1L) }
        coVerify { contactRepository.deleteContact(1L) }
    }
}
