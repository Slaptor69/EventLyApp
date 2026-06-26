package com.example.eventlyapp.features.app.domain

import com.example.eventlyapp.core.id.Id
import com.example.eventlyapp.features.tasks.domain.model.TaskData
import com.example.eventlyapp.features.tasks.domain.model.TaskPriority
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppReducerTest {
    private val reducer = AppReducer()

    @Test
    fun editTaskClicked_opensEditTaskScreenAndStoresTask() {
        val task = TaskData(
            id = Id("task-1"),
            title = "Задача",
            description = null,
            priority = TaskPriority.MEDIUM,
            isFlagged = false,
            deadline = null,
            isDone = false,
            subtasks = emptyList()
        )

        val update = reducer.update(AppState(), AppMsg.EditTaskClicked(task))

        assertEquals(AppScreen.EDIT_TASK, update.state.currentScreen)
        assertEquals(task, update.state.editingTask)
    }

    @Test
    fun mainScreenRequested_clearsEditingStateButKeepsSelectedTab() {
        val state = AppState(
            currentScreen = AppScreen.EDIT_TASK,
            currentMainTab = MainTab.TASKS,
            editingTask = TaskData(
                id = Id("task-1"),
                title = "Задача",
                description = null,
                priority = TaskPriority.MEDIUM,
                isFlagged = false,
                deadline = null,
                isDone = false,
                subtasks = emptyList()
            )
        )

        val update = reducer.update(state, AppMsg.MainScreenRequested)

        assertEquals(AppScreen.MAIN_TABS, update.state.currentScreen)
        assertEquals(MainTab.TASKS, update.state.currentMainTab)
        assertNull(update.state.editingTask)
    }
}
