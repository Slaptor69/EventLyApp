package com.example.eventlyapp.features.news.domain

sealed interface NewsCommand {
    data object LoadCachedNews : NewsCommand
    data class RefreshNews(val showLoader: Boolean) : NewsCommand
    data object StartAutoRefresh : NewsCommand
}
