package com.example.eventlyapp.features.app.data

import com.example.eventlyapp.core.id.Id
import com.example.eventlyapp.features.app.data.local.TaskWithSubTasks
import com.example.eventlyapp.features.tasks.domain.model.SubTaskData
import com.example.eventlyapp.features.tasks.domain.model.TaskData
import com.example.eventlyapp.features.tasks.domain.model.TaskPriority
import org.junit.Assert.assertEquals
import org.junit.Test

class PlannerLocalMapperTest {
    @Test
    fun taskRoundTrip_keepsPlannerFieldsAndSubtaskOrder() {
        val task = TaskData(
            id = Id("task-1"),
            title = "Сдать проект",
            description = "Проверить все экраны",
            priority = TaskPriority.HIGH,
            isFlagged = true,
            deadline = "20.06.2026 18:00",
            isDone = false,
            subtasks = listOf(
                SubTaskData(Id("sub-1"), "Тесты", false),
                SubTaskData(Id("sub-2"), "Сборка", false)
            )
        )

        val restored = TaskWithSubTasks(
            task = task.toTaskEntity(position = 4),
            subtasks = task.toSubTaskEntities().reversed()
        ).toTaskData()

        assertEquals(task, restored)
    }
}
