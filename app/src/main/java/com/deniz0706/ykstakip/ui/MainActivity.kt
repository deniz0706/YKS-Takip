package com.deniz0706.ykstakip.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.deniz0706.ykstakip.ui.navigation.YksShell
import com.deniz0706.ykstakip.ui.theme.YksTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            YksTheme {
                YksShell(context = this@MainActivity)
            }
        }
    }
}
