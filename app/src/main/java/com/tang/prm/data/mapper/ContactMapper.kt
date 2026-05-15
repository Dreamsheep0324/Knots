package com.tang.prm.data.mapper

// Alignment checklist for Contact ↔ ContactEntity:
// When adding/removing fields, ensure ALL of the following are updated:
// 1. Contact (domain model)
// 2. ContactEntity (Room entity)
// 3. ContactMapper (toEntity / toDomain)
// 4. ContactDao (queries)
// 5. Room migration (if entity schema changes)
// 6. UI forms (AddContactScreen, ContactDetailScreen)

import com.tang.prm.data.local.entity.ContactEntity
import com.tang.prm.data.local.entity.ContactGroupEntity
import com.tang.prm.data.local.entity.ContactTagEntity
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.ContactGroup
import com.tang.prm.domain.model.ContactTag

fun ContactEntity.toDomain() = Contact(
    id = id, name = name, avatar = avatar, nickname = nickname, gender = gender,
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
    id = id, name = name, avatar = avatar, nickname = nickname, gender = gender,
    birthday = birthday, isLunarBirthday = isLunarBirthday, knowingDate = knowingDate,
    phone = phone, email = email, city = city, address = address, education = education,
    company = company, jobTitle = jobTitle, industry = industry, hobby = hobby, habit = habit,
    diet = diet, skill = skill, mbti = mbti, spouseName = spouseName,
    childrenCount = childrenCount, childrenNames = childrenNames, introducer = introducer,
    relationshipLevel = relationshipLevel, relationship = relationship, groupId = groupId,
    intimacyScore = intimacyScore, lastInteractionTime = lastInteractionTime,
    customFields = customFields, notes = notes, createdAt = createdAt, updatedAt = updatedAt
)

fun ContactGroupEntity.toDomain() = ContactGroup(id, name, color, sortOrder)

fun ContactGroup.toEntity() = ContactGroupEntity(id, name, color, sortOrder)

fun ContactTagEntity.toDomain() = ContactTag(id, name, color)

fun ContactTag.toEntity() = ContactTagEntity(id, name, color)
