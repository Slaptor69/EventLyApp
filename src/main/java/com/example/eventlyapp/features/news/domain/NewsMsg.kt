package com.example.eventlyapp.features.news.domain

import com.example.eventlyapp.features.news.domain.model.NewsSnapshotData

sealed interface NewsMsg {
    data object ScreenStarted : NewsMsg
    data class CachedSnapshotLoaded(
        val snapshot: NewsSnapshotData?,
        val lastUpdatedLabel: String?
    ) : NewsMsg
    data class RefreshRequested(val showLoader: Boolean) : NewsMsg
    data class RefreshStarted(val showLoader: Boolean) : NewsMsg
    data class RefreshSucceeded(
        val snapshot: NewsSnapshotData,
        val lastUpdatedLabel: String
    ) : NewsMsg
    data class RefreshFailed(val message: String) : NewsMsg
}
