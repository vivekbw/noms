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
import kotlinx.coroutines.launch
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.layout.ContentScale
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
import androidx.annotation.RequiresApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.example.noms.backend.*


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


//@Preview(showBackground = true)
//@Composable
//fun PreviewPhotoGridScreen() {
//    MaterialTheme {
//        Surface {
//            PhotoGridScreen()
//        }
//    }
//}



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


//@Preview(showBackground = true)
//@Composable
//fun PreviewReviewScreen() {
//    MaterialTheme {
//        Surface {
//            ReviewScreen { review, rating ->
//                println("Review: $review, Rating: $rating")
//            }
//        }
//    }
//}
//





@RequiresApi(Build.VERSION_CODES.O)
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
                    try {
                        // Write the review
                        writeReview(4, restaurant.rid ?: 0, reviewText, rating)
                        
                        // Add review to local list
                        reviews.add(
                            Review(
                                id = null,
                                created_date = "today",
                                uid = 0,
                                rid = restaurant.rid ?: 0,
                                text = reviewText,
                                rating = rating
                            )
                        )
                        
                        // Calculate new average rating
                        val newAverageRating = reviews.map { it.rating }.average().toFloat()
                        
                        // Update restaurant rating in Supabase
                        updateRestaurantRating(restaurant.rid ?: 0, newAverageRating)
                        
                        // Update local restaurant object
                        restaurant.rating = newAverageRating
                        
                        Log.d("TAG", "Review submitted: $reviewText, Rating: $rating, New Average: $newAverageRating")
                    } catch (e: Exception) {
                        Log.e("TAG", "Error submitting review: ${e.message}")
                    }
                }
            }
        }
    }
}



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
//                println("Review submitted: $review, Rating: $rating")
//            }
//        )
//    }
//}
