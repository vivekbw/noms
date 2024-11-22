package com.example.noms.backend

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import io.github.jan.supabase.postgrest.from

// PlayList Functions

// gets list of playlists of a user, not the restaurants
suspend fun getPlaylistsofUser(uid: Int): List<Playlist>{
    val result = supabase.from("playlists").select(){
        filter {
            eq("uid", uid)
        }
    }.decodeList<Playlist>()
    return result
}

// input playlist id, get all restaurants in that playlist
suspend fun getPlaylist(pid: Int): List<Restaurant> {
    val temp = supabase.from("playlist_restaurants").select() {
        filter{
            eq("pid", pid)
        }
    }.decodeList<PlaylistRestaurantid>()
    val restaurantIds: List<Int> = temp.map { it.rid }
    val restaurants = supabase.from("restaurants").select(){
        filter{
            isIn("rid", restaurantIds)
        }
    }.decodeList<Restaurant>()
    return restaurants
}



suspend fun createPlaylist(name:String, uid:Int){
    val newPlaylist = Playlist(
        uid = uid,
        name = name
    )
    supabase.from("playlists").insert(newPlaylist)
}

suspend fun addRestaurantToPlaylist(rid:Int, pid:Int){
    val addRestaurant = PlaylistRestaurantid(
        pid = pid,
        rid = rid
    )
    supabase.from("playlist_restaurants").insert(addRestaurant)
}

suspend fun getPlaylistId(name: String): Int? {
    // Fetch the playlist ID based on the playlist name
    val result = supabase.from("playlists").select() {
        filter {
            eq("name", name)
        }
    }.decodeSingle<Playlist>()  // Decode the result into a Playlist object
    return result.id
}


suspend fun followPlaylist(uid: Int, pid:Int){
    val follow = FollowPlaylist(
        uid = uid,
        pid = pid
    )
    supabase.from("follow_playlist").insert(follow)
}