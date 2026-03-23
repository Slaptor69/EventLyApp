package com.example.eventlyapp.ui.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.eventlyapp.data.NewsRepository
import com.example.eventlyapp.model.NewsFeedState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NewsViewModel(
    private val repository: NewsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NewsFeedState())
    val state: StateFlow<NewsFeedState> = _state.asStateFlow()

    private var refreshJob: Job? = null
    private var autoRefreshJob: Job? = null

    init {
        refresh(showLoader = true)
        startAutoRefresh()
    }

    fun refresh(showLoader: Boolean) {
        val runningRefreshJob = refreshJob
        if (runningRefreshJob?.isActive == true) {
            return
        }

        refreshJob = viewModelScope.launch {
            if (showLoader) {
                _state.update { current ->
                    current.copy(isLoading = true, errorMessage = null)
                }
            } else {
                _state.update { current ->
                    current.copy(errorMessage = null)
                }
            }

            repository.fetchNews()
                .onSuccess { articles ->
                    _state.update { current ->
                        current.copy(
                            isLoading = false,
                            articles = articles,
                            errorMessage = null,
                            lastUpdatedLabel = formatLastUpdated()
                        )
                    }
                    repository.sendDebugRequest(articles.size)
                }
                .onFailure { throwable ->
                    _state.update { current ->
                        current.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Не удалось загрузить новости"
                        )
                    }
                }
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

    fun formatPublishedAt(rawDate: String): String = repository.formatPublishedAt(rawDate)

    override fun onCleared() {
        autoRefreshJob?.cancel()
        refreshJob?.cancel()
        super.onCleared()
    }

    private fun formatLastUpdated(): String {
        return SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault()).format(Date())
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