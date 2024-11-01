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
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import com.example.noms.*
import androidx.compose.foundation.Canvas
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.lazy.grid.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.noms.backend.*
import io.github.jan.supabase.exceptions.HttpRequestException


@Composable
fun PhotoGridScreen() {
    val photos = remember { mutableStateListOf<Bitmap?>() }
    val maxPhotos = 6
    val coroutineScope = rememberCoroutineScope()

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(maxPhotos) { index ->
            val photo = photos.getOrNull(index)
            PhotoUploadSlot(photo) { bitmap ->
                // Update or add the photo to the list
                if (index < photos.size) {
                    photos[index] = bitmap
                } else {
                    photos.add(bitmap)
                }

                // Launch coroutine to store the photo
                coroutineScope.launch {
                    Log.d("TAG", "Storing data")
                    storeReviewImage(bitmap, 1, 1)  // Use `bitmap` instead of `photo`
                }
            }
        }
    }
}


@Composable
fun PhotoUploadSlot(
    photo: Bitmap?,
    onPhotoSelected: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val bitmap = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            } else {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            }
            onPhotoSelected(bitmap)
        }
    }

    Box(
        modifier = Modifier
            .size(100.dp)
            .background(Color.Black, RoundedCornerShape(8.dp))
            .clickable { launcher.launch("image/*") },
        contentAlignment = Alignment.Center
    ) {
        if (photo != null) {
            Image(
                bitmap = photo.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Upload Photo",
                modifier = Modifier.size(32.dp),
                tint = Color.White
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewPhotoGridScreen() {
    MaterialTheme {
        Surface {
            PhotoGridScreen()
        }
    }
}



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

@Composable
fun ReviewScreen(onReviewSubmit: (String, Float) -> Unit) {
    var reviewText by remember { mutableStateOf("") }
    var rating by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
//        Text(text = "Leave a Review", style = MaterialTheme.typography.titleMedium)


        // Star Rating Bar
        StarRatingBar(
            maxStars = 5,
            rating = rating,
            onRatingChanged = { newRating -> rating = newRating }
        )

        // Review Input Field
        OutlinedTextField(
            value = reviewText,
            onValueChange = { reviewText = it },
            label = { Text("Write your review") },
            placeholder = { Text("Share your experience...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            textStyle = TextStyle(fontSize = 16.sp),
            maxLines = 5
        )

        // Submit Button
        Button(
            onClick = {
                onReviewSubmit(reviewText, rating)
                // Clear inputs after submission
                reviewText = ""
                rating = 0f
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            enabled = rating > 0 && reviewText.isNotBlank()
        ) {
            Text("Submit Review")
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewReviewScreen() {
    MaterialTheme {
        Surface {
            ReviewScreen { review, rating ->
                println("Review: $review, Rating: $rating")
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

    BottomSheetScaffold(
        scaffoldState = rememberBottomSheetScaffoldState(),
        sheetContent = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(restaurants) { restaurant ->
                    RestaurantCard(context, restaurant) {
                        // Navigate to the combined screen on click
                        navController.navigate("restaurantDetails/${restaurant.placeId}")
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
                modifier = Modifier.fillMaxSize(),
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
    val coroutineScope = rememberCoroutineScope()

    // Launch coroutine to fetch the image when the card is loaded
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
            .height(150.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Column for text on the left
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
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
                    // you should not change restauarant rating here, it should be a api call to update restaurant
                    onRatingChanged = { newRating ->
                        restaurant.rating = newRating
                        println("New rating: $newRating")
                    }
                )
            }

            // Image on the right side
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
    }
}



//@Composable
//fun ImageDialog(photoBitmap: Bitmap, onDismiss: () -> Unit) {
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        text = {
//            Image(
//                bitmap = photoBitmap.asImageBitmap(),
//                contentDescription = null,
//                modifier = Modifier.fillMaxWidth()
//            )
//        },
//        confirmButton = {
//            TextButton(onClick = onDismiss) {
//                Text("Close")
//            }
//        }
//    )
//}

//@Composable
//fun RatingBar(rating: Float) {
//    Row {
//        repeat(5) { index ->
//            Icon(
//                imageVector = if (index < rating.toInt()) Icons.Filled.Star else Icons.Outlined.Star,
//                contentDescription = "Star",
//                tint = Color(0xFF2E8B57),
//                modifier = Modifier.size(20.dp)
//            )
//        }
//    }
//}

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
fun ReviewsList(reviews: List<Review>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)  // Limit height to avoid infinite constraints
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
                onRatingChanged = {}  // No-op since this is read-only
            )
        }
    }
}


@Composable
fun RestaurantDetailsScreen(
    restaurant: Restaurant,
    onReviewSubmit: (String, Float) -> Unit = { _, _ -> }
) {
    // State to hold the list of reviews loaded from the suspend function
    val reviews = remember { mutableStateListOf<Review>() }
    val coroutineScope = rememberCoroutineScope()

    // Launch coroutine to load reviews when the composable is first composed
    LaunchedEffect(restaurant.rid) {
        restaurant.rid?.let { rid ->
            val fetchedReviews = getReviewsFromRestaurant(rid)
            reviews.clear() // Clear existing items if any
            reviews.addAll(fetchedReviews)
        }
    }
    Log.e("TAG", "reviews retrieved")
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Photo Grid Section
        item {
            Text(text = "Upload Photos", style = MaterialTheme.typography.titleMedium)
            PhotoGridScreen()
        }

        // Divider
        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        }

        // Existing Reviews Section
        item {
            Text(text = "Reviews", style = MaterialTheme.typography.titleMedium)
            ReviewsList(reviews) // Display the reactive list of reviews
        }

        // Divider before review section
        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        }

        // Review Section Header
        item {
            Text(text = "Leave a Review", style = MaterialTheme.typography.titleMedium)

            // Review Screen with a callback to add new reviews to the list
            ReviewScreen { reviewText, rating ->
                coroutineScope.launch {
                    writeReview(4, restaurant.rid ?: 0, reviewText, rating) // Call writeReview in a coroutine (hard coded uid)
                    reviews.add(
                        Review(
                            id = null, // Replace with appropriate id logic if needed
                            created_date = "today", // Replace with actual date
                            uid = 0, // Replace with the current user ID if available
                            rid = restaurant.rid ?: 0,
                            text = reviewText,
                            rating = rating
                        )
                    )
                    Log.d("TAG", "Review submitted: $reviewText, Rating: $rating")
                }
            }
        }
    }
}



@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun PreviewRestaurantDetailsScreen() {
    val sampleRestaurant = Restaurant(
        rid = 10,
        placeId = "ChIJ8dUjLgH0K4gREB0QrExd6W4",
        name = "Sample Restaurant",
        description = "A delightful place to enjoy delicious meals.",
        location = "123 Main Street, Waterloo, ON",
        rating = 4.5f
    )

    MaterialTheme {
        RestaurantDetailsScreen(
            restaurant = sampleRestaurant,
            onReviewSubmit = { review, rating ->
                println("Review submitted: $review, Rating: $rating")
            }
        )
    }
}








//DIVIDER
//@Composable
//fun RestaurantDetailsScreen(restaurant: Restaurant, onReviewSubmit: (String, Float) -> Unit) {
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        verticalArrangement = Arrangement.spacedBy(24.dp)
//    ) {
//        // Photo Grid Section
//        Text(text = "Upload Photos", style = MaterialTheme.typography.titleMedium)
//        PhotoGridScreen()
//
//        // Divider
//        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
//
//        // Review Section
////        Text(text = "Leave a Review", style = MaterialTheme.typography.titleMedium)
//        ReviewScreen { review, rating ->
//            onReviewSubmit(review, rating)
//            println("Review submitted for ${restaurant.name}: $review, Rating: $rating")
//        }
//    }
//}



//NO DIVIDER
//@Composable
//fun RestaurantDetailsScreen(restaurant: Restaurant, onReviewSubmit: (String, Float) -> Unit) {
//    LazyColumn(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        verticalArrangement = Arrangement.spacedBy(24.dp)
//    ) {
//        item {
//            Text(text = "Upload Photos", style = MaterialTheme.typography.titleMedium)
//            PhotoGridScreen()  // Uses a fixed height to avoid layout issues
//        }
//
////        item {
////            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
////        }
//        item {
//            ReviewScreen(onReviewSubmit)  // Takes only the space it needs
//        }
//    }
//}



//@Preview(showBackground = true, widthDp = 360, heightDp = 640)
//@Composable
//fun PreviewRestaurantDetailsScreen() {
//    val sampleRestaurant = Restaurant(
//        rid = 10,
//        placeId = "ChIJ8dUjLgH0K4gREB0QrExd6W4",
//        name = "Sample Restaurant",
//        description = "A delightful place to enjoy delicious meals.",
//        location = "123 Main Street, Waterloo, ON",
//        rating = 4.5f
//    )
//
//    MaterialTheme {
//        RestaurantDetailsScreen(
//            restaurant = sampleRestaurant,
//            onReviewSubmit = { review, rating ->
//                println("Review: $review, Rating: $rating")
//            }
//        )
//    }
//}





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