package com.tang.prm.domain.repository

import com.tang.prm.domain.model.Gift
import kotlinx.coroutines.flow.Flow

interface GiftRepository {
    fun getAllGifts(): Flow<List<Gift>>
    fun getGiftById(id: Long): Flow<Gift?>
    fun getGiftsByContactId(contactId: Long): Flow<List<Gift>>
    fun getGiftsBySentType(isSent: Boolean): Flow<List<Gift>>
    suspend fun insertGift(gift: Gift): Long
    suspend fun updateGift(gift: Gift)
    suspend fun deleteGiftById(id: Long)
    suspend fun deleteGiftsByContactId(contactId: Long)
    suspend fun saveGiftWithPhotos(gift: Gift, photoUris: List<String>): Pair<Long, Int>
    fun getGiftCount(): Flow<Int>
}
