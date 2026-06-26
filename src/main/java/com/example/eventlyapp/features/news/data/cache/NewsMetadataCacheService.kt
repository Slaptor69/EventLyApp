package com.example.eventlyapp.features.news.data.cache

import com.example.eventlyapp.core.id.Id
import com.example.eventlyapp.features.news.domain.model.NewsArticleData
import javax.inject.Inject

class NewsMetadataCacheService @Inject constructor(
    private val newsCacheDao: NewsCacheDao
) {

    suspend fun readSnapshot(): CachedNewsSnapshot? {
        val cachedArticles = newsCacheDao.getArticles()
        if (cachedArticles.isEmpty()) {
            return null
        }

        return CachedNewsSnapshot(
            articles = cachedArticles.map { article -> article.toNewsArticleData() },
            updatedAtMillis = cachedArticles.first().updatedAtMillis
        )
    }

    suspend fun replaceArticles(snapshot: CachedNewsSnapshot) {
        newsCacheDao.replaceArticles(
            snapshot.articles.mapIndexed { index, article ->
                article.toNewsCacheEntity(
                    position = index,
                    updatedAtMillis = snapshot.updatedAtMillis
                )
            }
        )
    }

    suspend fun clearAll() {
        newsCacheDao.clearAll()
    }
}

private fun NewsCacheEntity.toNewsArticleData(): NewsArticleData {
    return NewsArticleData(
        id = Id(articleId),
        title = title,
        abstractText = abstractText,
        source = source,
        publishedAt = publishedAt,
        imagePath = imagePath
    )
}

private fun NewsArticleData.toNewsCacheEntity(
    position: Int,
    updatedAtMillis: Long
): NewsCacheEntity {
    return NewsCacheEntity(
        articleId = id.value,
        position = position,
        title = title,
        abstractText = abstractText,
        source = source,
        publishedAt = publishedAt,
        imagePath = imagePath,
        updatedAtMillis = updatedAtMillis
    )
}
