package com.example.eventlyapp.features.news.data.cache

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [NewsCacheEntity::class],
    version = 2,
    exportSchema = false
)
abstract class NewsCacheDatabase : RoomDatabase() {
    abstract fun newsCacheDao(): NewsCacheDao
}
