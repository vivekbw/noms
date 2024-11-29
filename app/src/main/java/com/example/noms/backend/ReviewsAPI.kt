package com.example.noms.backend
import android.os.Build
import androidx.annotation.RequiresApi
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import java.time.format.DateTimeFormatter
import java.util.Collections

// Get all reviews from a user
suspend fun getReviewsFromUser(uid: Int = -1): List<Review>{
    val result = supabase.from("reviews").select(){
        filter{
            eq("uid", uid)
        }
    }.decodeList<Review>()
    return result
}

// Get all reviews from a user
suspend fun getFollowingReviews(uid:Int): List<Review>{
    val following = getFollowing(uid)
    val following_ids:List<Int> = following.mapNotNull { it.uid }
    val reviews: List<Review> = following_ids.flatMap { getReviewsFromUser(it) }
    return reviews
}

// Get all reviews from a restaurant
suspend fun getReviewsFromRestaurant(rid: Int): List<Review>{
    val result = supabase.from("reviews").select(){
        filter{
            eq("rid", rid)
        }
    }.decodeList<Review>()
    return result
}

// Get all reviews
suspend fun getAllReviews(currentUid: Int): List<Review> {
    val result = supabase.from("reviews").select() {
        filter {
            neq("uid", currentUid)
        }
    }.decodeList<Review>()
    return result
}

// Get a review by ID
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

// Get a review by ID
suspend fun reviewsToPost(reviews: List<Review>): List<ReviewPost>{
    return reviews.mapNotNull { review ->
        val user = getUser(review.uid)
        val restaurant = getRestaurant(review.rid)

        if (user != null && restaurant != null) {
            ReviewPost(
                reviewerName = "${user.first_name} ${user.last_name}",
                restaurantName = restaurant.name,
                rating = review.rating,
                comment = review.text,
                image = getImage(review.id?:0)
            )
        } else {
            null
        }
    }
}

// temp before recommendation engine
suspend fun recommendRestaurants(): List<ReviewPost> {
    val allReviews = getAllReviews(getCurrentUid())

    // Use reviewToPost to convert the list of first reviews to ReviewPost format
    return reviewsToPost(allReviews)
}

// Get reviews from following users
suspend fun followingRecommendedRestaurant(uid: Int): List<ReviewPost>{
    val followerReviews = getFollowingReviews(uid)
    return reviewsToPost(followerReviews)
}

// Get reviews from following users for current user
suspend fun getCurrentUserReviews(): List<ReviewPost>{
    return reviewsToPost(getReviewsFromUser(getCurrentUid()))
}

// Get next available review ID
suspend fun getNextReviewID(): Int?{
    return supabase.postgrest.rpc("nextReviewID").data.toIntOrNull()
}