package com.example.noms.backend

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.filter.TextSearchType
import kotlin.reflect.jvm.internal.impl.types.TypeCheckerState.SupertypesPolicy.None

suspend fun getUser(uid: Int): User? {
    val result = supabase.from("users").select(){
        filter {
            eq("uid", uid)
        }
    }.decodeList<User>()  // Decode as a list instead of a single item

    return result.firstOrNull()
}

suspend fun confirmUser(phoneNumber: String): Boolean{
    val result = supabase.from("users").select(){
        filter {
            eq("phone_number", phoneNumber)
        }
    }.data

    return result != "[]"
}

// only used for demo
suspend fun getAllUsers(): List<User>{
    val result = supabase.from("users").select().decodeList<User>()
    return result
}

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

suspend fun findFriends(name: String): List<User>{
    val result = supabase.from("users").select(columns = Columns.list("first_name")){
        filter{
            textSearch(column = "first_name", query = name, textSearchType = TextSearchType.TSVECTOR)
        }
    }.decodeList<User>()
    return result
}