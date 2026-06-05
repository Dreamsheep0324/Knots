package com.tang.prm.data.repository

import com.tang.prm.data.local.dao.ContactGroupDao
import com.tang.prm.data.mapper.mapList
import com.tang.prm.data.mapper.mapNullable
import com.tang.prm.data.mapper.toDomain
import com.tang.prm.data.mapper.toEntity
import com.tang.prm.domain.model.ContactGroup
import com.tang.prm.domain.repository.ContactGroupRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactGroupRepositoryImpl @Inject constructor(
    private val groupDao: ContactGroupDao
) : ContactGroupRepository {
    override fun getAllGroups(): Flow<List<ContactGroup>> =
        groupDao.getAllGroups().mapList { it.toDomain() }

    override fun getGroupById(id: Long): Flow<ContactGroup?> =
        groupDao.getGroupById(id).mapNullable { it.toDomain() }

    override suspend fun insertGroup(group: ContactGroup): Long =
        groupDao.insertGroup(group.toEntity())

    override suspend fun updateGroup(group: ContactGroup) =
        groupDao.updateGroup(group.toEntity())

    override suspend fun deleteGroupById(id: Long) =
        groupDao.deleteGroupById(id)
}
