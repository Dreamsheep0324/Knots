package com.tang.prm.data.repository

import com.tang.prm.data.local.dao.CustomTypeDao
import com.tang.prm.data.mapper.toDomain
import com.tang.prm.data.mapper.toEntity
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.repository.CustomTypeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomTypeRepositoryImpl @Inject constructor(
    private val customTypeDao: CustomTypeDao
) : CustomTypeRepository {

    override fun getTypesByCategory(category: String): Flow<List<CustomType>> {
        return customTypeDao.getTypesByCategory(category).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAllTypes(): Flow<List<CustomType>> {
        return customTypeDao.getAllTypes().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTypeById(id: Long): Flow<CustomType?> {
        return customTypeDao.getTypeById(id).map { it?.toDomain() }
    }

    override suspend fun insertType(type: CustomType): Long {
        return customTypeDao.insertType(type.toEntity())
    }

    override suspend fun insertTypes(types: List<CustomType>) {
        customTypeDao.insertTypes(types.map { it.toEntity() })
    }

    override suspend fun updateType(type: CustomType) {
        customTypeDao.updateType(type.toEntity())
    }

    override suspend fun deleteTypeById(id: Long) {
        customTypeDao.deleteTypeById(id)
    }

    override suspend fun getTypeCountByCategory(category: String): Int {
        return customTypeDao.getTypeCountByCategory(category)
    }

}
