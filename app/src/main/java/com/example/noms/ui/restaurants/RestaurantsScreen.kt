package com.example.noms.ui.restaurants

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.PhotoMetadata
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import com.example.noms.backend.Restaurant
import com.example.noms.backend.User
import com.example.noms.backend.getAllRestaurants
import com.example.noms.backend.getAllUsers

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

// Aiden's
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantsScreen(innerPadding: PaddingValues) {
    val restaurants = remember { mutableStateListOf<Restaurant>() }
    val context = LocalContext.current

    // Simulate data fetching
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            // Karthik: Replaced with real data
            val realData = getAllRestaurants()
            restaurants.addAll(realData)
        }
    }

    val waterloo = LatLng(43.4643, -80.5204)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(waterloo, 10f)
    }

    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState,
        sheetContent = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(restaurants) { restaurant ->
                    RestaurantCard(context, restaurant) {
                        println("Clicked on ${restaurant.name}")
                    }
                }
            }
        },
        sheetPeekHeight = 200.dp
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            GoogleMap(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { _ ->
                            coroutineScope.launch {
                                if (bottomSheetScaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
                                    bottomSheetScaffoldState.bottomSheetState.partialExpand()
                                }
                            }
                        }
                    },
                cameraPositionState = cameraPositionState
            ) {
                Marker(
                    state = MarkerState(position = waterloo),
                    title = "Waterloo",
                    snippet = "Marker in Waterloo"
                )
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
    Row {
        repeat(5) { index ->
            Icon(
                imageVector = if (index < rating.toInt()) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = "Star",
                tint = Color(0xFF2E8B57),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun UserList() {
    val users = remember { mutableStateListOf<User>() }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val results = getAllUsers()
            users.addAll(results)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 100.dp
        ),
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