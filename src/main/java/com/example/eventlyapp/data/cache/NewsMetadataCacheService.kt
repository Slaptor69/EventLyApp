package com.example.eventlyapp.data.cache

import android.content.ContentValues
import com.example.eventlyapp.id.Id
import com.example.eventlyapp.model.NewsArticleData

class NewsMetadataCacheService(
    private val dbHelper: NewsCacheDatabaseHelper
) {

    fun readSnapshot(): CachedNewsSnapshot? {
        val db = dbHelper.readableDatabase
        db.query(
            NewsCacheDatabaseHelper.TABLE_NEWS_CACHE,
            null,
            null,
            null,
            null,
            null,
            "${NewsCacheDatabaseHelper.COLUMN_POSITION} ASC"
        ).use { cursor ->
            if (cursor.count == 0) {
                return null
            }

            val articles = mutableListOf<NewsArticleData>()
            var updatedAtMillis = 0L

            while (cursor.moveToNext()) {
                val article = NewsArticleData(
                    id = Id(cursor.getString(cursor.getColumnIndexOrThrow(NewsCacheDatabaseHelper.COLUMN_ARTICLE_ID))),
                    title = cursor.getString(cursor.getColumnIndexOrThrow(NewsCacheDatabaseHelper.COLUMN_TITLE)),
                    abstractText = cursor.getString(cursor.getColumnIndexOrThrow(NewsCacheDatabaseHelper.COLUMN_ABSTRACT)),
                    source = cursor.getString(cursor.getColumnIndexOrThrow(NewsCacheDatabaseHelper.COLUMN_SOURCE)),
                    publishedAt = cursor.getString(cursor.getColumnIndexOrThrow(NewsCacheDatabaseHelper.COLUMN_PUBLISHED_AT)),
                    imagePath = cursor.getString(cursor.getColumnIndexOrThrow(NewsCacheDatabaseHelper.COLUMN_IMAGE_PATH))
                )
                articles.add(article)
                updatedAtMillis = cursor.getLong(cursor.getColumnIndexOrThrow(NewsCacheDatabaseHelper.COLUMN_UPDATED_AT_MS))
            }

            return CachedNewsSnapshot(
                articles = articles,
                updatedAtMillis = updatedAtMillis
            )
        }
    }

    fun replaceArticles(snapshot: CachedNewsSnapshot) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            db.delete(NewsCacheDatabaseHelper.TABLE_NEWS_CACHE, null, null)
            snapshot.articles.forEachIndexed { index, article ->
                val values = ContentValues().apply {
                    put(NewsCacheDatabaseHelper.COLUMN_ARTICLE_ID, article.id.value)
                    put(NewsCacheDatabaseHelper.COLUMN_POSITION, index)
                    put(NewsCacheDatabaseHelper.COLUMN_TITLE, article.title)
                    put(NewsCacheDatabaseHelper.COLUMN_ABSTRACT, article.abstractText)
                    put(NewsCacheDatabaseHelper.COLUMN_SOURCE, article.source)
                    put(NewsCacheDatabaseHelper.COLUMN_PUBLISHED_AT, article.publishedAt)
                    put(NewsCacheDatabaseHelper.COLUMN_IMAGE_PATH, article.imagePath)
                    put(NewsCacheDatabaseHelper.COLUMN_UPDATED_AT_MS, snapshot.updatedAtMillis)
                }
                db.insert(NewsCacheDatabaseHelper.TABLE_NEWS_CACHE, null, values)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun clearAll() {
        dbHelper.writableDatabase.delete(NewsCacheDatabaseHelper.TABLE_NEWS_CACHE, null, null)
    }
}