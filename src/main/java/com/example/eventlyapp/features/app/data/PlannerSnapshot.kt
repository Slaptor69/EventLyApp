package com.example.eventlyapp.features.app.data

import com.example.eventlyapp.features.notes.domain.model.NoteData
import com.example.eventlyapp.features.tasks.domain.model.TaskData

data class PlannerSnapshot(
    val tasks: List<TaskData>,
    val notes: List<NoteData>
)
