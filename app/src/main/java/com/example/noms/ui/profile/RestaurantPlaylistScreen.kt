package com.example.noms.ui.profile

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import com.example.noms.backend.getPlaylist
import com.example.noms.backend.getPlaylistsofUser
import io.ktor.websocket.Frame
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.noms.backend.Playlist
import com.example.noms.backend.Restaurant
import com.example.noms.backend.getUser
import com.example.noms.ui.restaurants.fetchPhoto
import com.example.noms.ui.restaurants.fetchPhotoReference

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


//Fetching restaurants for playlist: AidenTestPlaylist (ID: 2)
//2024-11-08 14:46:22.122 15831-15831 RestaurantPlaylist      com.example.noms                     I  Restaurants in playlist 'AidenTestPlaylist':
//2024-11-08 14:46:22.123 15831-15831 RestaurantPlaylist      com.example.noms                     I  - Lazeez Shawarma at 123 Main St [Rating: 4.0]
//2024-11-08 14:46:22.123 15831-15831 RestaurantPlaylist      com.example.noms                     I  - Gol's Lanzhou Noodle at 150 University Ave [Rating: 5.0]
//2024-11-08 14:46:22.123 15831-15831 RestaurantPlaylist      com.example.noms                     I  - Shinwa at 456 Elm St [Rating: 1.0]



@Composable
fun RestaurantPlaylistCard(context: Context, restaurant: Restaurant) {
    var photoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Fetch photo when the card is loaded
    LaunchedEffect(restaurant.placeId) {
        coroutineScope.launch {
            val photoMetadata = fetchPhotoReference(context, restaurant.placeId)
            if (photoMetadata != null) {
                photoBitmap = fetchPhoto(context, photoMetadata)
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp) // Spacing between cards
            .border(
                width = 2.dp,
                color = Color(0xFF2E8B57), // Green border
                shape = RoundedCornerShape(16.dp) // Rounded corners
            )
            .background(Color(0xFFF8F8F8), shape = RoundedCornerShape(16.dp)) // Light grey background
            .padding(horizontal = 16.dp, vertical = 12.dp), // Inner padding
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
                style = MaterialTheme.typography.bodyMedium, // Medium text style
                color = Color.Black,
                modifier = Modifier.padding(bottom = 4.dp)
            )
//            Text(
//                text = restaurant.description,
//                style = MaterialTheme.typography.bodySmall, // Smaller text style
//                color = Color.Gray
//            )
        }

        // Display Photo
        if (photoBitmap != null) {
            Image(
                bitmap = photoBitmap!!.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp)) // Rounded corners for the image
            )
        } else {
            // Placeholder for the image while loading
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(Color.LightGray, RoundedCornerShape(8.dp)), // Placeholder styling
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Img",
                    style = TextStyle(fontSize = 12.sp, color = Color.DarkGray) // Placeholder text
                )
            }
        }
    }
}




@Composable
fun RestaurantPlaylistScreenWithCards(uid: Int) {
    val context = LocalContext.current // Get the context
    val tag = "RestaurantPlaylist"
    val playlists = remember { mutableStateListOf<Playlist>() }
    val playlistRestaurants = remember { mutableStateMapOf<Int, List<Restaurant>>() }
    var userName by remember { mutableStateOf<String?>(null) } // State for user's name
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uid) {
        coroutineScope.launch {
            try {
                // Fetch user's name
                val user = getUser(uid) // Use the existing getUser function
                userName = user?.let { "${it.first_name} ${it.last_name}" } ?: "Unknown User"

                // Fetch playlists for the user
                val fetchedPlaylists = getPlaylistsofUser(uid)
                playlists.clear()
                playlists.addAll(fetchedPlaylists)

                // Fetch restaurants for each playlist
                fetchedPlaylists.forEach { playlist ->
                    val restaurants = playlist.id?.let { getPlaylist(it) } ?: emptyList()
                    playlist.id?.let { playlistRestaurants[it] = restaurants }
                }
            } catch (e: Exception) {
                Log.e(tag, "Error fetching playlists or restaurants: ${e.message}", e)
            }
        }
    }

    if (playlists.isEmpty()) {
        // Show a message if no playlists are found
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BasicText(
                text = if (userName != null) "No playlists found for $userName." else "No playlists found.",
                style = TextStyle(fontSize = 18.sp, color = Color.Gray)
            )
        }
    } else {
        // Enclosing LazyColumn with a grey pill-shaped background and green border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 2.dp,
                    color = Color(0xFF2E8B57), // Green border
                    shape = RoundedCornerShape(16.dp)
                )
                .background(Color(0xFFEBEBEB), shape = RoundedCornerShape(16.dp)) // Grey background
                .padding(16.dp) // Padding inside the enclosing box
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 64.dp) // Add padding for better UI
            ) {
                playlists.forEach { playlist ->
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Playlist Name
                            Text(
                                text = playlist.name,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color(0xFF2E8B57),
                                modifier = Modifier
                                    .padding(bottom = 8.dp)
                                    .align(Alignment.Start)
                            )

                            // Restaurants under this playlist
                            playlistRestaurants[playlist.id]?.let { restaurants ->
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


//non grouped playlists
//@Composable
//fun RestaurantPlaylistScreenWithCards(uid: Int) {
//    val context = LocalContext.current // Get the context
//    val tag = "RestaurantPlaylist"
//    val restaurants = remember { mutableStateListOf<Restaurant>() }
//    val coroutineScope = rememberCoroutineScope()
//
//    LaunchedEffect(uid) {
//        coroutineScope.launch {
//            try {
//                // Fetch playlists for the user
//                val playlists = getPlaylistsofUser(uid)
//                restaurants.clear()
//
//                // Fetch restaurants for each playlist and combine them into a single list
//                playlists.forEach { playlist ->
//                    val playlistRestaurants = playlist.id?.let { getPlaylist(it) } ?: emptyList()
//                    restaurants.addAll(playlistRestaurants)
//                }
//            } catch (e: Exception) {
//                Log.e(tag, "Error fetching playlists or restaurants: ${e.message}", e)
//            }
//        }
//    }
//
//    if (restaurants.isEmpty()) {
//        // Show a message if no restaurants are found
//        Column(
//            modifier = Modifier.fillMaxSize(),
//            verticalArrangement = Arrangement.Center,
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            BasicText(
//                text = "No restaurants found for user $uid.",
//                style = TextStyle(fontSize = 18.sp, color = Color.Gray)
//            )
//        }
//    } else {
//        // Enclosing LazyColumn with a grey pill-shaped background and green border
//        Box(
//            modifier = Modifier
////                .fillMaxSize()
//                .height(200.dp)
//                .border(
//                    width = 2.dp,
//                    color = Color(0xFF2E8B57), // Green border
//                    shape = RoundedCornerShape(16.dp) // Rounded corners for the pill shape
//                )
//                .background(Color(0xFFEBEBEB), shape = RoundedCornerShape(16.dp)) // Grey background
//                .padding(16.dp) // Padding inside the enclosing box
//        ) {
//            LazyColumn(
//                modifier = Modifier.fillMaxSize(),
//                verticalArrangement = Arrangement.spacedBy(8.dp),
//                contentPadding = PaddingValues(bottom = 64.dp) // Add padding for better UI
//            ) {
//                items(restaurants) { restaurant ->
//                    RestaurantPlaylistCard(context = context, restaurant = restaurant) // Pass the context
//                }
//            }
//        }
//    }
//}


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

