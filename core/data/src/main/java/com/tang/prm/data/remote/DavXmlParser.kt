package com.tang.prm.data.remote

import org.kxml2.io.KXmlParser
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader
import java.util.logging.Level
import java.util.logging.Logger
import javax.inject.Inject

/**
 * PROPFIND multistatus 响应解析器。
 *
 * A-2 修复：从 [WebDavClient] 抽取的纯解析职责。统一了原 `parsePropfindResponse`
 * 与 `parseFileSizeFromPropfind` 两块几乎逐行相同的 XmlPullParser 脚手架。
 *
 * - 纯 JVM 对象，不依赖 Android 框架类型，可用 fixture XML 直接进 `test/` 单测
 * - href 已做路径段解码（`+` → 字面加号，`%20` → 空格），与 RFC 3986 路径段语义对齐
 * - 解析错误不抛出，返回已成功解析的条目（与原 `parsePropfindResponse` 行为一致）
 */
class DavXmlParser @Inject constructor() {

    /**
     * PROPFIND 响应中的单个条目。
     *
     * @param href 已解码的 href（如 `/remote.php/dav/files/user/tang_backup_20240101_120000.zip`）
     * @param size 文件字节数，缺失或解析失败为 0
     * @param lastModified HTTP 日期格式字符串（如 `Mon, 01 Jan 2024 12:00:00 GMT`），缺失为空串
     */
    data class PropfindEntry(
        val href: String,
        val size: Long,
        val lastModified: String
    )

    /**
     * 解析 PROPFIND multistatus 响应，返回所有 [PropfindEntry]。
     *
     * 调用方可按需从 `entry.href` 提取文件名，或按 `entry.size` / `entry.lastModified`
     * 构造业务对象——原两个解析方法分别做这两件事，现统一为单一数据源。
     */
    fun parseMultistatus(xml: String): List<PropfindEntry> {
        val entries = mutableListOf<PropfindEntry>()

        try {
            // 直接用 KXmlParser 替代 XmlPullParserFactory.newInstance()：
            // - Android 运行时内置 KXmlParser（XmlPullParserFactory 默认就是用它）
            // - 纯 JVM 单测中 XmlPullParserFactory 是 Android stub（"not mocked"），
            //   直接用 KXmlParser 配合 testImplementation("net.sf.kxml:kxml2") 即可单测
            val parser = KXmlParser().apply {
                setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
            }
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
                            "href" -> currentHref = parser.nextText().trim()
                            "propstat" -> inPropstat = true
                            "prop" -> inProp = true
                            "getcontentlength" -> if (inPropstat && inProp) {
                                currentSize = parser.nextText().trim().toLongOrNull() ?: 0L
                            }
                            "getlastmodified" -> if (inPropstat && inProp) {
                                currentModified = parser.nextText().trim()
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        when (parser.name) {
                            "propstat" -> inPropstat = false
                            "prop" -> inProp = false
                            "response" -> {
                                entries.add(
                                    PropfindEntry(
                                        href = decodePathSegment(currentHref),
                                        size = currentSize,
                                        lastModified = currentModified
                                    )
                                )
                                currentHref = ""
                                currentSize = 0L
                                currentModified = ""
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            // 用 java.util.logging 而非 android.util.Log，使此类保持纯 JVM 可测试
            logger.log(Level.WARNING, "解析 PROPFIND 响应 XML 失败", e)
        }

        return entries
    }

    /**
     * B-19 修复：解码 PROPFIND href 中的文件名。
     *
     * `URLDecoder.decode` 是表单编码语义（`+`→空格），但 RFC 3986 路径段中
     * `+` 是字面加号、空格已被服务器编码为 `%20`。先把 `+` 替换为 `%2B`
     * 再 decode，使 `+` 保留为字面加号、`%20` 仍正确译为空格。
     */
    private fun decodePathSegment(href: String): String =
        java.net.URLDecoder.decode(href.replace("+", "%2B"), "UTF-8")

    companion object {
        private val logger = Logger.getLogger(DavXmlParser::class.java.name)
    }
}

