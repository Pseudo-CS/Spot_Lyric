package com.spotlyric.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.spotlyric.app.presentation.navigation.SpotLyricNavHost
import com.spotlyric.app.presentation.theme.SpotLyricTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpotLyricTheme {
                SpotLyricNavHost()
            }
        }
    }
}
