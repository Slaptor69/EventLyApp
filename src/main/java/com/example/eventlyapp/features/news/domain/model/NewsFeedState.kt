package com.example.eventlyapp.features.news.domain.model

data class NewsFeedState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val articles: List<NewsArticleData> = emptyList(),
    val errorMessage: String? = null,
    val lastUpdatedLabel: String? = null,
    val sourceLabel: String? = null
)
