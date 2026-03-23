package com.example.eventlyapp.data.cache

import com.example.eventlyapp.model.NewsArticleData

data class CachedNewsSnapshot(
    val articles: List<NewsArticleData>,
    val updatedAtMillis: Long
)