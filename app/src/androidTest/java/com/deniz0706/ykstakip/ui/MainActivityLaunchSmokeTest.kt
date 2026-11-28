package com.deniz0706.ykstakip.ui

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Exercises the real launcher Activity rather than rendering a screen in isolation.
 * This catches startup failures in Application, theme setup, navigation, ViewModels,
 * repositories, database initialization, and the first Compose destination.
 */
@RunWith(AndroidJUnit4::class)
class MainActivityLaunchSmokeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun launcherActivity_opensAndRendersHomeScreen() {
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Bugün").assertExists()
        composeRule.onNodeWithText("AI Koç").assertExists()
    }
}
