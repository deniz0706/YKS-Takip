package com.deniz0706.ykstakip.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.deniz0706.ykstakip.ui.navigation.YksShell

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // YksShell owns the adaptive app theme; avoid wrapping the same tree twice.
        setContent {
            YksShell(context = this@MainActivity)
        }
    }
}
