package com.example.noms.backend

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.filter.TextSearchType
import android.util.Log

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