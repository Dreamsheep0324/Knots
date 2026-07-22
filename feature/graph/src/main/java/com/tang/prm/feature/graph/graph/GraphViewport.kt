package com.tang.prm.feature.graph.graph

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.max
import kotlin.math.min

/**
 * 图谱视口：管理世界坐标 ↔ 屏幕坐标的变换。
 *
 * 概念：
 * - 世界坐标（world space）：节点在物理模拟中的绝对位置，无上限
 * - 屏幕坐标（screen space）：Canvas 绘制时的像素坐标，受 [scale] 与 [offset] 影响
 *
 * 变换公式：
 * - screenX = (worldX - offset.x) × scale
 * - screenY = (worldY - offset.y) × scale
 * - worldX = screenX / scale + offset.x
 *
 * 字段使用 [mutableStateOf]，任何变化都自动触发 Compose 重组，
 * 让依赖 viewport 的 NodeLayer / BackgroundCanvas 实时重绘。
 *
 * 约束：
 * - [scale] 限制在 [minScale, maxScale]，防止过度缩放
 * - [offset] 无硬限制，但 [clampToContent] 可在内容范围已知时收敛视口
 */
class GraphViewport(
    val minScale: Float = 0.3f,
    val maxScale: Float = 4f
) {
    /** 当前缩放系数（1 = 100%）。 */
    var scale: Float by mutableStateOf(1f)
        private set

    /** 视口偏移（世界坐标系下，视口左上角对应的世界坐标）。 */
    var offsetX: Float by mutableStateOf(0f)
        private set
    var offsetY: Float by mutableStateOf(0f)
        private set

    /**
     * 平移视口（拖拽手势）。
     *
     * 采用"拖动内容"模式：内容跟随手指方向移动（与 Google Maps / Apple Maps 一致）。
     * - 手指向右滑（dx > 0）→ 内容向右移 → offsetX 减小 → screenX 增大
     * - 手指向左滑（dx < 0）→ 内容向左移 → offsetX 增大 → screenX 减小
     *
     * @param dx 屏幕坐标系的水平位移（像素）
     * @param dy 屏幕坐标系的垂直位移（像素）
     */
    fun pan(dx: Float, dy: Float) {
        offsetX -= dx / scale
        offsetY -= dy / scale
    }

    /**
     * 以指定屏幕坐标为锚点缩放（双指捏合手势）。
     * 锚点保持视觉位置不变：缩放前后的 (screenX, screenY) 对应同一世界点。
     */
    fun zoom(focusX: Float, focusY: Float, newScale: Float) {
        val clamped = newScale.coerceIn(minScale, maxScale)
        if (clamped == scale) return
        val worldX = screenToWorldX(focusX)
        val worldY = screenToWorldY(focusY)
        scale = clamped
        offsetX = worldX - focusX / scale
        offsetY = worldY - focusY / scale
    }

    /** 重置到默认视口（scale=1, offset=0）。 */
    fun reset() {
        scale = 1f
        offsetX = 0f
        offsetY = 0f
    }

    /**
     * 居中到指定世界坐标，并以指定缩放展示。
     */
    fun centerOn(
        viewWidth: Float,
        viewHeight: Float,
        targetWorldX: Float,
        targetWorldY: Float,
        targetScale: Float = 1f
    ) {
        scale = targetScale.coerceIn(minScale, maxScale)
        offsetX = targetWorldX - viewWidth / 2f / scale
        offsetY = targetWorldY - viewHeight / 2f / scale
    }

    // ---------- 坐标变换 ----------

    fun worldToScreenX(worldX: Float): Float = (worldX - offsetX) * scale
    fun worldToScreenY(worldY: Float): Float = (worldY - offsetY) * scale

    fun screenToWorldX(screenX: Float): Float = screenX / scale + offsetX
    fun screenToWorldY(screenY: Float): Float = screenY / scale + offsetY

    fun isVisible(
        worldX: Float,
        worldY: Float,
        viewWidth: Float,
        viewHeight: Float,
        margin: Float = 50f
    ): Boolean {
        val sx = worldToScreenX(worldX)
        val sy = worldToScreenY(worldY)
        return sx >= -margin && sx <= viewWidth + margin &&
            sy >= -margin && sy <= viewHeight + margin
    }

    @Suppress("LongParameterList")
    fun clampToContent(
        contentMinX: Float,
        contentMinY: Float,
        contentMaxX: Float,
        contentMaxY: Float,
        viewWidth: Float,
        viewHeight: Float,
        padding: Float = 100f
    ) {
        val contentWidth = (contentMaxX - contentMinX) * scale
        val contentHeight = (contentMaxY - contentMinY) * scale
        val minX = if (contentWidth < viewWidth) {
            contentMaxX - viewWidth / scale + padding / scale
        } else {
            contentMaxX - viewWidth / scale + padding / scale
        }
        val maxX = if (contentWidth < viewWidth) {
            contentMinX - padding / scale
        } else {
            contentMinX - padding / scale
        }
        val minY = if (contentHeight < viewHeight) {
            contentMaxY - viewHeight / scale + padding / scale
        } else {
            contentMaxY - viewHeight / scale + padding / scale
        }
        val maxY = if (contentHeight < viewHeight) {
            contentMinY - padding / scale
        } else {
            contentMinY - padding / scale
        }
        offsetX = if (minX < maxX) offsetX.coerceIn(minX, maxX) else min(minX, maxX)
        offsetY = if (minY < maxY) offsetY.coerceIn(minY, maxY) else min(minY, maxY)
    }
}
