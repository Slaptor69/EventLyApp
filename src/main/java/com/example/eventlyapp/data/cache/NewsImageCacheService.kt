package com.example.eventlyapp.data.cache

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.security.MessageDigest

class NewsImageCacheService(
    context: Context,
    private val okHttpClient: OkHttpClient
) {

    private val imageCacheDirectory: File = File(context.cacheDir, IMAGE_CACHE_DIRECTORY_NAME).apply {
        mkdirs()
    }

    suspend fun cacheImage(remoteUrl: String?): String? = withContext(Dispatchers.IO) {
        if (remoteUrl.isNullOrBlank()) {
            return@withContext null
        }

        val targetFile = fileForUrl(remoteUrl)
        val isFreshFile = targetFile.exists() &&
            targetFile.length() > 0L &&
            System.currentTimeMillis() - targetFile.lastModified() <= IMAGE_CACHE_TTL_MILLIS
        if (isFreshFile) {
            return@withContext targetFile.absolutePath
        }

        val request = Request.Builder()
            .url(remoteUrl)
            .build()

        return@withContext runCatching {
            okHttpClient.newCall(request).execute().use { response ->
                val responseBody = response.body
                if (response.isSuccessful.not() || responseBody == null) {
                    targetFile.delete()
                    return@use null
                }

                targetFile.outputStream().use { output ->
                    responseBody.byteStream().use { input ->
                        input.copyTo(output)
                    }
                }
                targetFile.absolutePath
            }
        }.getOrElse {
            targetFile.delete()
            null
        }
    }

    suspend fun clearUnusedImages(activeImagePaths: Set<String>) = withContext(Dispatchers.IO) {
        imageCacheDirectory.listFiles().orEmpty().forEach { imageFile ->
            val isUnused = activeImagePaths.contains(imageFile.absolutePath).not()
            if (isUnused) {
                imageFile.delete()
            }
        }
    }

    suspend fun clearAllImages() = withContext(Dispatchers.IO) {
        imageCacheDirectory.listFiles().orEmpty().forEach { imageFile ->
            imageFile.delete()
        }
    }

    fun sanitizeImagePath(imagePath: String?): String? {
        if (imagePath.isNullOrBlank()) {
            return null
        }

        val file = File(imagePath)
        return if (file.exists()) file.absolutePath else null
    }

    private fun fileForUrl(remoteUrl: String): File {
        val fileName = sha256(remoteUrl)
        return File(imageCacheDirectory, "$fileName.img")
    }

    private fun sha256(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return digest.joinToString(separator = "") { byte -> "%02x".format(byte) }
    }

    companion object {
        private const val IMAGE_CACHE_DIRECTORY_NAME = "nyt_news_images"
        private const val IMAGE_CACHE_TTL_MILLIS = 24L * 60L * 60L * 1000L
    }
}