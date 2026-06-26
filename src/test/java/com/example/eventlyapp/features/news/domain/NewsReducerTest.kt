package com.example.eventlyapp.features.news.domain

import com.example.eventlyapp.core.id.Id
import com.example.eventlyapp.features.news.domain.model.NewsArticleData
import com.example.eventlyapp.features.news.domain.model.NewsFeedState
import com.example.eventlyapp.features.news.domain.model.NewsSnapshotData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NewsReducerTest {
    private val reducer = NewsReducer()

    @Test
    fun screenStarted_requestsCachedNewsCommand() {
        val update = reducer.update(NewsFeedState(), NewsMsg.ScreenStarted)

        assertEquals(listOf(NewsCommand.LoadCachedNews), update.commands)
    }

    @Test
    fun cachedSnapshotLoaded_showsCacheAndRefreshesInBackground() {
        val snapshot = NewsSnapshotData(
            articles = listOf(article("news-1")),
            updatedAtMillis = 100L
        )

        val update = reducer.update(
            state = NewsFeedState(),
            msg = NewsMsg.CachedSnapshotLoaded(
                snapshot = snapshot,
                lastUpdatedLabel = "18 июн. 2026"
            )
        )

        assertEquals(snapshot.articles, update.state.articles)
        assertEquals("18 июн. 2026", update.state.lastUpdatedLabel)
        assertEquals("Источник: локальный кэш", update.state.sourceLabel)
        assertEquals(
            listOf(
                NewsCommand.RefreshNews(showLoader = false),
                NewsCommand.StartAutoRefresh
            ),
            update.commands
        )
    }

    @Test
    fun refreshStarted_usesLoaderOnlyWhenFeedIsEmpty() {
        val emptyUpdate = reducer.update(
            state = NewsFeedState(articles = emptyList()),
            msg = NewsMsg.RefreshStarted(showLoader = true)
        )
        val cachedUpdate = reducer.update(
            state = NewsFeedState(articles = listOf(article("news-1"))),
            msg = NewsMsg.RefreshStarted(showLoader = true)
        )

        assertTrue(emptyUpdate.state.isLoading)
        assertFalse(cachedUpdate.state.isLoading)
        assertTrue(cachedUpdate.state.isRefreshing)
    }

    @Test
    fun refreshFailed_keepsCachedSourceAndEmitsEffect() {
        val update = reducer.update(
            state = NewsFeedState(articles = listOf(article("news-1"))),
            msg = NewsMsg.RefreshFailed("Нет интернета")
        )

        assertEquals("Нет интернета", update.state.errorMessage)
        assertEquals("Источник: локальный кэш", update.state.sourceLabel)
        assertEquals(listOf(NewsEffect.ShowRefreshError("Нет интернета")), update.effects)
    }

    private fun article(id: String): NewsArticleData {
        return NewsArticleData(
            id = Id(id),
            title = "Новость",
            abstractText = "Описание",
            source = "NYTimes",
            publishedAt = "2026-06-18T10:00:00Z",
            imagePath = null
        )
    }
}
