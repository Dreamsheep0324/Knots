package com.tang.prm.feature.reflect.thoughts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.components.ContactAvatar

/**
 * C-3 修复：ThoughtFeedCard 与 ThoughtDetailDialog 中重复的"圆角胶囊 + ContactAvatar + 名称"结构。
 *
 * 差异维度仅 avatarSize（ThoughtFeedCard=18，ThoughtDetailDialog=20），其余样式统一。
 * padding 统一为 (start=6, end=10, top=4, bottom=4)，spacer width 统一为 6.dp，
 * 与 ThoughtFeedCard 原样式完全一致；ThoughtDetailDialog 原样式差异为 1dp（top/bottom=5, spacer=5），
 * 视觉影响可忽略。
 *
 * 注意：ThoughtDialog.kt 行 186-201 的"关联人物选择表单字段"是矩形圆角（12.dp）+ fillMaxWidth 的
 * 表单输入，与"圆角胶囊 chip"语义和结构均不同，不纳入同一抽象。
 */
@Composable
internal fun ContactNameChip(
    name: String,
    avatar: String?,
    avatarSize: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(100.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = AnimationTokens.Alpha.half)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(start = 6.dp, end = 10.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ContactAvatar(avatar = avatar, name = name, size = avatarSize.dp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                name,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
