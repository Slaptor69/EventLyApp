package com.example.eventlyapp.features.app.domain

import com.example.eventlyapp.core.id.Id
import com.example.eventlyapp.features.app.data.PlannerSnapshot
import com.example.eventlyapp.features.notes.domain.model.NoteData
import com.example.eventlyapp.features.tasks.domain.model.SubTaskData
import com.example.eventlyapp.features.tasks.domain.model.TaskData
import com.example.eventlyapp.features.tasks.domain.model.TaskPriority
import com.example.eventlyapp.features.tasks.domain.model.TaskSortMode

sealed interface PlannerMsg {
    data object ScreenStarted : PlannerMsg
    data class SnapshotLoaded(val snapshot: PlannerSnapshot) : PlannerMsg

    data class SetTaskSortMode(val sortMode: TaskSortMode) : PlannerMsg
    data class SetShowFlaggedOnly(val enabled: Boolean) : PlannerMsg

    data class AddTaskRequested(
        val title: String,
        val description: String,
        val priority: TaskPriority,
        val isFlagged: Boolean,
        val deadline: String?,
        val subtasks: List<SubTaskData>
    ) : PlannerMsg

    data class TaskIdGenerated(
        val id: Id,
        val title: String,
        val description: String,
        val priority: TaskPriority,
        val isFlagged: Boolean,
        val deadline: String?,
        val subtasks: List<SubTaskData>
    ) : PlannerMsg

    data class UpdateTaskRequested(
        val task: TaskData,
        val title: String,
        val description: String,
        val priority: TaskPriority,
        val isFlagged: Boolean,
        val deadline: String?,
        val subtasks: List<SubTaskData>
    ) : PlannerMsg

    data class DeleteTaskRequested(val task: TaskData) : PlannerMsg
    data class CompleteTaskRequested(val task: TaskData) : PlannerMsg
    data class SetTaskDone(val task: TaskData, val isDone: Boolean) : PlannerMsg
    data class SetSubTaskDone(val task: TaskData, val subTask: SubTaskData, val isDone: Boolean) : PlannerMsg

    data class AddNoteRequested(
        val title: String,
        val text: String
    ) : PlannerMsg

    data class NoteIdGenerated(
        val id: Id,
        val title: String,
        val text: String
    ) : PlannerMsg

    data class UpdateNoteRequested(
        val note: NoteData,
        val title: String,
        val text: String
    ) : PlannerMsg

    data class DeleteNoteRequested(val note: NoteData) : PlannerMsg
}
