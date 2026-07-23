package com.tang.prm.domain.model

/**
 * 首页轨道模块显示模式。
 *
 * C-3 修复：原定义在 [com.tang.prm.domain.repository.SettingsRepository] 文件内，
 * 被 4 个 feature 文件从 repository 包导入，让人误以为是 repository 概念。
 * 现迁移到 model 包，与其他领域类型一致。
 */
enum class HomeOrbitalMode { ORBITAL, FORCE_GRAPH }
