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

suspend fun reviewsToPost(reviews: List<Review>): List<ReviewPost>{
    return reviews.mapNotNull { review ->
        val user = getUser(review.uid)
        val restaurant = getRestaurant(review.rid)

        if (user != null && restaurant != null) {
            ReviewPost(
                reviewerName = "${user.first_name} ${user.last_name}",
                restaurantName = restaurant.name,
                rating = review.rating,
                comment = review.text
//                image =
            )
        } else {
            null
        }
    }
}

// temp before recommendation engine
suspend fun recommendRestaurants(): List<ReviewPost> {
    val allRestaurants = getAllRestaurants()
    val firstReviews = allRestaurants.mapNotNull { restaurant ->
        val reviews = getReviewsFromRestaurant(restaurant.rid ?: return@mapNotNull null)
        reviews.firstOrNull() // Take the first review if available
    }

    // Use reviewToPost to convert the list of first reviews to ReviewPost format
    return reviewsToPost(firstReviews)
}

suspend fun followersRecommendedRestaurant(uid: Int): List<ReviewPost>{
    val followerReviews = getFollowerReviews(uid)
    return reviewsToPost(followerReviews)
}