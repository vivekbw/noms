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
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.navigation.NavController
import com.example.noms.backend.*
import io.github.jan.supabase.exceptions.HttpRequestException
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest


@Composable
fun StarRatingBar(
    maxStars: Int = 5,
    rating: Float,
    onRatingChanged: (Float) -> Unit
) {
    val density = LocalDensity.current.density
    val starSize = (8f * density).dp
    val starSpacing = (0.5f * density).dp

    Row(
        modifier = Modifier.selectableGroup(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        for (i in 1..maxStars) {
            val isSelected = i <= rating
            val icon = if (isSelected) Icons.Filled.Star else Icons.Default.Star
            val iconTintColor = if (isSelected) Color(0xFF2E8B57) else Color.Gray
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTintColor,
                modifier = Modifier
                    .selectable(
                        selected = isSelected,
                        onClick = {
                            onRatingChanged(i.toFloat())
                        }
                    )
                    .width(starSize).height(starSize)
            )

            if (i < maxStars) {
                Spacer(modifier = Modifier.width(starSpacing))
            }
        }
    }
}



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
fun RestaurantsScreen(navController: NavController, innerPadding: PaddingValues) {
    val restaurants = remember { mutableStateListOf<Restaurant>() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()
    
    var searchQuery by remember { mutableStateOf(TextFieldValue()) }
    var searchResults by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    
    val waterloo = LatLng(43.4643, -80.5204)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(waterloo, 10f)
    }

    LaunchedEffect(Unit) {
        try {
            val fetchedRestaurants = getAllRestaurants()
            restaurants.clear()
            restaurants.addAll(fetchedRestaurants)
        } catch (e: Exception) {
            Log.e("RestaurantsScreen", "Error fetching restaurants: ${e.message}")
        }
    }

    fun parseLocationString(locationStr: String): LatLng? {
        return try {
            val (lat, lng) = locationStr.split(",").map { it.trim().toDouble() }
            LatLng(lat, lng)
        } catch (e: Exception) {
            null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                scaffoldState = scaffoldState,
                sheetContent = {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        items(restaurants) { restaurant ->
                            RestaurantCard(
                                context = context,
                                restaurant = restaurant,
                                onAddReviewClick = {
                                    navController.navigate("restaurantDetails/${restaurant.placeId}")
                                },
                                onCardClick = {
                                    parseLocationString(restaurant.location)?.let { latLng ->
                                        coroutineScope.launch {
                                            cameraPositionState.animate(
                                                update = CameraUpdateFactory.newLatLngZoom(latLng, 15f),
                                                durationMs = 1000
                                            )
                                        }
                                    }
                                }
                            )
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
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState
                    ) {
                        restaurants.forEach { restaurant ->
                            parseLocationString(restaurant.location)?.let { latLng ->
                                Marker(
                                    state = MarkerState(position = latLng),
                                    title = restaurant.name,
                                    snippet = "Rating: ${restaurant.rating}",
                                    onClick = {
                                        coroutineScope.launch {
                                            cameraPositionState.animate(
                                                update = CameraUpdateFactory.newLatLngZoom(latLng, 15f),
                                                durationMs = 1000
                                            )
                                        }
                                        true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (searchResults.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 80.dp)
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
                                            update = CameraUpdateFactory.newLatLngZoom(latLng, 18f),
                                            durationMs = 1000
                                        )
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



@Composable
fun RestaurantCard(
    context: Context,
    restaurant: Restaurant,
    onAddReviewClick: () -> Unit,
    onCardClick: () -> Unit
) {
    var photoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(restaurant.placeId) {
        val photoMetadata = fetchPhotoReference(context, restaurant.placeId)
        if (photoMetadata != null) {
            photoBitmap = fetchPhoto(context, photoMetadata)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clickable { onCardClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = restaurant.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = restaurant.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Location: ${restaurant.location}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    StarRatingBar(
                        maxStars = 5,
                        rating = restaurant.rating,
                        onRatingChanged = { newRating ->
                            println("New rating: $newRating")
                        }
                    )
                }

                if (photoBitmap != null) {
                    Image(
                        bitmap = photoBitmap!!.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(125.dp)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    // Placeholder for the image
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .padding(8.dp)
                            .background(Color.LightGray, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Loading...", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Button(
                onClick = onAddReviewClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E8B57))
            ) {
                Text(text = "Add a Review", color = Color.White)
            }
        }
    }
}


@Composable
fun ReviewsList(reviews: List<Review>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(reviews) { review ->
            ReviewCard(review)
        }
    }
}

@Composable
fun ReviewCard(review: Review) {
    val user = remember { mutableStateOf<User?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(review.uid) {
        coroutineScope.launch {
            try {
                user.value = getUser(review.uid)
            } catch (e: HttpRequestException) {
                Log.e("ReviewCard", "Failed to fetch user: ${e.message}")
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = user.value?.let { "${it.first_name} ${it.last_name}" } ?: "Loading...",
                style = MaterialTheme.typography.titleSmall,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = review.text,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(4.dp))

            StarRatingBar(
                maxStars = 5,
                rating = review.rating,
                onRatingChanged = {}
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
