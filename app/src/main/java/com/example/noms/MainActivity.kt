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
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest


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
                        val remoteConfig = FirebaseRemoteConfig.getInstance()
                        val configSettings = FirebaseRemoteConfigSettings.Builder()
                            .setMinimumFetchIntervalInSeconds(3600)
                            .build()
                        remoteConfig.setConfigSettingsAsync(configSettings)
                        remoteConfig.setDefaultsAsync(mapOf(
                            "supabase_url" to "",
                            "supabase_key" to "",
                            "places_api_key" to "",
                            "google_maps_key" to "",
                        ))

                        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d("RemoteConfig", "Config params updated: ${task.result}")
                                Log.d("RemoteConfig", "Supabase URL: ${remoteConfig.getString("supabase_url")}")
                                Log.d("RemoteConfig", "Supabase Key: ${remoteConfig.getString("supabase_key")}")
                                Log.d("RemoteConfig", "Places API Key: ${remoteConfig.getString("places_api_key")}")
                                Log.d("RemoteConfig", "Google Maps Key: ${remoteConfig.getString("google_maps_key")}")

                                val mapsApiKey = "AIzaSyAirvBM5eu-0NebTtxFcG0eINDZtJVfAoQ"
                                if (mapsApiKey.isNotEmpty()) {
                                    Places.initialize(applicationContext, mapsApiKey)
                                    Log.d("PlacesAPI", "Places API initialized successfully with Remote Config key")
                                } else {
                                    Log.e("Maps", "No API key available from Remote Config")
                                }

                                val supabaseUrl = "https://xoffilinikbhnlvdfaib.supabase.co"
                                val supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InhvZmZpbGluaWtiaG5sdmRmYWliIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Mjc0OTYxMzEsImV4cCI6MjA0MzA3MjEzMX0.2x8XkQS3ahCmYJJHSn6581ki2wh4-mbcWzBEUEmGtu0"
                                if (supabaseUrl.isNotEmpty() && supabaseKey.isNotEmpty()) {
                                    val supabase = createSupabaseClient(
                                        supabaseUrl = supabaseUrl,
                                        supabaseKey = supabaseKey
                                    ) {
                                        install(Postgrest)
                                    }
                                    Log.d("Supabase", "Supabase client initialized successfully.")
                                } else {
                                    Log.e("Supabase", "Supabase URL or key is empty")
                                }
                            } else {
                                Log.e("RemoteConfig", "Fetch failed")
                            }
                        }
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
