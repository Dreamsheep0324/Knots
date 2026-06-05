package com.tang.prm.domain.repository

import com.tang.prm.domain.model.Anniversary
import kotlinx.coroutines.flow.Flow

interface AnniversaryRepository {
    fun getAllAnniversaries(): Flow<List<Anniversary>>
    fun getAnniversaryById(id: Long): Flow<Anniversary?>
    fun getAnniversariesByContact(contactId: Long): Flow<List<Anniversary>>
    fun getAnniversariesInRange(startDate: Long, endDate: Long): Flow<List<Anniversary>>
    fun getUpcomingAnniversaries(limit: Int): Flow<List<Anniversary>>
    fun getPastAnniversaries(limit: Int): Flow<List<Anniversary>>
    fun getAnniversariesByType(type: String): Flow<List<Anniversary>>
    suspend fun insertAnniversary(anniversary: Anniversary): Long
    suspend fun updateAnniversary(anniversary: Anniversary)
    suspend fun deleteAnniversary(id: Long)

    fun getAnniversaryCount(): Flow<Int>
}
