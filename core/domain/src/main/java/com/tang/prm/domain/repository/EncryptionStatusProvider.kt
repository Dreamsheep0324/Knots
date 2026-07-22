package com.tang.prm.domain.repository

import kotlinx.coroutines.flow.StateFlow

/**
 * A-2 修复：加密存储状态读取接口（domain 层接口，data 层实现）。
 *
 * 原 [com.tang.prm.domain.util.EncryptionStatus] 是 domain 层的 `object` 全局可变单例，
 * 违反「domain 层不应持有可变状态」原则，且测试间状态泄露难以隔离。
 *
 * 现迁移为：domain 层定义只读接口，data 层 [com.tang.prm.data.repository.EncryptionStatusProviderImpl]
 * 持有 StateFlow 并通过 Hilt 注入。UI（如 SettingsViewModel）订阅 [isDegraded] 响应式获取状态，
 * data 层组件（DatabaseModule/WebDavClient）调用 [markDegraded] 标记降级。
 *
 * 测试可注入 fake 实现或直接构造 `StateFlow<Boolean>` 隔离状态。
 */
interface EncryptionStatusProvider {
    /** 加密存储是否处于降级模式（true 表示敏感数据未加密，UI 应提示用户） */
    val isDegraded: StateFlow<Boolean>

    /** 标记加密降级（EncryptedSharedPreferences 初始化失败或 trustAllCertificates 启用时调用） */
    fun markDegraded()
}
