package com.tang.prm.data.mapper

// Alignment checklist for Anniversary ↔ AnniversaryEntity:
// When adding/removing fields, ensure ALL of the following are updated:
// 1. Anniversary (domain model)
// 2. AnniversaryEntity (Room entity)
// 3. AnniversaryMapper (toEntity / toDomain)
// 4. AnniversaryDao (queries)
// 5. Room migration (if entity schema changes)
// 6. UI forms (AddAnniversaryScreen, AnniversaryDetailScreen)

import com.tang.prm.data.local.entity.AnniversaryEntity
import com.tang.prm.domain.model.Anniversary
import com.tang.prm.domain.model.AnniversaryType

// MAP-Q-3 修复：移除 android.util.Log 依赖，与其它 Mapper 静默降级风格保持一致。
// toEnumOrDefault 已提供 fallback，重复 Log.w 只会造成日志噪声且引入 Android Framework 依赖。
fun AnniversaryEntity.toDomain(contactName: String?, contactAvatar: String?) = Anniversary(
    id = id, contactId = contactId, name = name,
    type = type.toEnumOrDefault(AnniversaryType.BIRTHDAY),
    date = date, isRepeat = isRepeat, reminderDays = reminderDays,
    remarks = remarks, contactName = contactName, contactAvatar = contactAvatar,
    icon = icon, createdAt = createdAt, updatedAt = updatedAt
)

fun Anniversary.toEntity() = AnniversaryEntity(
    id = id, contactId = contactId, name = name, type = type.name,
    date = date, isRepeat = isRepeat, reminderDays = reminderDays,
    remarks = remarks, icon = icon, createdAt = createdAt, updatedAt = updatedAt
)
