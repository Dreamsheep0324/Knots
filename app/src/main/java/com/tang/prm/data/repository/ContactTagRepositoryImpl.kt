package com.tang.prm.data.repository

import com.tang.prm.data.local.dao.ContactTagDao
import com.tang.prm.data.mapper.toDomain
import com.tang.prm.data.mapper.toEntity
import com.tang.prm.domain.model.ContactTag
import com.tang.prm.domain.repository.ContactTagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactTagRepositoryImpl @Inject constructor(
    private val tagDao: ContactTagDao
) : ContactTagRepository {
    override fun getAllTags(): Flow<List<ContactTag>> =
        tagDao.getAllTags().map { entities -> entities.map { it.toDomain() } }

    override fun getTagById(id: Long): Flow<ContactTag?> =
        tagDao.getTagById(id).map { it?.toDomain() }

    override suspend fun insertTag(tag: ContactTag): Long =
        tagDao.insertTag(tag.toEntity())

    override suspend fun updateTag(tag: ContactTag) =
        tagDao.updateTag(tag.toEntity())

    override suspend fun deleteTagById(id: Long) =
        tagDao.deleteTagById(id)
}
