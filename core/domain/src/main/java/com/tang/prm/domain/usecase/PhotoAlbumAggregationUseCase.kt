package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.AlbumPhoto
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.EventType
import com.tang.prm.domain.model.Gift
import com.tang.prm.domain.model.SourceTypes
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.domain.repository.EventRepository
import com.tang.prm.domain.repository.GiftRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

/**
 * 照片数据聚合结果
 */
data class PhotoAlbumAggregateData(
    val allPhotos: List<AlbumPhoto>,
    val contacts: List<Contact>
)

/**
 * 照片相册数据聚合 UseCase
 *
 * 将 PhotoAlbumViewModel 中的多 Repository 数据聚合逻辑提取到此 UseCase。
 */
class PhotoAlbumAggregationUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val giftRepository: GiftRepository,
    private val contactRepository: ContactRepository
) {
    fun getAggregateData(): Flow<PhotoAlbumAggregateData> = combine(
        eventRepository.getAllEvents().distinctUntilChanged(),
        eventRepository.getEventsByType(EventType.CONVERSATION.name).distinctUntilChanged(),
        giftRepository.getAllGifts().distinctUntilChanged(),
        contactRepository.getAllContacts().distinctUntilChanged()
    ) { events, conversations, gifts, contacts ->
        val allEvents = events + conversations
        val contactMap = contacts.associateBy { it.id }
        val allPhotos = mutableListOf<AlbumPhoto>()

        allEvents.forEach { event ->
            event.photos.forEachIndexed { photoIndex, photoUri ->
                val participant = event.participants.firstOrNull()
                val allNames = event.participants.map { it.name }
                val allAvatars = event.participants.map { it.avatar }
                val sourceType = if (event.type == EventType.CONVERSATION) SourceTypes.ALBUM_CHAT else SourceTypes.ALBUM_EVENT
                allPhotos.add(
                    AlbumPhoto(
                        id = "${sourceType}_${event.id}_${photoIndex}",
                        uri = photoUri,
                        sourceType = sourceType,
                        sourceId = event.id,
                        sourceTitle = event.title,
                        contactId = participant?.id,
                        contactName = participant?.name,
                        contactAvatar = participant?.avatar,
                        allContactNames = allNames,
                        allContactAvatars = allAvatars,
                        date = event.time,
                        location = event.location
                    )
                )
            }
        }

        gifts.forEach { gift ->
            gift.photos.forEachIndexed { photoIndex, pathString ->
                allPhotos.add(
                    AlbumPhoto(
                        id = "gift_${gift.id}_${photoIndex}",
                        uri = pathString.trim(),
                        sourceType = SourceTypes.ALBUM_GIFT,
                        sourceId = gift.id,
                        sourceTitle = gift.giftName,
                        contactId = gift.contactId,
                        contactName = contactMap[gift.contactId]?.name,
                        contactAvatar = contactMap[gift.contactId]?.avatar,
                        date = gift.date,
                        location = gift.location
                    )
                )
            }
        }

        allPhotos.sortByDescending { it.date }
        PhotoAlbumAggregateData(allPhotos, contacts)
    }
}
