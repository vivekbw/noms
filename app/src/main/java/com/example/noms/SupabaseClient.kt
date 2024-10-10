package com.example.noms

import android.os.Build
import androidx.annotation.RequiresApi
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import java.util.Date
import java.time.LocalDate.*
import java.time.format.DateTimeFormatter

val supabase = createSupabaseClient(
    // both keys are meant to be exposed to client, so no security issues
    supabaseUrl = "https://xoffilinikbhnlvdfaib.supabase.co",
    supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InhvZmZpbGluaWtiaG5sdmRmYWliIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Mjc0OTYxMzEsImV4cCI6MjA0MzA3MjEzMX0.2x8XkQS3ahCmYJJHSn6581ki2wh4-mbcWzBEUEmGtu0"
) {
    install(Postgrest)
}

@Serializable
data class User (
    val id: Int,
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
    val id: Int,
    val name: String,
    val location: String        // will need to change
)

@Serializable
data class Review(
    val id: Int,
    val created_date: String,
    val uid: Int,
    val rid: Int,
    val text: String,
    val rating: Int
)

suspend fun getUsers(uid: Int): User {
    val result = supabase.from("users").select(){
        filter {
            eq("id", uid)
        }
    }.decodeSingle<User>()
    return result
}

suspend fun doesFollow(currUser: Int, followId: Int): Boolean{
    val count = supabase.from("followers").select(){
        filter {
            and {
                eq("follower_id", currUser)
                eq("followee_id", followId)
            }
        }
    }.data
    return count != "[]"
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun followUser(currUser: Int, followee: Int){
    val curDate = java.time.LocalDate.now()
    val newFollow = Follow(
        follower_id = currUser,
        followee_id = followee,
        timestamp = curDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
    )
    if (!doesFollow(currUser, followee)) {
        supabase.from("followers").insert(newFollow)
    }
}

suspend fun getFollowerCount(uid: Int): Int {
    // this is indeed inefficient but I can't figure out how to use the count
    // function so this is temporary solution
    val followercount = supabase.from("followers").select(){
        count
        filter {
            eq("followee_id", uid)
        }
    }.decodeList<Follow>()
    return followercount.size
}