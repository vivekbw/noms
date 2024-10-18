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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
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
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

val supabase = createSupabaseClient(
    // both keys are meant to be exposed to client, so no security issues
    supabaseUrl = "https://xoffilinikbhnlvdfaib.supabase.co",
    supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InhvZmZpbGluaWtiaG5sdmRmYWliIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Mjc0OTYxMzEsImV4cCI6MjA0MzA3MjEzMX0.2x8XkQS3ahCmYJJHSn6581ki2wh4-mbcWzBEUEmGtu0"
) {
    install(Postgrest)
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



@Serializable
data class Restaurant(
    val id: Int,
    val name: String,
    val description: String,
    val location: String,
    val rating: Float,
    val placeId: String
)

// Aiden's
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantsScreen() {
    val restaurants = remember { mutableStateListOf<Restaurant>() }
    val context = LocalContext.current

    // Simulate data fetching
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            // Replace with actual data fetching from Supabase or any other source
            val mockData = listOf(
                Restaurant(1, "Lazeez Shawarma", "Best shawarma in town", "123 Main St", 3.5f, "ChIJ8dUjLgH0K4gREB0QrExd6W4"),
                Restaurant(2, "Shinwa", "Authentic Japanese cuisine", "456 Elm St", 4.0f, "ChIJg8Gc9iP1K4gREgG-kyXe6tk"),
                Restaurant(3, "Williams Fresh Cafe", "Cozy coffee shop", "789 Oak St", 4.2f, "ChIJf93czgb0K4gR2anL3Rkcy3c"),
                Restaurant(4, "Campus Pizza", "Delicious pizzas", "321 Maple St", 4.8f, "ChIJP_Ie6gb0K4gRO7D5w_qpCyE"),
                Restaurant(5, "Gols", "Chinese", "321 Maple St", 4.8f, "ChIJs-uUWrL1K4gRmr2UyfjqxBo")
            )
            restaurants.addAll(mockData)
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
//                contentPadding = PaddingValues(16.dp)
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
    var userRating by remember { mutableStateOf(restaurant.rating) } // User's selected rating
    var showReviews by remember { mutableStateOf(false) } // State for showing reviews
    var showAddReviewDialog by remember { mutableStateOf(false) } // State for showing add review dialog
    val reviews = remember { mutableStateListOf(
        Review("Ahmed Ahmed", 4.5f, "What a great Spot Would recommend to everyone"),
        Review("John Doe", 4.0f, "Tasty food, friendly service."),
        Review("Jane Smith", 5.0f, "Excellent place! Best in town."),
        Review("Emily Davis", 3.5f, "Good food, but slow service.")
    ) }
    val coroutineScope = rememberCoroutineScope()

    // Launch coroutine to fetch the image as soon as the card is composed
    LaunchedEffect(restaurant.placeId) {
        coroutineScope.launch {
            val photoMetadata = fetchPhotoReference(context, restaurant.placeId)
            if (photoMetadata != null) {
                photoBitmap = fetchPhoto(context, photoMetadata)
            }
        }
    }

    // Card layout with image on the right-hand side
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .height(190.dp)
            .clickable(onClick = onClick), // Only handles onClick for reviews, no longer for image
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Text content on the left-hand side
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
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

                RatingBar(rating = userRating) { newRating ->
                    userRating = newRating
                    // You can also update restaurant.rating or trigger other side effects
                }

                // Button to show reviews
                Row {
                    TextButton(onClick = { showReviews = true }) {
                        Text("Show Reviews")
                    }

                    TextButton(onClick = { showAddReviewDialog = true }) {
                        Text("Add Review")
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Display the image on the right-hand side if available
            photoBitmap?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Restaurant Image",
                    modifier = Modifier
                        .size(110.dp) // Set the size of the image
                        .clip(RoundedCornerShape(8.dp)) // Optionally round the corners
                        .background(Color.LightGray) // Placeholder background
                )
            }
        }
    }

    // Show reviews dialog if showReviews is true
    if (showReviews) {
        ReviewsDialog(reviews) {
            showReviews = false // Hide reviews dialog when dismissed
        }
    }

    // Show add review dialog if showAddReviewDialog is true
    if (showAddReviewDialog) {
        AddReviewDialog(
            onSubmit = { name, rating, comment ->
                reviews.add(Review(name, rating, comment)) // Add the new review to the list
                showAddReviewDialog = false
            },
            onDismiss = { showAddReviewDialog = false }
        )
    }
}


@Composable
fun ReviewsDialog(reviews: List<Review>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(reviews) { review ->
                    ReviewCard(review)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun ReviewCard(review: Review) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Reviewer's name
            Text(
                text = review.name,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black
            )

            // Rating bar displaying the static rating
            RatingBar1(rating = review.rating)

            // Review comment
            Text(
                text = review.comment,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}


@Composable
fun RatingBar1(
    rating: Float // Current rating value
) {
    Row {
        repeat(5) { index ->
            Icon(
                imageVector = if (index < rating.toInt()) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = "Star",
                tint = Color(0xFF2E8B57), // Custom star color
                modifier = Modifier.size(20.dp)
            )
        }
    }
}






data class Review(val name: String, val rating: Float, val comment: String)

@Composable
fun AddReviewDialog(onSubmit: (String, Float, String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(0f) } // Keep track of rating as Float
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Add Review", style = MaterialTheme.typography.titleMedium)

                Spacer(modifier = Modifier.height(8.dp))

                // Input for reviewer's name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Rating bar to select the rating
                RatingBar(rating = rating, onRatingChanged = { newRating ->
                    rating = newRating
                })

                Spacer(modifier = Modifier.height(8.dp))

                // Input for review comment
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comment") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && rating in 0f..5f && comment.isNotBlank()) {
                        onSubmit(name, rating, comment) // Submit the review with correct rating
                    }
                }
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
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
fun RatingBar(
    rating: Float, // Current rating value
    onRatingChanged: (Float) -> Unit // Callback for when the rating changes
) {
    var selectedRating by remember { mutableStateOf(rating) } // Keep track of the selected rating

    Row {
        repeat(5) { index ->
            Icon(
                imageVector = if (index < selectedRating.toInt()) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = "Star",
                tint = Color(0xFF2E8B57),
                modifier = Modifier
                    .size(24.dp)
                    .clickable {
                        // Update the rating when a star is clicked
                        selectedRating = (index + 1).toFloat()
                        onRatingChanged(selectedRating)
                    }
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