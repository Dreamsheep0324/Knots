package com.tang.prm.data.repository

import android.content.Context
import android.net.Uri
import com.tang.prm.data.local.dao.GiftDao
import com.tang.prm.data.local.database.TangDatabase
import com.tang.prm.data.mapper.toDomain
import com.tang.prm.data.mapper.toEntity
import com.tang.prm.domain.model.Gift
import com.tang.prm.domain.model.SaveResult
import com.tang.prm.domain.model.SourceTypes
import com.tang.prm.domain.repository.FavoriteRepository
import com.tang.prm.domain.repository.GiftRepository
import com.tang.prm.data.util.ImageFileManager
import com.tang.prm.data.util.computeRemovedPhotos
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
    private val favoriteRepository: FavoriteRepository,
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
        // 读+写放在同一事务中，避免竞态导致照片文件误删/漏删
        val removedPhotos = database.withTransaction {
            val oldEntity = giftDao.getGiftByIdOnce(gift.id)
            // REP-Q-5 修复：复用 computeRemovedPhotos 统一 photos 差集计算逻辑。
            val removed = computeRemovedPhotos(oldEntity, gift.photos) { it.photos }
            giftDao.updateGift(gift.toEntity())
            removed
        }
        // 事务外删除文件（文件 I/O 不阻塞数据库事务）
        removedPhotos?.let { ImageFileManager.deleteLocalPhotos(context, it.toList()) }
    }

    override suspend fun deleteGiftById(id: Long) {
        // 先在事务内收集待删除文件路径 + 清理收藏 + 删除数据库记录
        val photosToDelete = database.withTransaction {
            val photos = giftDao.getGiftByIdOnce(id)?.photos ?: emptyList()
            favoriteRepository.deleteFavoriteBySource(SourceTypes.GIFT, id)
            giftDao.deleteGiftById(id)
            photos
        }
        // 事务外删除文件
        ImageFileManager.deleteLocalPhotos(context, photosToDelete)
    }

    override suspend fun deleteGiftsByContactId(contactId: Long) {
        // 先在事务内收集待删除文件路径 + 清理收藏 + 删除数据库记录
        val allPhotos = database.withTransaction {
            val gifts = giftDao.getGiftsByContactIdOnce(contactId)
            gifts.forEach { favoriteRepository.deleteFavoriteBySource(SourceTypes.GIFT, it.id) }
            giftDao.deleteGiftsByContactId(contactId)
            gifts.flatMap { it.photos }
        }
        // 事务外删除文件
        ImageFileManager.deleteLocalPhotos(context, allPhotos)
    }

    override suspend fun saveGiftWithPhotos(gift: Gift, photoUris: List<String>): SaveResult {
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
        return SaveResult(id = id, failedPhotoCount = failedCount)
    }

    override fun getGiftCount(): Flow<Int> = giftDao.getGiftCount()

    override fun getPhotoCount(): Flow<Int> = giftDao.getPhotoCount()
}
