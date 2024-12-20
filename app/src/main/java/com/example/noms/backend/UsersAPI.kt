package com.example.noms.backend

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.filter.TextSearchType
import kotlin.reflect.jvm.internal.impl.types.TypeCheckerState.SupertypesPolicy.None

private var CurrentUser: User? = null

// Get a user by their ID
suspend fun getUser(uid: Int): User? {
    val result = supabase.from("users").select(){
        filter {
            eq("uid", uid)
        }
    }.decodeList<User>()  // Decode as a list instead of a single item

    return result.firstOrNull()
}

// Get a user by their phone number
suspend fun setCurrentUser(phoneNumber: String){
    CurrentUser = supabase.from("users").select(){
        filter {
            eq("phone_number", phoneNumber)
        }
    }.decodeList<User>().first()  // Decode as a list instead of a single item
}

// Get the current user's ID
fun getCurrentUid(): Int {
    return CurrentUser?.uid ?: 1
}

// Get the current user's phone number
suspend fun confirmUser(phoneNumber: String): Boolean{
    val result = supabase.from("users").select(){
        filter {
            eq("phone_number", phoneNumber)
        }
    }.data

    return result != "[]"
}

// Get all users
suspend fun getAllUsers(): List<User>{
    val result = supabase.from("users").select().decodeList<User>()
    return result
}

// Create a new user
suspend fun createUser(firstName: String, lastName: String, phoneNumber: String): Int{
    if (confirmUser(phoneNumber)) {
        return -1
    }
    val newUser = User(
        first_name = firstName,
        last_name = lastName,
        phone_number = phoneNumber
    )
    supabase.from("users").insert(newUser)
    return 0
}

// Find friends by name
suspend fun findFriends(name: String): List<User>{
    val result = supabase.from("users").select(columns = Columns.list("first_name")){
        filter{
            textSearch(column = "first_name", query = name, textSearchType = TextSearchType.TSVECTOR)
        }
    }.decodeList<User>()
    return result
}