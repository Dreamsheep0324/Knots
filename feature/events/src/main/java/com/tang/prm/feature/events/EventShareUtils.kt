package com.tang.prm.feature.events

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.net.toUri
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.model.EventType
import com.tang.prm.domain.util.DateUtils

/**
 * feature/events 的分享工具集。
 *
 * 将非 UI 的平台逻辑（Intent 构造、URI 处理）从 Composable 文件中剥离，
 * 保持 `EventDetailComponents.kt` 等文件只承担 UI 职责。
 */

/**
 * 调用系统分享面板分享事件。
 *
 * 文本内容包含：标题、描述、类型、地点、天气、情绪、时间、参与者、备注。
 * 当事件包含照片且首张照片为 content URI 时，附加为图片流（type 切换为 image 类型）；
 * 否则仅分享纯文本。
 *
 * @param context 用于启动分享 Intent 的 Context
 * @param event 待分享的事件
 */
internal fun shareEvent(context: Context, event: Event) {
    val sb = StringBuilder()

    event.title.let { if (it.isNotBlank()) sb.appendLine(it) }

    event.description?.let { if (it.isNotBlank()) sb.appendLine(it) }

    sb.appendLine()

    if (event.type != EventType.OTHER || event.customTypeName != null) sb.append("🏷️ ${event.typeDisplayName}  ")
    event.location?.let { if (it.isNotBlank()) sb.append("📍 $it  ") }
    event.weather?.let { if (it.isNotBlank()) sb.append("🌤️ $it  ") }
    event.emotion?.let { if (it.isNotBlank()) sb.append("💭 $it  ") }

    sb.appendLine()
    sb.append("📅 ${DateUtils.formatDateTime(event.time)}")

    if (event.participants.isNotEmpty()) {
        sb.appendLine()
        sb.append("👥 ${event.participants.joinToString("、") { it.name }}")
    }

    event.remarks?.let { if (it.isNotBlank()) sb.appendLine().appendLine().append("✨ $it") }

    sb.appendLine().append("— 来自 YU")

    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, sb.toString().trim())
        type = "text/plain"

        if (event.photos.isNotEmpty()) {
            try {
                val uri = event.photos.first().toUri()
                if (uri.scheme == "content") {
                    putExtra(Intent.EXTRA_STREAM, uri)
                    type = "image/*"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            } catch (e: Exception) {
                Log.w("shareEvent", "无法附加照片到分享 Intent，仅分享文本", e)
            }
        }
    }

    context.startActivity(Intent.createChooser(shareIntent, "分享事件"))
}
