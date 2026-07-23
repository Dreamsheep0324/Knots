package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.Ingredient
import java.util.Locale
import javax.inject.Inject

/**
 * 食材份量缩放 UseCase
 *
 * 将 RecipeDetailViewModel 中的食材缩放业务规则下沉到 Domain 层：
 * - 根据 originalServings → targetServings 的比例缩放可缩放食材的数量
 * - 数量格式化规则（整数去小数、小于1保留两位、其余保留一位）
 */
class RecipeScalingUseCase @Inject constructor() {

    /**
     * 按份量比例缩放食材列表
     *
     * @param ingredients 原始食材列表
     * @param originalServings 原始份数（<=0 时不缩放，返回原列表）
     * @param targetServings 目标份数（<=0 时不缩放，返回原列表）
     * @return 缩放后的食材列表
     */
    fun scaleIngredients(
        ingredients: List<Ingredient>,
        originalServings: Int,
        targetServings: Int
    ): List<Ingredient> {
        if (originalServings <= 0 || targetServings <= 0) return ingredients
        val ratio = targetServings.toDouble() / originalServings
        return ingredients.map { ingredient ->
            if (!ingredient.isScalable) {
                ingredient
            } else {
                val originalAmount = ingredient.amount.toDoubleOrNull()
                if (originalAmount != null) {
                    ingredient.copy(amount = formatScaledAmount(originalAmount * ratio))
                } else {
                    ingredient
                }
            }
        }
    }

    /**
     * 格式化缩放后的数量：
     * - 整数值去小数（2.0 → "2"）
     * - 小于 1 保留两位小数（0.50 → "0.5"）
     * - 其余保留一位小数（1.25 → "1.3"）
     *
     * M-4 修复：改为 private（DC-2 遗漏，仅同类内部调用，无外部消费点）。
     * M-6 修复：使用 Locale.US 而非默认 Locale.getDefault()，避免非英语/非中文 locale 下
     * String.format 产生逗号小数点（如德语 "0,50"），导致后续 toDoubleOrNull() 解析失败。
     */
    private fun formatScaledAmount(value: Double): String = when {
        value == value.toLong().toDouble() -> value.toLong().toString()
        value < 1 -> String.format(Locale.US, "%.2f", value).trimEnd('0').trimEnd('.')
        else -> String.format(Locale.US, "%.1f", value).trimEnd('0').trimEnd('.')
    }
}
