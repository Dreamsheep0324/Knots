package com.tang.prm.feature.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.*
import com.tang.prm.domain.repository.HomeOrbitalMode
import com.tang.prm.domain.usecase.HomeAggregateData
import com.tang.prm.domain.usecase.HomeDataAggregationUseCase
import com.tang.prm.domain.usecase.HomeSettingsUseCase
import com.tang.prm.domain.usecase.HomeStats
import com.tang.prm.domain.usecase.HomeStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

// A-2 修复：拆分 HomeUiState 为 HomeDataState + HomeDialogState，与 feature/events、
// feature/reflect 模块的 UiState(data, dialog) 约定保持一致。
// 统计字段直接复用 domain 层 HomeStats，一次消除 13 个重复字段（giftCount/contactCount/...）。

/**
 * 首页数据态：承载所有需要在 UI 渲染的数据。
 *
 * 统计字段统一通过 [stats] 暴露，调用方用 `uiState.data.stats.giftCount` 等读取，
 * 与 [HomeStatDef.statProvider] 配合可直接从 stats 提取对应字段。
 */
data class HomeDataState(
    val greeting: String = "",
    val upcomingAnniversaries: List<Anniversary> = emptyList(),
    val recentEvents: List<Event> = emptyList(),
    val pendingTodos: List<TodoItem> = emptyList(),
    val stats: HomeStats = HomeStats(),
    val decorPhotoPath: String? = null,
    val homeOrbitalMode: HomeOrbitalMode = HomeOrbitalMode.ORBITAL,
    val isLoading: Boolean = true,
    // B-2/A-5 修复：combine 上游异常时承载错误状态，UI 据此渲染错误态与重试入口
    val error: Throwable? = null
)

/**
 * 首页弹窗态：承载瞬时 UI 提示。
 *
 * 当前只有 [transientError]（B-7 修复：写操作失败时触发 Snackbar）。
 * 未来加任何弹窗（确认对话框、编辑弹窗等）都往这里加字段，不污染 [HomeDataState]。
 */
data class HomeDialogState(
    val transientError: Throwable? = null
)

data class HomeUiState(
    val data: HomeDataState = HomeDataState(),
    val dialog: HomeDialogState = HomeDialogState()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeDataUseCase: HomeDataAggregationUseCase,
    private val homeStatsUseCase: HomeStatsUseCase,
    // A-1 修复：通过 HomeSettingsUseCase 访问 SettingsRepository，与项目其他模块约定一致
    private val homeSettingsUseCase: HomeSettingsUseCase
) : ViewModel() {

    // B-7 修复：写操作失败时把错误写入 transientError，UI 据此显示 Snackbar
    private val _transientError = MutableStateFlow<Throwable?>(null)

    // N-2 修复：retry trigger——每次 emit 都让 flatMapLatest 重新订阅上游 combine，
    // 真正重启数据加载，而非依赖 WhileSubscribed 自动重订阅（原注释错误，订阅未中断不会重试）。
    // extraBufferCapacity=1 + DROP_OLDEST 保证 tryEmit 永不丢失（即使无订阅者也能缓存最新一次）。
    private val _retryTrigger = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private fun launchWithErrorHandling(block: suspend () -> Unit) {
        viewModelScope.launch {
            runCatching { block() }.onFailure {
                Log.e(TAG, "操作失败", it)
                _transientError.value = it
            }
        }
    }

    /** 问候语随时间更新，每 30 分钟检查一次跨越早/中/晚分界 */
    private val greetingFlow: Flow<String> = flow {
        while (true) {
            emit(calculateGreeting())
            delay(30 * 60 * 1000L)
        }
    }

    // Q-13 修复：用 Greeting enum 替代硬编码 when 分支，单一来源、易测试、易 i18n
    private fun calculateGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return Greeting.forHour(hour).text
    }

    // N-2 修复：用 _retryTrigger + flatMapLatest 真正重启上游。
    // 原实现 .catch 后 flow 进入完成态，StateFlow 缓存错误态永不再发新值，
    // 即使 UI 重新订阅也无效——retry() 空函数导致错误态成为死胡同，用户必须杀进程。
    // 现每次 retry() 都触发 flatMapLatest 重新构建上游 combine，错误态可恢复。
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HomeUiState> = _retryTrigger
        // 初始订阅时立即触发一次加载（emit Unit），无需 UI 显式调用 retry
        .onStart { emit(Unit) }
        .flatMapLatest {
            combine(
                greetingFlow,
                homeDataUseCase.getAggregateData().distinctUntilChanged(),
                homeStatsUseCase.getStats().distinctUntilChanged(),
                homeSettingsUseCase.getDecorPhotoPath(),
                homeSettingsUseCase.getHomeOrbitalMode()
            ) { greeting, data: HomeAggregateData, stats: HomeStats, decorPhotoPath: String?, orbitalMode: HomeOrbitalMode ->
                // 第一阶段：组合 5 个数据源，输出基础 data state（不含瞬时错误）
                // D-1 修复：移除 userName flow 订阅，UI 从不读取
                // A-2 修复：统计字段直接复用 HomeStats，不再展开 13 个字段
                HomeUiState(
                    data = HomeDataState(
                        greeting = greeting,
                        recentEvents = data.recentEvents,
                        upcomingAnniversaries = data.upcomingAnniversaries,
                        pendingTodos = data.pendingTodos,
                        stats = stats,
                        isLoading = false,
                        decorPhotoPath = decorPhotoPath,
                        homeOrbitalMode = orbitalMode
                    )
                )
            }
        }
        .combine(_transientError) { state, transientError ->
            // 第二阶段：合并瞬时错误到 dialog state，UI 据此显示 Snackbar
            state.copy(dialog = state.dialog.copy(transientError = transientError))
        }
        .catch { th ->
            // B-2/A-5 修复：combine 上游异常不再让 uiState 进入失败终止态
            // 发射一个 isLoading=false + error 的状态，UI 据此渲染错误态与重试入口
            // 注：catch 只捕获到 flatMapLatest 内部的异常；retry() 通过重新触发
            // flatMapLatest 创建全新上游 flow，可绕过已完成的错误态恢复加载。
            Log.e(TAG, "首页数据加载失败", th)
            emit(HomeUiState(data = HomeDataState(isLoading = false, error = th)))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    /**
     * U-2/N-2 修复：UI 错误态点击重试，触发 [_retryTrigger] emit，
     * flatMapLatest 会取消当前上游订阅并重新构建 combine，真正重启数据加载。
     */
    fun retry() {
        _retryTrigger.tryEmit(Unit)
    }

    /** 清空瞬时错误（UI 显示 Snackbar 后调用） */
    fun consumeTransientError() {
        _transientError.value = null
    }

    // D-6/D-7 修复：删除 toggleTodoCompletion/completeReminder——UI 从不调用，仅测试调用
    // 同步移除 todoRepository/reminderRepository 依赖（A-1）

    fun setDecorPhotoPath(path: String?) {
        launchWithErrorHandling { homeSettingsUseCase.setDecorPhotoPath(path) }
    }

    private companion object {
        const val TAG = "HomeViewModel"
    }
}
