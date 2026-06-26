package com.example.eventlyapp.features.app.domain

import com.example.eventlyapp.core.id.Id
import com.example.eventlyapp.features.notes.domain.model.NoteData
import com.example.eventlyapp.features.tasks.domain.model.SubTaskData
import com.example.eventlyapp.features.tasks.domain.model.TaskPriority
import com.example.eventlyapp.features.tasks.domain.model.TaskSortMode
import com.example.eventlyapp.features.tasks.domain.model.isDeadlineOverdue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlannerReducerTest {
    private val reducer = PlannerReducer()

    @Test
    fun addTaskRequested_returnsGenerateTaskIdCommand() {
        val update = reducer.update(
            state = PlannerState(),
            msg = PlannerMsg.AddTaskRequested(
                title = "Концерт",
                description = "",
                priority = TaskPriority.HIGH,
                isFlagged = true,
                deadline = "20.06.2026 20:00",
                subtasks = emptyList()
            )
        )

        assertEquals(PlannerState(), update.state)
        assertEquals(
            listOf(
                PlannerCommand.GenerateTaskId(
                    title = "Концерт",
                    description = "",
                    priority = TaskPriority.HIGH,
                    isFlagged = true,
                    deadline = "20.06.2026 20:00",
                    subtasks = emptyList()
                )
            ),
            update.commands
        )
    }

    @Test
    fun screenStarted_requestsPlannerSnapshotCommand() {
        val update = reducer.update(PlannerState(), PlannerMsg.ScreenStarted)

        assertEquals(listOf(PlannerCommand.LoadPlannerSnapshot), update.commands)
    }

    @Test
    fun taskIdGenerated_trimsOptionalDescriptionAndKeepsSubtasks() {
        val subTask = SubTaskData(
            id = Id("sub-1"),
            title = "Проверить билеты",
            isDone = false
        )

        val update = reducer.update(
            PlannerState(),
            PlannerMsg.TaskIdGenerated(
                id = Id("task-1"),
                title = "  Концерт  ",
                description = "   ",
                priority = TaskPriority.HIGH,
                isFlagged = true,
                deadline = "20.06.2026 20:00",
                subtasks = listOf(subTask)
            )
        )
        val state = update.state

        val task = state.tasks.single()
        assertEquals("Концерт", task.title)
        assertNull(task.description)
        assertEquals(TaskPriority.HIGH, task.priority)
        assertTrue(task.isFlagged)
        assertEquals(listOf(subTask), task.subtasks)
        assertEquals(listOf(PlannerCommand.SaveTask(task)), update.commands)
    }

    @Test
    fun visibleTasks_sortsByPriorityWhenEnabled() {
        val unsortedState = PlannerState()
            .addTask("low", "B", TaskPriority.LOW)
            .addTask("high", "A", TaskPriority.HIGH)
            .addTask("medium", "C", TaskPriority.MEDIUM)

        val sortedState = reduce(
            unsortedState,
            PlannerMsg.SetTaskSortMode(TaskSortMode.PRIORITY)
        )

        assertEquals(
            listOf("high", "medium", "low"),
            sortedState.visibleTasks.map { task -> task.id.value }
        )
    }

    @Test
    fun visibleTasks_filtersFlaggedAndSortsByDeadline() {
        val state = PlannerState()
            .addTask("late", "Late", TaskPriority.LOW, isFlagged = true, deadline = "21.06.2026 12:00")
            .addTask("early", "Early", TaskPriority.LOW, isFlagged = true, deadline = "19.06.2026 12:00")
            .addTask("plain", "Plain", TaskPriority.HIGH, isFlagged = false, deadline = null)
            .let { current -> reduce(current, PlannerMsg.SetShowFlaggedOnly(true)) }
            .let { current -> reduce(current, PlannerMsg.SetTaskSortMode(TaskSortMode.DEADLINE)) }

        assertEquals(
            listOf("early", "late"),
            state.visibleTasks.map { task -> task.id.value }
        )
    }

    @Test
    fun setSubTaskDone_removesSelectedSubtask() {
        val firstSubTask = SubTaskData(Id("sub-1"), "Первое", false)
        val secondSubTask = SubTaskData(Id("sub-2"), "Второе", false)
        val state = PlannerState().addTask(
            id = "task-1",
            title = "Задача",
            priority = TaskPriority.MEDIUM,
            subtasks = listOf(firstSubTask, secondSubTask)
        )

        val task = state.tasks.single()
        val updatedState = reduce(
            state,
            PlannerMsg.SetSubTaskDone(task, secondSubTask, true)
        )
        val updatedTask = updatedState.tasks.single()

        assertEquals(listOf(firstSubTask), updatedTask.subtasks)
    }

    @Test
    fun deleteTaskRequested_removesTaskAndRequestsPersistedDelete() {
        val state = PlannerState().addTask("task-1", "Задача", TaskPriority.MEDIUM)
        val task = state.tasks.single()

        val update = reducer.update(state, PlannerMsg.DeleteTaskRequested(task))

        assertEquals(emptyList<String>(), update.state.tasks.map { current -> current.id.value })
        assertEquals(listOf(PlannerCommand.DeleteTask(task)), update.commands)
    }

    @Test
    fun setTaskDone_removesOnlySelectedTask() {
        val state = PlannerState()
            .addTask("task-1", "Первая", TaskPriority.MEDIUM)
            .addTask("task-2", "Вторая", TaskPriority.HIGH)

        val updatedState = reduce(
            state,
            PlannerMsg.SetTaskDone(
                task = state.tasks.first(),
                isDone = true
            )
        )

        assertEquals(listOf("task-2"), updatedState.tasks.map { task -> task.id.value })
    }

    @Test
    fun updateNote_trimsTextAndKeepsOtherNotes() {
        val firstNote = NoteData(
            id = Id("note-1"),
            title = "План",
            text = "Старый текст"
        )
        val secondNote = NoteData(
            id = Id("note-2"),
            title = "Идея",
            text = "Не трогать"
        )
        val state = PlannerState(notes = listOf(firstNote, secondNote))

        val updatedState = reduce(
            state,
            PlannerMsg.UpdateNoteRequested(
                note = firstNote,
                title = "  Новый план  ",
                text = "  Новый текст  "
            )
        )

        assertEquals("Новый план", updatedState.notes.first().title)
        assertEquals("Новый текст", updatedState.notes.first().text)
        assertEquals(secondNote, updatedState.notes.last())
    }

    @Test
    fun isDeadlineOverdue_returnsTrueWhenDeadlineIsBeforeNow() {
        val state = PlannerState().addTask(
            id = "task-1",
            title = "Сдать отчет",
            priority = TaskPriority.HIGH,
            deadline = "18.06.2026 10:00"
        )

        assertTrue(state.tasks.single().isDeadlineOverdue(nowMillis = 1781769600000L))
    }

    private fun PlannerState.addTask(
        id: String,
        title: String,
        priority: TaskPriority,
        isFlagged: Boolean = false,
        deadline: String? = null,
        subtasks: List<SubTaskData> = emptyList()
    ): PlannerState {
        return reduce(
            this,
            PlannerMsg.TaskIdGenerated(
                id = Id(id),
                title = title,
                description = "",
                priority = priority,
                isFlagged = isFlagged,
                deadline = deadline,
                subtasks = subtasks
            )
        )
    }

    private fun reduce(
        state: PlannerState,
        msg: PlannerMsg
    ): PlannerState = reducer.update(state, msg).state
}
