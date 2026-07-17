package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.CustomCategories
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.domain.repository.CustomTypeRepository
import com.tang.prm.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

/**
 * 事件列表聚合数据。
 *
 * 将 EventsViewModel 中分散的 3 路 combine（contacts + eventTypes + events）下沉到 Domain 层，
 * 使 ViewModel 不再直接持有 Repository。
 *
 * @param contacts 全部联系人（用于筛选、参与者展示等）
 * @param eventTypes 事件类型自定义选项
 * @param events 全部事件（未筛选）
 */
data class EventsAggregate(
    val contacts: List<Contact> = emptyList(),
    val eventTypes: List<CustomType> = emptyList(),
    val events: List<Event> = emptyList()
)

/**
 * 观察事件列表页所需的聚合数据。
 *
 * 合并 3 个 Repository 的流：contacts、eventTypes、events，
 * 任一上游变更都会重新发射聚合结果。
 *
 * 与 [ObserveEventReferenceDataUseCase] 的区别：
 * - 本 UseCase 面向列表/日历页，包含 `events` 主数据
 * - [ObserveEventReferenceDataUseCase] 面向表单页，包含 emotions + weathers 但不含 events
 */
class ObserveEventsAggregateUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val contactRepository: ContactRepository,
    private val customTypeRepository: CustomTypeRepository
) {
    fun invoke(): Flow<EventsAggregate> = combine(
        contactRepository.getAllContacts().distinctUntilChanged(),
        customTypeRepository.getTypesByCategory(CustomCategories.EVENT_TYPE).distinctUntilChanged(),
        eventRepository.getAllEvents()
    ) { contacts, eventTypes, events ->
        EventsAggregate(contacts, eventTypes, events)
    }
}
