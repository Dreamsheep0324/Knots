package com.tang.prm.data.mapper

// Alignment checklist for Contact ↔ ContactEntity:
// When adding/removing fields, ensure ALL of the following are updated:
// 1. Contact (domain model)
// 2. ContactEntity (Room entity)
// 3. ContactMapper (toEntity / toDomain)
// 4. ContactDao (queries)
// 5. Room migration (if entity schema changes)
// 6. UI forms (AddContactScreen, ContactDetailScreen)

import com.tang.prm.data.local.entity.ContactAttributeEntity
import com.tang.prm.data.local.entity.ContactEntity
import com.tang.prm.data.local.entity.ContactGroupEntity
import com.tang.prm.data.local.entity.ContactTagEntity
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.ContactGroup
import com.tang.prm.domain.model.ContactTag
import com.tang.prm.domain.model.Gender

fun ContactEntity.toDomain() = Contact(
    id = id, name = name, avatar = avatar, nickname = nickname, gender = Gender.fromValue(gender),
    birthday = birthday, isLunarBirthday = isLunarBirthday, knowingDate = knowingDate,
    phone = phone, email = email, city = city, address = address, education = education,
    company = company, jobTitle = jobTitle, industry = industry, hobby = hobby, habit = habit,
    diet = diet, skill = skill, mbti = mbti, spouseName = spouseName,
    childrenCount = childrenCount, childrenNames = childrenNames, introducer = introducer,
    relationshipLevel = relationshipLevel, relationship = relationship, groupId = groupId,
    intimacyScore = intimacyScore, lastInteractionTime = lastInteractionTime,
    customFields = customFields, notes = notes, createdAt = createdAt, updatedAt = updatedAt
)

fun Contact.toEntity() = ContactEntity(
    id = id, name = name, avatar = avatar, nickname = nickname, gender = gender.value,
    birthday = birthday, isLunarBirthday = isLunarBirthday, knowingDate = knowingDate,
    phone = phone, email = email, city = city, address = address, education = education,
    company = company, jobTitle = jobTitle, industry = industry, hobby = hobby, habit = habit,
    diet = diet, skill = skill, mbti = mbti, spouseName = spouseName,
    childrenCount = childrenCount, childrenNames = childrenNames, introducer = introducer,
    relationshipLevel = relationshipLevel, relationship = relationship, groupId = groupId,
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
        birthday = birthday, isLunarBirthday = isLunarBirthday, knowingDate = knowingDate,
        phone = phone, email = email, city = city, address = address, education = education,
        company = company, jobTitle = jobTitle, industry = industry,
        hobby = attrMap["hobby"]?.toJsonString() ?: hobby,
        habit = attrMap["habit"]?.toJsonString() ?: habit,
        diet = attrMap["diet"]?.toJsonString() ?: diet,
        skill = attrMap["skill"]?.toJsonString() ?: skill,
        mbti = mbti, spouseName = spouseName,
        childrenCount = childrenCount, childrenNames = childrenNames, introducer = introducer,
        relationshipLevel = relationshipLevel, relationship = relationship, groupId = groupId,
        intimacyScore = intimacyScore, lastInteractionTime = lastInteractionTime,
        customFields = customFields, notes = notes, createdAt = createdAt, updatedAt = updatedAt
    )
}

private fun List<ContactAttributeEntity>.toJsonString(): String {
    val values = this.map { it.value }
    return values.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }
}

/**
 * Parse a JSON string like ["篮球","足球"] into a list of string values.
 */
fun parseJsonList(json: String?): List<String> {
    if (json.isNullOrBlank() || json == "[]") return emptyList()
    return json
        .trim()
        .removeSurrounding("[", "]")
        .split(",")
        .map { it.trim().removeSurrounding("\"") }
        .filter { it.isNotBlank() }
}

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
        for (value in parseJsonList(json)) {
            result.add(ContactAttributeEntity(contactId = id, category = category, value = value))
        }
    }
    return result
}

fun ContactGroupEntity.toDomain() = ContactGroup(id, name, color, sortOrder)

fun ContactGroup.toEntity() = ContactGroupEntity(id, name, color, sortOrder)

fun ContactTagEntity.toDomain() = ContactTag(id, name, color)

fun ContactTag.toEntity() = ContactTagEntity(id, name, color)
