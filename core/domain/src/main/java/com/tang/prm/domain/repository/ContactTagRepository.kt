package com.tang.prm.domain.repository

import com.tang.prm.domain.model.ContactTag
import kotlinx.coroutines.flow.Flow

/**
 * A-13 修复：从 ContactRepository.kt 拆分到独立文件。
 */
interface ContactTagRepository {
    fun getAllTags(): Flow<List<ContactTag>>
    suspend fun insertTag(tag: ContactTag): Long
    suspend fun updateTag(tag: ContactTag)
    suspend fun deleteTagById(id: Long)
}
