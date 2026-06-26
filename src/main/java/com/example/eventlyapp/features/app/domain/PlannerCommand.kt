package com.example.eventlyapp.features.app.domain

import com.example.eventlyapp.features.tasks.domain.model.SubTaskData
import com.example.eventlyapp.features.notes.domain.model.NoteData
import com.example.eventlyapp.features.tasks.domain.model.TaskData
import com.example.eventlyapp.features.tasks.domain.model.TaskPriority

sealed interface PlannerCommand {
    data object LoadPlannerSnapshot : PlannerCommand

    data class GenerateTaskId(
        val title: String,
        val description: String,
        val priority: TaskPriority,
        val isFlagged: Boolean,
        val deadline: String?,
        val subtasks: List<SubTaskData>
    ) : PlannerCommand

    data class GenerateNoteId(
        val title: String,
        val text: String
    ) : PlannerCommand

    data class SaveTask(val task: TaskData) : PlannerCommand
    data class DeleteTask(val task: TaskData) : PlannerCommand
    data class SaveNote(val note: NoteData) : PlannerCommand
    data class DeleteNote(val note: NoteData) : PlannerCommand
}
