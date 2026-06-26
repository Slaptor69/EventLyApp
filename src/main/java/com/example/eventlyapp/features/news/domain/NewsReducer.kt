package com.example.eventlyapp.features.news.domain

import com.example.eventlyapp.core.elm.ElmUpdate
import com.example.eventlyapp.core.elm.toElmUpdate
import com.example.eventlyapp.features.news.domain.model.NewsFeedState
import javax.inject.Inject

class NewsReducer @Inject constructor() {
    fun update(
        state: NewsFeedState,
        msg: NewsMsg
    ): ElmUpdate<NewsFeedState, NewsCommand, NewsEffect> {
        return when (msg) {
            NewsMsg.ScreenStarted -> state.toElmUpdate(
                commands = listOf(NewsCommand.LoadCachedNews)
            )
            is NewsMsg.CachedSnapshotLoaded -> {
                if (msg.snapshot == null) {
                    state.toElmUpdate(
                        commands = listOf(
                            NewsCommand.RefreshNews(showLoader = true),
                            NewsCommand.StartAutoRefresh
                        )
                    )
                } else {
                    state.copy(
                        isLoading = false,
                        isRefreshing = false,
                        articles = msg.snapshot.articles,
                        errorMessage = null,
                        lastUpdatedLabel = msg.lastUpdatedLabel,
                        sourceLabel = "Источник: локальный кэш"
                    ).toElmUpdate(
                        commands = listOf(
                            NewsCommand.RefreshNews(showLoader = false),
                            NewsCommand.StartAutoRefresh
                        )
                    )
                }
            }
            is NewsMsg.RefreshRequested -> state.toElmUpdate(
                commands = listOf(NewsCommand.RefreshNews(showLoader = msg.showLoader))
            )
            is NewsMsg.RefreshStarted -> state.copy(
                isLoading = msg.showLoader && state.articles.isEmpty(),
                isRefreshing = state.articles.isNotEmpty(),
                errorMessage = null
            ).toElmUpdate()
            is NewsMsg.RefreshSucceeded -> state.copy(
                isLoading = false,
                isRefreshing = false,
                articles = msg.snapshot.articles,
                errorMessage = null,
                lastUpdatedLabel = msg.lastUpdatedLabel,
                sourceLabel = "Источник: сеть, кэш обновлён"
            ).toElmUpdate()
            is NewsMsg.RefreshFailed -> {
                val hasArticles = state.articles.isNotEmpty()
                state.copy(
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = msg.message,
                    sourceLabel = if (hasArticles) "Источник: локальный кэш" else state.sourceLabel
                ).toElmUpdate(
                    effects = listOf(NewsEffect.ShowRefreshError(msg.message))
                )
            }
        }
    }
}
