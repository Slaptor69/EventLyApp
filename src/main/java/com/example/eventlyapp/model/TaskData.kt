package com.example.eventlyapp.model

import com.example.eventlyapp.id.HasId
import com.example.eventlyapp.id.Id

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
