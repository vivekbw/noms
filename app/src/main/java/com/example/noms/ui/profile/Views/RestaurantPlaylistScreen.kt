package com.example.noms.ui.profile.Views

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import com.example.noms.backend.getPlaylist
import com.example.noms.backend.getPlaylistsofUser
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.LazyColumn

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.noms.backend.Playlist
import com.example.noms.backend.Restaurant
import com.example.noms.backend.getUser
import com.example.noms.ui.profile.ViewModels.*
import androidx.lifecycle.viewmodel.compose.viewModel


//Fetching restaurants for playlist: AidenTestPlaylist (ID: 2)
//2024-11-08 14:46:22.122 15831-15831 RestaurantPlaylist      com.example.noms                     I  Restaurants in playlist 'AidenTestPlaylist':
//2024-11-08 14:46:22.123 15831-15831 RestaurantPlaylist      com.example.noms                     I  - Lazeez Shawarma at 123 Main St [Rating: 4.0]
//2024-11-08 14:46:22.123 15831-15831 RestaurantPlaylist      com.example.noms                     I  - Gol's Lanzhou Noodle at 150 University Ave [Rating: 5.0]
//2024-11-08 14:46:22.123 15831-15831 RestaurantPlaylist      com.example.noms                     I  - Shinwa at 456 Elm St [Rating: 1.0]


/**
 * This is the restaurant playlist card for the restaurant playlist screen.
 * 
 * @param context The context for the screen.
 * @param restaurant The restaurant to display.
 */

@Composable
fun RestaurantPlaylistCard(
    context: Context,
    restaurant: Restaurant,
) {
    val viewModel: RestaurantPlaylistCardViewModel = viewModel(
        key = restaurant.placeId,
        factory = RestaurantPlaylistCardViewModel.Factory(restaurant.placeId)
    )

    val photoBitmap by viewModel.photoBitmap.collectAsState()

    // Trigger photo fetching when the card is loaded
    LaunchedEffect(restaurant.placeId) {
        viewModel.getPhoto(context, restaurant.placeId)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(
                width = 2.dp,
                color = Color(0xFF2E8B57),
                shape = RoundedCornerShape(16.dp)
            )
            .background(Color(0xFFF8F8F8), shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Restaurant Name and Description
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = restaurant.name,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 4.dp)
            )
//            Uncomment if description is needed
//            Text(
//                text = restaurant.description,
//                style = MaterialTheme.typography.bodySmall,
//                color = Color.Gray
//            )
        }

        // Display Photo or Placeholder
        if (photoBitmap != null) {
            Image(
                bitmap = photoBitmap!!.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(Color.LightGray, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Img",
                    style = TextStyle(fontSize = 12.sp, color = Color.DarkGray)
                )
            }
        }
    }
}



/**
 * This is the restaurant playlist screen with cards for the restaurant playlist screen.
 * 
 * @param uid The user id.
 * @param viewModel The view model for managing the restaurant playlist.
 */

@Composable
fun RestaurantPlaylistScreenWithCards(
    uid: Int,
    viewModel: RestaurantPlaylistViewModel = viewModel()
) {
    val context = LocalContext.current
    val playlists by viewModel.playlists.collectAsState()
    val playlistRestaurants by viewModel.playlistRestaurants.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val loading by viewModel.loading.collectAsState()

    // Trigger data fetch when screen is composed
    LaunchedEffect(uid) {
        viewModel.fetchData(uid)
    }

    when {
        loading -> {
            // Loading State
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Loading playlists...",
                    style = TextStyle(fontSize = 18.sp, color = Color.Gray)
                )
            }
        }
        playlists.isEmpty() -> {
            // No Playlists Found
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (userName != null) "No playlists found for $userName." else "No playlists found.",
                    style = TextStyle(fontSize = 18.sp, color = Color.Gray)
                )
            }
        }
        else -> {
            // Display Playlists and Restaurants
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp)
                    .background(Color(0xFFEBEBEB), shape = RoundedCornerShape(16.dp))
                    .border(
                        width = 2.dp,
                        color = Color(0xFF2E8B57),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 64.dp)
            ) {
                playlists.forEach { playlist ->
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Playlist Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = 2.dp,
                                        color = Color(0xFF2E8B57),
                                        shape = RoundedCornerShape(50)
                                    )
                                    .background(Color(0xFFF8F8F8), shape = RoundedCornerShape(50))
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                                    .clickable {
                                        playlist.id?.let {
                                            viewModel.togglePlaylistExpansion(it)
                                        }
                                    },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Playlist Name
                                Text(
                                    text = playlist.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Black
                                )

                                // Expand/Collapse Indicator
                                Icon(
                                    imageVector = if (viewModel.isPlaylistExpanded(playlist.id!!)) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = Color(0xFF2E8B57)
                                )
                            }

                            // Restaurants under this playlist (expand/collapse)
                            if (viewModel.isPlaylistExpanded(playlist.id!!)) {
                                playlistRestaurants[playlist.id]?.let { restaurants ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        restaurants.forEach { restaurant ->
                                            RestaurantPlaylistCard(context = context, restaurant = restaurant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


/**
 * This is the restaurant playlist screen.
 * 
 * @param uid The user id.
 */

@Composable
fun RestaurantPlaylistScreen(uid: Int) {
    LaunchedEffect(uid) {
        try {
            // Directly call the suspend function without `withContext`
            fetchUserPlaylistsAndRestaurants(uid)
        } catch (e: Exception) {
            Log.e("RestaurantPlaylist", "Error in RestaurantPlaylistScreen: ${e.message}", e)
        }
    }
}

