package com.example.eventlyapp.features.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class PlannerNoteEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val text: String,
    val position: Int
)
