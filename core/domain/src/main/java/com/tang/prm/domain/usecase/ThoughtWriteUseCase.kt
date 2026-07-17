package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.Thought
import com.tang.prm.domain.repository.ThoughtRepository
import javax.inject.Inject

/**
 * A-1 修复：ThoughtsViewModel 写操作下沉。
 *
 * 将原 ViewModel 直接持有的 ThoughtRepository 写调用（insert/update/delete/toggleDone）
 * 统一封装到 UseCase，与读路径的 ThoughtListUseCase 抽象对齐。
 *
 * 时间戳设置（updatedAt = System.currentTimeMillis()）和状态翻转（isDone = !isDone）
 * 等业务逻辑统一在 UseCase 内，避免调用方重复实现，并为后续扩展写入前置校验、日志、
 * 缓存失效等逻辑提供单一入口。
 */
class ThoughtWriteUseCase @Inject constructor(
    private val thoughtRepository: ThoughtRepository
) {
    suspend fun insert(thought: Thought) = thoughtRepository.insertThought(thought)

    suspend fun update(thought: Thought) =
        thoughtRepository.updateThought(thought.copy(updatedAt = System.currentTimeMillis()))

    suspend fun delete(id: Long) = thoughtRepository.deleteThought(id)

    suspend fun toggleDone(thought: Thought) =
        thoughtRepository.updateThought(
            thought.copy(isDone = !thought.isDone, updatedAt = System.currentTimeMillis())
        )
}
