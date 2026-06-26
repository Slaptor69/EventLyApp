package com.example.eventlyapp.features.tasks.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.eventlyapp.MainActivity
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TaskFlowTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun createTask_happyPath_showsTaskInList() {
        composeRule.onNodeWithText("Задачи").performClick()
        composeRule.onNodeWithContentDescription("Добавить задачу").performClick()

        composeRule.onNodeWithTag("task-title-input").performTextInput("Купить подарок")
        composeRule.onNodeWithTag("task-save-button").performClick()

        composeRule.onNodeWithText("Купить подарок").assertIsDisplayed()
    }

    @Test
    fun taskList_snapshotSmoke_rendersNonEmptyImage() {
        composeRule.onNodeWithText("Задачи").performClick()

        val snapshot = composeRule.onNodeWithTag("task-list").captureToImage()

        assertTrue(snapshot.width > 0)
        assertTrue(snapshot.height > 0)
    }
}
