package com.example.noms.backend
import io.github.jan.supabase.postgrest.from

suspend fun getReviews(uid: Int = -1, location: Pair<Double, Double>): List<Review>{
    if (uid == -1){
        // handle the search for reviews part with location... IDK what to do as of yet
    }
    val result = supabase.from("reviews").select(){
        filter{
            eq("uid", uid)
        }
    }.decodeList<Review>()

    return result
}