package com.tang.prm.feature.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.tang.prm.domain.usecase.HomeStats
import com.tang.prm.ui.navigation.ContactListRoute
import com.tang.prm.ui.navigation.DivinationRoute
import com.tang.prm.ui.navigation.FavoritesRoute
import com.tang.prm.ui.navigation.FootprintsRoute
import com.tang.prm.ui.navigation.GiftsRoute
import com.tang.prm.ui.navigation.PhotoAlbumRoute
import com.tang.prm.ui.navigation.RecipesRoute
import com.tang.prm.ui.navigation.SubscriptionsRoute
import com.tang.prm.ui.navigation.ThoughtsRoute
import com.tang.prm.ui.theme.SignalAmber
import com.tang.prm.ui.theme.SignalCoral
import com.tang.prm.ui.theme.SignalElectric
import com.tang.prm.ui.theme.SignalGold
import com.tang.prm.ui.theme.SignalGreen
import com.tang.prm.ui.theme.SignalPurple
import com.tang.prm.ui.theme.SignalSky

/**
 * 首页模块共享常量与定义，单一来源。
 *
 * 设计原则：
 * - 消除魔法数字（Q-3）与重复文案（Q-6/C-1）
 * - 让硬编码中文集中可见，便于未来 i18n 抽取（Q-13）
 * - 统一统计配置（C-2）与频道定义（C-7），消除两处重复
 * - 不引入 strings.xml 框架，与项目当前「ViewModel 不注入 Context」惯例保持一致
 */

/** 首页品牌文案，HomeSignalCard 打字机与 HomeTabletJournal 日期横幅共用（Q-6/C-1）。 */
internal const val HOME_TAGLINE = "但今天只是今天，未来也只是今天的未来"

/** 信号强度满格阈值，所有信号条/进度条/格点统一引用（Q-3）。 */
internal const val MAX_SIGNAL_STRENGTH = 50

/**
 * 时段问候语（Q-13）。
 *
 * 用枚举替代散落的 `hour < 12 / hour < 18` 分支，单一来源；
 * 未来若要 i18n，只需把 [text] 改为 R.string 引用，调用方逻辑不变。
 *
 * @param maxHour 该时段结束小时（不含），从早到晚递增
 */
internal enum class Greeting(val maxHour: Int, val text: String) {
    MORNING(maxHour = 12, text = "早上好"),
    AFTERNOON(maxHour = 18, text = "下午好"),
    EVENING(maxHour = 24, text = "晚上好");

    companion object {
        /** 根据当前小时（0-23）返回对应时段的问候语。 */
        fun forHour(hour: Int): Greeting =
            entries.first { hour < it.maxHour }
    }
}

/**
 * 首页"人物/事件/礼物/纪念日/对话"5 项统计的统一配置（C-2 修复）。
 *
 * 消除 HomeSignalCard 与 HomeTabletJournal 两处重复的 label/color/icon 列表；
 * 同时把 [statProvider] 绑定到每项，调用方只需传入 [HomeStats] 即可取值，
 * 不再需要按 index 或字段名单独传参。
 *
 * 注意：对话图标用 `Icons.AutoMirrored.Filled.Chat`，RTL 布局下自动镜像，
 * 与原 HomeTabletJournal 的 `Icons.Default.Chat`（不镜像）不一致——这是修复
 * RTL 正确性的副作用。
 *
 * @param label 中文标签，用于 UI 显示
 * @param color 主题色
 * @param icon 图标
 * @param max 满格阈值，默认 [MAX_SIGNAL_STRENGTH]；礼物/纪念日等小数值用更小阈值
 * @param statProvider 从 [HomeStats] 提取对应统计值的函数
 */
internal enum class HomeStatDef(
    val label: String,
    val color: Color,
    val icon: ImageVector,
    val max: Int = MAX_SIGNAL_STRENGTH,
    val statProvider: (HomeStats) -> Int
) {
    CONTACTS("人物", SignalSky, Icons.Default.People, statProvider = { it.contactCount }),
    EVENTS("事件", SignalGreen, Icons.Default.Event, statProvider = { it.eventCount }),
    GIFTS("礼物", SignalCoral, Icons.Default.CardGiftcard, max = 30, statProvider = { it.giftCount }),
    ANNIVERSARIES("纪念日", SignalPurple, Icons.Default.Favorite, max = 20, statProvider = { it.anniversaryCount }),
    CONVERSATIONS("对话", SignalAmber, Icons.AutoMirrored.Filled.Chat, statProvider = { it.conversationCount });
}

/**
 * 首页频道定义（C-7 修复）。
 *
 * 把 [signalProvider] 绑定到频道，消除 HomeScreen 中 channels 列表与 signalStrengths map
 * 分离维护的问题——新增频道只需改一处，且"频道有但统计无"的情况（如占卜）通过
 * 默认 `{ 0 }` 显式表达。
 *
 * @param name 频道名
 * @param color 主题色
 * @param route 导航路由
 * @param desc 描述文案
 * @param icon 图标（与 [textIcon] 二选一）
 * @param textIcon 文字图标（如「☯」），用于无 Material 图标的频道
 * @param signalProvider 从 [HomeStats] 提取该频道信号强度（计数）的函数，默认返回 0
 */
internal data class ChannelDef(
    val name: String,
    val color: Color,
    val route: Any,
    val desc: String,
    val icon: ImageVector? = null,
    val textIcon: String? = null,
    val signalProvider: (HomeStats) -> Int = { 0 }
)

/** 频道列表，UI 与 stats 通过 signalProvider 关联，单一来源（C-7 修复）。 */
internal val channels = listOf(
    ChannelDef("礼物", SignalCoral, GiftsRoute, "收送记录与心愿单", Icons.Default.CardGiftcard) { it.giftCount },
    ChannelDef("圈子", SignalPurple, ContactListRoute, "社交分组与关系管理", Icons.Default.Hub) { it.circleCount },
    ChannelDef("相册", SignalSky, PhotoAlbumRoute.default(), "共享回忆与时光轴", Icons.Default.Image) { it.photoCount },
    ChannelDef("足迹", SignalGreen, FootprintsRoute, "共同地点与旅行轨迹", Icons.Default.Map) { it.footprintCount },
    ChannelDef("想法", SignalAmber, ThoughtsRoute, "灵感笔记与待办事项", Icons.Default.Lightbulb) { it.thoughtCount },
    ChannelDef("收藏", SignalGold, FavoritesRoute, "珍藏回忆与重要内容", Icons.Default.Star) { it.favoriteCount },
    ChannelDef("占卜", SignalElectric, DivinationRoute, "梅花易数 · 六爻纳甲", textIcon = "☯"),
    ChannelDef("订阅", SignalSky, SubscriptionsRoute, "会员订阅与到期提醒", Icons.Default.CreditCard) { it.subscriptionCount },
    ChannelDef("菜谱", SignalElectric, RecipesRoute, "一起做过的菜与味道", Icons.Default.Restaurant) { it.recipeCount }
)
