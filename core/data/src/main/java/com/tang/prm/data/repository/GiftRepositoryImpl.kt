package com.tang.prm.data.repository

import android.content.Context
import android.net.Uri
import com.tang.prm.data.local.dao.GiftDao
import com.tang.prm.data.local.database.TangDatabase
import com.tang.prm.data.mapper.toDomain
import com.tang.prm.data.mapper.toEntity
import com.tang.prm.domain.model.Gift
import com.tang.prm.domain.repository.GiftRepository
import com.tang.prm.data.util.ImageFileManager
import androidx.room.withTransaction
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import com.tang.prm.data.mapper.mapList
import com.tang.prm.data.mapper.mapNullable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GiftRepositoryImpl @Inject constructor(
    private val giftDao: GiftDao,
    private val database: TangDatabase,
    @ApplicationContext private val context: Context
) : GiftRepository {

    override fun getAllGifts(): Flow<List<Gift>> =
        giftDao.getAllGifts().mapList { it.toDomain() }

    override fun getGiftById(id: Long): Flow<Gift?> =
        giftDao.getGiftById(id).mapNullable { it.toDomain() }

    override fun getGiftsByContactId(contactId: Long): Flow<List<Gift>> =
        giftDao.getGiftsByContactId(contactId).mapList { it.toDomain() }

    override suspend fun insertGift(gift: Gift): Long = giftDao.insertGift(gift.toEntity())

    override suspend fun updateGift(gift: Gift) {
        // 先收集被移除的照片路径，更新数据库成功后再删除文件
        val oldEntity = giftDao.getGiftByIdOnce(gift.id)
        val removedPhotos = oldEntity?.let { old ->
            (old.photos.toSet() - gift.photos.toSet()).takeIf { it.isNotEmpty() }
        }
        giftDao.updateGift(gift.toEntity())
        removedPhotos?.let { deletePhotoFiles(it.toList()) }
    }

    override suspend fun deleteGiftById(id: Long) {
        // 先在事务内收集待删除文件路径 + 删除数据库记录
        val photosToDelete = database.withTransaction {
            val photos = giftDao.getGiftByIdOnce(id)?.photos ?: emptyList()
            giftDao.deleteGiftById(id)
            photos
        }
        // 事务外删除文件
        deletePhotoFiles(photosToDelete)
    }

    override suspend fun deleteGiftsByContactId(contactId: Long) {
        // 先在事务内收集待删除文件路径 + 删除数据库记录
        val allPhotos = database.withTransaction {
            val photos = giftDao.getGiftsByContactIdOnce(contactId).flatMap { it.photos }
            giftDao.deleteGiftsByContactId(contactId)
            photos
        }
        // 事务外删除文件
        deletePhotoFiles(allPhotos)
    }

    private suspend fun deletePhotoFiles(photos: List<String>) =
        ImageFileManager.deleteLocalPhotos(photos)

    override suspend fun saveGiftWithPhotos(gift: Gift, photoUris: List<String>): Pair<Long, Int> {
        // 并发复制图片，缩短用户保存等待时间
        val copyResults = coroutineScope {
            photoUris.map { uriString ->
                async {
                    val uri = Uri.parse(uriString)
                    ImageFileManager.copyToInternalStorage(context, uri, "gift")
                }
            }.awaitAll()
        }
        val savedPaths = copyResults.mapNotNull { it }
        val failedCount = copyResults.count { it == null }

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
