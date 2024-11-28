package com.example.noms.backend

import android.os.Build
import androidx.annotation.RequiresApi
import io.github.jan.supabase.postgrest.from
import java.time.format.DateTimeFormatter


suspend fun getFollowers(uid: Int): List<User> {
    val followerList = supabase.from("followers").select(){
        filter {
            eq("followee_id", uid)
        }
    }.decodeList<Follow>()
    val listFollowerIds: List<Int> = followerList.map { it.follower_id }
    val followers = supabase.from("users").select(){
        filter {
            isIn("uid", listFollowerIds)
        }
    }.decodeList<User>()
    return followers
}

suspend fun getFollowing(uid: Int): List<User> {
    val followerList = supabase.from("followers").select(){
        filter {
            eq("follower_id", uid)
        }
    }.decodeList<Follow>()
    val listFollowerIds: List<Int> = followerList.map { it.followee_id }
    val followers = supabase.from("users").select(){
        filter {
            isIn("uid", listFollowerIds)
        }
    }.decodeList<User>()
    return followers
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

suspend fun unfollowUser(follower_uid:Int, following_uid:Int){
    supabase.from("followers").delete{
        filter{
            eq("follower_id", follower_uid)
            eq("followee_id", following_uid)
        }
    }
}