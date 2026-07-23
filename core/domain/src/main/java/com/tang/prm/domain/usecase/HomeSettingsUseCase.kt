package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.HomeOrbitalMode
import com.tang.prm.domain.repository.UiPreferencesSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

/**
 * 首页相关设置的下沉封装（A-1 修复）。
 *
 * 设计目的：
 * - 让 [HomeViewModel][com.tang.prm.feature.home.HomeViewModel] 不再直接依赖
 *   [com.tang.prm.domain.repository.SettingsRepository]，与 feature/events、feature/reflect 等模块的
 *   "ViewModel 通过 UseCase 访问 Repository" 约定保持一致。
 * - 仅暴露首页真正需要的设置项（装饰照片路径、轨道模块模式），屏蔽 SettingsRepository 中
 *   主题、AI、备份等无关接口，缩小 ViewModel 的可见面。
 *
 * R-1 修复：依赖由 [com.tang.prm.domain.repository.SettingsRepository] 收窄为
 * [UiPreferencesSettings]，彻底断开首页与 AI 密钥等敏感配置的依赖关系。
 */
class HomeSettingsUseCase @Inject constructor(
    private val uiPreferences: UiPreferencesSettings
) {
    /** 首页日记装饰卡片照片路径，distinctUntilChanged 避免相同值重复触发下游重组。 */
    fun getDecorPhotoPath(): Flow<String?> =
        uiPreferences.homeDecorPhotoPath.distinctUntilChanged()

    /** 更新装饰照片路径。 */
    suspend fun setDecorPhotoPath(path: String?) =
        uiPreferences.setHomeDecorPhotoPath(path)

    /** 首页轨道模块显示模式（轨道罗盘 / 力导向图）。 */
    fun getHomeOrbitalMode(): Flow<HomeOrbitalMode> =
        uiPreferences.homeOrbitalMode.distinctUntilChanged()
}
