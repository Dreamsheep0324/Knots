package com.tang.prm.data.repository

import android.content.Context
import android.net.Uri
import com.tang.prm.data.local.dao.GiftDao
import com.tang.prm.data.mapper.toDomain
import com.tang.prm.data.mapper.toEntity
import com.tang.prm.domain.model.Gift
import com.tang.prm.domain.repository.GiftRepository
import com.tang.prm.util.ImageCacheManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GiftRepositoryImpl @Inject constructor(
    private val giftDao: GiftDao,
    @ApplicationContext private val context: Context
) : GiftRepository {

    override fun getAllGifts(): Flow<List<Gift>> =
        giftDao.getAllGifts().map { it.map { entity -> entity.toDomain() } }

    override fun getGiftsWithExistingPhotos(): Flow<List<Gift>> =
        giftDao.getAllGifts().map { entities ->
            entities.map { it.toDomain() }.map { gift ->
                gift.copy(photos = gift.photos.filter { path ->
                    java.io.File(path.trim()).exists()
                })
            }
        }

    override fun getGiftById(id: Long): Flow<Gift?> =
        giftDao.getGiftById(id).map { it?.toDomain() }

    override fun getGiftsByContactId(contactId: Long): Flow<List<Gift>> =
        giftDao.getGiftsByContactId(contactId).map { it.map { entity -> entity.toDomain() } }

    override fun getGiftsBySentType(isSent: Boolean): Flow<List<Gift>> =
        giftDao.getGiftsBySentType(isSent).map { it.map { entity -> entity.toDomain() } }

    override fun getContactsWithGifts(): Flow<List<Long>> = giftDao.getContactsWithGifts()

    override suspend fun insertGift(gift: Gift): Long = giftDao.insertGift(gift.toEntity())

    override suspend fun updateGift(gift: Gift) = giftDao.updateGift(gift.toEntity())

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

    private suspend fun deletePhotoFiles(photos: List<String>) {
        photos.forEach { path ->
            if (ImageCacheManager.isLocalPath(path)) {
                ImageCacheManager.deleteImage(path)
            }
        }
    }

    override suspend fun saveGiftWithPhotos(gift: Gift, photoUris: List<Uri>): Pair<Long, Int> {
        val savedPaths = mutableListOf<String>()
        var failedCount = 0
        photoUris.forEach { uri ->
            val path = ImageCacheManager.copyToInternalStorage(context, uri, "gift")
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

    override fun getGiftPhotoCount(): Flow<Int> = giftDao.getAllPhotosRaw().map { list ->
        list.sumOf { photos ->
            try {
                org.json.JSONArray(photos).length()
            } catch (_: Exception) { 0 }
        }
    }
}
