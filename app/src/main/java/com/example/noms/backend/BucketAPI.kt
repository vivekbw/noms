package com.example.noms.backend

import io.github.jan.supabase.storage.storage
import android.content.Context
import android.graphics.BitmapFactory
import java.io.File
import android.graphics.Bitmap
import java.io.ByteArrayOutputStream

// To convert between Bitmap and ByteArray
fun byteArrayToBitmap(byteArray: ByteArray): Bitmap? {
    return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
}

// To convert between Bitmap and ByteArray
fun bitmapToByteArray(bitmap: Bitmap?): ByteArray {
    val outputStream = ByteArrayOutputStream()
    // Compress the bitmap and write it to the output stream
    bitmap?.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    return outputStream.toByteArray()
}

// Store the profile picture of the user
suspend fun storeProfilePicture(image: Bitmap?, uid: Int) {
    val byteArrayImage = bitmapToByteArray(image)
    supabase.storage.from("ProfileImages").upload(uid.toString(), byteArrayImage, true)
}

//  Check if Image exists
suspend fun findImage(rid:Int): Boolean{
    val images = supabase.storage.from("Images")
    val files = images.list()
    return files.any { it.name == rid.toString()}
}

// Store image of the review
suspend fun storeReviewImage(image: Bitmap?, reviewId: Int) {
    val byteArrayImage = bitmapToByteArray(image)
    val imageName = reviewId.toString()

    supabase.storage.from("Images").upload(imageName, byteArrayImage, true)
}

// Get image of the review
suspend fun getImage(reviewId: Int): Bitmap? {
    if (!findImage(reviewId)){
        return null
    }
    val bucket = supabase.storage.from("Images")
    val bytes = bucket.downloadPublic(reviewId.toString())
    return byteArrayToBitmap(bytes)
}