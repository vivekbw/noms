package com.example.noms.backend
import android.os.Build
import androidx.annotation.RequiresApi
import io.github.jan.supabase.postgrest.from
import java.time.format.DateTimeFormatter
import java.util.Collections

suspend fun getReviewsFromUser(uid: Int = -1): List<Review>{
    val result = supabase.from("reviews").select(){
        filter{
            eq("uid", uid)
        }
    }.decodeList<Review>()

    return result
}

suspend fun getFollowerReviews(uid:Int): List<Review>{
    val followers = getFollowers(uid)
    val follower_ids:List<Int> = followers.mapNotNull { it.uid }
    val reviews: List<Review> = follower_ids.flatMap { getReviewsFromUser(it) }
    return reviews
}

suspend fun getReviewsFromRestaurant(rid: Int): List<Review>{
    val result = supabase.from("reviews").select(){
        filter{
            eq("rid", rid)
        }
    }.decodeList<Review>()
    return result
}


@RequiresApi(Build.VERSION_CODES.O)
suspend fun writeReview(uid: Int, rid: Int, text:String, rating:Float){
    val curDate = java.time.LocalDate.now()
    val newReview = Review(
        created_date = curDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
        uid = uid,
        rid = rid,
        text = text,
        rating = rating
    )

    supabase.from("reviews").insert(newReview)
}