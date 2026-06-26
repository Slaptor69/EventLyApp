package com.example.eventlyapp.features.news.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventlyapp.core.presentation.SingleViewModelFactory
import com.example.eventlyapp.features.news.data.NewsRepository
import com.example.eventlyapp.features.news.data.cache.CachedNewsSnapshot
import com.example.eventlyapp.features.news.domain.NewsCommand
import com.example.eventlyapp.features.news.domain.NewsEffect
import com.example.eventlyapp.features.news.domain.NewsMsg
import com.example.eventlyapp.features.news.domain.NewsReducer
import com.example.eventlyapp.features.news.domain.model.NewsFeedState
import com.example.eventlyapp.features.news.domain.model.NewsSnapshotData
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

class NewsViewModel(
    private val repository: NewsRepository,
    private val reducer: NewsReducer
) : ViewModel() {

    private val _state = MutableStateFlow(NewsFeedState())
    val state: StateFlow<NewsFeedState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<NewsEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<NewsEffect> = _effects

    private var refreshJob: Job? = null
    private var autoRefreshJob: Job? = null

    init {
        accept(NewsMsg.ScreenStarted)
    }

    fun accept(msg: NewsMsg) {
        val update = reducer.update(_state.value, msg)
        _state.value = update.state
        update.effects.forEach { effect -> _effects.tryEmit(effect) }
        update.commands.forEach { command -> execute(command) }
    }

    private fun execute(command: NewsCommand) {
        when (command) {
            NewsCommand.LoadCachedNews -> loadCachedNews()
            is NewsCommand.RefreshNews -> refreshNews(command.showLoader)
            NewsCommand.StartAutoRefresh -> startAutoRefresh()
        }
    }

    private fun loadCachedNews() {
        viewModelScope.launch {
            val cachedSnapshot = repository.loadCachedNews()
            accept(
                NewsMsg.CachedSnapshotLoaded(
                    snapshot = cachedSnapshot?.toNewsSnapshotData(),
                    lastUpdatedLabel = cachedSnapshot?.let { snapshot ->
                        repository.formatCacheTimestamp(snapshot.updatedAtMillis)
                    }
                )
            )
        }
    }

    private fun refreshNews(showLoader: Boolean) {
        val runningRefreshJob = refreshJob
        if (runningRefreshJob?.isActive == true) {
            return
        }

        refreshJob = viewModelScope.launch {
            accept(NewsMsg.RefreshStarted(showLoader = showLoader))

            repository.refreshNews()
                .onSuccess { snapshot ->
                    accept(
                        NewsMsg.RefreshSucceeded(
                            snapshot = snapshot.toNewsSnapshotData(),
                            lastUpdatedLabel = repository.formatCacheTimestamp(snapshot.updatedAtMillis)
                        )
                    )
                }
                .onFailure { throwable ->
                    accept(NewsMsg.RefreshFailed(throwable.message ?: "Не удалось загрузить новости"))
                }
        }
    }

    fun formatPublishedAt(rawDate: String): String = repository.formatPublishedAt(rawDate)

    override fun onCleared() {
        autoRefreshJob?.cancel()
        refreshJob?.cancel()
        super.onCleared()
    }

    private fun startAutoRefresh() {
        if (autoRefreshJob?.isActive == true) {
            return
        }

        autoRefreshJob = viewModelScope.launch {
            while (isActive) {
                delay(120_000)
                accept(NewsMsg.RefreshRequested(showLoader = false))
            }
        }
    }

}

class NewsViewModelFactory @Inject constructor(
    private val repository: NewsRepository,
    private val reducer: NewsReducer
) : SingleViewModelFactory<NewsViewModel>(NewsViewModel::class.java) {
    override fun createViewModel(): NewsViewModel = NewsViewModel(repository, reducer)
}

private fun CachedNewsSnapshot.toNewsSnapshotData(): NewsSnapshotData {
    return NewsSnapshotData(
        articles = articles,
        updatedAtMillis = updatedAtMillis
    )
}
