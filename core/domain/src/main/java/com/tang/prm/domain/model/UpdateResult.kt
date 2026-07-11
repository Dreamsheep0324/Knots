package com.tang.prm.domain.model

/**
 * 应用更新检查结果。
 */
sealed class UpdateResult {
    data class HasUpdate(val latestVersion: String, val releaseUrl: String) : UpdateResult()
    data object NoUpdate : UpdateResult()
    data class Error(val message: String) : UpdateResult()
}
