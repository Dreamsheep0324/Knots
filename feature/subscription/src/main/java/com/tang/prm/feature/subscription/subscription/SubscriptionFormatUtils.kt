package com.tang.prm.feature.subscription.subscription

/** 格式化价格数值：整数值显示无小数，否则保留两位小数 */
fun formatPriceValue(price: Double): String =
    if (price == price.toLong().toDouble()) "%.0f".format(price) else "%.2f".format(price)

/** 货币代码 → 符号映射 */
fun formatCurrencySymbol(currency: String): String = when (currency) {
    "CNY" -> "¥"
    "USD" -> "$"
    "EUR" -> "€"
    "GBP" -> "£"
    else -> currency
}

/** 组合显示：货币符号 + 价格 */
fun formatPriceWithSymbol(price: Double, currency: String): String =
    "${formatCurrencySymbol(currency)}${formatPriceValue(price)}"
