package com.tang.prm.data.mapper

// Alignment checklist for Contact ↔ ContactEntity:
// When adding/removing fields, ensure ALL of the following are updated:
// 1. Contact (domain model)
// 2. ContactEntity (Room entity)
// 3. ContactMapper (toEntity / toDomain)
// 4. ContactDao (queries)
// 5. Room migration (if entity schema changes)
// 6. UI forms (AddContactScreen, ContactDetailScreen)

import com.tang.prm.data.local.dao.ContactListItemEntity
import com.tang.prm.data.local.database.ListStringConverter
import com.tang.prm.data.local.entity.ContactAttributeEntity
import com.tang.prm.data.local.entity.ContactEntity
import com.tang.prm.data.local.entity.ContactGroupEntity
import com.tang.prm.data.local.entity.ContactTagEntity
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.ContactGroup
import com.tang.prm.domain.model.ContactTag
import com.tang.prm.domain.model.Gender

/** Map lightweight [ContactListItemEntity] to domain [Contact], with unused fields at defaults. */
fun ContactListItemEntity.toDomain() = Contact(
    id = id, name = name, avatar = avatar, nickname = nickname,
    phone = phone, relationship = relationship, groupId = groupId,
    intimacyScore = intimacyScore, lastInteractionTime = lastInteractionTime,
    updatedAt = updatedAt
)

fun Contact.toEntity() = ContactEntity(
    id = id, name = name, avatar = avatar, nickname = nickname, gender = gender.value,
    birthday = birthday, knowingDate = knowingDate,
    phone = phone, email = email, city = city, address = address, education = education,
    company = company, jobTitle = jobTitle, industry = industry, hobby = hobby, habit = habit,
    diet = diet, skill = skill, mbti = mbti, spouseName = spouseName,
    childrenCount = childrenCount, childrenNames = childrenNames, introducer = introducer,
    relationship = relationship, groupId = groupId,
    intimacyScore = intimacyScore, lastInteractionTime = lastInteractionTime,
    customFields = customFields, notes = notes, createdAt = createdAt, updatedAt = updatedAt
)

/**
 * Merge ContactEntity with its ContactAttributeEntity list to produce a domain Contact.
 * Attributes from contact_attributes override the JSON string fields in ContactEntity.
 */
fun ContactEntity.toDomainWithAttributes(attributes: List<ContactAttributeEntity>): Contact {
    val attrMap = attributes.groupBy { it.category.lowercase() }
    return Contact(
        id = id, name = name, avatar = avatar, nickname = nickname, gender = Gender.fromValue(gender),
        birthday = birthday, knowingDate = knowingDate,
        phone = phone, email = email, city = city, address = address, education = education,
        company = company, jobTitle = jobTitle, industry = industry,
        hobby = attrMap["hobby"]?.toAttributeValueJson() ?: hobby,
        habit = attrMap["habit"]?.toAttributeValueJson() ?: habit,
        diet = attrMap["diet"]?.toAttributeValueJson() ?: diet,
        skill = attrMap["skill"]?.toAttributeValueJson() ?: skill,
        mbti = mbti, spouseName = spouseName,
        childrenCount = childrenCount, childrenNames = childrenNames, introducer = introducer,
        relationship = relationship, groupId = groupId,
        intimacyScore = intimacyScore, lastInteractionTime = lastInteractionTime,
        customFields = customFields, notes = notes, createdAt = createdAt, updatedAt = updatedAt
    )
}

/**
 * 复用 Room 的 [ListStringConverter] 把同一类别的多条 [ContactAttributeEntity] 序列化为 JSON 字符串，
 * 与数据库存储格式保持一致，避免手写 JSON 拼接导致特殊字符（`"`、`\`、控制字符）未转义。
 */
private fun List<ContactAttributeEntity>.toAttributeValueJson(): String =
    LIST_STRING_CONVERTER.fromList(this.map { it.value })

private val LIST_STRING_CONVERTER = ListStringConverter()

/**
 * Convert a contact's hobby/habit/diet/skill JSON strings into ContactAttributeEntity list.
 */
fun Contact.toAttributeEntities(): List<ContactAttributeEntity> {
    val result = mutableListOf<ContactAttributeEntity>()
    for ((category, json) in listOf(
        "HOBBY" to hobby,
        "HABIT" to habit,
        "DIET" to diet,
        "SKILL" to skill
    )) {
        for (value in LIST_STRING_CONVERTER.fromString(json)) {
            result.add(ContactAttributeEntity(contactId = id, category = category, value = value))
        }
    }
    return result
}

fun ContactGroupEntity.toDomain() = ContactGroup(id, name, color, sortOrder, createdAt)

fun ContactGroup.toEntity() = ContactGroupEntity(id, name, color, sortOrder, createdAt)

fun ContactTagEntity.toDomain() = ContactTag(id, name, color, createdAt)

fun ContactTag.toEntity() = ContactTagEntity(id, name, color, createdAt)
