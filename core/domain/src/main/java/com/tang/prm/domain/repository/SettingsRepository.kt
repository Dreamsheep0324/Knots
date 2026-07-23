package com.tang.prm.domain.repository

import com.tang.prm.domain.model.HomeOrbitalMode
import com.tang.prm.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

/**
 * 主题设置（R-1 ISP 拆分）。
 *
 * 仅含主题模式相关配置，避免依赖无关的 AI 密钥等敏感配置。
 */
interface ThemeSettings {
    val themeMode: Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)
}

/**
 * 用户资料设置（R-1 ISP 拆分）。
 */
interface UserProfileSettings {
    val userName: Flow<String>
    suspend fun setUserName(name: String)
    val userSignature: Flow<String>
    suspend fun setUserSignature(signature: String)
}

/**
 * AI 配置设置（R-1 ISP 拆分）。
 *
 * 含 API 密钥等敏感信息，仅 AI 相关 ViewModel 应依赖此接口，
 * HomeViewModel 等无关模块不应被迫依赖含密钥的完整接口。
 */
interface AiSettings {
    val aiApiKey: Flow<String>
    suspend fun setAiApiKey(key: String)
    val aiBaseUrl: Flow<String>
    suspend fun setAiBaseUrl(url: String)
    val aiModel: Flow<String>
    suspend fun setAiModel(model: String)
    val aiGender: Flow<String>
    suspend fun setAiGender(gender: String)
    val aiBirthDate: Flow<String>
    suspend fun setAiBirthDate(date: String)
}

/**
 * 备份设置（R-1 ISP 拆分）。
 *
 * M-5 修复：风格统一为 `val`（与其余 4 个子接口一致），去掉 `fun get` 前缀。
 */
interface BackupSettings {
    val autoBackupEnabled: Flow<Boolean>
    suspend fun setAutoBackupEnabled(enabled: Boolean)
}

/**
 * UI 偏好设置（R-1 ISP 拆分）。
 *
 * HomeViewModel 只需依赖此接口即可获取 home 相关偏好，
 * 无需依赖含 AI 密钥的完整 SettingsRepository。
 */
interface UiPreferencesSettings {
    val tabletModeEnabled: Flow<Boolean>
    suspend fun setTabletModeEnabled(enabled: Boolean)

    /** 首页日记装饰卡片照片路径 */
    val homeDecorPhotoPath: Flow<String?>
    suspend fun setHomeDecorPhotoPath(path: String?)

    /** 首页轨道模块显示模式：轨道罗盘 / 力导向图 */
    val homeOrbitalMode: Flow<HomeOrbitalMode>
    suspend fun setHomeOrbitalMode(mode: HomeOrbitalMode)
}

/**
 * 设置聚合接口（R-1 ISP 拆分）。
 *
 * 继承 5 个子接口，data 层 [SettingsRepositoryImpl] 实现此聚合接口，
 * Hilt 绑定不变。ViewModel 可渐进迁移为依赖所需子接口，
 * 如 HomeViewModel 改依赖 [UiPreferencesSettings] 以收窄 AI 密钥暴露面。
 */
interface SettingsRepository : ThemeSettings, UserProfileSettings, AiSettings,
    BackupSettings, UiPreferencesSettings
