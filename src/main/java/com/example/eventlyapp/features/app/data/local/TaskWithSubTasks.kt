package com.example.eventlyapp.features.app.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class TaskWithSubTasks(
    @Embedded
    val task: PlannerTaskEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "taskId"
    )
    val subtasks: List<PlannerSubTaskEntity>
)
