package com.tang.prm.domain.model

enum class GiftType(val key: String, val displayName: String) {
    DIGITAL("DIGITAL", "数码"),
    CLOTHING("CLOTHING", "服饰"),
    FOOD("FOOD", "食品"),
    COSMETICS("COSMETICS", "美妆"),
    BOOKS("BOOKS", "书籍"),
    TOYS("TOYS", "潮玩"),
    TRAVEL("TRAVEL", "旅行"),
    SPORTS("SPORTS", "运动"),
    HOME("HOME", "家居"),
    OTHER("OTHER", "其他")
}
