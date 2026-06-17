package com.tang.prm.domain.model

/**
 * 亲密度等级统一知识源（Single Source of Truth）
 *
 * 阈值采用钟形分布：初识(0-14) / 泛交(15-39) / 朋友(40-74) / 密友(75-89) / 至亲(90-100)
 * 朋友区间最宽（35 分），符合社交关系"多数人为普通朋友"的实际分布。
 *
 * 颜色统一使用 CardRarity 配色：UR 金 / SSR 红 / SR 紫 / R 蓝 / N 灰
 * 所有 UI 层、Domain 层、UseCase 必须委托到此 enum，禁止再写 when 分支。
 *
 * 注意：[colorValue] 使用 ARGB Long 格式（如 0xFF94A3B8），UI 层通过
 * [com.tang.prm.ui.theme.IntimacyColors] 转换为 Compose Color。
 */
enum class IntimacyTier(
    val label: String,
    val cardRarity: String,
    val minScore: Int,
    val maxScore: Int,
    val colorValue: Long,
    val stars: Int
) {
    NEW("初识", "N", 0, 14, 0xFF94A3B8, 1),
    ACQUAINTANCE("泛交", "R", 15, 39, 0xFF3B82F6, 2),
    FRIEND("朋友", "SR", 40, 74, 0xFF8B5CF6, 3),
    CLOSE("密友", "SSR", 75, 89, 0xFFEF4444, 4),
    FAMILY("至亲", "UR", 90, 100, 0xFFF59E0B, 5);

    companion object {
        fun of(score: Int): IntimacyTier {
            val s = score.coerceIn(0, 100)
            return when {
                s >= 90 -> FAMILY
                s >= 75 -> CLOSE
                s >= 40 -> FRIEND
                s >= 15 -> ACQUAINTANCE
                else -> NEW
            }
        }
    }
}

fun getIntimacyLabel(score: Int): String = IntimacyTier.of(score).label
fun getIntimacyColor(score: Int): Long = IntimacyTier.of(score).colorValue
fun getCardRarityLabel(score: Int): String = IntimacyTier.of(score).cardRarity
