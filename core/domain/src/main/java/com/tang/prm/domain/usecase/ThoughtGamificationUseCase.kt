package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.Thought
import com.tang.prm.domain.model.ThoughtType
import java.util.Calendar
import javax.inject.Inject

data class GamificationState(
    val totalCount: Int,
    val friendCount: Int,
    val planCount: Int,
    val murmurCount: Int,
    val currentExp: Int,
    val currentLevel: Int,
    val nextLevel: Int,
    val expInLevel: Int,
    val expNeeded: Int,
    val expForNextLevel: Int,
    val expToNextLevel: Int,
    val levelProgress: Float,
    val streak: Int,
    val todoDoneCount: Int,
    val todoTotalCount: Int
)

data class ContactThoughts(
    val contact: Contact,
    val thoughts: List<Thought>,
    val latestThought: Thought?
)

class ThoughtGamificationUseCase @Inject constructor() {

    fun calcLevel(exp: Int): Int {
        var level = 1
        while (expForLevel(level + 1) <= exp) level++
        return level
    }

    fun expForLevel(level: Int): Int {
        if (level <= 1) return 0
        return 15 * level * (level - 1) / 2
    }

    fun calcStreak(thoughts: List<Thought>): Int {
        if (thoughts.isEmpty()) return 0
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val dayMs = 24 * 60 * 60 * 1000L
        val daysWithThoughts = thoughts.map { it.createdAt / dayMs }.toSet()
        var streak = 0
        var checkDay = calendar.timeInMillis / dayMs
        if (daysWithThoughts.contains(checkDay)) {
            streak = 1
            checkDay--
            while (daysWithThoughts.contains(checkDay)) { streak++; checkDay-- }
        }
        return streak
    }

    fun thoughtExp(thought: Thought): Int =
        3 + (if (thought.isDone) 2 else 0) + (if (thought.contactId != null) 1 else 0)

    fun getGamificationState(
        allThoughts: List<Thought>,
        todoThoughts: List<Thought>,
        contactThoughts: List<ContactThoughts>
    ): GamificationState {
        val totalCount = allThoughts.size
        val friendCount = allThoughts.count { it.type == ThoughtType.FRIEND }
        val planCount = allThoughts.count { it.type == ThoughtType.PLAN }
        val murmurCount = allThoughts.count { it.type == ThoughtType.MURMUR }
        val streak = calcStreak(allThoughts)
        val currentExp = allThoughts.size * 3 +
                todoThoughts.count { it.isDone } * 2 +
                contactThoughts.size * 1 +
                streak * 1
        val currentLevel = calcLevel(currentExp)
        val nextLevel = currentLevel + 1
        val expInLevel = currentExp - expForLevel(currentLevel)
        val expNeeded = expForLevel(nextLevel) - expForLevel(currentLevel)
        val levelProgress = if (expNeeded > 0) expInLevel.toFloat() / expNeeded else 1f
        val expForNextLevel = expForLevel(nextLevel)
        val expToNextLevel = expForNextLevel - currentExp

        return GamificationState(
            totalCount = totalCount,
            friendCount = friendCount,
            planCount = planCount,
            murmurCount = murmurCount,
            currentExp = currentExp,
            currentLevel = currentLevel,
            nextLevel = nextLevel,
            expInLevel = expInLevel,
            expNeeded = expNeeded,
            expForNextLevel = expForNextLevel,
            expToNextLevel = expToNextLevel,
            levelProgress = levelProgress,
            streak = streak,
            todoDoneCount = todoThoughts.count { it.isDone },
            todoTotalCount = todoThoughts.size
        )
    }
}
