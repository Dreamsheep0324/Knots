package com.tang.prm.feature.events

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tang.prm.domain.model.AppStrings
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.model.EventType
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.theme.Dimens
import com.tang.prm.ui.theme.SignalAmber
import com.tang.prm.ui.theme.SignalElectric
import com.tang.prm.ui.theme.SignalGreen
import com.tang.prm.ui.theme.SignalPurple
import com.tang.prm.ui.theme.getEmotionColor
import com.tang.prm.ui.theme.getEventTypeStyle
import com.tang.prm.ui.theme.getWeatherColor
import com.tang.prm.ui.theme.toComposeColor
import java.util.Calendar

/**
 * feature/events 跨文件共享的解析逻辑。
 *
 * 卡片/详情页/日历视图可有差异化 UI 实现，但数据层规则（颜色解析、类型查找、
 * 日期分组等）必须单一来源，避免多文件各自硬编码产生不一致。
 */

// ─────────────────────────────────────────────────────────────────
// C-2: 事件类型显示名
// ─────────────────────────────────────────────────────────────────

/**
 * 事件类型的显示名：优先使用自定义类型名，否则回退到枚举默认显示名。
 *
 * 例：用户为 MEETUP 自定义名称为"聚会"时返回"聚会"；未自定义时返回"见面"。
 */
internal val Event.typeDisplayName: String
    get() = customTypeName ?: type.displayName

// ─────────────────────────────────────────────────────────────────
// C-3: 自定义类型查找
// ─────────────────────────────────────────────────────────────────

/**
 * 解析事件对应的自定义类型。
 *
 * 规则：
 * - 非 OTHER 类型：优先按 key 匹配（事件类型名），其次按 name 匹配
 * - OTHER 类型：使用事件上记录的 customTypeName 查找
 *
 * @return 匹配的自定义类型，无匹配时返回 null（由调用方决定 fallback 策略）
 */
internal fun resolveEventCustomType(
    event: Event,
    eventTypes: List<CustomType>
): CustomType? = if (event.type != EventType.OTHER) {
    eventTypes.find { it.key == event.type.name }
        ?: eventTypes.find { it.name == event.type.name }
} else {
    event.customTypeName?.let { ctn -> eventTypes.find { it.name == ctn } }
}

// ─────────────────────────────────────────────────────────────────
// Q-4: 事件主色调解析（统一两个 getEventAccentColor 重载）
// ─────────────────────────────────────────────────────────────────

/**
 * 解析事件的主色调。
 *
 * 规则：
 * - 若 [eventTypes] 中能找到对应的自定义类型，使用其配置颜色（兜底 SignalPurple）
 * - 否则使用事件类型默认 style 的 accentColor
 * - OTHER 类型无自定义类型时，使用 SignalElectric
 *
 * @param eventTypes 当前可用的事件类型列表，空列表时使用默认 style
 */
@Composable
internal fun resolveEventAccentColor(
    event: Event,
    eventTypes: List<CustomType> = emptyList()
): Color {
    val customType = resolveEventCustomType(event, eventTypes)
    return if (customType != null) {
        customType.color?.toComposeColor(SignalPurple) ?: SignalPurple
    } else if (event.type != EventType.OTHER) {
        getEventTypeStyle(event.type).accentColor
    } else {
        SignalElectric
    }
}

// ─────────────────────────────────────────────────────────────────
// C-1: 天气/情绪颜色解析
// ─────────────────────────────────────────────────────────────────

/**
 * 解析天气名称对应的 Compose Color。
 *
 * 查表失败时兜底 [SignalAmber]（与天气主色调一致）。
 */
internal fun resolveWeatherColor(weather: String): Color =
    getWeatherColor(weather)?.toComposeColor(SignalAmber) ?: SignalAmber

/**
 * 解析情绪名称对应的 Compose Color。
 *
 * 查表失败时兜底 [SignalPurple]（与情绪主色调一致）。
 */
internal fun resolveEmotionColor(emotion: String): Color =
    getEmotionColor(emotion)?.toComposeColor(SignalPurple) ?: SignalPurple

// ─────────────────────────────────────────────────────────────────
// C-5: 日期分组
// ─────────────────────────────────────────────────────────────────

/**
 * 按日期（年月日）分组事件，并按日期倒序排列（最新日期在前）。
 *
 * 返回值：`List<Pair<Triple<year, month, day>, events>>`，
 * 每组的 events 保留原顺序（如需按时间排序由调用方处理）。
 */
internal fun groupEventsByDate(events: List<Event>): List<Pair<Triple<Int, Int, Int>, List<Event>>> =
    events.groupBy { event ->
        Calendar.getInstance().apply { timeInMillis = event.time }.let {
            Triple(it.get(Calendar.YEAR), it.get(Calendar.MONTH), it.get(Calendar.DAY_OF_MONTH))
        }
    }.toList().sortedByDescending { (date, _) ->
        Calendar.getInstance().apply { set(date.first, date.second, date.third) }.timeInMillis
    }

// ─────────────────────────────────────────────────────────────────
// C-4: 头像或首字母占位
// ─────────────────────────────────────────────────────────────────

/**
 * 头像或首字母占位组件。
 *
 * 规则：
 * - [avatarUrl] 非空时显示网络/本地头像图像（裁剪填充）
 * - 为空时显示名称首字母居中，背景色由 [bgColor] 指定
 *
 * 调用方负责外层容器（如 Column+名字、Box+删除按钮叠加），本组件只负责
 * "有头像显示头像，无头像显示首字母"的核心逻辑，避免 3 处重复实现。
 *
 * @param avatarUrl 头像 URL，null 或空串视为无头像
 * @param name 用于提取首字母的名称（取首个字符，无字符时显示 "?"）
 * @param modifier 组件修饰符（应包含尺寸）
 * @param shape 头像/占位的裁剪形状，默认圆形
 * @param bgColor 首字母占位的背景色
 * @param initialColor 首字母文字颜色
 * @param initialFontSize 首字母字号
 */
@Composable
internal fun AvatarOrInitial(
    avatarUrl: String?,
    name: String,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    bgColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = AnimationTokens.Alpha.faint),
    initialColor: Color = MaterialTheme.colorScheme.primary,
    initialFontSize: TextUnit = TextUnit.Unspecified
) {
    Box(
        modifier = modifier.clip(shape).background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        if (!avatarUrl.isNullOrBlank()) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = name.firstOrNull()?.toString() ?: "?",
                color = initialColor,
                fontWeight = FontWeight.Bold,
                fontSize = initialFontSize
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// C-6: 事件类型筛选 chips 行
// ─────────────────────────────────────────────────────────────────

/**
 * 事件类型筛选 chip 的视觉样式。
 *
 * - [Compact]：手机版，紧凑无圆点指示，全部 chip 与类型 chip 均使用 primary 作选中色
 * - [Prominent]：平板版，带圆点指示；"全部" 用 [SignalGreen]，类型 chip 用其自定义类型颜色
 */
internal enum class FilterChipStyle {
    Compact,
    Prominent
}

/**
 * 事件类型筛选 chips 行：包含"全部" + 各自定义事件类型。
 *
 * 抽取自手机版 `EventsScreen` 与平板版 `TabletFilterChips` 的重复实现，
 * 通过 [style] 控制视觉差异，核心逻辑（选中态判断、key 设置、点击回调）保持一致。
 *
 * @param eventTypes 自定义事件类型列表
 * @param selectedType 当前选中的类型名（null 表示"全部"）
 * @param onTypeSelect 点击回调，null 表示点击了"全部"
 * @param style chip 视觉样式，默认紧凑（手机版）
 */
@Composable
internal fun EventTypeFilterRow(
    eventTypes: List<CustomType>,
    selectedType: String?,
    onTypeSelect: (String?) -> Unit,
    modifier: Modifier = Modifier,
    style: FilterChipStyle = FilterChipStyle.Compact
) {
    val isCompact = style == FilterChipStyle.Compact
    val allChipColor = if (isCompact) MaterialTheme.colorScheme.primary else SignalGreen
    val rowPadding = if (isCompact) {
        Modifier.padding(horizontal = Dimens.paddingPage)
    } else {
        Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
    }
    val spacing: Dp = if (isCompact) 8.dp else 10.dp
    val shape: Shape = if (isCompact) RoundedCornerShape(20.dp) else RoundedCornerShape(22.dp)
    val chipHorizontalPadding: Dp = if (isCompact) 14.dp else 18.dp
    val chipVerticalPadding: Dp = if (isCompact) 5.dp else 8.dp

    LazyRow(
        modifier = modifier.then(rowPadding),
        horizontalArrangement = Arrangement.spacedBy(spacing)
    ) {
        item {
            FilterChipItem(
                text = AppStrings.Tabs.ALL,
                isSelected = selectedType == null,
                selectedColor = allChipColor,
                onClick = { onTypeSelect(null) },
                shape = shape,
                horizontalPadding = chipHorizontalPadding,
                verticalPadding = chipVerticalPadding,
                isCompact = isCompact
            )
        }
        items(eventTypes, key = { it.id }) { eventType ->
            val typeColor = if (isCompact) {
                MaterialTheme.colorScheme.primary
            } else {
                eventType.color?.toComposeColor(SignalPurple) ?: SignalPurple
            }
            FilterChipItem(
                text = eventType.name,
                isSelected = selectedType == eventType.name,
                selectedColor = typeColor,
                onClick = { onTypeSelect(eventType.name) },
                shape = shape,
                horizontalPadding = chipHorizontalPadding,
                verticalPadding = chipVerticalPadding,
                isCompact = isCompact
            )
        }
    }
}

@Composable
private fun FilterChipItem(
    text: String,
    isSelected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit,
    shape: Shape,
    horizontalPadding: Dp,
    verticalPadding: Dp,
    isCompact: Boolean
) {
    Surface(
        onClick = onClick,
        shape = shape,
        color = if (isSelected) selectedColor else MaterialTheme.colorScheme.surfaceVariant
    ) {
        if (isCompact) {
            Text(
                text = text,
                modifier = Modifier.padding(horizontal = horizontalPadding, vertical = verticalPadding),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Row(
                modifier = Modifier.padding(horizontal = horizontalPadding, vertical = verticalPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            if (isSelected) Color.White else selectedColor,
                            CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
        }
    }
}
