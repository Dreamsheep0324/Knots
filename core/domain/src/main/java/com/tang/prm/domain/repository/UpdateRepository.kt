package com.tang.prm.domain.repository

import com.tang.prm.domain.model.UpdateResult

/**
 * 应用更新检查仓库 — 负责查询 GitHub Release 最新版本。
 */
interface UpdateRepository {
    suspend fun checkForUpdate(currentVersion: String): UpdateResult
}
