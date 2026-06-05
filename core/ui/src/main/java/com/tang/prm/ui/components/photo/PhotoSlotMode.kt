package com.tang.prm.ui.components.photo

/**
 * 图片展示模式
 *
 * 不同场景使用不同的展示风格：
 * - AVATAR: 圆形头像，100dp，用于联系人头像
 * - THUMBNAIL: 圆角矩形缩略图，80dp，用于礼物/通用照片
 * - POLAROID: 宝丽来风格，80dp，用于事件照片
 */
enum class PhotoSlotMode {
    AVATAR,
    THUMBNAIL,
    POLAROID
}
