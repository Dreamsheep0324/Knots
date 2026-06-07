package com.tang.prm.data.remote

import android.util.Base64
import com.tang.prm.domain.model.CloudBackupVersion
import com.tang.prm.domain.model.ConnectionTestResult
import com.tang.prm.domain.model.SyncResult
import com.tang.prm.domain.model.WebDavConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.io.StringReader
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class WebDavClient @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    private val rfc1123Format = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
    private val displayFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    companion object {
        private const val BUFFER_SIZE = 256 * 1024 // 256KB
    }

    /**
     * 测试 WebDAV 连接：PROPFIND Depth:0
     */
    suspend fun testConnection(config: WebDavConfig): ConnectionTestResult = withContext(Dispatchers.IO) {
        try {
            val url = normalizeUrl(config.serverUrl)
            val request = Request.Builder()
                .url(url)
                .header("Authorization", buildBasicAuth(config.username, config.password))
                .header("Depth", "0")
                .method("PROPFIND", "".toRequestBody())
                .build()

            val response = okHttpClient.newCall(request).execute()
            when {
                response.isSuccessful || response.code == 207 -> ConnectionTestResult.Success
                response.code == 401 -> ConnectionTestResult.Error("认证失败，请检查用户名和密码")
                response.code == 404 -> ConnectionTestResult.Error("服务器地址不存在")
                else -> ConnectionTestResult.Error("连接失败 (${response.code})")
            }
        } catch (e: SocketTimeoutException) {
            ConnectionTestResult.Error("连接超时，请检查网络")
        } catch (e: UnknownHostException) {
            ConnectionTestResult.Error("无法连接到服务器，请检查地址")
        } catch (e: IOException) {
            ConnectionTestResult.Error("网络连接失败：${e.message}")
        } catch (e: Exception) {
            ConnectionTestResult.Error("操作失败：${e.message}")
        }
    }

    /**
     * 确保远程目录存在：MKCOL
     */
    suspend fun ensureRemoteDir(config: WebDavConfig) = withContext(Dispatchers.IO) {
        try {
            val url = buildRemoteUrl(config, "")
            val request = Request.Builder()
                .url(url)
                .header("Authorization", buildBasicAuth(config.username, config.password))
                .method("MKCOL", null)
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful && response.code != 405) {
                throw Exception("创建远程目录失败 (${response.code})")
            }
        } catch (e: Exception) {
            // 目录可能已存在，忽略错误
        }
    }

    /**
     * 确保远程子目录存在：MKCOL
     */
    suspend fun ensureRemoteSubDir(config: WebDavConfig, subDir: String) = withContext(Dispatchers.IO) {
        try {
            val url = buildSubDirUrl(config, subDir)
            val request = Request.Builder()
                .url(url)
                .header("Authorization", buildBasicAuth(config.username, config.password))
                .method("MKCOL", null)
                .build()

            val response = okHttpClient.newCall(request).execute()
            // 405 = 目录已存在，视为成功
        } catch (_: Exception) {}
    }

    /**
     * 列出远程目录下的备份文件：PROPFIND Depth:1
     */
    suspend fun listFiles(config: WebDavConfig): List<CloudBackupVersion> = withContext(Dispatchers.IO) {
        try {
            val url = buildRemoteUrl(config, "")
            val propfindBody = """
                <?xml version="1.0" encoding="utf-8"?>
                <d:propfind xmlns:d="DAV:">
                    <d:prop>
                        <d:getcontentlength/>
                        <d:getlastmodified/>
                    </d:prop>
                </d:propfind>
            """.trimIndent()

            val request = Request.Builder()
                .url(url)
                .header("Authorization", buildBasicAuth(config.username, config.password))
                .header("Depth", "1")
                .header("Content-Type", "application/xml; charset=utf-8")
                .method("PROPFIND", propfindBody.toRequestBody())
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful && response.code != 207) {
                throw Exception("列出文件失败 (${response.code})")
            }

            val body = response.body?.string() ?: return@withContext emptyList()
            parsePropfindResponse(body, config.remotePath)
        } catch (e: SocketTimeoutException) {
            throw Exception("连接超时，请检查网络")
        } catch (e: UnknownHostException) {
            throw Exception("无法连接到服务器，请检查地址")
        } catch (e: IOException) {
            throw Exception("网络连接失败：${e.message}")
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 上传文件：PUT（带进度回调）
     */
    fun uploadFileWithProgress(config: WebDavConfig, fileName: String, file: File): Flow<SyncResult> = channelFlow {
        val startTime = System.currentTimeMillis()
        val totalBytes = file.length()
        val bytesWrittenToSink = java.util.concurrent.atomic.AtomicLong(0L)

        val monitorJob = launch(Dispatchers.IO) {
            while (true) {
                delay(500)
                val written = bytesWrittenToSink.get()
                val displayBytes = (written * 0.9).toLong()
                if (displayBytes < totalBytes) {
                    trySend(SyncResult.UploadProgress("上传中", 0, 0, "${formatFileSize(displayBytes)}/${formatFileSize(totalBytes)}"))
                }
            }
        }

        val uploadJob = launch(Dispatchers.IO) {
            try {
                val url = buildRemoteUrl(config, fileName)
                val mediaType = "application/zip".toMediaType()

                val trackingBody = object : okhttp3.RequestBody() {
                    override fun contentType() = mediaType
                    override fun contentLength() = totalBytes

                    override fun writeTo(sink: okio.BufferedSink) {
                        file.inputStream().use { input ->
                            val buffer = ByteArray(BUFFER_SIZE)
                            var len: Int
                            while (input.read(buffer).also { len = it } > 0) {
                                sink.write(buffer, 0, len)
                                bytesWrittenToSink.addAndGet(len.toLong())
                            }
                        }
                    }
                }

                val request = Request.Builder()
                    .url(url)
                    .header("Authorization", buildBasicAuth(config.username, config.password))
                    .put(trackingBody)
                    .build()

                val response = okHttpClient.newCall(request).execute()
                monitorJob.cancel()

                if (!response.isSuccessful && response.code != 201 && response.code != 204) {
                    trySend(SyncResult.Error("上传失败 (${response.code})"))
                } else {
                    trySend(SyncResult.UploadSuccess(fileName))
                }
                close()
            } catch (e: Exception) {
                monitorJob.cancel()
                val error = when (e) {
                    is SocketTimeoutException -> "连接超时，请检查网络"
                    is UnknownHostException -> "无法连接到服务器，请检查地址"
                    is IOException -> "网络连接失败：${e.message}"
                    else -> "上传失败：${e.message}"
                }
                trySend(SyncResult.Error(error))
                close()
            }
        }

        awaitClose {
            monitorJob.cancel()
            uploadJob.cancel()
        }
    }.flowOn(Dispatchers.IO)

    /**
     * 下载文件：GET（带进度回调）
     */
    fun downloadFileWithProgress(config: WebDavConfig, fileName: String, targetFile: File): Flow<SyncResult> = channelFlow {
        val startTime = System.currentTimeMillis()

        val downloadJob = launch(Dispatchers.IO) {
            try {
                val url = buildRemoteUrl(config, fileName)
                val request = Request.Builder()
                    .url(url)
                    .header("Authorization", buildBasicAuth(config.username, config.password))
                    .get()
                    .build()

                val response = okHttpClient.newCall(request).execute()
                if (!response.isSuccessful) {
                    trySend(SyncResult.Error("下载失败 (${response.code})"))
                    close()
                    return@launch
                }

                val totalBytes = response.body?.contentLength() ?: -1L
                val downloadedBytes = java.util.concurrent.atomic.AtomicLong(0L)

                val monitorJob = launch(Dispatchers.IO) {
                    while (true) {
                        delay(500)
                        val downloaded = downloadedBytes.get()
                        trySend(SyncResult.DownloadProgress("下载中", 0, 0, "${formatFileSize(downloaded)}/${if (totalBytes > 0) formatFileSize(totalBytes) else "?"}"))
                    }
                }

                response.body?.byteStream()?.use { input ->
                    targetFile.outputStream().use { output ->
                        val buffer = ByteArray(BUFFER_SIZE)
                        var len: Int
                        while (input.read(buffer).also { len = it } > 0) {
                            output.write(buffer, 0, len)
                            downloadedBytes.addAndGet(len.toLong())
                        }
                    }
                } ?: throw Exception("下载失败：响应体为空")

                monitorJob.cancel()
                trySend(SyncResult.DownloadSuccess(fileName))
                close()
            } catch (e: Exception) {
                val error = when (e) {
                    is SocketTimeoutException -> "连接超时，请检查网络"
                    is UnknownHostException -> "无法连接到服务器，请检查地址"
                    is IOException -> "网络连接失败：${e.message}"
                    else -> "下载失败：${e.message}"
                }
                trySend(SyncResult.Error(error))
                close()
            }
        }

        awaitClose {
            downloadJob.cancel()
        }
    }.flowOn(Dispatchers.IO)

    /**
     * 删除文件：DELETE
     */
    suspend fun deleteFile(config: WebDavConfig, fileName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = buildRemoteUrl(config, fileName)
            val request = Request.Builder()
                .url(url)
                .header("Authorization", buildBasicAuth(config.username, config.password))
                .delete()
                .build()

            val response = okHttpClient.newCall(request).execute()
            response.isSuccessful || response.code == 204
        } catch (e: SocketTimeoutException) {
            throw Exception("连接超时，请检查网络")
        } catch (e: UnknownHostException) {
            throw Exception("无法连接到服务器，请检查地址")
        } catch (e: IOException) {
            throw Exception("网络连接失败：${e.message}")
        } catch (e: Exception) {
            false
        }
    }

    // ===== 增量同步方法 =====

    /**
     * 下载小文件（manifest.json 等）
     * @return 文件内容字符串，不存在返回 null
     */
    suspend fun downloadSmallFile(config: WebDavConfig, fileName: String): String? = withContext(Dispatchers.IO) {
        try {
            val url = buildRemoteUrl(config, fileName)
            val request = Request.Builder()
                .url(url)
                .header("Authorization", buildBasicAuth(config.username, config.password))
                .get()
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (response.code == 404) return@withContext null
            if (!response.isSuccessful) return@withContext null
            response.body?.string()
        } catch (_: Exception) {
            null
        }
    }

    /**
     * 上传小文件（manifest.json 等）
     */
    suspend fun uploadSmallFile(config: WebDavConfig, fileName: String, content: String) = withContext(Dispatchers.IO) {
        val url = buildRemoteUrl(config, fileName)
        val mediaType = "application/json".toMediaType()
        val request = Request.Builder()
            .url(url)
            .header("Authorization", buildBasicAuth(config.username, config.password))
            .put(content.toRequestBody(mediaType))
            .build()

        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful && response.code != 201 && response.code != 204) {
            throw Exception("上传清单失败 (${response.code})")
        }
    }

    /**
     * 上传单个图片文件到指定子目录
     */
    suspend fun uploadImageFile(config: WebDavConfig, subDir: String, fileName: String, file: File) = withContext(Dispatchers.IO) {
        val url = buildSubDirFileUrl(config, subDir, fileName)
        val mediaType = getMediaType(fileName)
        val request = Request.Builder()
            .url(url)
            .header("Authorization", buildBasicAuth(config.username, config.password))
            .put(file.asRequestBody(mediaType))
            .build()

        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful && response.code != 201 && response.code != 204) {
            throw Exception("上传图片失败 (${response.code})")
        }
    }

    /**
     * 下载单个图片文件
     */
    suspend fun downloadImageFile(config: WebDavConfig, subDir: String, fileName: String, targetFile: File) = withContext(Dispatchers.IO) {
        val url = buildSubDirFileUrl(config, subDir, fileName)
        val request = Request.Builder()
            .url(url)
            .header("Authorization", buildBasicAuth(config.username, config.password))
            .get()
            .build()

        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            throw Exception("下载图片失败 (${response.code})")
        }

        response.body?.byteStream()?.use { input ->
            targetFile.parentFile?.mkdirs()
            BufferedOutputStream(FileOutputStream(targetFile), BUFFER_SIZE).use { output ->
                val buffer = ByteArray(BUFFER_SIZE)
                var len: Int
                while (input.read(buffer).also { len = it } > 0) {
                    output.write(buffer, 0, len)
                }
            }
        } ?: throw Exception("下载图片失败：响应体为空")
    }

    /**
     * 删除远程子目录下的文件
     */
    suspend fun deleteRemoteFile(config: WebDavConfig, subDir: String, fileName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = buildSubDirFileUrl(config, subDir, fileName)
            val request = Request.Builder()
                .url(url)
                .header("Authorization", buildBasicAuth(config.username, config.password))
                .delete()
                .build()

            val response = okHttpClient.newCall(request).execute()
            response.isSuccessful || response.code == 204 || response.code == 404
        } catch (_: Exception) {
            false
        }
    }

    /**
     * 上传数据库文件到 db/ 子目录
     */
    suspend fun uploadDbFile(config: WebDavConfig, fileName: String, file: File) = withContext(Dispatchers.IO) {
        val url = buildSubDirFileUrl(config, "db", fileName)
        val mediaType = "application/zip".toMediaType()
        val request = Request.Builder()
            .url(url)
            .header("Authorization", buildBasicAuth(config.username, config.password))
            .put(file.asRequestBody(mediaType))
            .build()

        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful && response.code != 201 && response.code != 204) {
            throw Exception("上传数据库失败 (${response.code})")
        }
    }

    /**
     * 下载数据库文件
     */
    suspend fun downloadDbFile(config: WebDavConfig, fileName: String, targetFile: File) = withContext(Dispatchers.IO) {
        val url = buildSubDirFileUrl(config, "db", fileName)
        val request = Request.Builder()
            .url(url)
            .header("Authorization", buildBasicAuth(config.username, config.password))
            .get()
            .build()

        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            throw Exception("下载数据库失败 (${response.code})")
        }

        response.body?.byteStream()?.use { input ->
            targetFile.parentFile?.mkdirs()
            BufferedOutputStream(FileOutputStream(targetFile), BUFFER_SIZE).use { output ->
                val buffer = ByteArray(BUFFER_SIZE)
                var len: Int
                while (input.read(buffer).also { len = it } > 0) {
                    output.write(buffer, 0, len)
                }
            }
        } ?: throw Exception("下载数据库失败：响应体为空")
    }

    /**
     * 获取远端子目录中文件的大小（通过 PROPFIND）
     */
    suspend fun getSubDirFileSize(config: WebDavConfig, subDir: String, fileName: String): Long = withContext(Dispatchers.IO) {
        try {
            val url = buildSubDirUrl(config, subDir)
            val propfindBody = """
                <?xml version="1.0" encoding="utf-8"?>
                <d:propfind xmlns:d="DAV:">
                    <d:prop>
                        <d:getcontentlength/>
                    </d:prop>
                </d:propfind>
            """.trimIndent()

            val request = Request.Builder()
                .url(url)
                .header("Authorization", buildBasicAuth(config.username, config.password))
                .header("Depth", "1")
                .header("Content-Type", "application/xml; charset=utf-8")
                .method("PROPFIND", propfindBody.toRequestBody())
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful && response.code != 207) return@withContext 0L

            val body = response.body?.string() ?: return@withContext 0L
            parseFileSizeFromPropfind(body, fileName)
        } catch (_: Exception) { 0L }
    }

    private fun parseFileSizeFromPropfind(xml: String, targetFileName: String): Long {
        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xml))

            var currentHref = ""
            var currentSize = 0L
            var inPropstat = false
            var inProp = false

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "href" -> currentHref = parser.nextText().trim()
                            "propstat" -> inPropstat = true
                            "prop" -> inProp = true
                            "getcontentlength" -> if (inPropstat && inProp) {
                                currentSize = parser.nextText().trim().toLongOrNull() ?: 0L
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        when (parser.name) {
                            "propstat" -> inPropstat = false
                            "prop" -> inProp = false
                            "response" -> {
                                val hrefDecoded = java.net.URLDecoder.decode(currentHref, "UTF-8")
                                val hrefFileName = hrefDecoded.trimEnd('/').substringAfterLast('/')
                                if (hrefFileName == targetFileName) {
                                    return currentSize
                                }
                                currentHref = ""
                                currentSize = 0L
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (_: Exception) {}
        return 0L
    }

    // ===== 工具方法 =====

    private fun categorizeException(e: Exception): Exception {
        return when (e) {
            is SocketTimeoutException -> Exception("连接超时，请检查网络")
            is UnknownHostException -> Exception("无法连接到服务器，请检查地址")
            is IOException -> Exception("网络连接失败：${e.message}")
            else -> Exception("操作失败：${e.message}")
        }
    }

    private fun buildBasicAuth(username: String, password: String): String {
        val credentials = "$username:$password"
        val encoded = Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
        return "Basic $encoded"
    }

    private fun normalizeUrl(url: String): String {
        var normalized = url.trim()
        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            normalized = "https://$normalized"
        }
        return normalized
    }

    private fun buildRemoteUrl(config: WebDavConfig, fileName: String): String {
        val baseUrl = normalizeUrl(config.serverUrl).trimEnd('/')
        val remotePath = config.remotePath.trim('/')
        return if (fileName.isEmpty()) {
            "$baseUrl/$remotePath/"
        } else {
            "$baseUrl/$remotePath/${URLEncoder.encode(fileName, "UTF-8")}"
        }
    }

    private fun buildSubDirUrl(config: WebDavConfig, subDir: String): String {
        val baseUrl = normalizeUrl(config.serverUrl).trimEnd('/')
        val remotePath = config.remotePath.trim('/')
        return "$baseUrl/$remotePath/$subDir/"
    }

    private fun buildSubDirFileUrl(config: WebDavConfig, subDir: String, fileName: String): String {
        val baseUrl = normalizeUrl(config.serverUrl).trimEnd('/')
        val remotePath = config.remotePath.trim('/')
        return "$baseUrl/$remotePath/$subDir/${URLEncoder.encode(fileName, "UTF-8")}"
    }

    private fun getMediaType(fileName: String): okhttp3.MediaType {
        return when {
            fileName.endsWith(".jpg", ignoreCase = true) || fileName.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg".toMediaType()
            fileName.endsWith(".png", ignoreCase = true) -> "image/png".toMediaType()
            fileName.endsWith(".webp", ignoreCase = true) -> "image/webp".toMediaType()
            fileName.endsWith(".gif", ignoreCase = true) -> "image/gif".toMediaType()
            else -> "application/octet-stream".toMediaType()
        }
    }

    private fun formatFileSize(size: Long): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${"%.1f".format(size / 1024.0)} KB"
            size < 1024 * 1024 * 1024 -> "${"%.1f".format(size / (1024.0 * 1024.0))} MB"
            else -> "${"%.1f".format(size / (1024.0 * 1024.0 * 1024.0))} GB"
        }
    }

    private fun parsePropfindResponse(xml: String, remotePath: String): List<CloudBackupVersion> {
        val versions = mutableListOf<CloudBackupVersion>()
        val remotePathNormalized = remotePath.trim('/')

        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xml))

            var currentHref = ""
            var currentSize = 0L
            var currentModified = ""
            var inPropstat = false
            var inProp = false

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "href" -> {
                                currentHref = parser.nextText().trim()
                            }
                            "propstat" -> inPropstat = true
                            "prop" -> inProp = true
                            "getcontentlength" -> if (inPropstat && inProp) {
                                val text = parser.nextText().trim()
                                currentSize = text.toLongOrNull() ?: 0L
                            }
                            "getlastmodified" -> if (inPropstat && inProp) {
                                currentModified = parser.nextText().trim()
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        when (parser.name) {
                            "propstat" -> {
                                inPropstat = false
                            }
                            "prop" -> {
                                inProp = false
                            }
                            "response" -> {
                                val hrefDecoded = java.net.URLDecoder.decode(currentHref, "UTF-8")
                                val hrefPath = hrefDecoded.trimEnd('/')
                                val fileName = hrefPath.substringAfterLast('/')

                                // 支持旧版 ZIP 和新版增量目录
                                val isZip = fileName.endsWith(".zip") && fileName.startsWith("tang_backup_")
                                val isManifest = fileName == "manifest.json"
                                val isSubDir = fileName in setOf("db", "images", "gift_photos")

                                if (isZip) {
                                    val displayName = try {
                                        val timePart = fileName
                                            .removePrefix("tang_backup_")
                                            .removeSuffix(".zip")
                                        val inputFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                                        val date = inputFormat.parse(timePart)
                                        if (date != null) displayFormat.format(date) else fileName
                                    } catch (_: Exception) {
                                        fileName
                                    }

                                    versions.add(CloudBackupVersion(
                                        fileName = fileName,
                                        fileSize = currentSize,
                                        lastModified = currentModified,
                                        displayName = displayName,
                                        isIncremental = false
                                    ))
                                }

                                currentHref = ""
                                currentSize = 0L
                                currentModified = ""
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (_: Exception) {}

        return versions.sortedByDescending { it.displayName }
    }
}
