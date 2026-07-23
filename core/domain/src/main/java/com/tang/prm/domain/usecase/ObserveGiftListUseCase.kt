package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.Gift
import com.tang.prm.domain.model.GiftType
import com.tang.prm.domain.model.SourceTypes
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.domain.repository.GiftRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

/**
 * 礼物列表展示聚合模型。
 *
 * C-1 修复：从 feature/gifts 的 GiftsViewModel 移入 domain 层，
 * 使 ViewModel 不再直接依赖 Repository 的观察方法。
 *
 * 注意：原 feature 层的 GiftRecord 含 `photos: List<Uri>` getter，
 * 但 `Uri` 是 `android.net.Uri`，domain 层为纯 JVM 模块不能依赖 Android，
 * 故移除该 getter。调用方如需 Uri 列表，请使用
 * `giftRecord.gift.photos.map { Uri.parse(it) }`。
 */
data class GiftRecord(
    val gift: Gift,
    val contactName: String,
    val contactAvatar: String?
) {
    val id get() = gift.id
    val contactId get() = gift.contactId
    val giftName get() = gift.giftName
    val giftType: GiftType get() = gift.giftType
    val date get() = gift.date
    val isSent get() = gift.isSent
    val occasion get() = gift.occasion
    val description get() = gift.description
    val location get() = gift.location
    val createdAt get() = gift.createdAt
}

/**
 * 礼物列表聚合数据。
 */
data class GiftListAggregateData(
    val gifts: List<GiftRecord>,
    val availableContacts: List<Contact>,
    val favoriteGiftIds: Set<Long>
)

/**
 * 礼物列表观察 UseCase。
 *
 * C-1 修复：将 GiftsViewModel 中对 GiftRepository + ContactRepository + ObserveFavoritesUseCase
 * 的三路聚合逻辑下沉到 domain 层，ViewModel 不再直接依赖 Repository 的观察方法。
 *
 * 遵循 Q-3 约定：上游 Flow 加 `distinctUntilChanged()` 避免无意义重发。
 */
class ObserveGiftListUseCase @Inject constructor(
    private val giftRepository: GiftRepository,
    private val contactRepository: ContactRepository,
    private val observeFavoritesUseCase: ObserveFavoritesUseCase
) {
    operator fun invoke(): Flow<GiftListAggregateData> = combine(
        giftRepository.getAllGifts().distinctUntilChanged(),
        contactRepository.getAllContacts().distinctUntilChanged(),
        observeFavoritesUseCase.getFavoriteIds(SourceTypes.GIFT).distinctUntilChanged()
    ) { giftList, contacts, favoriteIds ->
        val contactMap = contacts.associateBy { it.id }
        val gifts = giftList.map { gift ->
            GiftRecord(
                gift = gift,
                contactName = contactMap[gift.contactId]?.name ?: "未知人物",
                contactAvatar = contactMap[gift.contactId]?.avatar
            )
        }
        GiftListAggregateData(
            gifts = gifts,
            availableContacts = contacts,
            favoriteGiftIds = favoriteIds
        )
    }
}
