package com.tang.prm.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * A-2 / Q-10③ 修复：DavXmlParser fixture 测试。
 *
 * 原 WebDavClient 的两块 PROPFIND 解析逻辑（parsePropfindResponse / parseFileSizeFromPropfind）
 * 零测试覆盖，命名空间/大小写/href 编码脆弱。抽出 DavXmlParser 后用 fixture XML 直接进 test/ 单测。
 */
class DavXmlParserTest {

    private val parser = DavXmlParser()

    @Test
    fun `解析包含多个备份文件的 multistatus 响应`() {
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <d:multistatus xmlns:d="DAV:">
              <d:response>
                <d:href>/tang/tang_backup_20240101_120000.zip</d:href>
                <d:propstat>
                  <d:prop>
                    <d:getcontentlength>1048576</d:getcontentlength>
                    <d:getlastmodified>Mon, 01 Jan 2024 12:00:00 GMT</d:getlastmodified>
                  </d:prop>
                </d:propstat>
              </d:response>
              <d:response>
                <d:href>/tang/tang_backup_20240102_130000.zip</d:href>
                <d:propstat>
                  <d:prop>
                    <d:getcontentlength>2097152</d:getcontentlength>
                    <d:getlastmodified>Tue, 02 Jan 2024 13:00:00 GMT</d:getlastmodified>
                  </d:prop>
                </d:propstat>
              </d:response>
            </d:multistatus>
        """.trimIndent()

        val entries = parser.parseMultistatus(xml)

        assertEquals(2, entries.size)
        assertEquals("/tang/tang_backup_20240101_120000.zip", entries[0].href)
        assertEquals(1048576L, entries[0].size)
        assertEquals("Mon, 01 Jan 2024 12:00:00 GMT", entries[0].lastModified)
        assertEquals(2097152L, entries[1].size)
    }

    @Test
    fun `解析 href 中百分号编码的文件名 - 空格解码为百分之二十`() {
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <d:multistatus xmlns:d="DAV:">
              <d:response>
                <d:href>/tang/photo%202024.jpg</d:href>
                <d:propstat>
                  <d:prop>
                    <d:getcontentlength>1024</d:getcontentlength>
                  </d:prop>
                </d:propstat>
              </d:response>
            </d:multistatus>
        """.trimIndent()

        val entries = parser.parseMultistatus(xml)

        assertEquals(1, entries.size)
        // B-19 修复：%20 应正确解码为空格
        assertEquals("/tang/photo 2024.jpg", entries[0].href)
    }

    @Test
    fun `解析 href 中字面加号 - 加号保留不被误解码为空格`() {
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <d:multistatus xmlns:d="DAV:">
              <d:response>
                <d:href>/tang/C++Guide.pdf</d:href>
                <d:propstat>
                  <d:prop>
                    <d:getcontentlength>5120</d:getcontentlength>
                  </d:prop>
                </d:propstat>
              </d:response>
            </d:multistatus>
        """.trimIndent()

        val entries = parser.parseMultistatus(xml)

        assertEquals(1, entries.size)
        // B-19 修复：+ 是字面加号，不应被 URLDecoder 误解为空格
        assertEquals("/tang/C++Guide.pdf", entries[0].href)
    }

    @Test
    fun `缺失 getcontentlength 时 size 默认为零`() {
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <d:multistatus xmlns:d="DAV:">
              <d:response>
                <d:href>/tang/unknown.zip</d:href>
                <d:propstat>
                  <d:prop>
                    <d:getlastmodified>Wed, 03 Jan 2024 09:00:00 GMT</d:getlastmodified>
                  </d:prop>
                </d:propstat>
              </d:response>
            </d:multistatus>
        """.trimIndent()

        val entries = parser.parseMultistatus(xml)

        assertEquals(1, entries.size)
        assertEquals(0L, entries[0].size)
        assertEquals("Wed, 03 Jan 2024 09:00:00 GMT", entries[0].lastModified)
    }

    @Test
    fun `缺失 getlastmodified 时 lastModified 默认为空串`() {
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <d:multistatus xmlns:d="DAV:">
              <d:response>
                <d:href>/tang/no_date.zip</d:href>
                <d:propstat>
                  <d:prop>
                    <d:getcontentlength>256</d:getcontentlength>
                  </d:prop>
                </d:propstat>
              </d:response>
            </d:multistatus>
        """.trimIndent()

        val entries = parser.parseMultistatus(xml)

        assertEquals(1, entries.size)
        assertEquals("", entries[0].lastModified)
    }

    @Test
    fun `非法 XML 返回空列表不抛异常`() {
        val xml = "not valid xml <<<"

        val entries = parser.parseMultistatus(xml)

        assertTrue(entries.isEmpty())
    }

    @Test
    fun `空 multistatus 返回空列表`() {
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <d:multistatus xmlns:d="DAV:">
            </d:multistatus>
        """.trimIndent()

        val entries = parser.parseMultistatus(xml)

        assertTrue(entries.isEmpty())
    }

    @Test
    fun `非数字 getcontentlength 时 size 降级为零`() {
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <d:multistatus xmlns:d="DAV:">
              <d:response>
                <d:href>/tang/bad_size.zip</d:href>
                <d:propstat>
                  <d:prop>
                    <d:getcontentlength>not-a-number</d:getcontentlength>
                  </d:prop>
                </d:propstat>
              </d:response>
            </d:multistatus>
        """.trimIndent()

        val entries = parser.parseMultistatus(xml)

        assertEquals(1, entries.size)
        assertEquals(0L, entries[0].size)
    }

    @Test
    fun `无命名空间前缀的 multistatus 也能解析`() {
        // 某些服务器不使用 d: 前缀，直接用默认命名空间
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <multistatus xmlns="DAV:">
              <response>
                <href>/tang/tang_backup_20240301_080000.zip</href>
                <propstat>
                  <prop>
                    <getcontentlength>8192</getcontentlength>
                    <getlastmodified>Fri, 01 Mar 2024 08:00:00 GMT</getlastmodified>
                  </prop>
                </propstat>
              </response>
            </multistatus>
        """.trimIndent()

        val entries = parser.parseMultistatus(xml)

        assertEquals(1, entries.size)
        assertEquals(8192L, entries[0].size)
    }

    @Test
    fun `从 href 提取文件名 - 末尾斜杠视为目录名`() {
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <d:multistatus xmlns:d="DAV:">
              <d:response>
                <d:href>/tang/images/</d:href>
                <d:propstat>
                  <d:prop>
                    <d:getcontentlength>0</d:getcontentlength>
                  </d:prop>
                </d:propstat>
              </d:response>
              <d:response>
                <d:href>/tang/tang_backup_20240401.zip</d:href>
                <d:propstat>
                  <d:prop>
                    <d:getcontentlength>4096</d:getcontentlength>
                  </d:prop>
                </d:propstat>
              </d:response>
            </d:multistatus>
        """.trimIndent()

        val entries = parser.parseMultistatus(xml)

        assertEquals(2, entries.size)
        // 第一个是目录，trimEnd('/') 后 substringAfterLast('/') 得空串
        assertEquals("/tang/images/", entries[0].href)
        assertEquals("/tang/tang_backup_20240401.zip", entries[1].href)
    }

    @Test
    fun `getcontentlength 在 prop 外时被忽略 - 不误采`() {
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <d:multistatus xmlns:d="DAV:">
              <d:response>
                <d:href>/tang/outside.zip</d:href>
                <d:propstat>
                  <d:prop>
                    <d:getlastmodified>Mon, 01 Jan 2024 12:00:00 GMT</d:getlastmodified>
                  </d:prop>
                </d:propstat>
                <d:getcontentlength>999999</d:getcontentlength>
              </d:response>
            </d:multistatus>
        """.trimIndent()

        val entries = parser.parseMultistatus(xml)

        assertEquals(1, entries.size)
        // getcontentlength 在 prop 外，应被忽略，size 保持 0
        assertEquals(0L, entries[0].size)
    }
}
