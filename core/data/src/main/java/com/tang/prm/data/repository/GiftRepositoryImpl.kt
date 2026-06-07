package com.tang.prm.data.repository

import android.content.Context
import android.net.Uri
import com.tang.prm.data.local.dao.GiftDao
import com.tang.prm.data.mapper.toDomain
import com.tang.prm.data.mapper.toEntity
import com.tang.prm.domain.model.Gift
import com.tang.prm.domain.repository.GiftRepository
import com.tang.prm.data.util.ImageFileManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import com.tang.prm.data.mapper.mapList
import com.tang.prm.data.mapper.mapNullable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GiftRepositoryImpl @Inject constructor(
    private val giftDao: GiftDao,
    @ApplicationContext private val context: Context
) : GiftRepository {

    override fun getAllGifts(): Flow<List<Gift>> =
        giftDao.getAllGifts().mapList { it.toDomain() }

    override fun getGiftById(id: Long): Flow<Gift?> =
        giftDao.getGiftById(id).mapNullable { it.toDomain() }

    override fun getGiftsByContactId(contactId: Long): Flow<List<Gift>> =
        giftDao.getGiftsByContactId(contactId).mapList { it.toDomain() }

    override fun getGiftsBySentType(isSent: Boolean): Flow<List<Gift>> =
        giftDao.getGiftsBySentType(isSent).mapList { it.toDomain() }

    override suspend fun insertGift(gift: Gift): Long = giftDao.insertGift(gift.toEntity())

    override suspend fun updateGift(gift: Gift) {
        // 查询旧数据，对比照片列表，清理被移除的旧照片文件
        val oldEntity = giftDao.getGiftByIdOnce(gift.id)
        if (oldEntity != null) {
            val removedPhotos = oldEntity.photos.toSet() - gift.photos.toSet()
            if (removedPhotos.isNotEmpty()) {
                deletePhotoFiles(removedPhotos.toList())
            }
        }
        giftDao.updateGift(gift.toEntity())
    }

    override suspend fun deleteGiftById(id: Long) {
        giftDao.getGiftByIdOnce(id)?.let { entity ->
            deletePhotoFiles(entity.photos)
        }
        giftDao.deleteGiftById(id)
    }

    override suspend fun deleteGiftsByContactId(contactId: Long) {
        giftDao.getGiftsByContactIdOnce(contactId).forEach { entity ->
            deletePhotoFiles(entity.photos)
        }
        giftDao.deleteGiftsByContactId(contactId)
    }

    private suspend fun deletePhotoFiles(photos: List<String>) =
        ImageFileManager.deleteLocalPhotos(photos)

    override suspend fun saveGiftWithPhotos(gift: Gift, photoUris: List<String>): Pair<Long, Int> {
        val savedPaths = mutableListOf<String>()
        var failedCount = 0
        photoUris.forEach { uriString ->
            val uri = Uri.parse(uriString)
            val path = ImageFileManager.copyToInternalStorage(context, uri, "gift")
            if (path != null) {
                savedPaths.add(path)
            } else {
                failedCount++
            }
        }
        val newGift = gift.copy(
            id = 0,
            photos = savedPaths,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val id = giftDao.insertGift(newGift.toEntity())
        return id to failedCount
    }

    override fun getGiftCount(): Flow<Int> = giftDao.getGiftCount()

    override fun getPhotoCount(): Flow<Int> = giftDao.getPhotoCount()
}
