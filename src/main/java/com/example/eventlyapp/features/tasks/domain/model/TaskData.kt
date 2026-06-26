package com.example.eventlyapp.features.tasks.domain.model

import com.example.eventlyapp.core.id.HasId
import com.example.eventlyapp.core.id.Id

data class TaskData(
    override val id: Id,
    val title: String,
    val description: String?,
    val priority: TaskPriority,
    val isFlagged: Boolean,
    val deadline: String?,
    val isDone: Boolean,
    val subtasks: List<SubTaskData>
) : HasId
