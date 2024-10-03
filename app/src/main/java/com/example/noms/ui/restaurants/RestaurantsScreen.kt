package com.example.noms.ui.restaurants

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.foundation.clickable
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.Serializable
import android.graphics.Bitmap
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPhotoRequest

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import com.google.android.libraries.places.api.model.PhotoMetadata
import kotlinx.coroutines.launch



val supabase = createSupabaseClient(
    // both keys are meant to be exposed to client, so no security issues
    supabaseUrl = "https://xoffilinikbhnlvdfaib.supabase.co",
    supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InhvZmZpbGluaWtiaG5sdmRmYWliIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Mjc0OTYxMzEsImV4cCI6MjA0MzA3MjEzMX0.2x8XkQS3ahCmYJJHSn6581ki2wh4-mbcWzBEUEmGtu0"
) {
    install(Postgrest)
}

//@Composable
//fun RestaurantsScreen() {
//    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//        Text("Restaurants Screen")
//    }
//}
//


suspend fun fetchPhotoReference(context: Context, placeId: String): PhotoMetadata? {
    return withContext(Dispatchers.IO) {
        val placesClient = Places.createClient(context)
        val placeFields = listOf(Place.Field.PHOTO_METADATAS)
        val request = FetchPlaceRequest.newInstance(placeId, placeFields)
        val response = placesClient.fetchPlace(request).await()
        response.place.photoMetadatas?.firstOrNull()
    }
}

suspend fun fetchPhoto(context: Context, photoMetadata: PhotoMetadata): Bitmap? {
    return withContext(Dispatchers.IO) {
        val placesClient = Places.createClient(context)
        val photoRequest = FetchPhotoRequest.builder(photoMetadata).build()
        val response = placesClient.fetchPhoto(photoRequest).await()
        response.bitmap
    }
}



@Serializable
data class Restaurant(
    val id: Int,
    val name: String,
    val description: String,
    val location: String,
    val rating: Float,
    val placeId: String
)

@Composable
fun RestaurantsScreen() {

    val restaurants = remember { mutableStateListOf<Restaurant>() }
    val context = LocalContext.current

    // Simulate data fetching
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            // Replace with actual data fetching from Supabase or any other source
            val mockData = listOf(
                Restaurant(1, "Lazeez Shawarma", "Best shawarma in town", "123 Main St", 4.5f, "ChIJ8dUjLgH0K4gREB0QrExd6W4"),
                Restaurant(2, "Shinwa", "Authentic Japanese cuisine", "456 Elm St", 4.0f, "ChIJg8Gc9iP1K4gREgG-kyXe6tk")
            )
            restaurants.addAll(mockData)
        }
    }

    // Show the restaurant cards in a LazyColumn
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(restaurants) { restaurant ->
            RestaurantCard(context, restaurant) {
                println("Clicked on ${restaurant.name}")
            }
        }
    }
}


@Composable
fun RestaurantCard(context: Context, restaurant: Restaurant, onClick: () -> Unit) {
    var photoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Card layout without showing image initially
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .height(150.dp)
            .clickable {
                // Launch a coroutine to fetch the image
                coroutineScope.launch {
                    val photoMetadata = fetchPhotoReference(context, restaurant.placeId)
                    if (photoMetadata != null) {
                        photoBitmap = fetchPhoto(context, photoMetadata)
                    }
                }
                onClick()
                showDialog = true // Show image dialog on click
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween // Keep space between elements
        ) {
            Text(text = restaurant.name, style = MaterialTheme.typography.titleMedium, color = Color.Black)
            Text(text = restaurant.description, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Text(text = "Location: ${restaurant.location}", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
            RatingBar(rating = restaurant.rating) // Display rating
        }
    }

    // Show image dialog if showDialog is true and the photoBitmap is available
    if (showDialog && photoBitmap != null) {
        ImageDialog(photoBitmap!!) {
            showDialog = false // Hide the dialog when dismissed
        }
    }
}

@Composable
fun ImageDialog(photoBitmap: Bitmap, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
//        title = { Text("Restaurant Image") },
        text = {
            Image(
                bitmap = photoBitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}



@Composable
fun RatingBar(rating: Float) {
    // Simplified rating bar: display stars based on the rating
    Row {
        repeat(5) { index ->
            Icon(
                imageVector = if (index < rating.toInt()) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = "Star",
                tint = Color.Yellow,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}


@Serializable
data class User (
    val id: Int,
    val first_name: String,
    val last_name: String,
    val phone_number: String
)

@Composable
fun UserList() {
    val users = remember { mutableStateListOf<User>() }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val results = supabase.from("users").select().decodeList<User>()
            users.addAll(results)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(users) { user ->
            UserCard(user)
        }
    }
}

@Composable
fun UserCard(user: User) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${user.first_name} ${user.last_name}",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF2E8B57)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = user.phone_number,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}
