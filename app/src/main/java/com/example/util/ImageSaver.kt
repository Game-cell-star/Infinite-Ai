package com.example.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

object ImageSaver {

    /**
     * Downloads an image from a URL and saves it to the public picture gallery.
     * Returns the local file path or URI string if successful, or null on failure.
     */
    suspend fun downloadAndSaveToGallery(context: Context, url: String, prompt: String): String? = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("ImageSaver", "Failed to download image. HTTP Code: ${response.code}")
                    return@withContext null
                }
                val inputStream: InputStream = response.body?.byteStream() ?: return@withContext null
                val bitmap = BitmapFactory.decodeStream(inputStream) ?: return@withContext null
                
                return@withContext saveBitmapToGallery(context, bitmap, prompt)
            }
        } catch (e: Exception) {
            Log.e("ImageSaver", "Error downloading/saving image", e)
            return@withContext null
        }
    }

    private fun saveBitmapToGallery(context: Context, bitmap: Bitmap, prompt: String): String? {
        val cleanPrompt = prompt.replace(Regex("[^a-zA-Z0-9]"), "_").take(30)
        val filename = "InfiniteAI_${cleanPrompt}_${System.currentTimeMillis()}.jpg"
        val resolver = context.contentResolver

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ (Scoped Storage) - NO permissions needed for MediaStore insertion
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/InfiniteAI")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (imageUri != null) {
                    resolver.openOutputStream(imageUri).use { outStream ->
                        if (outStream != null) {
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
                        }
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(imageUri, contentValues, null, null)
                    return imageUri.toString()
                }
            } else {
                // Older Android - Save to public directory directly or context.getExternalFilesDir
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val appDir = File(imagesDir, "InfiniteAI")
                if (!appDir.exists()) {
                    appDir.mkdirs()
                }
                val imageFile = File(appDir, filename)
                FileOutputStream(imageFile).use { outStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
                }
                
                // Add to gallery via media scanner insertion
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DATA, imageFile.absolutePath)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                }
                resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                return imageFile.absolutePath
            }
        } catch (e: Exception) {
            Log.e("ImageSaver", "Error saving bitmap", e)
        }
        return null
    }
}
