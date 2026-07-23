package com.tang.prm.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.CustomCategories
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.model.EventType
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.domain.repository.CustomTypeRepository
import com.tang.prm.domain.repository.EventRepository
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
class FootprintAggregationUseCaseTest {

    @MockK
    private lateinit var eventRepository: EventRepository

    @MockK
    private lateinit var contactRepository: ContactRepository

    @MockK
    private lateinit var customTypeRepository: CustomTypeRepository

    private lateinit var useCase: FootprintAggregationUseCase

    @BeforeEach
    fun setUp() {
        useCase = FootprintAggregationUseCase(eventRepository, contactRepository, customTypeRepository)
    }

    private fun setupMocks(
        events: List<Event> = emptyList(),
        contacts: List<Contact> = emptyList(),
        eventTypes: List<CustomType> = emptyList()
    ) {
        every { eventRepository.getEventsWithLocation() } returns flowOf(events)
        every { contactRepository.getAllContacts() } returns flowOf(contacts)
        every { customTypeRepository.getTypesByCategory(CustomCategories.EVENT_TYPE) } returns flowOf(eventTypes)
    }

    @Nested
    @DisplayName("事件→足迹映射")
    inner class MappingTest {

        @Test
        fun `events with location mapped to footprints`() = runTest {
            val contact = Contact(id = 1L, name = "Alice")
            val events = listOf(
                Event(
                    id = 1, title = "咖啡", type = EventType.MEETUP,
                    time = 2000L, location = "星巴克",
                    participants = listOf(contact)
                )
            )
            setupMocks(events = events)

            useCase().test {
                val data = awaitItem()
                assertThat(data.footprints).hasSize(1)
                assertThat(data.footprints[0].location).isEqualTo("星巴克")
                assertThat(data.footprints[0].eventTitle).isEqualTo("咖啡")
                assertThat(data.footprints[0].contactName).isEqualTo("Alice")
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `events without location filtered out`() = runTest {
            val events = listOf(
                Event(id = 1, title = "通话", type = EventType.CALL, time = 1000L, location = null),
                Event(id = 2, title = "见面", type = EventType.MEETUP, time = 2000L, location = "  "),
                Event(id = 3, title = "旅游", type = EventType.TRAVEL, time = 3000L, location = "北京")
            )
            setupMocks(events = events)

            useCase().test {
                val data = awaitItem()
                assertThat(data.footprints).hasSize(1)
                assertThat(data.footprints[0].eventTitle).isEqualTo("旅游")
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    @DisplayName("排序")
    inner class SortTest {

        @Test
        fun `footprints sorted by date descending`() = runTest {
            val events = listOf(
                Event(id = 1, title = "旧", type = EventType.MEETUP, time = 1000L, location = "A"),
                Event(id = 2, title = "新", type = EventType.MEETUP, time = 3000L, location = "B"),
                Event(id = 3, title = "中", type = EventType.MEETUP, time = 2000L, location = "C")
            )
            setupMocks(events = events)

            useCase().test {
                val data = awaitItem()
                assertThat(data.footprints.map { it.eventTitle })
                    .containsExactly("新", "中", "旧").inOrder()
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    @DisplayName("聚合数据完整性")
    inner class AggregateDataTest {

        @Test
        fun `returns contacts and event types`() = runTest {
            val contacts = listOf(Contact(id = 1L, name = "Alice"))
            val eventTypes = listOf(CustomType(id = 1L, name = "聚会", category = CustomCategories.EVENT_TYPE))
            setupMocks(contacts = contacts, eventTypes = eventTypes)

            useCase().test {
                val data = awaitItem()
                assertThat(data.contacts).hasSize(1)
                assertThat(data.eventTypes).hasSize(1)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `custom type name used when available`() = runTest {
            val events = listOf(
                Event(id = 1, title = "团建", type = EventType.MEETUP,
                    customTypeName = "公司活动", time = 1000L, location = "公园")
            )
            setupMocks(events = events)

            useCase().test {
                val data = awaitItem()
                assertThat(data.footprints[0].eventType).isEqualTo("公司活动")
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `type name falls back to event type name`() = runTest {
            val events = listOf(
                Event(id = 1, title = "见面", type = EventType.MEETUP,
                    customTypeName = null, time = 1000L, location = "公园")
            )
            setupMocks(events = events)

            useCase().test {
                val data = awaitItem()
                assertThat(data.footprints[0].eventType).isEqualTo("MEETUP")
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
}
