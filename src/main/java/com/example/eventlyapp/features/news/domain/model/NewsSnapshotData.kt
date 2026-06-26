package com.example.eventlyapp.features.news.domain.model

data class NewsSnapshotData(
    val articles: List<NewsArticleData>,
    val updatedAtMillis: Long
)
