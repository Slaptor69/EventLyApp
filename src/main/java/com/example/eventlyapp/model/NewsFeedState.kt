package com.example.eventlyapp.model

data class NewsFeedState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val articles: List<NewsArticleData> = emptyList(),
    val errorMessage: String? = null,
    val lastUpdatedLabel: String? = null,
    val sourceLabel: String? = null
)