package com.example.eventlyapp.features.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        PlannerTaskEntity::class,
        PlannerSubTaskEntity::class,
        PlannerNoteEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PlannerDatabase : RoomDatabase() {
    abstract fun plannerDao(): PlannerDao
}
