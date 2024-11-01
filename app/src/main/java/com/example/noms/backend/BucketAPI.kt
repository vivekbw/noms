package com.example.noms.backend

import io.github.jan.supabase.storage.storage
import android.content.Context
import android.graphics.BitmapFactory
import java.io.File
import android.graphics.Bitmap
import java.io.ByteArrayOutputStream

fun bitmapToByteArray(bitmap: Bitmap?): ByteArray {
    val outputStream = ByteArrayOutputStream()
    // Compress the bitmap and write it to the output stream
    bitmap?.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    return outputStream.toByteArray()
}

suspend fun storeProfilePicture(image: Bitmap?, uid: Int) {
    val byteArrayImage = bitmapToByteArray(image)
    supabase.storage.from("ProfileImages").upload(uid.toString(), byteArrayImage, true)
}

suspend fun storeReviewImage(image: Bitmap?, uid:Int, rid:Int){
    val byteArrayImage = bitmapToByteArray(image)
    val imageName = uid.toString()+'-'+rid.toString()
    supabase.storage.from("Images").upload(imageName, byteArrayImage, true)
}