package com.example.noms.backend

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.filter.TextSearchType
import android.util.Log
import android.content.Context
import android.graphics.Bitmap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.api.model.PhotoMetadata
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.model.TypeFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


// Convert latitude and longitude to a comma-separated string
fun latLngToString(latitude: Double, longitude: Double): String {
    return "$latitude,$longitude"
}

// Convert the comma-separated string back to latitude and longitude
fun stringToLatLng(location: String): Pair<Double, Double>? {
    val parts = location.split(",")
    return if (parts.size == 2) {
        val latitude = parts[0].toDoubleOrNull()
        val longitude = parts[1].toDoubleOrNull()
        if (latitude != null && longitude != null) {
            Pair(latitude, longitude)
        } else {
            null  // Return null if the conversion fails
        }
    } else {
        null  // Return null if format is incorrect
    }
}

suspend fun getRestaurant(rid: Int): Restaurant{
    val result = supabase.from("restaurants").select(){
        filter {
            eq("rid", rid)
        }
    }.decodeSingle<Restaurant>()
    return result
}

suspend fun getAllRestaurants(): List<Restaurant>{
    val restaurants = supabase.from("restaurants").select().decodeList<Restaurant>()
    return restaurants
}

suspend fun searchByLocation(location: String): Boolean {
//    Log.d("RestaurantsAPI", "Searching for restaurant at location: $location")
    val result = supabase.from("restaurants").select(columns = Columns.list("location")) {
        filter {
            eq("location", location)
        }
    }.data
//    Log.d("RestaurantsAPI", "Search result: $result")
    return result != "[]"
}

suspend fun addRestaurant(location: String, name: String, placeId: String) {
//    Log.d("RestaurantsAPI", "Checking if restaurant exists at location: $location")
    if (!searchByLocation(location)) {
//        Log.d("RestaurantsAPI", "Restaurant not found, creating new entry")
        val newRestaurant = Restaurant(
            name = name,
            location = location,
            rating = 0.0f,
            placeId = placeId,
            description = ""
        )
        try {
            supabase.from("restaurants").insert(newRestaurant)
            Log.d("RestaurantsAPI", "Successfully added new restaurant: $name")
        } catch (e: Exception) {
            Log.e("RestaurantsAPI", "Failed to insert restaurant", e)
            throw e
        }
    } else {
        Log.d("RestaurantsAPI", "Restaurant already exists at location: $location")
    }
}


suspend fun fetchPhotoReference(context: Context, placeId: String): PhotoMetadata? {
    return withContext(Dispatchers.IO) {
        val placesClient = com.google.android.libraries.places.api.Places.createClient(context)
        val placeFields = listOf(com.google.android.libraries.places.api.model.Place.Field.PHOTO_METADATAS)
        val request = FetchPlaceRequest.newInstance(placeId, placeFields)
        try {
            val response = placesClient.fetchPlace(request).await()
            response.place.photoMetadatas?.firstOrNull()
        } catch (e: Exception) {
            Log.e("fetchPhotoReference", "Error fetching photo reference: ${e.message}")
            null
        }
    }
}

suspend fun fetchPhoto(context: Context, photoMetadata: PhotoMetadata): Bitmap? {
    return withContext(Dispatchers.IO) {
        val placesClient = com.google.android.libraries.places.api.Places.createClient(context)
        val photoRequest = FetchPhotoRequest.builder(photoMetadata).build()
        try {
            val response = placesClient.fetchPhoto(photoRequest).await()
            response.bitmap
        } catch (e: Exception) {
            Log.e("fetchPhoto", "Error fetching photo: ${e.message}")
            null
        }
    }
}

suspend fun searchPlaces(context: Context, query: String): List<com.google.android.libraries.places.api.model.AutocompletePrediction> {
    return withContext(Dispatchers.IO) {
        val placesClient = com.google.android.libraries.places.api.Places.createClient(context)
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setTypeFilter(TypeFilter.ESTABLISHMENT)
            .build()

        try {
            val response = placesClient.findAutocompletePredictions(request).await()
            response.autocompletePredictions.filter { prediction ->
                prediction.placeTypes.contains(com.google.android.libraries.places.api.model.Place.Type.RESTAURANT)
            }
        } catch (e: Exception) {
            Log.e("searchPlaces", "Error searching places: ${e.message}")
            emptyList()
        }
    }
}

suspend fun getPlaceDetails(context: Context, placeId: String): com.google.android.libraries.places.api.model.Place? {
    return withContext(Dispatchers.IO) {
        val placesClient = com.google.android.libraries.places.api.Places.createClient(context)
        val request = FetchPlaceRequest.newInstance(placeId, listOf(
            com.google.android.libraries.places.api.model.Place.Field.ID,
            com.google.android.libraries.places.api.model.Place.Field.NAME,
            com.google.android.libraries.places.api.model.Place.Field.LAT_LNG,
            com.google.android.libraries.places.api.model.Place.Field.ADDRESS,
            com.google.android.libraries.places.api.model.Place.Field.RATING
        ))

        try {
            val response = placesClient.fetchPlace(request).await()
            response.place
        } catch (e: Exception) {
            Log.e("getPlaceDetails", "Error fetching place details: ${e.message}")
            null
        }
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

fun isLocationVisible(location: String, visibleRegion: LatLngBounds): Boolean {
    return parseLocationString(location)?.let { latLng ->
        visibleRegion.contains(latLng)
    } ?: false
}