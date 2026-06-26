package com.example.eventlyapp.features.settings.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.eventlyapp.MainActivity
import org.junit.Rule
import org.junit.Test

class SettingsFlowTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun selectDarkTheme_updatesSettingsRow() {
        composeRule.onNodeWithContentDescription("Настройки").performClick()

        composeRule.onNodeWithText("Настройки").assertIsDisplayed()
        composeRule.onNodeWithText("Тема").performClick()
        composeRule.onNodeWithText("Тема приложения").assertIsDisplayed()
        composeRule.onNodeWithText("Темная").performClick()

        composeRule.onNodeWithText("Темная").assertIsDisplayed()
    }
}
