package com.example.eventlyapp.data.cache

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class NewsCacheDatabaseHelper(
    context: Context
) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_NEWS_CACHE (
                $COLUMN_ARTICLE_ID TEXT PRIMARY KEY,
                $COLUMN_POSITION INTEGER NOT NULL,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_ABSTRACT TEXT NOT NULL,
                $COLUMN_SOURCE TEXT NOT NULL,
                $COLUMN_PUBLISHED_AT TEXT NOT NULL,
                $COLUMN_IMAGE_PATH TEXT,
                $COLUMN_UPDATED_AT_MS INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NEWS_CACHE")
        onCreate(db)
    }

    companion object {
        private const val DATABASE_NAME = "evently_news_cache.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_NEWS_CACHE = "news_cache"
        const val COLUMN_ARTICLE_ID = "article_id"
        const val COLUMN_POSITION = "position_index"
        const val COLUMN_TITLE = "title"
        const val COLUMN_ABSTRACT = "abstract_text"
        const val COLUMN_SOURCE = "source"
        const val COLUMN_PUBLISHED_AT = "published_at"
        const val COLUMN_IMAGE_PATH = "image_path"
        const val COLUMN_UPDATED_AT_MS = "updated_at_ms"
    }
}