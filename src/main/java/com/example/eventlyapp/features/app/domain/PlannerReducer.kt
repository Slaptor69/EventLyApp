package com.example.eventlyapp.features.app.domain

import com.example.eventlyapp.core.elm.ElmUpdate
import com.example.eventlyapp.core.elm.toElmUpdate
import com.example.eventlyapp.core.id.Id
import com.example.eventlyapp.features.notes.domain.model.NoteData
import com.example.eventlyapp.features.tasks.domain.model.SubTaskData
import com.example.eventlyapp.features.tasks.domain.model.TaskData
import com.example.eventlyapp.features.tasks.domain.model.TaskPriority
import com.example.eventlyapp.features.tasks.domain.model.TaskSortMode
import javax.inject.Inject

class PlannerReducer @Inject constructor() {
    fun update(
        state: PlannerState,
        msg: PlannerMsg
    ): ElmUpdate<PlannerState, PlannerCommand, Nothing> {
        return when (msg) {
            PlannerMsg.ScreenStarted -> state.toElmUpdate(
                commands = listOf(PlannerCommand.LoadPlannerSnapshot)
            )
            is PlannerMsg.SnapshotLoaded -> state.copy(
                tasks = msg.snapshot.tasks,
                notes = msg.snapshot.notes
            ).toElmUpdate()
            is PlannerMsg.SetTaskSortMode -> {
                setTaskSortMode(state, msg.sortMode).toElmUpdate()
            }
            is PlannerMsg.SetShowFlaggedOnly -> {
                setShowFlaggedOnly(state, msg.enabled).toElmUpdate()
            }
            is PlannerMsg.AddTaskRequested -> {
                state.toElmUpdate(
                    commands = listOf(
                        PlannerCommand.GenerateTaskId(
                            title = msg.title,
                            description = msg.description,
                            priority = msg.priority,
                            isFlagged = msg.isFlagged,
                            deadline = msg.deadline,
                            subtasks = msg.subtasks
                        )
                    )
                )
            }
            is PlannerMsg.TaskIdGenerated -> {
                val nextState = addTask(
                    state = state,
                    id = msg.id,
                    title = msg.title,
                    description = msg.description,
                    priority = msg.priority,
                    isFlagged = msg.isFlagged,
                    deadline = msg.deadline,
                    subtasks = msg.subtasks
                )
                nextState.toElmUpdate(
                    commands = listOf(PlannerCommand.SaveTask(nextState.tasks.last()))
                )
            }
            is PlannerMsg.UpdateTaskRequested -> {
                val nextState = updateTask(
                    state = state,
                    task = msg.task,
                    title = msg.title,
                    description = msg.description,
                    priority = msg.priority,
                    isFlagged = msg.isFlagged,
                    deadline = msg.deadline,
                    subtasks = msg.subtasks
                )
                val updatedTask = nextState.tasks.firstOrNull { task -> task.id == msg.task.id }
                nextState.toElmUpdate(
                    commands = updatedTask?.let { task -> listOf(PlannerCommand.SaveTask(task)) }.orEmpty()
                )
            }
            is PlannerMsg.DeleteTaskRequested -> deleteTask(state, msg.task).toElmUpdate(
                commands = listOf(PlannerCommand.DeleteTask(msg.task))
            )
            is PlannerMsg.CompleteTaskRequested -> deleteTask(state, msg.task).toElmUpdate(
                commands = listOf(PlannerCommand.DeleteTask(msg.task))
            )
            is PlannerMsg.SetTaskDone -> {
                val nextState = setTaskDone(state, msg.task, msg.isDone)
                nextState.toElmUpdate(
                    commands = if (msg.isDone) listOf(PlannerCommand.DeleteTask(msg.task)) else emptyList()
                )
            }
            is PlannerMsg.SetSubTaskDone -> {
                val nextState = setSubTaskDone(
                    state = state,
                    task = msg.task,
                    subTask = msg.subTask,
                    isDone = msg.isDone
                )
                val updatedTask = nextState.tasks.firstOrNull { task -> task.id == msg.task.id }
                nextState.toElmUpdate(
                    commands = updatedTask?.let { task -> listOf(PlannerCommand.SaveTask(task)) }
                        ?: listOf(PlannerCommand.DeleteTask(msg.task))
                )
            }
            is PlannerMsg.AddNoteRequested -> {
                state.toElmUpdate(
                    commands = listOf(
                        PlannerCommand.GenerateNoteId(
                            title = msg.title,
                            text = msg.text
                        )
                    )
                )
            }
            is PlannerMsg.NoteIdGenerated -> {
                val nextState = addNote(
                    state = state,
                    id = msg.id,
                    title = msg.title,
                    text = msg.text
                )
                nextState.toElmUpdate(
                    commands = listOf(PlannerCommand.SaveNote(nextState.notes.last()))
                )
            }
            is PlannerMsg.UpdateNoteRequested -> {
                val nextState = updateNote(
                    state = state,
                    note = msg.note,
                    title = msg.title,
                    text = msg.text
                )
                val updatedNote = nextState.notes.firstOrNull { note -> note.id == msg.note.id }
                nextState.toElmUpdate(
                    commands = updatedNote?.let { note -> listOf(PlannerCommand.SaveNote(note)) }.orEmpty()
                )
            }
            is PlannerMsg.DeleteNoteRequested -> deleteNote(state, msg.note).toElmUpdate(
                commands = listOf(PlannerCommand.DeleteNote(msg.note))
            )
        }
    }

    private fun setTaskSortMode(
        state: PlannerState,
        sortMode: TaskSortMode
    ): PlannerState = state.copy(taskSortMode = sortMode)

    private fun setShowFlaggedOnly(
        state: PlannerState,
        enabled: Boolean
    ): PlannerState = state.copy(showFlaggedOnly = enabled)

    private fun addTask(
        state: PlannerState,
        id: Id,
        title: String,
        description: String,
        priority: TaskPriority,
        isFlagged: Boolean,
        deadline: String?,
        subtasks: List<SubTaskData>
    ): PlannerState {
        val task = TaskData(
            id = id,
            title = title.trim(),
            description = description.trim().takeIf { value -> value.isNotBlank() },
            priority = priority,
            isFlagged = isFlagged,
            deadline = deadline,
            isDone = false,
            subtasks = subtasks
        )

        return state.copy(tasks = state.tasks + task)
    }

    private fun addNote(
        state: PlannerState,
        id: Id,
        title: String,
        text: String
    ): PlannerState {
        val note = NoteData(
            id = id,
            title = title.trim(),
            text = text.trim()
        )

        return state.copy(notes = state.notes + note)
    }

    private fun updateTask(
        state: PlannerState,
        task: TaskData,
        title: String,
        description: String,
        priority: TaskPriority,
        isFlagged: Boolean,
        deadline: String?,
        subtasks: List<SubTaskData>
    ): PlannerState {
        return state.copy(
            tasks = state.tasks.map { current ->
                if (current.id == task.id) {
                    current.copy(
                        title = title.trim(),
                        description = description.trim().takeIf { value -> value.isNotBlank() },
                        priority = priority,
                        isFlagged = isFlagged,
                        deadline = deadline,
                        subtasks = subtasks
                    )
                } else {
                    current
                }
            }
        )
    }

    private fun deleteTask(
        state: PlannerState,
        task: TaskData
    ): PlannerState = state.copy(tasks = state.tasks.filterNot { current -> current.id == task.id })

    private fun updateNote(
        state: PlannerState,
        note: NoteData,
        title: String,
        text: String
    ): PlannerState {
        return state.copy(
            notes = state.notes.map { current ->
                if (current.id == note.id) {
                    current.copy(
                        title = title.trim(),
                        text = text.trim()
                    )
                } else {
                    current
                }
            }
        )
    }

    private fun deleteNote(
        state: PlannerState,
        note: NoteData
    ): PlannerState = state.copy(notes = state.notes.filterNot { current -> current.id == note.id })

    private fun setTaskDone(
        state: PlannerState,
        task: TaskData,
        isDone: Boolean
    ): PlannerState {
        if (isDone) {
            return deleteTask(state, task)
        }

        return state
    }

    private fun setSubTaskDone(
        state: PlannerState,
        task: TaskData,
        subTask: SubTaskData,
        isDone: Boolean
    ): PlannerState {
        return state.copy(
            tasks = state.tasks.map { currentTask ->
                if (currentTask.id != task.id) {
                    currentTask
                } else {
                    if (isDone) {
                        currentTask.copy(
                            subtasks = currentTask.subtasks.filterNot { currentSubTask ->
                                currentSubTask.id == subTask.id
                            }
                        )
                    } else {
                        currentTask.copy(
                            subtasks = currentTask.subtasks.map { currentSubTask ->
                                if (currentSubTask.id == subTask.id) {
                                    currentSubTask.copy(isDone = false)
                                } else {
                                    currentSubTask
                                }
                            }
                        )
                    }
                }
            }
        )
    }
}
