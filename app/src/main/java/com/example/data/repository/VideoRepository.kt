package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.local.HarshgamerDao
import com.example.data.local.VideoItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.Buffer
import okio.BufferedSink
import okio.source
import org.json.JSONObject
import java.io.File

data class Video(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val videoUrl: String = "",
    val thumbUrl: String = "",
    val views: Long = 0L,
    val createdAt: Long = 0L,
    val duration: String = "HD",
    val viewsLabel: String = "0 views",
    val isSaved: Boolean = false,
    val uploadedBy: String = "Harshgamer113",
    val ownerEmail: String = "squad113@harshgamer.app"
)

class VideoRepository(
    private val dao: HarshgamerDao,
    private val client: OkHttpClient = OkHttpClient()
) {
    private val defaultPreset = "hg_videos"

    fun observeVideos(): Flow<List<Video>> = dao.observeVideos().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun persistPickedMediaLocally(
        ctx: Context,
        uri: Uri,
        folderName: String,
        expectedKind: String,
        maxBytes: Long
    ): String = withContext(Dispatchers.IO) {
        val resolver = ctx.contentResolver
        val size = resolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: -1L
        if (size > maxBytes) {
            val mb = maxBytes / (1024L * 1024L)
            throw IllegalArgumentException("Selected file exceeds ${mb}MB limit.")
        }
        val mime = resolver.getType(uri) ?: if (expectedKind == "video") "video/mp4" else "image/jpeg"
        if (!mime.startsWith("$expectedKind/")) {
            throw IllegalArgumentException("Invalid file type ($mime). Please choose a $expectedKind file.")
        }
        val ext = when {
            mime.contains("mp4") -> "mp4"
            mime.contains("png") -> "png"
            mime.contains("webp") -> "webp"
            expectedKind == "video" -> "mp4"
            else -> "jpg"
        }
        val dir = File(ctx.filesDir, folderName).apply { mkdirs() }
        val dest = File(dir, "${expectedKind}_${System.currentTimeMillis()}.$ext")
        resolver.openInputStream(uri)?.use { input ->
            dest.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalStateException("Unable to read selected file.")
        Uri.fromFile(dest).toString()
    }

    suspend fun uploadToCloudinary(
        ctx: Context,
        uri: Uri,
        cloudName: String,
        uploadPreset: String = defaultPreset,
        onProgress: (Int) -> Unit
    ): String = withContext(Dispatchers.IO) {
        val cleanCloud = cloudName.trim()
        val cleanPreset = uploadPreset.trim().ifBlank { defaultPreset }
        if (cleanCloud.isBlank() || cleanCloud == "YOUR_CLOUD_NAME") {
            throw IllegalArgumentException(
                "Cloud storage not configured. Set your Cloudinary Cloud Name in Settings (or above) to upload files to cloud."
            )
        }

        val resolver = ctx.contentResolver
        val size = resolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: -1L
        if (size > 100L * 1024 * 1024) {
            throw IllegalArgumentException("Video must be under 100MB")
        }
        val mime = resolver.getType(uri) ?: "video/mp4"
        if (!mime.startsWith("video/") && !mime.startsWith("image/")) {
            throw IllegalArgumentException("Only video (MP4) or image files are allowed")
        }

        val fileBody = object : RequestBody() {
            override fun contentType() = mime.toMediaTypeOrNull()
            override fun contentLength() = size
            override fun writeTo(sink: BufferedSink) {
                val inputStream = resolver.openInputStream(uri)
                    ?: throw IllegalStateException("Unable to read selected file.")
                inputStream.source().use { src ->
                    var total = 0L
                    val buf = Buffer()
                    while (true) {
                        val read = src.read(buf, 8192)
                        if (read == -1L) break
                        sink.write(buf, read)
                        total += read
                        if (size > 0) {
                            onProgress(((total * 100) / size).toInt().coerceIn(0, 100))
                        }
                    }
                }
            }
        }

        val kind = if (mime.startsWith("video/")) "video" else "image"
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("upload_preset", cleanPreset)
            .addFormDataPart("file", "upload", fileBody)
            .build()

        val req = Request.Builder()
            .url("https://api.cloudinary.com/v1_1/$cleanCloud/$kind/upload")
            .post(body)
            .build()

        client.newCall(req).execute().use { res ->
            val text = res.body?.string().orEmpty()
            if (!res.isSuccessful) {
                throw IllegalStateException("Cloud upload failed (${res.code}): $text")
            }
            JSONObject(text).getString("secure_url")
        }
    }

    suspend fun saveVideo(
        title: String,
        desc: String,
        category: String,
        videoUrl: String,
        thumbUrl: String,
        uploadedBy: String = "Harshgamer113",
        ownerEmail: String = "squad113@harshgamer.app",
        duration: String = "HD"
    ) {
        val cleanTitle = title.trim()
        val cleanUrl = videoUrl.trim()
        require(cleanTitle.isNotBlank() && cleanTitle.length <= 120) { "Video title is required (max 120 chars)" }
        require(cleanUrl.isNotBlank()) { "Video file or valid video URL is required" }

        dao.insertVideo(
            VideoItemEntity(
                title = cleanTitle,
                category = category.trim().ifBlank { "Montages" },
                duration = duration.ifBlank { "HD" },
                viewsLabel = "0 views",
                youtubeUrl = cleanUrl,
                thumbUrl = thumbUrl.trim(),
                viewsCount = 0L,
                uploadedBy = uploadedBy.trim().ifBlank { "Harshgamer113" },
                ownerEmail = ownerEmail.trim().lowercase().ifBlank { "squad113@harshgamer.app" },
                description = desc.trim().take(600),
                isSaved = false,
                publishedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateVideoDetails(
        videoId: String,
        currentUserEmail: String,
        isAdmin: Boolean,
        title: String,
        desc: String,
        category: String,
        thumbUrl: String
    ): Result<Unit> {
        val numericId = videoId.toLongOrNull()
            ?: return Result.failure(IllegalArgumentException("Invalid video ID."))
        val existing = dao.getVideoById(numericId)
            ?: return Result.failure(IllegalStateException("Video not found."))
        val caller = currentUserEmail.trim().lowercase()
        if (!isAdmin && existing.ownerEmail.lowercase() != caller) {
            return Result.failure(SecurityException("Access denied: you can only edit your own uploaded videos."))
        }
        val cleanTitle = title.trim()
        if (cleanTitle.isBlank()) {
            return Result.failure(IllegalArgumentException("Video title cannot be empty."))
        }
        dao.updateVideo(
            existing.copy(
                title = cleanTitle.take(120),
                description = desc.trim().take(600),
                category = category.trim().ifBlank { existing.category },
                thumbUrl = thumbUrl.trim()
            )
        )
        return Result.success(Unit)
    }

    suspend fun getVideos(): List<Video> {
        return dao.getVideosOnce().map { it.toDomain() }
    }

    suspend fun deleteVideo(
        id: String,
        currentUserEmail: String = "",
        isAdmin: Boolean = true
    ): Result<Unit> {
        val numericId = id.toLongOrNull()
            ?: return Result.failure(IllegalArgumentException("Invalid video ID."))
        val existing = dao.getVideoById(numericId)
            ?: return Result.failure(IllegalStateException("Video not found."))
        val caller = currentUserEmail.trim().lowercase()
        if (!isAdmin && existing.ownerEmail.lowercase() != caller) {
            return Result.failure(SecurityException("Access denied: you can only delete your own videos."))
        }
        dao.deleteVideoById(numericId)
        return Result.success(Unit)
    }

    suspend fun toggleSaved(video: Video) {
        val numericId = video.id.toLongOrNull() ?: return
        val existing = dao.getVideoById(numericId) ?: return
        dao.updateVideo(existing.copy(isSaved = !existing.isSaved))
    }

    suspend fun incrementViews(id: String) {
        val numericId = id.toLongOrNull() ?: return
        dao.incrementVideoViews(numericId)
    }

    fun isAdmin(userEmail: String?, adminModeEnabled: Boolean): Boolean {
        val normalized = userEmail?.trim()?.lowercase()
        return adminModeEnabled || normalized == "squad113@harshgamer.app"
    }

    private fun VideoItemEntity.toDomain(): Video {
        val computedViewsLabel = if (viewsCount > 0L) {
            "$viewsCount views"
        } else {
            viewsLabel
        }
        return Video(
            id = id.toString(),
            title = title,
            description = description,
            category = category,
            videoUrl = youtubeUrl,
            thumbUrl = thumbUrl,
            views = viewsCount,
            createdAt = publishedAt,
            duration = duration,
            viewsLabel = computedViewsLabel,
            isSaved = isSaved,
            uploadedBy = uploadedBy,
            ownerEmail = ownerEmail
        )
    }
}
