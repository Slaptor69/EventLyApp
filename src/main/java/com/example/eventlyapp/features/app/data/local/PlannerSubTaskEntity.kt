package com.example.eventlyapp.features.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "subtasks",
    foreignKeys = [
        ForeignKey(
            entity = PlannerTaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("taskId")]
)
data class PlannerSubTaskEntity(
    @PrimaryKey
    val id: String,
    val taskId: String,
    val title: String,
    val isDone: Boolean,
    val position: Int
)
