package com.example.noms.ui.restaurants

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.noms.backend.Playlist
import com.example.noms.backend.Restaurant
import com.example.noms.backend.Review
import com.example.noms.backend.User
import com.example.noms.backend.fetchPhoto
import com.example.noms.backend.fetchPhotoReference
import com.example.noms.backend.getUser
import com.example.noms.backend.parseLocationString
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import io.github.jan.supabase.exceptions.HttpRequestException
import kotlinx.coroutines.launch
import android.Manifest
import com.google.maps.android.compose.Marker
import com.example.noms.backend.addRestaurant
import com.example.noms.backend.getAllRestaurants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantsScreen(
    navController: NavController,
    innerPadding: PaddingValues,
    viewModel: RestaurantsViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    val scaffoldState = rememberBottomSheetScaffoldState()
    val cameraPositionState = rememberCameraPositionState {
        position = viewModel.currentCameraPosition.value
    }

    val context = LocalContext.current

    // Observing ViewModel states
    val restaurants = viewModel.restaurants
    val visibleRestaurants = viewModel.visibleRestaurants
    val playlists = viewModel.playlists
    val searchQuery = viewModel.searchQuery.value
    val searchResults = viewModel.searchResults.value
    val showDialog = viewModel.showDialog.value
    val selectedPrediction = viewModel.selectedPrediction.value
    val showPlaylistDialog = viewModel.showPlaylistDialog.value
    val selectedPlaylistId = viewModel.selectedPlaylistId.value

    // Location Permission Launcher (Handle in Activity layer ideally)
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            viewModel.hasLocationPermission.value =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        }
    )

    // Request permissions on first launch
    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // Update camera position when ViewModel's current position changes
    LaunchedEffect(viewModel.currentCameraPosition.value) {
        cameraPositionState.position = viewModel.currentCameraPosition.value
    }

    // Update visible restaurants when camera position changes
    LaunchedEffect(cameraPositionState.position) {
        val latLng = cameraPositionState.position.target
        viewModel.updateVisibleRestaurants(latLng)
    }

    if (isLandscape) {
        LandscapeLayout(
            restaurants = visibleRestaurants,
            cameraPositionState = cameraPositionState,
            viewModel = viewModel,
            navController = navController,
            context = context
        )
    } else {
        PortraitLayout(
            restaurants = visibleRestaurants,
            cameraPositionState = cameraPositionState,
            viewModel = viewModel,
            navController = navController,
            context = context,
            scaffoldState = scaffoldState
        )
    }

    // Search Results Overlay
    if (searchResults.isNotEmpty()) {
        SearchResultsOverlay(
            searchResults = searchResults,
            onResultClick = { prediction ->
                viewModel.onSearchResultSelected(prediction)
            }
        )
    }

    // Add Restaurant Dialog
    if (showDialog && selectedPrediction != null) {
        AddRestaurantDialog(
            prediction = selectedPrediction,
            onConfirm = { viewModel.addRestaurantFromPrediction() },
            onAddToPlaylist = { viewModel.showPlaylistDialog.value = true },
            onDismiss = { viewModel.showDialog.value = false }
        )
    }

    // Add to Playlist Dialog
    if (showPlaylistDialog) {
        AddToPlaylistDialog(
            playlists = playlists,
            selectedPlaylistId = selectedPlaylistId,
            onSelect = { pid -> viewModel.selectedPlaylistId.value = pid },
            onConfirm = { viewModel.addRestaurantToPlaylist() },
            onDismiss = { viewModel.showPlaylistDialog.value = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandscapeLayout(
    restaurants: List<Restaurant>,
    cameraPositionState: CameraPositionState,
    viewModel: RestaurantsViewModel,
    navController: NavController,
    context: Context
) {
    Row(modifier = Modifier.fillMaxSize()) {
        // Left Panel - List
        Box(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                SearchBar(
                    query = viewModel.searchQuery.value,
                    onQueryChange = { viewModel.onSearchQueryChanged(it) }
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 16.dp)
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
                                    viewModel.updateVisibleRestaurants(latLng)
                                    viewModel.currentCameraPosition.value =
                                        CameraPosition.fromLatLngZoom(latLng, 15f)
                                }
                            }
                        )
                    }
                }
            }
        }

        // Right Panel - Map
        Box(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight()
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = viewModel.hasLocationPermission.value)
            ) {
                restaurants.forEach { restaurant ->
                    parseLocationString(restaurant.location)?.let { latLng ->
                        Marker(
                            state = MarkerState(position = latLng),
                            title = restaurant.name,
                            snippet = "Rating: ${restaurant.rating}",
                            onClick = {
                                viewModel.currentCameraPosition.value =
                                    CameraPosition.fromLatLngZoom(latLng, 15f)
                                true
                            }
                        )
                    }
                }
            }

            MyLocationButton(
                onClick = { viewModel.getCurrentLocation() },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortraitLayout(
    restaurants: List<Restaurant>,
    cameraPositionState: CameraPositionState,
    viewModel: RestaurantsViewModel,
    navController: NavController,
    context: Context,
    scaffoldState: BottomSheetScaffoldState
) {
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
                                viewModel.updateVisibleRestaurants(latLng)
                                viewModel.currentCameraPosition.value =
                                    CameraPosition.fromLatLngZoom(latLng, 15f)
                            }
                        }
                    )
                }
            }
        },
        sheetPeekHeight = 200.dp
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            SearchBar(
                query = viewModel.searchQuery.value,
                onQueryChange = { viewModel.onSearchQueryChanged(it) }
            )

            Box(modifier = Modifier.weight(1f)) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = viewModel.hasLocationPermission.value)
                ) {
                    restaurants.forEach { restaurant ->
                        parseLocationString(restaurant.location)?.let { latLng ->
                            Marker(
                                state = MarkerState(position = latLng),
                                title = restaurant.name,
                                snippet = "Rating: ${restaurant.rating}",
                                onClick = {
                                    viewModel.currentCameraPosition.value =
                                        CameraPosition.fromLatLngZoom(latLng, 15f)
                                    true
                                }
                            )
                        }
                    }
                }

                MyLocationButton(
                    onClick = { viewModel.getCurrentLocation() },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = { onQueryChange(it) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        placeholder = { Text("Search for restaurants") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") }
    )
}

@Composable
fun SearchResultsOverlay(
    searchResults: List<AutocompletePrediction>,
    onResultClick: (AutocompletePrediction) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 80.dp)
            .background(Color.White)
    ) {
        LazyColumn {
            items(searchResults) { prediction ->
                Text(
                    text = prediction.getFullText(null).toString(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable { onResultClick(prediction) }
                )
            }
        }
    }
}

@Composable
fun AddRestaurantDialog(
    prediction: AutocompletePrediction,
    onConfirm: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = prediction.getFullText(null).toString(),
                style = MaterialTheme.typography.titleMedium
            )
        },
        confirmButton = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E8B57))
                ) {
                    Text("Add Review")
                }

                Button(
                    onClick = onAddToPlaylist,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E8B57))
                ) {
                    Text("Add to Playlist")
                }
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun AddToPlaylistDialog(
    playlists: List<Playlist>,
    selectedPlaylistId: Int?,
    onSelect: (Int?) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Playlist") },
        text = {
            LazyColumn {
                items(playlists) { playlist ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(playlist.id) }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = playlist.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (selectedPlaylistId == playlist.id)
                                Color(0xFF2E8B57) else Color.Black
                        )
                        if (selectedPlaylistId == playlist.id) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Selected",
                                tint = Color(0xFF2E8B57)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = selectedPlaylistId != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedPlaylistId != null) Color(0xFF2E8B57) else Color.Gray
                )
            ) {
                Text("Add to Playlist")
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
fun MyLocationButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .background(Color.White, CircleShape)
            .size(40.dp)
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "My Location",
            tint = Color(0xFF2E8B57)
        )
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
                            // Handle rating change if needed
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
                onClick = {
                    // Navigate immediately
                    onAddReviewClick()
                    
                    // Handle restaurant addition in the background
                    coroutineScope.launch {
                        try {
                            val existingRestaurants = getAllRestaurants()
                            if (existingRestaurants.none { it.placeId == restaurant.placeId }) {
                                addRestaurant(
                                    location = restaurant.location,
                                    name = restaurant.name,
                                    placeId = restaurant.placeId
                                )
                            }
                        } catch (e: Exception) {
                            Log.e("RestaurantCard", "Error handling restaurant: ${e.message}")
                        }
                    }
                },
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
            val icon = if (isSelected) Icons.Filled.Star else Icons.Outlined.Star
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
