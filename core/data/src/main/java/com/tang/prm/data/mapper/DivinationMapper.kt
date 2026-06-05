package com.tang.prm.data.mapper

import com.tang.prm.data.local.entity.DivinationRecordEntity
import com.tang.prm.domain.divination.model.DivinationRecord

fun DivinationRecordEntity.toDomain(): DivinationRecord {
    return DivinationRecord(
        id = id,
        method = method,
        question = question,
        resultJson = resultJson,
        createdAt = createdAt,
        aiAnalysis = aiAnalysis
    )
}

fun DivinationRecord.toEntity(): DivinationRecordEntity {
    return DivinationRecordEntity(
        id = if (id > 0) id else 0,
        method = method,
        question = question,
        resultJson = resultJson,
        createdAt = createdAt,
        aiAnalysis = aiAnalysis
    )
}
