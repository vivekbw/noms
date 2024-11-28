package com.example.noms.ui.restaurants

import android.Manifest
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.location.Location
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.noms.backend.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.PhotoMetadata
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.github.jan.supabase.exceptions.HttpRequestException

/**
 * ViewModel for managing restaurant data, search functionality, location updates, and playlists.
 */
class RestaurantsViewModel(application: Application) : AndroidViewModel(application) {
    private val mapsApiKey = remoteConfig.getString("google_maps_key")

    // Initialize Places API
    init {
        if (!Places.isInitialized()) {
            Places.initialize(application, mapsApiKey)
        }
    }

    private val context: Context = getApplication<Application>().applicationContext
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)


    // List of all restaurants fetched from the backend
    val restaurants = mutableStateListOf<Restaurant>()

    // List of restaurants currently visible on the map based on camera position
    val visibleRestaurants = mutableStateListOf<Restaurant>()

    // User's playlists
    val playlists = mutableStateListOf<Playlist>()

    // Search query input by the user
    var searchQuery = mutableStateOf("")

    // Autocomplete predictions based on the search query
    var searchResults = mutableStateOf<List<AutocompletePrediction>>(emptyList())

    // The currently selected autocomplete prediction
    var selectedPrediction = mutableStateOf<AutocompletePrediction?>(null)

    // Flags to control the visibility of dialogs
    var showDialog = mutableStateOf(false)
    var showPlaylistDialog = mutableStateOf(false)

    // The ID of the selected playlist
    var selectedPlaylistId = mutableStateOf<Int?>(null)

    // Flag indicating whether location permission is granted
    var hasLocationPermission = mutableStateOf(false)

    // Current camera position on the map
    var currentCameraPosition = mutableStateOf(
        CameraPosition.fromLatLngZoom(LatLng(43.4643, -80.5204), 10f) // Default to Waterloo
    )


    private val _navigationEvents = MutableSharedFlow<NavigationEvent>()
    val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents


    init {
        viewModelScope.launch {
            fetchAllRestaurants()
            fetchUserPlaylists(getCurrentUid())
        }
    }


    /**
     * Fetches all restaurants from the backend and updates the restaurants list.
     */
    private suspend fun fetchAllRestaurants() {
        try {
            val fetchedRestaurants = getAllRestaurants()
            restaurants.clear()
            restaurants.addAll(fetchedRestaurants)
            updateVisibleRestaurants(currentCameraPosition.value.target)
        } catch (e: Exception) {
            Log.e("RestaurantsViewModel", "Error fetching restaurants: ${e.message}")
        }
    }

    /**
     * Fetches playlists for a given user and updates the playlists list.
     */
    private suspend fun fetchUserPlaylists(userId: Int) {
        try {
            val fetchedPlaylists = getPlaylistsofUser(userId)
            playlists.clear()
            playlists.addAll(fetchedPlaylists)
        } catch (e: Exception) {
            Log.e("RestaurantsViewModel", "Error fetching playlists: ${e.message}")
        }
    }

    /**
     * Updates the list of visible restaurants based on the current camera position.
     */
    fun updateVisibleRestaurants(currentLatLng: LatLng) {
        viewModelScope.launch {
            // Get the visible region from the current camera position
            val visibleRegion = LatLngBounds(
                LatLng(
                    currentLatLng.latitude - (0.01 * (20 - currentCameraPosition.value.zoom)),
                    currentLatLng.longitude - (0.01 * (20 - currentCameraPosition.value.zoom))
                ),
                LatLng(
                    currentLatLng.latitude + (0.01 * (20 - currentCameraPosition.value.zoom)),
                    currentLatLng.longitude + (0.01 * (20 - currentCameraPosition.value.zoom))
                )
            )

            visibleRestaurants.clear()
            visibleRestaurants.addAll(
                restaurants.filter { restaurant ->
                    parseLocationString(restaurant.location)?.let { latLng ->
                        visibleRegion.contains(latLng)
                    } ?: false
                }
            )
        }
    }

    /**
     * Calculates LatLngBounds based on the current camera position.
     */
    private fun calculateBounds(currentLatLng: LatLng): LatLngBounds {
        val delta = 0.1
        val southwest = LatLng(currentLatLng.latitude - delta, currentLatLng.longitude - delta)
        val northeast = LatLng(currentLatLng.latitude + delta, currentLatLng.longitude + delta)
        return LatLngBounds(southwest, northeast)
    }

    /**
     * Checks if a given location string is within the visible region.
     */
    private fun isLocationVisible(location: String, visibleRegion: LatLngBounds): Boolean {
        return parseLocationString(location)?.let { latLng ->
            visibleRegion.contains(latLng)
        } ?: false
    }

    /**
     * Handles changes to the search query and updates the search results.
     */
    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
        viewModelScope.launch {
            if (query.isNotBlank()) {
                searchResults.value = searchPlaces(context, query)
            } else {
                searchResults.value = emptyList()
            }
        }
    }

    /**
     * Handles the selection of a search result.
     */
    fun onSearchResultSelected(prediction: AutocompletePrediction) {
        selectedPrediction.value = prediction
        showDialog.value = true
        searchQuery.value = ""
        searchResults.value = emptyList()
    }

    /**
     * Adds a restaurant based on the selected prediction and navigates to the restaurant details screen.
     */
    fun addRestaurantFromPrediction() {
        viewModelScope.launch {
            selectedPrediction.value?.let { prediction ->
                try {
                    val place = getPlaceDetails(context, prediction.placeId)
                    place?.latLng?.let { latLng ->
                        val locationString = "${latLng.latitude},${latLng.longitude}"
                        val newRestaurant = Restaurant(
                            name = place.name ?: prediction.getPrimaryText(null).toString(),
                            location = locationString,
                            rating = place.rating?.toFloat() ?: 0.0f,
                            placeId = prediction.placeId,
                            description = ""
                        )

                        // Add restaurant to backend
                        addRestaurant(
                            location = locationString,
                            name = newRestaurant.name,
                            placeId = newRestaurant.placeId
                        )

                        // Update local state
                        restaurants.add(newRestaurant)
                        updateVisibleRestaurants(latLng)

                        // Emit navigation event
                        emitNavigationEvent(NavigationEvent.NavigateToDetails(newRestaurant.placeId))

                        // Reset dialog states
                        showDialog.value = false
                        selectedPrediction.value = null
                    } ?: run {
                        Log.e("RestaurantsViewModel", "Failed to get LatLng from place details")
                    }
                } catch (e: Exception) {
                    Log.e("RestaurantsViewModel", "Failed to add restaurant: ${e.message}")
                }
            }
        }
    }

    /**
     * Adds a selected restaurant to a playlist.
     */
    fun addRestaurantToPlaylist() {
        viewModelScope.launch {
            selectedPlaylistId.value?.let { pid ->
                selectedPrediction.value?.let { prediction ->
                    try {
                        val place = getPlaceDetails(context, prediction.placeId)
                        place?.latLng?.let { latLng ->
                            val locationString = "${latLng.latitude},${latLng.longitude}"

                            val newRestaurant = Restaurant(
                                name = place.name ?: prediction.getPrimaryText(null).toString(),
                                location = locationString,
                                rating = place.rating?.toFloat() ?: 0.0f,
                                placeId = prediction.placeId,
                                description = ""
                            )

                            // Add restaurant to backend
                            addRestaurant(
                                location = locationString,
                                name = newRestaurant.name,
                                placeId = newRestaurant.placeId
                            )

                            // Fetch all restaurants again to get the latest data (including rid)
                            val allRestaurants = getAllRestaurants()
                            val restaurant =
                                allRestaurants.find { it.placeId == prediction.placeId }

                            restaurant?.rid?.let { rid ->
                                addRestaurantToPlaylist(rid = rid, pid = pid)
                                updateRestaurantLists(restaurant)

                                // Emit navigation event
                                emitNavigationEvent(NavigationEvent.NavigateToDetails(restaurant.placeId))
                            } ?: run {
                                Log.e(
                                    "RestaurantsViewModel",
                                    "Restaurant not found after adding to backend"
                                )
                            }

                            // Reset dialog states
                            showPlaylistDialog.value = false
                            selectedPlaylistId.value = null
                            selectedPrediction.value = null
                        } ?: run {
                            Log.e("RestaurantsViewModel", "Failed to get LatLng from place details")
                        }
                    } catch (e: Exception) {
                        Log.e("RestaurantsViewModel", "Error adding to playlist: ${e.message}")
                    }
                }
            }
        }
    }

    /**
     * Updates the restaurant lists when a new restaurant is added.
     */
    private fun updateRestaurantLists(newRestaurant: Restaurant) {
        if (!restaurants.any { it.placeId == newRestaurant.placeId }) {
            restaurants.add(newRestaurant)
        }

        parseLocationString(newRestaurant.location)?.let { latLng ->
            val bounds = calculateBounds(latLng)
            if (isLocationVisible(newRestaurant.location, bounds)) {
                if (!visibleRestaurants.any { it.placeId == newRestaurant.placeId }) {
                    visibleRestaurants.add(newRestaurant)
                }
            }
        }
    }

    /**
     * Fetches the current location and updates the camera position.
     * Should be called when the user clicks the "My Location" button.
     */
    fun getCurrentLocation() {
        if (hasLocationPermission.value) {
            try {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        location?.let {
                            val latLng = LatLng(it.latitude, it.longitude)
                            viewModelScope.launch {
                                currentCameraPosition.value =
                                    CameraPosition.fromLatLngZoom(latLng, 15f)
                                updateVisibleRestaurants(latLng)
                            }
                        }
                    }
            } catch (e: SecurityException) {
                Log.e("RestaurantsViewModel", "Error getting location", e)
            }
        } else {
            Log.w("RestaurantsViewModel", "Location permission not granted")
        }
    }

    /**
     * Emits a navigation event to the UI layer.
     */
    private fun emitNavigationEvent(event: NavigationEvent) {
        viewModelScope.launch {
            _navigationEvents.emit(event)
        }
    }

    /**
     * Sealed class representing navigation events.
     */
    sealed class NavigationEvent {
        data class NavigateToDetails(val placeId: String) : NavigationEvent()
        // Add more navigation events as needed
    }

    // **Helper Functions**

    /**
     * Parses a location string into a LatLng object.
     */
    private fun parseLocationString(locationStr: String): LatLng? {
        return try {
            val (lat, lng) = locationStr.split(",").map { it.trim().toDouble() }
            LatLng(lat, lng)
        } catch (e: Exception) {
            Log.e("RestaurantsViewModel", "Error parsing location string: ${e.message}")
            null
        }
    }

    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val R = 6371 // Earth's radius in kilometers
        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)
        val a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }

    fun takeMeToRestaurant(prediction: AutocompletePrediction) {
        viewModelScope.launch {
            try {
                val place = getPlaceDetails(context, prediction.placeId)
                place?.latLng?.let { latLng ->
                    // Update camera position to zoom to the restaurant
                    currentCameraPosition.value = CameraPosition.fromLatLngZoom(latLng, 15f)
                    
                    // Create restaurant object
                    val locationString = "${latLng.latitude},${latLng.longitude}"
                    val newRestaurant = Restaurant(
                        name = place.name ?: prediction.getPrimaryText(null).toString(),
                        location = locationString,
                        rating = place.rating?.toFloat() ?: 0.0f,
                        placeId = prediction.placeId,
                        description = ""
                    )

                    // Add to backend if it doesn't exist
                    val existingRestaurants = getAllRestaurants()
                    if (existingRestaurants.none { it.placeId == prediction.placeId }) {
                        addRestaurant(
                            location = locationString,
                            name = newRestaurant.name,
                            placeId = newRestaurant.placeId
                        )
                    }

                    // Add to local restaurants list if not already present
                    if (!restaurants.any { it.placeId == prediction.placeId }) {
                        restaurants.add(newRestaurant)
                    }

                    // Update visible restaurants
                    updateVisibleRestaurants(latLng)
                }
            } catch (e: Exception) {
                Log.e("RestaurantsViewModel", "Error taking to restaurant: ${e.message}")
            }
            
            // Close the dialog
            showDialog.value = false
        }
    }
}