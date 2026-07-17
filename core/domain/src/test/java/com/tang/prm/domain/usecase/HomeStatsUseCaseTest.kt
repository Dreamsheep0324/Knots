package com.tang.prm.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.EventType
import com.tang.prm.domain.repository.*
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class HomeStatsUseCaseTest {

    @MockK private lateinit var giftRepository: GiftRepository
    @MockK private lateinit var thoughtRepository: ThoughtRepository
    @MockK private lateinit var contactRepository: ContactRepository
    @MockK private lateinit var favoriteRepository: FavoriteRepository
    @MockK private lateinit var circleRepository: CircleRepository
    @MockK private lateinit var anniversaryRepository: AnniversaryRepository
    @MockK private lateinit var eventRepository: EventRepository
    @MockK private lateinit var subscriptionRepository: SubscriptionRepository
    @MockK private lateinit var recipeRepository: RecipeRepository

    private lateinit var useCase: HomeStatsUseCase

    @BeforeEach
    fun setUp() {
        useCase = HomeStatsUseCase(
            giftRepository, thoughtRepository, contactRepository,
            favoriteRepository, circleRepository, anniversaryRepository,
            eventRepository, subscriptionRepository, recipeRepository
        )
    }

    private fun setupCountMocks(
        giftCount: Int = 0, thoughtCount: Int = 0, contactCount: Int = 0,
        favoriteCount: Int = 0, circleCount: Int = 0, anniversaryCount: Int = 0,
        eventCount: Int = 0, conversationCount: Int = 0,
        footprintCount: Int = 0, eventPhotoCount: Int = 0, giftPhotoCount: Int = 0,
        subscriptionCount: Int = 0
    ) {
        every { giftRepository.getGiftCount() } returns flowOf(giftCount)
        every { thoughtRepository.getThoughtCount() } returns flowOf(thoughtCount)
        every { contactRepository.getContactCount() } returns flowOf(contactCount)
        every { favoriteRepository.getFavoriteCount() } returns flowOf(favoriteCount)
        every { circleRepository.getCircleCount() } returns flowOf(circleCount)
        every { anniversaryRepository.getAnniversaryCount() } returns flowOf(anniversaryCount)
        every { eventRepository.getEventCount() } returns flowOf(eventCount)
        every { eventRepository.getEventsByType(EventType.CONVERSATION.name) } returns flowOf(List(conversationCount) { mockEvent() })
        every { eventRepository.getEventsWithLocation() } returns flowOf(List(footprintCount) { mockEvent() })
        every { eventRepository.getPhotoCount() } returns flowOf(eventPhotoCount)
        every { giftRepository.getPhotoCount() } returns flowOf(giftPhotoCount)
        every { subscriptionRepository.getSubscriptionCount() } returns flowOf(subscriptionCount)
        every { recipeRepository.getRecipeCount() } returns flowOf(0)
        every { contactRepository.getAllContacts() } returns flowOf(emptyList())
    }

    private fun mockEvent() = com.tang.prm.domain.model.Event(
        id = 0, title = "mock", type = EventType.OTHER, time = 0L
    )

    @Test
    fun allZeroCounts() = runTest {
        setupCountMocks()

        useCase.getStats().test {
            val stats = awaitItem()
            assertThat(stats.giftCount).isEqualTo(0)
            assertThat(stats.contactCount).isEqualTo(0)
            assertThat(stats.photoCount).isEqualTo(0)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun countsAggregatedCorrectly() = runTest {
        setupCountMocks(
            giftCount = 5, thoughtCount = 10, contactCount = 20,
            favoriteCount = 3, circleCount = 2, anniversaryCount = 7,
            eventCount = 15, conversationCount = 4,
            footprintCount = 8, eventPhotoCount = 30, giftPhotoCount = 12,
            subscriptionCount = 1
        )

        useCase.getStats().test {
            val stats = awaitItem()
            assertThat(stats.giftCount).isEqualTo(5)
            assertThat(stats.thoughtCount).isEqualTo(10)
            assertThat(stats.contactCount).isEqualTo(20)
            assertThat(stats.favoriteCount).isEqualTo(3)
            assertThat(stats.circleCount).isEqualTo(2)
            assertThat(stats.anniversaryCount).isEqualTo(7)
            assertThat(stats.eventCount).isEqualTo(15)
            assertThat(stats.conversationCount).isEqualTo(4)
            assertThat(stats.footprintCount).isEqualTo(8)
            assertThat(stats.subscriptionCount).isEqualTo(1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Nested
    @DisplayName("photoCount = events + gifts")
    inner class PhotoCountTest {

        @Test
        fun photoCount_isSumOfEventAndGiftPhotos() = runTest {
            setupCountMocks(eventPhotoCount = 30, giftPhotoCount = 12)

            useCase.getStats().test {
                val stats = awaitItem()
                assertThat(stats.photoCount).isEqualTo(42)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
}
