package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.CustomCategories
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.model.FootprintItem
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.domain.repository.CustomTypeRepository
import com.tang.prm.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

/**
 * 足迹数据聚合结果
 */
data class FootprintAggregateData(
    val footprints: List<FootprintItem>,
    val contacts: List<Contact>,
    val eventTypes: List<CustomType>
)

/**
 * 足迹数据聚合 UseCase
 *
 * 将 FootprintsViewModel 中的多 Repository 数据聚合逻辑提取到此 UseCase。
 */
class FootprintAggregationUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val contactRepository: ContactRepository,
    private val customTypeRepository: CustomTypeRepository
) {
    fun getAggregateData(): Flow<FootprintAggregateData> = combine(
        eventRepository.getEventsWithLocation().distinctUntilChanged(),
        contactRepository.getAllContacts().distinctUntilChanged(),
        customTypeRepository.getTypesByCategory(CustomCategories.EVENT_TYPE).distinctUntilChanged()
    ) { events, contacts, eventTypes ->
        val footprints = events
            .filter { !it.location.isNullOrBlank() }
            .map { event ->
                val participant = event.participants.firstOrNull()
                FootprintItem(
                    id = event.id,
                    location = event.location ?: "",
                    date = event.time,
                    eventType = event.customTypeName ?: event.type.name,
                    eventTitle = event.title,
                    contactId = participant?.id,
                    contactName = participant?.name,
                    contactAvatar = participant?.avatar,
                    description = event.description,
                    weather = event.weather,
                    emotion = event.emotion,
                    photoCount = event.photosCount
                )
            }
            .sortedByDescending { it.date }

        FootprintAggregateData(footprints, contacts, eventTypes)
    }
}
