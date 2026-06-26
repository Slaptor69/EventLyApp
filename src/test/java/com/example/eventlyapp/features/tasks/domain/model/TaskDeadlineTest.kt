package com.example.eventlyapp.features.tasks.domain.model

import com.example.eventlyapp.core.id.Id
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskDeadlineTest {
    @Test
    fun deadlineDateKey_returnsDayKeyForValidDeadline() {
        val task = taskWithDeadline("18.06.2026 10:30")

        assertEquals("2026-06-18", task.deadlineDateKey())
    }

    @Test
    fun deadlineEpochMillis_returnsNullForInvalidDeadlineWithoutCrash() {
        val task = taskWithDeadline("сломанная дата")

        assertNull(task.deadlineEpochMillis())
        assertFalse(task.isDeadlineOverdue())
    }

    @Test
    fun isDeadlineOverdue_returnsFalseWhenDeadlineIsInFuture() {
        val task = taskWithDeadline("18.06.2026 10:30")
        val deadlineMillis = task.deadlineEpochMillis()

        assertTrue(deadlineMillis != null)
        assertFalse(task.isDeadlineOverdue(nowMillis = deadlineMillis!! - 1))
    }

    @Test
    fun dateKey_padsMonthAndDay() {
        assertEquals("2026-03-07", dateKey(year = 2026, month = 3, day = 7))
    }

    private fun taskWithDeadline(deadline: String): TaskData {
        return TaskData(
            id = Id("task"),
            title = "Задача",
            description = null,
            priority = TaskPriority.MEDIUM,
            isFlagged = false,
            deadline = deadline,
            isDone = false,
            subtasks = emptyList()
        )
    }
}
