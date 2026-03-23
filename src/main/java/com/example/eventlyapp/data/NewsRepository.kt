package com.example.eventlyapp.data

import android.content.Context
import com.example.eventlyapp.BuildConfig
import com.example.eventlyapp.data.cache.CachedNewsSnapshot
import com.example.eventlyapp.data.cache.NewsCacheDatabaseHelper
import com.example.eventlyapp.data.cache.NewsImageCacheService
import com.example.eventlyapp.data.cache.NewsMetadataCacheService
import com.example.eventlyapp.data.remote.NewsRemoteDataSource
import com.example.eventlyapp.id.Id
import com.example.eventlyapp.model.NewsArticleData
import com.example.eventlyapp.network.DebugApiService
import com.example.eventlyapp.network.NytTimesWireService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class NewsRepository(
    private val remoteDataSource: NewsRemoteDataSource,
    private val metadataCacheService: NewsMetadataCacheService,
    private val imageCacheService: NewsImageCacheService,
    private val nytApiKey: String
) {

    suspend fun loadCachedNews(): CachedNewsSnapshot? = withContext(Dispatchers.IO) {
        val snapshot = metadataCacheService.readSnapshot() ?: return@withContext null

        if (isCacheExpired(snapshot.updatedAtMillis)) {
            metadataCacheService.clearAll()
            imageCacheService.clearAllImages()
            return@withContext null
        }

        val cachedArticles = snapshot.articles.map { article ->
            article.copy(imagePath = imageCacheService.sanitizeImagePath(article.imagePath))
        }

        CachedNewsSnapshot(
            articles = cachedArticles,
            updatedAtMillis = snapshot.updatedAtMillis
        )
    }

    suspend fun refreshNews(): Result<CachedNewsSnapshot> {
        if (nytApiKey.isBlank()) {
            return Result.failure(IllegalStateException("Добавь nyt.api.key в local.properties"))
        }

        return runCatching {
            val remoteArticles = remoteDataSource.fetchArticles(nytApiKey)
            val articles = remoteArticles.mapIndexed { index, article ->
                NewsArticleData(
                    id = Id("news-$index-${article.publishedAt}"),
                    title = article.title,
                    abstractText = article.abstractText,
                    source = article.source,
                    publishedAt = article.publishedAt,
                    imagePath = imageCacheService.cacheImage(article.imageUrl)
                )
            }

            val snapshot = CachedNewsSnapshot(
                articles = articles,
                updatedAtMillis = System.currentTimeMillis()
            )

            withContext(Dispatchers.IO) {
                metadataCacheService.replaceArticles(snapshot)
            }
            imageCacheService.clearUnusedImages(articles.mapNotNull { article -> article.imagePath }.toSet())
            remoteDataSource.sendDebugRequest(articles.size)
            snapshot
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

    fun formatCacheTimestamp(timestampMillis: Long): String {
        return SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault()).format(Date(timestampMillis))
    }

    private fun isCacheExpired(updatedAtMillis: Long): Boolean {
        return System.currentTimeMillis() - updatedAtMillis > CACHE_RETENTION_MILLIS
    }

    companion object {
        // We always request fresh data on screen open, but keep the latest successful
        // snapshot for up to 24 hours so the feed can open without blocking on the network.
        private const val CACHE_RETENTION_MILLIS = 24L * 60L * 60L * 1000L

        fun create(context: Context): NewsRepository {
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
                remoteDataSource = NewsRemoteDataSource(
                    nytTimesWireService = nytRetrofit.create(NytTimesWireService::class.java),
                    debugApiService = debugRetrofit.create(DebugApiService::class.java)
                ),
                metadataCacheService = NewsMetadataCacheService(
                    dbHelper = NewsCacheDatabaseHelper(context.applicationContext)
                ),
                imageCacheService = NewsImageCacheService(
                    context = context.applicationContext,
                    okHttpClient = client
                ),
                nytApiKey = BuildConfig.NYT_API_KEY
            )
        }
    }
}