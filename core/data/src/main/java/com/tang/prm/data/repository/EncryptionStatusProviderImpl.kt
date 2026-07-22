package com.tang.prm.data.repository

import com.tang.prm.domain.repository.EncryptionStatusProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A-2 修复：[EncryptionStatusProvider] 的 data 层实现。
 *
 * 替代原 `com.tang.prm.domain.util.EncryptionStatus` 全局可变单例。
 * 通过 Hilt `@Singleton` 保证全应用单一实例，StateFlow 提供响应式订阅。
 */
@Singleton
class EncryptionStatusProviderImpl @Inject constructor() : EncryptionStatusProvider {

    private val _isDegraded = MutableStateFlow(false)
    override val isDegraded: StateFlow<Boolean> = _isDegraded.asStateFlow()

    override fun markDegraded() {
        _isDegraded.value = true
    }
}
