package com.example.noms.backend

import io.github.jan.supabase.postgrest.from

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