package com.tang.prm.feature.people.contacts.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.tang.prm.domain.model.PersonRelation

/**
 * 方案 B · 极简行式：单行关系条目（精装版）。
 *
 * 设计理念：
 * - 去除一切冗余标签（无"APP/外部"字样），通过头像视觉细微差异自然区分
 * - 两行布局：姓名为主，关系词（斜体衬线）+ 备注为次，层次清晰
 * - 42dp 圆形头像 + 1.5dp 关系类型色描边（半透明环，色彩克制）
 * - 0.5dp outlineVariant 分隔线（行间细分隔）
 *
 * 行为：
 * - [mode] = EDITOR：右侧显示删除按钮（×）
 * - [mode] = VIEWER：App 联系人显示箭头并可点击跳转；外部人物无箭头不可点击
 */
@Composable
fun PersonRelationRow(
    relation: PersonRelation,
    typeName: String?,
    typeColor: Color?,
    mode: PersonRelationRowMode,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val label = relation.resolveLabel(typeName)
    val displayName = relation.resolveDisplayName()
    val accentColor = typeColor ?: MaterialTheme.colorScheme.outline
    val note = relation.note?.takeIf { it.isNotBlank() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (mode == PersonRelationRowMode.VIEWER && relation.isAppContact) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            )
            .padding(vertical = 12.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧：头像（42dp 圆形，1.5dp 关系类型色半透明环）
        PersonRelationAvatar(
            avatar = relation.targetAvatar,
            name = displayName,
            borderColor = accentColor
        )

        Spacer(Modifier.width(14.dp))

        // 中间：姓名（主行）+ 关系词·备注（次行）
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 斜体衬线关系词：呼应编辑式风格
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontStyle = FontStyle.Italic,
                        fontFamily = FontFamily.Serif
                    ),
                    color = accentColor.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (note != null) {
                    // 中点分隔符
                    Box(
                        modifier = Modifier
                            .size(2.dp)
                            .background(
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                CircleShape
                            )
                    )
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // 右侧：操作按钮
        Spacer(Modifier.width(8.dp))
        when (mode) {
            PersonRelationRowMode.EDITOR -> DeleteButton(onDelete = onDelete)
            PersonRelationRowMode.VIEWER -> if (relation.isAppContact) ArrowButton()
        }
    }
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
        thickness = 0.5.dp
    )
}

enum class PersonRelationRowMode { EDITOR, VIEWER }

/**
 * 关系头像：42dp 圆形，1.5dp 关系类型色半透明环。
 *
 * 视觉统一：App 联系人与外部人物使用同一样式，
 * 仅通过头像内容（真实照片 vs 首字母色块）自然区分。
 */
@Composable
private fun PersonRelationAvatar(
    avatar: String?,
    name: String,
    borderColor: Color
) {
    val initial = name.firstOrNull()?.toString() ?: "?"
    val fallbackBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    val ringColor = borderColor.copy(alpha = 0.55f)

    Box(modifier = Modifier.size(42.dp)) {
        if (!avatar.isNullOrBlank()) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(avatar)
                    .crossfade(true)
                    .build(),
                contentDescription = name,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, ringColor, CircleShape),
                contentScale = ContentScale.Crop,
                loading = { AvatarFallback(initial, fallbackBg) },
                error = { AvatarFallback(initial, fallbackBg) }
            )
        } else {
            AvatarFallback(
                initial = initial,
                background = fallbackBg,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, ringColor, CircleShape)
            )
        }
    }
}

@Composable
private fun AvatarFallback(
    initial: String,
    background: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize().background(background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun DeleteButton(onDelete: () -> Unit) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .clickable { onDelete() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Close,
            contentDescription = "删除",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun ArrowButton() {
    Icon(
        Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier.size(18.dp)
    )
}

/**
 * 包装行可见性动画，用于新增/删除时的平滑过渡。
 */
@Composable
fun AnimatedPersonRelationRow(
    visible: Boolean,
    relation: PersonRelation,
    typeName: String?,
    typeColor: Color?,
    mode: PersonRelationRowMode,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 2 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 2 })
    ) {
        PersonRelationRow(
            relation = relation,
            typeName = typeName,
            typeColor = typeColor,
            mode = mode,
            onClick = onClick,
            onDelete = onDelete
        )
    }
}
