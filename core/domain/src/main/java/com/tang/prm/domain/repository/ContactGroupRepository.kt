package com.tang.prm.domain.repository

import com.tang.prm.domain.model.ContactGroup
import kotlinx.coroutines.flow.Flow

/**
 * A-13 修复：从 ContactRepository.kt 拆分到独立文件。
 *
 * 一个文件一个接口符合单一职责与接口隔离原则，避免新接入方误依赖同文件的其它接口。
 */
interface ContactGroupRepository {
    fun getAllGroups(): Flow<List<ContactGroup>>
    suspend fun insertGroup(group: ContactGroup): Long
    suspend fun updateGroup(group: ContactGroup)
    suspend fun deleteGroupById(id: Long)
}
