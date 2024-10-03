package com.example.noms

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.noms.ui.MainScreen
import com.google.android.libraries.places.api.Places


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val customColorScheme = lightColorScheme(
                primary = Color(0xFF2E8B57),
                secondary = Color(0xFF2E8B57),
                tertiary = Color(0xFF2E8B57),
                background = Color.White,
                surface = Color.White,
                onPrimary = Color.White,
                onSecondary = Color.White,
                onTertiary = Color.White,
                onBackground = Color.Black,
                onSurface = Color.Black,
            )

            LaunchedEffect(Unit) {
                if (!Places.isInitialized()) {
                    try {
                        Places.initialize(applicationContext, "AIzaSyDA0NJYciapVsDwGoZPA69UDaJAhzOmstE")
                        Log.d("PlacesAPI", "Places API initialized successfully.")
                    } catch (e: Exception) {
                        Log.e("PlacesAPI", "Error initializing Places API: ${e.message}")
                    }
                }
            }




            MaterialTheme(
                colorScheme = customColorScheme
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}