package com.example.eventlyapp.features.news.data.cache

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "news_cache")
data class NewsCacheEntity(
    @PrimaryKey
    @ColumnInfo(name = "article_id")
    val articleId: String,
    @ColumnInfo(name = "position_index")
    val position: Int,
    val title: String,
    @ColumnInfo(name = "abstract_text")
    val abstractText: String,
    val source: String,
    @ColumnInfo(name = "published_at")
    val publishedAt: String,
    @ColumnInfo(name = "image_path")
    val imagePath: String?,
    @ColumnInfo(name = "updated_at_ms")
    val updatedAtMillis: Long
)
