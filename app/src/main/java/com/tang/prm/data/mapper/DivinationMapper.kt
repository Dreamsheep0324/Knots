package com.tang.prm.data.mapper

import com.tang.prm.data.local.entity.DivinationRecordEntity
import com.tang.prm.domain.divination.model.DivinationRecord

object DivinationMapper {
    fun toDomain(entity: DivinationRecordEntity): DivinationRecord {
        return DivinationRecord(
            id = entity.id,
            method = entity.method,
            question = entity.question,
            resultJson = entity.resultJson,
            createdAt = entity.createdAt,
            aiAnalysis = entity.aiAnalysis
        )
    }

    fun toEntity(domain: DivinationRecord): DivinationRecordEntity {
        return DivinationRecordEntity(
            id = if (domain.id > 0) domain.id else 0,
            method = domain.method,
            question = domain.question,
            resultJson = domain.resultJson,
            createdAt = domain.createdAt,
            aiAnalysis = domain.aiAnalysis
        )
    }
}
