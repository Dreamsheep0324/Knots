package com.tang.prm.domain.repository

import com.tang.prm.domain.model.CustomType
import kotlinx.coroutines.flow.Flow

interface CustomTypeRepository {
    fun getTypesByCategory(category: String): Flow<List<CustomType>>
    fun getAllTypes(): Flow<List<CustomType>>
    fun getTypeById(id: Long): Flow<CustomType?>
    suspend fun insertType(type: CustomType): Long
    suspend fun insertTypes(types: List<CustomType>)
    suspend fun updateType(type: CustomType)
    suspend fun deleteTypeById(id: Long)
    suspend fun getTypeCountByCategory(category: String): Int
}
