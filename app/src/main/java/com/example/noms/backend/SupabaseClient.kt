package com.example.noms.backend

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable
import java.time.format.DateTimeFormatter

val remoteConfig = Firebase.remoteConfig
val supabase = createSupabaseClient(
    supabaseUrl = remoteConfig.getString("supabase_url"),
    supabaseKey = remoteConfig.getString("supabase_key")
) {
    install(Postgrest)
    install(Storage)
}

@Serializable
data class User (
    val uid: Int? = null,
    val first_name: String,
    val last_name: String,
    val phone_number: String
)

@Serializable
data class Follow (
    val follower_id: Int,
    val followee_id: Int,
    val timestamp: String
)

@Serializable
data class Restaurant(
    val rid: Int? = null,
    val name: String,
    val location: String,        // will need to change
    var rating: Float,
    val placeId: String,
    val description: String
)

@Serializable
data class Review(
    val id: Int? = null,
    val created_date: String,
    val uid: Int,
    val rid: Int,
    val text: String,
    val rating: Float
)

@Serializable
data class Playlist(
    val id: Int? = null,
    val uid: Int,
    val name: String,
)

@Serializable
data class ReviewPost(
    val reviewerName: String,
    val restaurantName: String,
    val rating: Float,
    val comment: String
)

// For backend only, don't use this
@Serializable
data class PlaylistRestaurantid(
    val pid: Int,
    val rid: Int,
)

@Serializable
data class FollowPlaylist(
    val uid: Int,
    val pid: Int
)