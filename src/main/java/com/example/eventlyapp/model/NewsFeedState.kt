package com.example.eventlyapp.model

data class NewsFeedState(
    val isLoading: Boolean = true,
    val articles: List<NewsArticleData> = emptyList(),
    val errorMessage: String? = null,
    val lastUpdatedLabel: String? = null
)