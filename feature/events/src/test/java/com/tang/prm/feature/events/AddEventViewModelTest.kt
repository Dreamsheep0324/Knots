package com.tang.prm.feature.events

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.model.EventType
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.domain.repository.CustomTypeRepository
import com.tang.prm.domain.repository.EventRepository
import com.tang.prm.domain.usecase.EventReferenceData
import com.tang.prm.domain.usecase.ObserveEventReferenceDataUseCase
import com.tang.prm.domain.usecase.UpdateInteractionUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class AddEventViewModelTest {

    private lateinit var eventRepository: EventRepository
    private lateinit var contactRepository: ContactRepository
    private lateinit var customTypeRepository: CustomTypeRepository
    private lateinit var updateInteractionUseCase: UpdateInteractionUseCase
    private lateinit var observeReferenceDataUseCase: ObserveEventReferenceDataUseCase
    private lateinit var viewModel: AddEventViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        eventRepository = mockk()
        contactRepository = mockk()
        customTypeRepository = mockk()
        updateInteractionUseCase = mockk()
        observeReferenceDataUseCase = mockk()

        every { observeReferenceDataUseCase.invoke() } returns flowOf(EventReferenceData())
        coEvery { customTypeRepository.getTypeCountByCategory(any()) } returns 1
        coEvery { customTypeRepository.insertTypes(any()) } returns Unit
        coEvery { eventRepository.insertEventWithParticipants(any(), any()) } returns 1L
        coEvery { eventRepository.updateEventWithParticipants(any(), any()) } returns Unit
        coEvery { updateInteractionUseCase(any(), any(), any()) } returns Unit

        viewModel = AddEventViewModel(
            eventRepository, contactRepository, customTypeRepository,
            updateInteractionUseCase, observeReferenceDataUseCase
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `saveEvent with blank title emits errorMessage`() = runTest {
        viewModel.errorMessage.test {
            viewModel.saveEvent()
            val msg = awaitItem()
            assertThat(msg).isEqualTo("请填写标题和事件类型")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saveEvent with valid data calls useCase and sets isSaved`() = runTest {
        viewModel.uiState.test {
            awaitItem()
            viewModel.updateTitle("测试事件")
            awaitItem()
            viewModel.updateType(EventType.MEETUP.name)
            awaitItem()
            viewModel.saveEvent()
            val state = awaitItem()
            assertThat(state.isSaved).isTrue()
            cancelAndIgnoreRemainingEvents()
        }

        coVerify { eventRepository.insertEventWithParticipants(any(), emptyList()) }
    }

    @Test
    fun `saveEvent in edit mode calls updateEventWithParticipants`() = runTest {
        val existingEvent = Event(
            id = 42, title = "已有事件", type = EventType.MEETUP, time = 1000L,
            customTypeName = null, description = "描述", remarks = "备注"
        )
        every { eventRepository.getEventById(42L) } returns flowOf(existingEvent)

        viewModel = AddEventViewModel(
            eventRepository, contactRepository, customTypeRepository,
            updateInteractionUseCase, observeReferenceDataUseCase
        )

        viewModel.uiState.test {
            awaitItem()
            viewModel.loadEvent(42L)
            val loaded = awaitItem()
            assertThat(loaded.title).isEqualTo("已有事件")
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.uiState.test {
            awaitItem()
            viewModel.saveEvent()
            val state = awaitItem()
            assertThat(state.isSaved).isTrue()
            cancelAndIgnoreRemainingEvents()
        }

        coVerify { eventRepository.updateEventWithParticipants(any(), emptyList()) }
    }

    @Test
    fun `loadEvent maps event fields to uiState correctly`() = runTest {
        val existingEvent = Event(
            id = 1, title = "加载测试", type = EventType.CONVERSATION,
            time = 5000L, location = "咖啡馆", weather = "晴", emotion = "开心",
            description = "描述内容", remarks = "备注内容",
            photos = listOf("photo1", "photo2")
        )
        every { eventRepository.getEventById(1L) } returns flowOf(existingEvent)

        viewModel = AddEventViewModel(
            eventRepository, contactRepository, customTypeRepository,
            updateInteractionUseCase, observeReferenceDataUseCase
        )

        viewModel.uiState.test {
            awaitItem()
            viewModel.loadEvent(1L)
            val state = awaitItem()
            assertThat(state.title).isEqualTo("加载测试")
            assertThat(state.type).isEqualTo(EventType.CONVERSATION.name)
            assertThat(state.time).isEqualTo(5000L)
            assertThat(state.location).isEqualTo("咖啡馆")
            assertThat(state.weather).isEqualTo("晴")
            assertThat(state.emotion).isEqualTo("开心")
            assertThat(state.description).isEqualTo("描述内容")
            assertThat(state.remarks).isEqualTo("备注内容")
            assertThat(state.photos).hasSize(2)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateTitle sets hasUnsavedChanges`() = runTest {
        viewModel.uiState.test {
            awaitItem()
            viewModel.updateTitle("新标题")
            val state = awaitItem()
            assertThat(state.title).isEqualTo("新标题")
            assertThat(state.hasUnsavedChanges).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addPhoto appends to photos list`() = runTest {
        viewModel.uiState.test {
            awaitItem()
            viewModel.addPhoto("uri1")
            val state1 = awaitItem()
            assertThat(state1.photos).hasSize(1)
            assertThat(state1.photos.first()).isEqualTo("uri1")

            viewModel.addPhoto("uri2")
            val state2 = awaitItem()
            assertThat(state2.photos).hasSize(2)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `removePhotoAt removes correct index`() = runTest {
        viewModel.uiState.test {
            awaitItem()
            viewModel.addPhoto("uri1")
            awaitItem()
            viewModel.addPhoto("uri2")
            awaitItem()
            viewModel.addPhoto("uri3")
            awaitItem()
            viewModel.removePhotoAt(1)
            val state = awaitItem()
            assertThat(state.photos).hasSize(2)
            assertThat(state.photos).containsExactly("uri1", "uri3")
            cancelAndIgnoreRemainingEvents()
        }
    }
}
