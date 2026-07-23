package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.AlbumPhoto
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.Event
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
    operator fun invoke(): Flow<PhotoAlbumAggregateData> = combine(
        eventRepository.getAllEvents().distinctUntilChanged(),
        giftRepository.getAllGifts().distinctUntilChanged(),
        contactRepository.getAllContacts().distinctUntilChanged()
    ) { events, gifts, contacts ->
        val contactMap = contacts.associateBy { it.id }
        val allPhotos = (extractEventPhotos(events) + extractGiftPhotos(gifts, contactMap))
            .sortedByDescending { it.date }
        PhotoAlbumAggregateData(allPhotos, contacts)
    }

    private fun extractEventPhotos(events: List<Event>): List<AlbumPhoto> =
        events.flatMap { event ->
            val sourceType = if (event.isConversation) SourceTypes.ALBUM_CHAT else SourceTypes.ALBUM_EVENT
            val participant = event.representativeParticipant
            event.photos.mapIndexed { photoIndex, photoUri ->
                AlbumPhoto(
                    id = "${sourceType}_${event.id}_${photoIndex}",
                    // B-5 修复：统一 trim 策略，与 extractGiftPhotos 一致，避免 URI 带空格时收藏键比对失配
                    uri = photoUri.trim(),
                    sourceType = sourceType,
                    sourceId = event.id,
                    sourceTitle = event.title,
                    contactId = participant?.id,
                    contactName = participant?.name,
                    contactAvatar = participant?.avatar,
                    // B-5 修复：保存全部参与者 ID，过滤时匹配任意参与者
                    allContactIds = event.participants.map { it.id },
                    allContactNames = event.participants.map { it.name },
                    allContactAvatars = event.participants.map { it.avatar },
                    date = event.time,
                    location = event.location
                )
            }
        }

    private fun extractGiftPhotos(gifts: List<Gift>, contactMap: Map<Long, Contact>): List<AlbumPhoto> =
        gifts.flatMap { gift ->
            gift.photos.mapIndexed { photoIndex, pathString ->
                AlbumPhoto(
                    id = "gift_${gift.id}_${photoIndex}",
                    uri = pathString.trim(),
                    sourceType = SourceTypes.ALBUM_GIFT,
                    sourceId = gift.id,
                    sourceTitle = gift.giftName,
                    contactId = gift.contactId,
                    contactName = contactMap[gift.contactId]?.name,
                    contactAvatar = contactMap[gift.contactId]?.avatar,
                    // B-5 修复：Gift 只关联一个联系人，allContactIds 含单个元素
                    allContactIds = listOfNotNull(gift.contactId),
                    date = gift.date,
                    location = gift.location
                )
            }
        }
}
