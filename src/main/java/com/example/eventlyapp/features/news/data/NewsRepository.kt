package com.example.eventlyapp.features.news.data

import com.example.eventlyapp.core.id.Id
import com.example.eventlyapp.di.AppModule
import com.example.eventlyapp.features.news.data.cache.CachedNewsSnapshot
import com.example.eventlyapp.features.news.data.cache.NewsImageCacheService
import com.example.eventlyapp.features.news.data.cache.NewsMetadataCacheService
import com.example.eventlyapp.features.news.data.remote.NewsRemoteDataSource
import com.example.eventlyapp.features.news.domain.model.NewsArticleData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Named

class NewsRepository @Inject constructor(
    private val remoteDataSource: NewsRemoteDataSource,
    private val metadataCacheService: NewsMetadataCacheService,
    private val imageCacheService: NewsImageCacheService,
    @Named(AppModule.NYT_API_KEY)
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
            return Result.failure(IllegalStateException(NewsErrorMessageMapper.missingApiKeyMessage()))
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
        }.recoverCatching { throwable ->
            throw IllegalStateException(NewsErrorMessageMapper.toUserMessage(throwable), throwable)
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
    }

}
