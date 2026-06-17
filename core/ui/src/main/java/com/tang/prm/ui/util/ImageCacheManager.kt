package com.tang.prm.ui.util

import android.content.Context
import android.net.Uri
import com.tang.prm.data.util.ImageFileManager

object ImageCacheManager {

    suspend fun copyToInternalStorage(context: Context, uri: Uri, prefix: String = "img"): String? =
        ImageFileManager.copyToInternalStorage(context, uri, prefix)

    fun isLocalPath(path: String): Boolean =
        ImageFileManager.isLocalPath(path)

    fun fileExists(path: String): Boolean =
        ImageFileManager.fileExists(path)

    suspend fun deleteImage(path: String) =
        ImageFileManager.deleteImage(path)

    fun countPhotosFromJson(photoJsons: List<String>): Int =
        ImageFileManager.countPhotosFromJson(photoJsons)

    suspend fun deleteLocalPhotos(photos: List<String>) =
        ImageFileManager.deleteLocalPhotos(photos)
}
