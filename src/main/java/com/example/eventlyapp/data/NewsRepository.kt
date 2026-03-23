package com.example.eventlyapp.data

import android.util.Log
import com.example.eventlyapp.BuildConfig
import com.example.eventlyapp.id.Id
import com.example.eventlyapp.model.NewsArticleData
import com.example.eventlyapp.network.DebugApiService
import com.example.eventlyapp.network.NytTimesWireService
import com.example.eventlyapp.network.dto.DebugPostRequest
import com.example.eventlyapp.network.dto.NytMultimediaDto
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class NewsRepository(
    private val nytTimesWireService: NytTimesWireService,
    private val debugApiService: DebugApiService,
    private val nytApiKey: String
) {

    suspend fun fetchNews(): Result<List<NewsArticleData>> {
        if (nytApiKey.isBlank()) {
            return Result.failure(IllegalStateException("Добавь nyt.api.key в local.properties"))
        }

        return runCatching {
            val response = nytTimesWireService.getNews(
                source = "all",
                section = "all",
                apiKey = nytApiKey
            )

            response.results.orEmpty().mapIndexed { index, item ->
                NewsArticleData(
                    id = Id("news-$index-${item.publishedDate.orEmpty()}"),
                    title = item.title.orEmpty().ifBlank { "Без заголовка" },
                    abstractText = item.abstractText.orEmpty().ifBlank { "Описание отсутствует" },
                    source = item.source.orEmpty().ifBlank { "NYTimes" },
                    publishedAt = item.publishedDate.orEmpty(),
                    imageUrl = item.multimedia.selectPreviewUrl()
                )
            }
        }
    }

    suspend fun sendDebugRequest(newsCount: Int) {
        runCatching {
            debugApiService.sendDebugEvent(
                request = DebugPostRequest(
                    title = "news-refresh",
                    body = "Loaded $newsCount items from NYT TimesWire",
                    userId = 3
                )
            )
        }.onSuccess { response ->
            Log.d("NewsRepository", "Debug POST success: id=${response.id}, title=${response.title}")
        }.onFailure { throwable ->
            Log.d("NewsRepository", "Debug POST failed: ${throwable.message}")
        }
    }

    fun formatPublishedAt(rawDate: String): String {
        if (rawDate.isBlank()) {
            return "Дата неизвестна"
        }

        val parsers = listOf(
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.US),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        )
        val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        outputFormat.timeZone = TimeZone.getDefault()

        parsers.forEach { parser ->
            runCatching {
                parser.timeZone = TimeZone.getTimeZone("UTC")
                val parsedDate = parser.parse(rawDate)
                if (parsedDate != null) {
                    return outputFormat.format(parsedDate)
                }
            }
        }

        return rawDate
    }

    companion object {
        fun create(): NewsRepository {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()

            val nytRetrofit = Retrofit.Builder()
                .baseUrl("https://api.nytimes.com/svc/news/v3/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val debugRetrofit = Retrofit.Builder()
                .baseUrl("https://jsonplaceholder.typicode.com/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return NewsRepository(
                nytTimesWireService = nytRetrofit.create(NytTimesWireService::class.java),
                debugApiService = debugRetrofit.create(DebugApiService::class.java),
                nytApiKey = BuildConfig.NYT_API_KEY
            )
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