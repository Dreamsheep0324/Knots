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

// MAP-Q-4 修复：移除冗余的 `if (id > 0) id else 0` 防御性归零——域模型 id 默认 0，
// DB 自增从 1 起，负数 id 无合法来源；归零会掩盖上游 bug。与其它 Mapper 一致直接赋值。
fun DivinationRecord.toEntity(): DivinationRecordEntity = DivinationRecordEntity(
    id = id,
    method = method,
    question = question,
    resultJson = resultJson,
    createdAt = createdAt,
    aiAnalysis = aiAnalysis
)
