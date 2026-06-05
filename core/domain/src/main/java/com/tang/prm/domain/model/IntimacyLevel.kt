package com.tang.prm.domain.model

enum class CardRarity(val label: String, val shortLabel: String, val colorValue: Long, val stars: Int) {
    N("初识", "N", 0xFF94A3B8, 1),
    R("泛交", "R", 0xFF3B82F6, 2),
    SR("朋友", "SR", 0xFF8B5CF6, 3),
    SSR("密友", "SSR", 0xFFEF4444, 4),
    UR("至亲", "UR", 0xFFF59E0B, 5)
}

fun getCardRarity(score: Int): CardRarity = when {
    score >= 81 -> CardRarity.UR
    score >= 61 -> CardRarity.SSR
    score >= 41 -> CardRarity.SR
    score >= 21 -> CardRarity.R
    else -> CardRarity.N
}
