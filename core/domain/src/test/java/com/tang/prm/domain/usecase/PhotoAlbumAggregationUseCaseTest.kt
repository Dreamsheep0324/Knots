package com.tang.prm.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.EventType
import com.tang.prm.domain.model.SourceTypes
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.model.Gift
import com.tang.prm.domain.model.GiftType
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.domain.repository.EventRepository
import com.tang.prm.domain.repository.GiftRepository
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
class PhotoAlbumAggregationUseCaseTest {

    @MockK
    private lateinit var eventRepository: EventRepository

    @MockK
    private lateinit var giftRepository: GiftRepository

    @MockK
    private lateinit var contactRepository: ContactRepository

    private lateinit var useCase: PhotoAlbumAggregationUseCase

    @BeforeEach
    fun setUp() {
        useCase = PhotoAlbumAggregationUseCase(eventRepository, giftRepository, contactRepository)
    }

    private fun setupMocks(
        events: List<Event> = emptyList(),
        gifts: List<Gift> = emptyList(),
        contacts: List<Contact> = emptyList()
    ) {
        every { eventRepository.getAllEvents() } returns flowOf(events)
        every { giftRepository.getAllGifts() } returns flowOf(gifts)
        every { contactRepository.getAllContacts() } returns flowOf(contacts)
    }

    @Nested
    @DisplayName("CONVERSATION 区分")
    inner class ConversationTest {

        @Test
        fun `conversation event maps to album chat source type`() = runTest {
            val events = listOf(
                Event(id = 1, title = "对话", type = EventType.CONVERSATION,
                    time = 1000L, photos = listOf("photo1.jpg"))
            )
            setupMocks(events = events)

            useCase().test {
                val data = awaitItem()
                assertThat(data.allPhotos).hasSize(1)
                assertThat(data.allPhotos[0].sourceType).isEqualTo(SourceTypes.ALBUM_CHAT)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `meetup event maps to album event source type`() = runTest {
            val events = listOf(
                Event(id = 1, title = "见面", type = EventType.MEETUP,
                    time = 1000L, photos = listOf("photo1.jpg"))
            )
            setupMocks(events = events)

            useCase().test {
                val data = awaitItem()
                assertThat(data.allPhotos).hasSize(1)
                assertThat(data.allPhotos[0].sourceType).isEqualTo(SourceTypes.ALBUM_EVENT)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    @DisplayName("照片聚合")
    inner class AggregationTest {

        @Test
        fun `event with multiple photos creates multiple album photos`() = runTest {
            val events = listOf(
                Event(id = 1, title = "旅游", type = EventType.TRAVEL,
                    time = 1000L, photos = listOf("p1.jpg", "p2.jpg", "p3.jpg"))
            )
            setupMocks(events = events)

            useCase().test {
                val data = awaitItem()
                assertThat(data.allPhotos).hasSize(3)
                assertThat(data.allPhotos.map { it.uri })
                    .containsExactly("p1.jpg", "p2.jpg", "p3.jpg").inOrder()
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `gift photos included with gift source type`() = runTest {
            val gifts = listOf(
                Gift(id = 1, contactId = 1L, giftName = "礼物", date = 1000L,
                    isSent = true, photos = listOf("gift1.jpg"))
            )
            setupMocks(gifts = gifts, contacts = listOf(Contact(id = 1L, name = "Bob")))

            useCase().test {
                val data = awaitItem()
                assertThat(data.allPhotos).hasSize(1)
                assertThat(data.allPhotos[0].sourceType).isEqualTo(SourceTypes.ALBUM_GIFT)
                assertThat(data.allPhotos[0].sourceTitle).isEqualTo("礼物")
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `mixed sources sorted by date descending`() = runTest {
            val events = listOf(
                Event(id = 1, title = "旧事件", type = EventType.MEETUP,
                    time = 1000L, photos = listOf("old.jpg"))
            )
            val gifts = listOf(
                Gift(id = 1, contactId = 1L, giftName = "新礼物", date = 3000L,
                    isSent = true, photos = listOf("new.jpg"))
            )
            setupMocks(events = events, gifts = gifts, contacts = listOf(Contact(id = 1L, name = "A")))

            useCase().test {
                val data = awaitItem()
                assertThat(data.allPhotos).hasSize(2)
                assertThat(data.allPhotos[0].sourceTitle).isEqualTo("新礼物")
                assertThat(data.allPhotos[1].sourceTitle).isEqualTo("旧事件")
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    @DisplayName("联系人信息")
    inner class ContactInfoTest {

        @Test
        fun `gift photo resolves contact from map`() = runTest {
            val gifts = listOf(
                Gift(id = 1, contactId = 42L, giftName = "礼物", date = 1000L,
                    isSent = true, photos = listOf("g.jpg"))
            )
            setupMocks(gifts = gifts, contacts = listOf(Contact(id = 42L, name = "Alice")))

            useCase().test {
                val data = awaitItem()
                assertThat(data.allPhotos[0].contactName).isEqualTo("Alice")
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `event photo uses first participant`() = runTest {
            val contact = Contact(id = 5L, name = "Charlie")
            val events = listOf(
                Event(id = 1, title = "见面", type = EventType.MEETUP,
                    time = 1000L, photos = listOf("p.jpg"), participants = listOf(contact))
            )
            setupMocks(events = events)

            useCase().test {
                val data = awaitItem()
                assertThat(data.allPhotos[0].contactName).isEqualTo("Charlie")
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
}
