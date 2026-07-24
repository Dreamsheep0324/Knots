package com.tang.prm.ui.theme

/**
 * 通用图标注册表，消除 XxxIconDef + xxxIconMap + getXxxIcon 的三文件重复模式。
 *
 * @param defs 图标定义列表
 * @param keySelector 从定义提取键的函数
 */
class IconRegistry<T : Any>(
    defs: List<T>,
    private val keySelector: (T) -> String
) {
    private val map: Map<String, T> = defs.associateBy(keySelector)
    private val list: List<T> = defs

    fun find(key: String?): T? = key?.let { map[it] }
    fun all(): List<T> = list
}
