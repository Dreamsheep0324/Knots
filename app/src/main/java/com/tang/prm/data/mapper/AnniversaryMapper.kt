package com.tang.prm.data.mapper

// Alignment checklist for Anniversary ↔ AnniversaryEntity:
// When adding/removing fields, ensure ALL of the following are updated:
// 1. Anniversary (domain model)
// 2. AnniversaryEntity (Room entity)
// 3. AnniversaryMapper (toEntity / toDomain)
// 4. AnniversaryDao (queries)
// 5. Room migration (if entity schema changes)
// 6. UI forms (AddAnniversaryScreen, AnniversaryDetailScreen)

import android.util.Log
import com.tang.prm.data.local.entity.AnniversaryEntity
import com.tang.prm.domain.model.Anniversary
import com.tang.prm.domain.model.AnniversaryType

fun AnniversaryEntity.toDomain(contactName: String?, contactAvatar: String?) = Anniversary(
    id = id, contactId = contactId, name = name,
    type = AnniversaryType.entries.find { it.name == type } ?: run {
        Log.w("AnniversaryMapper", "Unknown AnniversaryType '$type', falling back to BIRTHDAY for anniversary id=$id")
        AnniversaryType.BIRTHDAY
    },
    date = date, isLunar = isLunar, isRepeat = isRepeat, reminderDays = reminderDays,
    remarks = remarks, contactName = contactName, contactAvatar = contactAvatar,
    icon = icon, createdAt = createdAt, updatedAt = updatedAt
)

fun Anniversary.toEntity() = AnniversaryEntity(
    id = id, contactId = contactId, name = name, type = type.name,
    date = date, isLunar = isLunar, isRepeat = isRepeat, reminderDays = reminderDays,
    remarks = remarks, icon = icon, createdAt = createdAt, updatedAt = updatedAt
)
