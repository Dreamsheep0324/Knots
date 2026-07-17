@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.feature.people.contacts.tablet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.util.DateUtils
import com.tang.prm.domain.util.parseListField

// ═══════════════════════════════════════════════════════════════
// 三栏内容区 — 档案 / 时光 / 心绪
// ═══════════════════════════════════════════════════════════════

/**
 * 平板详情页四栏内容区所需的回调集合。
 * 将 5 个 onClick 打包，避免 GalleryBody 参数列表过长。
 */
internal data class GalleryClicks(
    val onEventClick: (Long) -> Unit,
    val onAnniversaryClick: (Long) -> Unit,
    val onGiftClick: (Long) -> Unit,
    val onThoughtClick: (Long) -> Unit,
    val onConversationClick: (Long) -> Unit
)

@Composable
internal fun GalleryBody(
    contact: Contact,
    tierColor: Color,
    events: List<Event>,
    conversations: List<Event>,
    anniversaries: List<com.tang.prm.domain.model.Anniversary>,
    gifts: List<com.tang.prm.domain.model.Gift>,
    thoughts: List<com.tang.prm.domain.model.Thought>,
    eventTypes: List<CustomType>,
    clicks: GalleryClicks
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 56.dp)
            .padding(bottom = 80.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp),
        verticalAlignment = Alignment.Top
    ) {
        // ── 左栏：档案（完整字段）──
        Column(modifier = Modifier.weight(1f)) {
            GallerySectionTitle(title = "PROFILE · 档案", accentColor = tierColor)

            // 联系方式
            if (hasAny(contact.phone, contact.email, contact.city, contact.address)) {
                GalleryInfoCard(title = "CONTACT · 联系方式") {
                    contact.phone?.takeIf { it.isNotBlank() }?.let { GalleryInfoRow(key = "电话", value = it) }
                    contact.email?.takeIf { it.isNotBlank() }?.let { GalleryInfoRow(key = "邮箱", value = it) }
                    contact.city?.takeIf { it.isNotBlank() }?.let { GalleryInfoRow(key = "城市", value = it) }
                    contact.address?.takeIf { it.isNotBlank() }?.let { GalleryInfoRow(key = "地址", value = it, maxLines = 2) }
                }
            }

            // 重要日期
            if (contact.birthday != null || contact.knowingDate != null || contact.lastInteractionTime != null) {
                GalleryInfoCard(title = "DATES · 重要日期") {
                    contact.birthday?.let {
                        GalleryInfoRow(key = "生日", value = buildString {
                            append(DateUtils.formatYearMonthDayChineseFull(it))
                            if (contact.isLunarBirthday) append(" · 农历")
                            if (contact.isLeapMonthBirthday) append(" · 闰月")
                        })
                    }
                    contact.knowingDate?.let {
                        GalleryInfoRow(key = "认识日", value = DateUtils.formatYearMonthDayChineseFull(it))
                    }
                    contact.lastInteractionTime?.let {
                        GalleryInfoRow(key = "最后互动", value = DateUtils.formatRelativeTime(it))
                    }
                }
            }

            // 职业信息
            if (hasAny(contact.company, contact.jobTitle, contact.industry)) {
                GalleryInfoCard(title = "OCCUPATION · 职业") {
                    contact.company?.takeIf { it.isNotBlank() }?.let { GalleryInfoRow(key = "公司", value = it) }
                    contact.jobTitle?.takeIf { it.isNotBlank() }?.let { GalleryInfoRow(key = "职位", value = it) }
                    contact.industry?.takeIf { it.isNotBlank() }?.let { GalleryInfoRow(key = "行业", value = it) }
                }
            }

            // 个人特征（hobby/habit/diet/skill 是 JSON 数组字符串，需解析）
            if (hasAny(contact.mbti, contact.hobby, contact.habit, contact.diet, contact.skill)) {
                GalleryInfoCard(title = "PERSONAL · 个性") {
                    contact.mbti?.takeIf { it.isNotBlank() }?.let { GalleryInfoRow(key = "MBTI", value = it) }
                    parseListField(contact.hobby).takeIf { it.isNotEmpty() }?.let {
                        GalleryInfoRow(key = "爱好", value = it.joinToString("、"))
                    }
                    parseListField(contact.habit).takeIf { it.isNotEmpty() }?.let {
                        GalleryInfoRow(key = "习惯", value = it.joinToString("、"))
                    }
                    parseListField(contact.diet).takeIf { it.isNotEmpty() }?.let {
                        GalleryInfoRow(key = "饮食", value = it.joinToString("、"))
                    }
                    parseListField(contact.skill).takeIf { it.isNotEmpty() }?.let {
                        GalleryInfoRow(key = "技能", value = it.joinToString("、"))
                    }
                }
            }

            // 家庭社交
            if (hasAny(contact.spouseName, contact.childrenNames, contact.introducer) || contact.childrenCount > 0) {
                GalleryInfoCard(title = "FAMILY · 家庭社交") {
                    contact.spouseName?.takeIf { it.isNotBlank() }?.let { GalleryInfoRow(key = "配偶", value = it) }
                    if (contact.childrenCount > 0) {
                        GalleryInfoRow(key = "子女", value = "${contact.childrenCount}人")
                    }
                    contact.childrenNames?.takeIf { it.isNotBlank() }?.let { GalleryInfoRow(key = "子女姓名", value = it, maxLines = 2) }
                    contact.introducer?.takeIf { it.isNotBlank() }?.let { GalleryInfoRow(key = "介绍人", value = it) }
                }
            }
        }

        // ── 中左栏：时光（事件画廊）──
        Column(modifier = Modifier.weight(1.25f)) {
            GallerySectionTitle(title = "MOMENTS · 时光", accentColor = tierColor)
            if (events.isEmpty()) {
                GalleryEmptyHint(text = "暂无事件记录")
            } else {
                val rows = events.take(6).chunked(2)
                rows.forEach { rowEvents ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        rowEvents.forEach { event ->
                            GalleryEventCard(
                                event = event,
                                eventTypes = eventTypes,
                                onClick = { clicks.onEventClick(event.id) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowEvents.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // ── 中右栏：纪念 + 礼物 ──
        Column(modifier = Modifier.weight(1.1f)) {
            GallerySectionTitle(title = "MILESTONES · 纪念", accentColor = tierColor)
            if (anniversaries.isEmpty()) {
                GalleryEmptyHint(text = "暂无纪念日")
            } else {
                anniversaries.take(4).forEach { a ->
                    GalleryAnniversaryCard(
                        anniversary = a,
                        onClick = { clicks.onAnniversaryClick(a.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            GallerySectionTitle(title = "GIFTS · 礼物", accentColor = tierColor)
            if (gifts.isEmpty()) {
                GalleryEmptyHint(text = "暂无礼物记录")
            } else {
                gifts.take(4).forEach { g ->
                    GalleryGiftCard(
                        gift = g,
                        onClick = { clicks.onGiftClick(g.id) }
                    )
                }
            }
        }

        // ── 右栏：心绪（想法笔记）+ 对话 ──
        Column(modifier = Modifier.weight(0.95f)) {
            GallerySectionTitle(title = "THOUGHTS · 心绪", accentColor = tierColor)
            if (thoughts.isEmpty()) {
                GalleryEmptyHint(text = "暂无想法记录")
            } else {
                thoughts.take(5).forEach { thought ->
                    GalleryNoteCard(
                        date = DateUtils.formatYearMonthDay(thought.createdAt),
                        content = thought.content,
                        onClick = { clicks.onThoughtClick(thought.id) }
                    )
                }
            }

            // 对话记录
            Spacer(modifier = Modifier.height(24.dp))
            GallerySectionTitle(title = "DIALOGUES · 对话", accentColor = tierColor)
            if (conversations.isEmpty()) {
                GalleryEmptyHint(text = "暂无对话记录")
            } else {
                conversations.take(4).forEach { c ->
                    GalleryConversationCard(
                        conversation = c,
                        onClick = { clicks.onConversationClick(c.id) }
                    )
                }
            }
        }
    }
}

internal fun hasAny(vararg values: String?): Boolean = values.any { !it.isNullOrBlank() }

@Composable
internal fun GallerySectionTitle(title: String, accentColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(2.dp)
                .background(MaterialTheme.colorScheme.onSurface)
        )
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 3.sp
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline)
        )
    }
}

@Composable
internal fun GalleryInfoCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            content()
        }
    }
}

@Composable
internal fun GalleryInfoRow(key: String, value: String?, maxLines: Int = 1) {
    if (value.isNullOrBlank()) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = key,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f, fill = false).padding(start = 16.dp)
        )
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    )
}
