package com.example.eventlyapp.data.remote

import android.util.Log
import com.example.eventlyapp.network.DebugApiService
import com.example.eventlyapp.network.NytTimesWireService
import com.example.eventlyapp.network.dto.DebugPostRequest
import com.example.eventlyapp.network.dto.NytMultimediaDto

class NewsRemoteDataSource(
    private val nytTimesWireService: NytTimesWireService,
    private val debugApiService: DebugApiService
) {

    suspend fun fetchArticles(apiKey: String): List<RemoteNewsArticle> {
        val response = nytTimesWireService.getNews(
            source = "all",
            section = "all",
            apiKey = apiKey
        )

        return response.results.orEmpty().map { item ->
            RemoteNewsArticle(
                title = item.title.orEmpty().ifBlank { "Без заголовка" },
                abstractText = item.abstractText.orEmpty().ifBlank { "Описание отсутствует" },
                source = item.source.orEmpty().ifBlank { "NYTimes" },
                publishedAt = item.publishedDate.orEmpty(),
                imageUrl = item.multimedia.selectPreviewUrl()
            )
        }
    }

    suspend fun sendDebugRequest(newsCount: Int) {
        runCatching {
            debugApiService.sendDebugEvent(
                request = DebugPostRequest(
                    title = "news-refresh",
                    body = "Loaded $newsCount items from NYT TimesWire",
                    userId = 4
                )
            )
        }.onSuccess { response ->
            Log.d("NewsRemoteDataSource", "Debug POST success: id=${response.id}, title=${response.title}")
        }.onFailure { throwable ->
            Log.d("NewsRemoteDataSource", "Debug POST failed: ${throwable.message}")
        }
    }
}

private fun List<NytMultimediaDto>?.selectPreviewUrl(): String? {
    val preferredFormats = listOf("mediumThreeByTwo210", "mediumThreeByTwo440", "Normal")

    preferredFormats.forEach { preferredFormat ->
        val candidate = this.orEmpty().firstOrNull { item ->
            item.format == preferredFormat && item.url.isNullOrBlank().not()
        }
        if (candidate != null && candidate.url.isNullOrBlank().not()) {
            return candidate.url
        }
    }

    return this.orEmpty().firstOrNull { item -> item.url.isNullOrBlank().not() }?.url
}