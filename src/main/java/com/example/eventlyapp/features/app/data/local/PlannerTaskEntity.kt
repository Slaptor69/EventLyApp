package com.example.eventlyapp.features.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class PlannerTaskEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String?,
    val priority: String,
    val isFlagged: Boolean,
    val deadline: String?,
    val isDone: Boolean,
    val position: Int
)
