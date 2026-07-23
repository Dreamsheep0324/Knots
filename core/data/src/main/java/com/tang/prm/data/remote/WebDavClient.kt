package com.tang.prm.data.remote

import android.util.Base64
import android.util.Log
import com.tang.prm.domain.model.CloudBackupVersion
import com.tang.prm.domain.model.ConnectionTestResult
import com.tang.prm.domain.model.WebDavConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.net.URLEncoder
import java.security.SecureRandom
import java.security.cert.X509Certificate
import com.tang.prm.domain.repository.EncryptionStatusProvider
import com.tang.prm.domain.util.DateUtils
import javax.inject.Inject
import javax.inject.Named
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class WebDavClient @Inject constructor(
    @Named("webdav") private val okHttpClient: OkHttpClient,
    // A-2 修复：注入 EncryptionStatusProvider 替代 domain 层全局单例
    private val encryptionStatusProvider: EncryptionStatusProvider,
    // A-2 修复：注入 DavXmlParser，PROPFIND 解析逻辑抽出为独立纯解析组件，可单测
    private val davXmlParser: DavXmlParser = DavXmlParser()
) {

    companion object {
        private const val TAG = "WebDavClient"
        private const val BUFFER_SIZE = 256 * 1024 // 256KB

        private val TRUST_ALL_MANAGER = object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }

        private val TRUST_ALL_SSL_CONTEXT: SSLContext = SSLContext.getInstance("TLS").apply {
            init(null, arrayOf<TrustManager>(TRUST_ALL_MANAGER), SecureRandom())
        }
    }

    /**
     * 根据 config 返回合适的 OkHttpClient。
     *
     * REM-B-4 修复：trustAllCertificates=true 时仍跳过 SSL 证书校验（保留对自签证书 NAS 的兼容），
     * 但会标记加密降级 + 输出警告日志，UI 层可据此提示用户风险。
     */
    private fun clientFor(config: WebDavConfig): OkHttpClient {
        if (!config.trustAllCertificates) return okHttpClient
        Log.w(TAG, "trustAllCertificates 已启用 — 存在 MITM 风险，请仅在可信网络环境下使用")
        encryptionStatusProvider.markDegraded()
        return okHttpClient.newBuilder()
            .sslSocketFactory(TRUST_ALL_SSL_CONTEXT.socketFactory, TRUST_ALL_MANAGER)
            .hostnameVerifier { _, _ -> true }
            .build()
    }

    /**
     * 测试 WebDAV 连接：PROPFIND Depth:0
     */
    suspend fun testConnection(config: WebDavConfig): ConnectionTestResult = withContext(Dispatchers.IO) {
        try {
            val url = normalizeUrl(config.serverUrl)
            val request = Request.Builder()
                .url(url)
                .withAuth(config)
                .header("Depth", "0")
                .method("PROPFIND", "".toRequestBody())
                .build()

            clientFor(config).newCall(request).execute().use { response ->
                when {
                    response.isSuccessful || response.code == 207 -> ConnectionTestResult.Success
                    response.code == 401 -> ConnectionTestResult.Error("认证失败，请检查用户名和密码")
                    response.code == 404 -> ConnectionTestResult.Error("服务器地址不存在")
                    else -> ConnectionTestResult.Error("连接失败 (${response.code})")
                }
            }
        } catch (e: Exception) {
            // REM-C-1 修复：统一异常→消息映射，消除 4 个 catch 块的重复。
            ConnectionTestResult.Error(e.toWebDavErrorMessage())
        }
    }

    /**
     * 确保远程目录存在：MKCOL
     *
     * REM-Q-1 修复：不再吞没所有异常。MKCOL 405（目录已存在）视为成功，
     * 其他失败（网络/SSL/401/500 等）向上抛出由调用方处理，避免掩盖真实故障。
     */
    suspend fun ensureRemoteDir(config: WebDavConfig) = withContext(Dispatchers.IO) {
        val url = buildRemoteUrl(config, "")
        val request = Request.Builder()
            .url(url)
            .withAuth(config)
            .method("MKCOL", null)
            .build()

        clientFor(config).newCall(request).execute().use { response ->
            // 405 Method Not Allowed = 目录已存在，视为成功
            if (!response.isSuccessful && response.code != 405) {
                throw Exception("创建远程目录失败 (${response.code})")
            }
        }
    }

    /**
     * 确保远程子目录存在：MKCOL
     *
     * B-15 修复：与 ensureRemoteDir 对齐——405 视为成功（目录已存在），其余异常上抛。
     * 原实现 catch 所有异常并仅 Log.w，掩盖了 401/SSL/网络故障，导致上传流程照常推进
     * 但每个文件都报"上传失败 (409)"，根因被掩盖。
     */
    suspend fun ensureRemoteSubDir(config: WebDavConfig, subDir: String) = withContext(Dispatchers.IO) {
        val url = buildSubDirUrl(config, subDir)
        val request = Request.Builder()
            .url(url)
            .withAuth(config)
            .method("MKCOL", null)
            .build()

        clientFor(config).newCall(request).execute().use { response ->
            // 405 Method Not Allowed = 目录已存在，视为成功
            if (!response.isSuccessful && response.code != 405) {
                throw Exception("创建远程子目录失败: $subDir (${response.code})")
            }
        }
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
                .withAuth(config)
                .header("Depth", "1")
                .header("Content-Type", "application/xml; charset=utf-8")
                .method("PROPFIND", propfindBody.toRequestBody())
                .build()

            clientFor(config).newCall(request).execute().use { response ->
                if (!response.isSuccessful && response.code != 207) {
                    throw Exception("列出文件失败 (${response.code})")
                }

                val body = response.body?.string() ?: return@withContext emptyList()
                // A-2 修复：用 DavXmlParser 统一解析，映射为 CloudBackupVersion 列表
                davXmlParser.parseMultistatus(body).mapNotNull { entry ->
                    val fileName = entry.href.trimEnd('/').substringAfterLast('/')
                    buildCloudBackupVersion(fileName, entry.size, entry.lastModified)
                }.sortedByDescending { it.displayName }
            }
        } catch (e: CancellationException) {
            // B-16 修复：取消信号必须向上传播，不能被 catch(Exception) 吞掉
            throw e
        } catch (e: SocketTimeoutException) {
            throw Exception("连接超时，请检查网络")
        } catch (e: UnknownHostException) {
            throw Exception("无法连接到服务器，请检查地址")
        } catch (e: IOException) {
            throw Exception("网络连接失败：${e.message}")
        }
    }

    /**
     * 删除文件：DELETE
     */
    suspend fun deleteFile(config: WebDavConfig, fileName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = buildRemoteUrl(config, fileName)
            val request = Request.Builder()
                .url(url)
                .withAuth(config)
                .delete()
                .build()

            clientFor(config).newCall(request).execute().use { response ->
                response.isSuccessful || response.code == 204
            }
        } catch (e: CancellationException) {
            // B-16 修复：取消信号必须向上传播
            throw e
        } catch (e: IOException) {
            // REM-C-1 修复：网络异常统一映射并向上抛出，由调用方处理。
            // 抛 IOException 子类（而非通用 Exception）既保留网络错误语义又满足 detekt 规则。
            throw IOException(e.toWebDavErrorMessage())
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
                .withAuth(config)
                .get()
                .build()

            clientFor(config).newCall(request).execute().use { response ->
                if (response.code == 404) return@withContext null
                if (!response.isSuccessful) return@withContext null
                response.body?.string()
            }
        } catch (e: CancellationException) {
            // B-16 修复：取消信号必须向上传播
            throw e
        } catch (e: IOException) {
            // B-16 修复：网络故障=抛错，而非伪装"文件不存在"
            throw IOException(e.toWebDavErrorMessage())
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
            .withAuth(config)
            .put(content.toRequestBody(mediaType))
            .build()

        clientFor(config).newCall(request).execute().use { response ->
            if (!response.isSuccessful && response.code != 201 && response.code != 204) {
                throw Exception("上传清单失败 (${response.code})")
            }
        }
    }

    /**
     * 上传单个图片文件到指定子目录
     */
    suspend fun uploadImageFile(config: WebDavConfig, subDir: String, fileName: String, file: File) {
        // Q-1 修复②：统一 PUT 上传，消除与 uploadDbFile 的脚手架复制
        putFile(config, buildSubDirFileUrl(config, subDir, fileName), file, getMediaType(fileName), "上传图片失败")
    }

    /**
     * 下载单个图片文件
     */
    suspend fun downloadImageFile(config: WebDavConfig, subDir: String, fileName: String, targetFile: File) = withContext(Dispatchers.IO) {
        val url = buildSubDirFileUrl(config, subDir, fileName)
        val request = Request.Builder()
            .url(url)
            .withAuth(config)
            .get()
            .build()

        clientFor(config).newCall(request).execute().use { response ->
            // Q-1 修复①：统一下载流式写文件
            response.downloadTo(targetFile, "下载图片失败")
        }
    }

    /**
     * 删除远程子目录下的文件
     */
    suspend fun deleteRemoteFile(config: WebDavConfig, subDir: String, fileName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = buildSubDirFileUrl(config, subDir, fileName)
            val request = Request.Builder()
                .url(url)
                .withAuth(config)
                .delete()
                .build()

            clientFor(config).newCall(request).execute().use { response ->
                response.isSuccessful || response.code == 204 || response.code == 404
            }
        } catch (e: CancellationException) {
            // B-16 修复：取消信号必须向上传播
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "删除远程文件失败", e)
            false
        }
    }

    /**
     * 上传数据库文件到 db/ 子目录
     */
    suspend fun uploadDbFile(config: WebDavConfig, fileName: String, file: File) {
        // Q-1 修复②：统一 PUT 上传，消除与 uploadImageFile 的脚手架复制
        putFile(config, buildSubDirFileUrl(config, "db", fileName), file, "application/zip".toMediaType(), "上传数据库失败")
    }

    /**
     * 下载数据库文件
     */
    suspend fun downloadDbFile(config: WebDavConfig, fileName: String, targetFile: File) = withContext(Dispatchers.IO) {
        val url = buildSubDirFileUrl(config, "db", fileName)
        val request = Request.Builder()
            .url(url)
            .withAuth(config)
            .get()
            .build()

        clientFor(config).newCall(request).execute().use { response ->
            // Q-1 修复①：统一下载流式写文件
            response.downloadTo(targetFile, "下载数据库失败")
        }
    }

    /**
     * B-6 修复：从根目录下载旧版全量备份文件。
     *
     * listRemoteBackups 降级路径从根目录列出 tang_backup_*.zip，但原回退下载调用的
     * downloadDbFile 进 db/ 子目录取同名文件，两个路径必有一个是错的。
     * 现为回退路径新增独立方法，与列表路径一致从根目录 GET。
     */
    suspend fun downloadLegacyFile(config: WebDavConfig, fileName: String, targetFile: File) = withContext(Dispatchers.IO) {
        val url = buildRemoteUrl(config, fileName)
        val request = Request.Builder()
            .url(url)
            .withAuth(config)
            .get()
            .build()

        clientFor(config).newCall(request).execute().use { response ->
            // Q-1 修复①：统一下载流式写文件
            response.downloadTo(targetFile, "下载旧版备份失败")
        }
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
                .withAuth(config)
                .header("Depth", "1")
                .header("Content-Type", "application/xml; charset=utf-8")
                .method("PROPFIND", propfindBody.toRequestBody())
                .build()

            clientFor(config).newCall(request).execute().use { response ->
                if (!response.isSuccessful && response.code != 207) return@withContext 0L

                val body = response.body?.string() ?: return@withContext 0L
                // A-2 修复：用 DavXmlParser 统一解析，按文件名匹配取大小
                davXmlParser.parseMultistatus(body)
                    .firstOrNull { it.href.trimEnd('/').substringAfterLast('/') == fileName }
                    ?.size ?: 0L
            }
        } catch (e: CancellationException) {
            // B-16 修复：取消信号必须向上传播
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "获取远端文件大小失败", e)
            0L
        }
    }

    // ===== 工具方法 =====

    /**
     * 将网络异常映射为统一的用户可读错误消息。
     *
     * REM-C-1 修复：消除 testConnection / listFiles / deleteFile 三处 catch 块中的
     * 异常→消息映射重复。SocketTimeoutException / UnknownHostException 均为
     * IOException 子类，按精度从高到低匹配。
     */
    private fun Throwable.toWebDavErrorMessage(): String = when (this) {
        is SocketTimeoutException -> "连接超时，请检查网络"
        is UnknownHostException -> "无法连接到服务器，请检查地址"
        is IOException -> "网络连接失败：$message"
        else -> "操作失败：$message"
    }

    private fun buildBasicAuth(username: String, password: String): String {
        val credentials = "$username:$password"
        val encoded = Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
        return "Basic $encoded"
    }

    // ===== Q-1 修复：统一四组孪生代码的辅助方法 =====

    /** Q-1 修复④：统一附加 Basic Auth header，消除每个方法一行的重复 */
    private fun Request.Builder.withAuth(config: WebDavConfig): Request.Builder =
        header("Authorization", buildBasicAuth(config.username, config.password))

    /** Q-1 修复①：统一下载流式写文件，消除 downloadImageFile/downloadDbFile/downloadLegacyFile 三处复制 */
    private fun okhttp3.Response.downloadTo(targetFile: File, errMsgPrefix: String) {
        if (!isSuccessful) throw Exception("$errMsgPrefix ($code)")
        body?.byteStream()?.use { input ->
            targetFile.parentFile?.mkdirs()
            BufferedOutputStream(FileOutputStream(targetFile), BUFFER_SIZE).use { output ->
                val buffer = ByteArray(BUFFER_SIZE)
                var len: Int
                while (input.read(buffer).also { len = it } > 0) {
                    output.write(buffer, 0, len)
                }
            }
        } ?: throw Exception("$errMsgPrefix：响应体为空")
    }

    /** Q-1 修复②：统一 PUT 上传，消除 uploadImageFile/uploadDbFile 两处复制 */
    private suspend fun putFile(
        config: WebDavConfig, url: String, file: File,
        mediaType: okhttp3.MediaType, errMsgPrefix: String
    ) = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .withAuth(config)
            .put(file.asRequestBody(mediaType))
            .build()

        clientFor(config).newCall(request).execute().use { response ->
            if (!response.isSuccessful && response.code != 201 && response.code != 204) {
                throw Exception("$errMsgPrefix (${response.code})")
            }
        }
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
        val basePath = "$baseUrl/$remotePath"
        return if (fileName.isEmpty()) {
            "$basePath/"
        } else {
            // B-19 修复：路径段编码用 HttpUrl.addPathSegment（空格→%20），
            // 避免 URLEncoder 表单编码（空格→+）与 RFC 3986 路径段语义不符
            basePath.toHttpUrl()?.newBuilder()?.addPathSegment(fileName)?.build()?.toString()
                ?: "$basePath/${URLEncoder.encode(fileName, "UTF-8")}"
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
        val basePath = "$baseUrl/$remotePath/$subDir"
        // B-19 修复：同 buildRemoteUrl，用 HttpUrl.addPathSegment 做正确的路径段编码
        return basePath.toHttpUrl()?.newBuilder()?.addPathSegment(fileName)?.build()?.toString()
            ?: "$basePath/${URLEncoder.encode(fileName, "UTF-8")}"
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

    private fun buildCloudBackupVersion(fileName: String, size: Long, modified: String): CloudBackupVersion? {
        if (!fileName.endsWith(".zip") || !fileName.startsWith("tang_backup_")) return null

        val displayName = try {
            val timePart = fileName.removePrefix("tang_backup_").removeSuffix(".zip")
            DateUtils.parseBackupTimestamp(timePart)?.let { DateUtils.formatDateTimeHyphen(it) } ?: fileName
        } catch (e: Exception) {
            Log.w(TAG, "解析备份文件名日期失败: $fileName", e)
            fileName
        }

        return CloudBackupVersion(
            fileName = fileName,
            fileSize = size,
            lastModified = modified,
            displayName = displayName,
            isIncremental = false
        )
    }
}
