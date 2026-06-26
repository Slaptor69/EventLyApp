package com.example.eventlyapp.features.news.data.cache

import com.example.eventlyapp.features.news.domain.model.NewsArticleData

data class CachedNewsSnapshot(
    val articles: List<NewsArticleData>,
    val updatedAtMillis: Long
)
