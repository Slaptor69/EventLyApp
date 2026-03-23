package com.example.eventlyapp.ui.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.eventlyapp.data.NewsRepository
import com.example.eventlyapp.data.cache.CachedNewsSnapshot
import com.example.eventlyapp.model.NewsFeedState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class NewsViewModel(
    private val repository: NewsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NewsFeedState())
    val state: StateFlow<NewsFeedState> = _state.asStateFlow()

    private var refreshJob: Job? = null
    private var autoRefreshJob: Job? = null

    init {
        viewModelScope.launch {
            val cachedSnapshot = repository.loadCachedNews()
            if (cachedSnapshot != null) {
                showCachedSnapshot(cachedSnapshot)
            }
            refresh(showLoader = cachedSnapshot == null)
            startAutoRefresh()
        }
    }

    fun refresh(showLoader: Boolean) {
        val runningRefreshJob = refreshJob
        if (runningRefreshJob?.isActive == true) {
            return
        }

        refreshJob = viewModelScope.launch {
            _state.update { current ->
                current.copy(
                    isLoading = showLoader && current.articles.isEmpty(),
                    isRefreshing = current.articles.isEmpty().not(),
                    errorMessage = null
                )
            }

            repository.refreshNews()
                .onSuccess { snapshot ->
                    _state.update { current ->
                        current.copy(
                            isLoading = false,
                            isRefreshing = false,
                            articles = snapshot.articles,
                            errorMessage = null,
                            lastUpdatedLabel = repository.formatCacheTimestamp(snapshot.updatedAtMillis),
                            sourceLabel = "Источник: сеть, кэш обновлён"
                        )
                    }
                }
                .onFailure { throwable ->
                    val hasArticles = _state.value.articles.isEmpty().not()
                    _state.update { current ->
                        current.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = throwable.message ?: "Не удалось загрузить новости",
                            sourceLabel = if (hasArticles) "Источник: локальный кэш" else current.sourceLabel
                        )
                    }
                }
        }
    }

    fun formatPublishedAt(rawDate: String): String = repository.formatPublishedAt(rawDate)

    override fun onCleared() {
        autoRefreshJob?.cancel()
        refreshJob?.cancel()
        super.onCleared()
    }

    private fun showCachedSnapshot(snapshot: CachedNewsSnapshot) {
        _state.update { current ->
            current.copy(
                isLoading = false,
                isRefreshing = false,
                articles = snapshot.articles,
                errorMessage = null,
                lastUpdatedLabel = repository.formatCacheTimestamp(snapshot.updatedAtMillis),
                sourceLabel = "Источник: локальный кэш"
            )
        }
    }

    private fun startAutoRefresh() {
        autoRefreshJob = viewModelScope.launch {
            while (isActive) {
                delay(120_000)
                refresh(showLoader = false)
            }
        }
    }

    companion object {
        fun factory(repository: NewsRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(NewsViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return NewsViewModel(repository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
        }
    }
}