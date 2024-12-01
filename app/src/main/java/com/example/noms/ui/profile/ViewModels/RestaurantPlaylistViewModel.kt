package com.example.noms.ui.profile.ViewModels

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.noms.backend.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * This is the function to fetch the user playlists and restaurants.
 * 
 * @param uid The user id.
 */

suspend fun fetchUserPlaylistsAndRestaurants(uid: Int) {
    val tag = "RestaurantPlaylist" // Tag for logging

    try {
        // Get playlists of the user
        val playlists = getPlaylistsofUser(uid)
        if (playlists.isEmpty()) {
            Log.i(tag, "No playlists found for user $uid")
            return
        }

        // Iterate through each playlist and fetch the restaurants
        playlists.forEach { playlist ->
            Log.i(tag, "Fetching restaurants for playlist: ${playlist.name} (ID: ${playlist.id})")

            val restaurants = playlist.id?.let { getPlaylist(it) } ?: emptyList()
            if (restaurants.isEmpty()) {
                Log.i(tag, "No restaurants found in playlist: ${playlist.name}")
            } else {
                Log.i(tag, "Restaurants in playlist '${playlist.name}':")
                restaurants.forEach { restaurant ->
                    Log.i(
                        tag,
                        "- ${restaurant.name} at ${restaurant.location} [Rating: ${restaurant.rating}]"
                    )
                }
            }
        }
    } catch (e: Exception) {
        Log.e(tag, "Error fetching playlists or restaurants: ${e.message}", e)
    }
}


/**
 * This is the view model for the restaurant playlist card.
 * 
 * @param placeId The place id.
 */

class RestaurantPlaylistCardViewModel(private val placeId: String) : ViewModel() {
    private val _photoBitmap = MutableStateFlow<Bitmap?>(null)
    val photoBitmap: StateFlow<Bitmap?> = _photoBitmap

    fun getPhoto(context: Context, placeId: String) {
        viewModelScope.launch {
            val photoMetadata = fetchPhotoReference(context, placeId)
            if (photoMetadata != null) {
                _photoBitmap.value = fetchPhoto(context, photoMetadata)
            }
        }
    }

    class Factory(private val placeId: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RestaurantPlaylistCardViewModel(placeId) as T
        }
    }
}


/**
 * This is the view model for the restaurant playlist screen.
 */

class RestaurantPlaylistViewModel : ViewModel() {
    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> get() = _playlists

    private val _playlistRestaurants = MutableStateFlow<Map<Int, List<Restaurant>>>(emptyMap())
    val playlistRestaurants: StateFlow<Map<Int, List<Restaurant>>> get() = _playlistRestaurants

    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> get() = _userName

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> get() = _loading

    private val expandedPlaylists = mutableStateMapOf<Int, Boolean>()

    // Function to check if a playlist is expanded
    fun isPlaylistExpanded(playlistId: Int): Boolean {
        return expandedPlaylists[playlistId] ?: false
    }

    // Function to toggle playlist expansion
    fun togglePlaylistExpansion(playlistId: Int) {
        expandedPlaylists[playlistId] = !(expandedPlaylists[playlistId] ?: false)
    }

    // Function to fetch user data
    fun fetchData(uid: Int) {
        viewModelScope.launch {
            try {
                _loading.value = true

                // Fetch user name
                val user = getUser(uid)
                _userName.value = user?.let { "${it.first_name} ${it.last_name}" } ?: "Unknown User"

                // Fetch playlists and their restaurants
                val fetchedPlaylists = getPlaylistsofUser(uid)
                _playlists.value = fetchedPlaylists

                val restaurantMap = fetchedPlaylists.associate { playlist ->
                    playlist.id!! to (getPlaylist(playlist.id) ?: emptyList())
                }
                _playlistRestaurants.value = restaurantMap
            } catch (e: Exception) {
                Log.e("RestaurantPlaylistVM", "Error fetching data: ${e.message}", e)
            } finally {
                _loading.value = false
            }
        }
    }
}
