package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.Anniversary
import com.tang.prm.domain.model.AnniversaryType
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.repository.AnniversaryRepository
import com.tang.prm.domain.repository.ContactRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * 创建/更新联系人 UseCase
 *
 * 将 AddContactVM.saveContact() 中的业务逻辑下沉到 Domain 层：
 * - 新建联系人时自动创建生日纪念日
 * - 编辑联系人时同步生日纪念日
 */
class CreateContactUseCase @Inject constructor(
    private val contactRepository: ContactRepository,
    private val anniversaryRepository: AnniversaryRepository
) {
    /**
     * 创建新联系人，若提供生日则自动创建生日纪念日
     *
     * A-8 修复：跨聚合写入通过 [ContactRepository.insertContactWithAnniversaries]
     * 在单一 Room 事务中完成，避免"联系人已落库但生日纪念日丢失"的半残状态。
     *
     * @return 新联系人 ID
     */
    suspend fun createContact(
        contact: Contact,
        contactName: String = contact.name,
        contactAvatar: String? = contact.avatar
    ): Long {
        val anniversaries = contact.birthday?.let { birthdayLong ->
            listOf(
                Anniversary(
                    contactId = 0L, // 由 Repository 填充为新联系人 ID
                    name = "生日",
                    type = AnniversaryType.BIRTHDAY,
                    date = birthdayLong,
                    isRepeat = true,
                    remarks = null,
                    contactName = contactName,
                    contactAvatar = contactAvatar
                )
            )
        } ?: emptyList()

        return contactRepository.insertContactWithAnniversaries(contact, anniversaries)
    }

    /**
     * 更新联系人，同步生日纪念日
     *
     * B-7 修复：当 birthday 从非 null 改为 null（清空）时，必须删除该联系人所有
     * BIRTHDAY 类型纪念日，否则详情页会一直显示"距离生日还有 X 天"。
     * 之前 `contact.birthday?.let` 直接跳过，旧生日纪念日残留。
     */
    suspend fun updateContact(
        contact: Contact,
        contactName: String = contact.name,
        contactAvatar: String? = contact.avatar
    ) {
        contactRepository.updateContact(contact)

        val birthdayLong = contact.birthday
        if (birthdayLong != null) {
            syncBirthdayAnniversary(contact.id, birthdayLong, contactName, contactAvatar)
        } else {
            deleteBirthdayAnniversaries(contact.id)
        }
    }

    /**
     * 同步生日纪念日：查找已有生日纪念日并更新日期
     */
    private suspend fun syncBirthdayAnniversary(
        contactId: Long,
        birthdayLong: Long,
        contactName: String,
        contactAvatar: String?
    ) {
        anniversaryRepository.getAnniversariesByContact(contactId).first()
            .filter { it.type == AnniversaryType.BIRTHDAY }
            .forEach { anniversary ->
                anniversaryRepository.updateAnniversary(
                    anniversary.copy(
                        date = birthdayLong,
                        contactName = contactName,
                        contactAvatar = contactAvatar
                    )
                )
            }
    }

    /**
     * 清空生日时删除该联系人所有 BIRTHDAY 类型纪念日
     */
    private suspend fun deleteBirthdayAnniversaries(contactId: Long) {
        anniversaryRepository.getAnniversariesByContact(contactId).first()
            .filter { it.type == AnniversaryType.BIRTHDAY }
            .forEach { anniversary ->
                anniversaryRepository.deleteAnniversary(anniversary.id)
            }
    }
}
