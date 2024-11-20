package com.example.noms.backend

import io.github.jan.supabase.storage.storage
import android.content.Context
import android.graphics.BitmapFactory
import java.io.File
import android.graphics.Bitmap
import java.io.ByteArrayOutputStream

fun byteArrayToBitmap(byteArray: ByteArray): Bitmap? {
    return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
}

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

suspend fun storeReviewImage(image: Bitmap?, reviewId: Int, rid: Int) {
    val byteArrayImage = bitmapToByteArray(image)
    val imageName = reviewId.toString()
    val bucketName = rid.toString()

    // Retrieve all buckets
    val allBuckets = supabase.storage.retrieveBuckets()

    // Check if a bucket with the matching name exists
    val bucketExists = allBuckets.any { it.name == bucketName }

    if (!bucketExists) {
        // Bucket exists, continue to store the image
        supabase.storage.createBucket(id = bucketName){
            public = true
        }
    }
    supabase.storage.from(bucketName).upload(imageName, byteArrayImage, true)
}

suspend fun getImage(reviewId: Int, rid: Int): Bitmap? {
    val bucket = supabase.storage.from(rid.toString())
    val bytes = bucket.downloadPublic(reviewId.toString())
    return byteArrayToBitmap(bytes)
}