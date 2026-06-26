package com.example.eventlyapp.features.news.data.cache

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface NewsCacheDao {
    @Query("SELECT * FROM news_cache ORDER BY position_index ASC")
    suspend fun getArticles(): List<NewsCacheEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<NewsCacheEntity>)

    @Query("DELETE FROM news_cache")
    suspend fun clearAll()

    @Transaction
    suspend fun replaceArticles(articles: List<NewsCacheEntity>) {
        clearAll()
        if (articles.isNotEmpty()) {
            insertArticles(articles)
        }
    }
}
