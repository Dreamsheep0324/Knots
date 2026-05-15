package com.tang.prm.data.repository

import com.tang.prm.data.local.dao.GiftDao
import com.tang.prm.data.mapper.toDomain
import com.tang.prm.data.mapper.toEntity
import com.tang.prm.domain.model.Gift
import com.tang.prm.domain.repository.GiftRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GiftRepositoryImpl @Inject constructor(
    private val giftDao: GiftDao
) : GiftRepository {

    override fun getAllGifts(): Flow<List<Gift>> =
        giftDao.getAllGifts().map { it.map { entity -> entity.toDomain() } }

    override fun getGiftById(id: Long): Flow<Gift?> =
        giftDao.getGiftById(id).map { it?.toDomain() }

    override fun getGiftsByContactId(contactId: Long): Flow<List<Gift>> =
        giftDao.getGiftsByContactId(contactId).map { it.map { entity -> entity.toDomain() } }

    override fun getGiftsBySentType(isSent: Boolean): Flow<List<Gift>> =
        giftDao.getGiftsBySentType(isSent).map { it.map { entity -> entity.toDomain() } }

    override fun getContactsWithGifts(): Flow<List<Long>> = giftDao.getContactsWithGifts()

    override suspend fun insertGift(gift: Gift): Long = giftDao.insertGift(gift.toEntity())

    override suspend fun updateGift(gift: Gift) = giftDao.updateGift(gift.toEntity())

    override suspend fun deleteGiftById(id: Long) = giftDao.deleteGiftById(id)

    override suspend fun deleteGiftsByContactId(contactId: Long) =
        giftDao.deleteGiftsByContactId(contactId)

    override fun getGiftCount(): Flow<Int> = giftDao.getGiftCount()

    override fun getGiftPhotoCount(): Flow<Int> = giftDao.getGiftPhotoCount()
}
