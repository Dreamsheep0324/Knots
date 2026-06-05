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
     * @return 新联系人 ID
     */
    suspend fun createContact(
        contact: Contact,
        isLunarBirthday: Boolean = false,
        contactName: String = contact.name,
        contactAvatar: String? = contact.avatar
    ): Long {
        val newId = contactRepository.insertContact(contact)

        contact.birthday?.let { birthdayLong ->
            anniversaryRepository.insertAnniversary(
                Anniversary(
                    contactId = newId,
                    name = "生日",
                    type = AnniversaryType.BIRTHDAY,
                    date = birthdayLong,
                    isLunar = isLunarBirthday,
                    isRepeat = true,
                    remarks = null,
                    contactName = contactName,
                    contactAvatar = contactAvatar
                )
            )
        }

        return newId
    }

    /**
     * 更新联系人，同步生日纪念日
     */
    suspend fun updateContact(
        contact: Contact,
        isLunarBirthday: Boolean = false,
        contactName: String = contact.name,
        contactAvatar: String? = contact.avatar
    ) {
        contactRepository.updateContact(contact)

        contact.birthday?.let { birthdayLong ->
            syncBirthdayAnniversary(contact.id, birthdayLong, isLunarBirthday, contactName, contactAvatar)
        }
    }

    /**
     * 同步生日纪念日：查找已有生日纪念日并更新日期和农历标记
     */
    private suspend fun syncBirthdayAnniversary(
        contactId: Long,
        birthdayLong: Long,
        isLunar: Boolean,
        contactName: String,
        contactAvatar: String?
    ) {
        anniversaryRepository.getAnniversariesByContact(contactId).first()
            .filter { it.type == AnniversaryType.BIRTHDAY }
            .forEach { anniversary ->
                anniversaryRepository.updateAnniversary(
                    anniversary.copy(
                        date = birthdayLong,
                        isLunar = isLunar,
                        contactName = contactName,
                        contactAvatar = contactAvatar
                    )
                )
            }
    }
}
