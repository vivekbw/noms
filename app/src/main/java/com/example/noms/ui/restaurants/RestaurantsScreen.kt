package com.example.noms.ui.restaurants

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.TextFieldValue
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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.model.TypeFilter
import com.example.noms.backend.User
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
    val context = LocalContext.current
    val waterloo = LatLng(43.4643, -80.5204)
    val toronto = LatLng(43.6532, -79.3832)
    
    // Test data for restaurants in Waterloo and Toronto
    val allRestaurants = remember {
        listOf(
            Restaurant("1", "Bauer Kitchen", "The Bauer Kitchen", LatLng(43.4639, -80.5228), 4.3f),
            Restaurant("2", "Proof Kitchen", "Proof Kitchen and Lounge", LatLng(43.4606, -80.5242), 4.2f),
            Restaurant("3", "Ethel's Lounge", "Ethel's Lounge", LatLng(43.4659, -80.5233), 4.4f),
            Restaurant("4", "Wildcraft", "Wildcraft Grill + Long Bar", LatLng(43.4577, -80.5384), 4.1f),
            Restaurant("5", "Morty's Pub", "Morty's Pub", LatLng(43.4730, -80.5384), 4.0f),
            Restaurant("6", "CN Tower 360 Restaurant", "Revolving restaurant with city views", LatLng(43.6425, -79.3873), 4.5f),
            Restaurant("7", "St. Lawrence Market", "Historic food market", LatLng(43.6488, -79.3715), 4.6f),
            Restaurant("8", "Kensington Market", "Eclectic neighborhood with diverse food options", LatLng(43.6547, -79.4025), 4.4f),
            Restaurant("9", "Alo Restaurant", "Fine dining experience", LatLng(43.6479, -79.3962), 4.8f),
            Restaurant("10", "Pai Northern Thai Kitchen", "Authentic Thai cuisine", LatLng(43.6479, -79.3889), 4.5f)
        )
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(waterloo, 11f)
    }

    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    var visibleRestaurants by remember { mutableStateOf(allRestaurants) }

    LaunchedEffect(cameraPositionState.position) {
        visibleRestaurants = allRestaurants.filter { restaurant ->
            val latDiff = Math.abs(restaurant.location.latitude - cameraPositionState.position.target.latitude)
            val lngDiff = Math.abs(restaurant.location.longitude - cameraPositionState.position.target.longitude)
            latDiff < 0.1 && lngDiff < 0.1
        }
    }

    var searchQuery by remember { mutableStateOf(TextFieldValue()) }
    var searchResults by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { newValue ->
                searchQuery = newValue
                coroutineScope.launch {
                    searchResults = searchPlaces(context, newValue.text)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Search for restaurants") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") }
        )

        BottomSheetScaffold(
            scaffoldState = bottomSheetScaffoldState,
            sheetContent = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(visibleRestaurants) { restaurant ->
                        RestaurantCard(context, restaurant) {
                            coroutineScope.launch {
                                cameraPositionState.animate(
                                    update = CameraUpdateFactory.newLatLngZoom(restaurant.location, 15f),
                                    durationMs = 1000
                                )
                            }
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
                    visibleRestaurants.forEach { restaurant ->
                        Marker(
                            state = MarkerState(position = restaurant.location),
                            title = restaurant.name,
                            snippet = "Rating: ${restaurant.rating}"
                        )
                    }
                }

                // Display search results
                if (searchResults.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .background(Color.White)
                    ) {
                        items(searchResults) { prediction ->
                            Text(
                                text = prediction.getFullText(null).toString(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .clickable {
                                        coroutineScope.launch {
                                            val place = getPlaceDetails(context, prediction.placeId)
                                            place?.let { 
                                                val latLng = LatLng(it.latLng.latitude, it.latLng.longitude)
                                                cameraPositionState.animate(
                                                    update = CameraUpdateFactory.newLatLngZoom(latLng, 15f),
                                                    durationMs = 1000
                                                )
//                                                val newRestaurant = Restaurant(
//                                                    it.id,
//                                                    it.name,
//                                                    it.address ?: "",
//                                                    latLng,
//                                                    it.rating?.toFloat() ?: 0f
//                                                )
//                                                visibleRestaurants = visibleRestaurants + newRestaurant
                                            }
                                            searchQuery = TextFieldValue()
                                            searchResults = emptyList()
                                        }
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}

data class Restaurant(
    val placeId: String,
    val name: String,
    val description: String,
    val location: LatLng,
    val rating: Float
)

@Composable
fun RestaurantCard(context: Context, restaurant: com.example.noms.ui.restaurants.Restaurant, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .height(100.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = restaurant.name, style = MaterialTheme.typography.titleMedium, color = Color.Black)
            Text(text = restaurant.description, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            RatingBar(rating = restaurant.rating)
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

suspend fun searchPlaces(context: Context, query: String): List<AutocompletePrediction> {
    return withContext(Dispatchers.IO) {
        val placesClient = Places.createClient(context)
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setTypeFilter(TypeFilter.ESTABLISHMENT)
            .build()

        try {
            val response = placesClient.findAutocompletePredictions(request).await()
            response.autocompletePredictions.filter { prediction ->
                prediction.placeTypes.contains(Place.Type.RESTAURANT)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

suspend fun getPlaceDetails(context: Context, placeId: String): Place? {
    return withContext(Dispatchers.IO) {
        val placesClient = Places.createClient(context)
        val request = FetchPlaceRequest.newInstance(placeId, listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.LAT_LNG,
            Place.Field.ADDRESS,
            Place.Field.RATING
        ))

        try {
            val response = placesClient.fetchPlace(request).await()
            response.place
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
