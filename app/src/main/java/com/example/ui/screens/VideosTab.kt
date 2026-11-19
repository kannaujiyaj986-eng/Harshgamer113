package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.repository.Video
import com.example.ui.HarshgamerUiState
import com.example.ui.VideoUploadState
import com.example.ui.components.CyberSectionHeader
import com.example.ui.components.CyberStatusBadge
import com.example.ui.components.EmptyCyberState
import com.example.ui.theme.HgRed

private val VIDEO_CATEGORIES = listOf(
    "All",
    "My Videos",
    "Saved",
    "Montages",
    "Sensitivity Guides",
    "Live Streams"
)
private val UPLOAD_CATEGORIES = listOf("Montages", "Sensitivity Guides", "Live Streams")

@Composable
fun VideosTab(
    uiState: HarshgamerUiState,
    onSelectCategory: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onToggleSave: (Video) -> Unit,
    onToggleAdminMode: () -> Unit,
    onWatchVideo: (Video) -> Unit,
    onShareVideo: (Video) -> Unit,
    onEditVideo: (
        videoId: String,
        title: String,
        desc: String,
        category: String,
        thumbUrl: String,
        onDone: (Result<Unit>) -> Unit
    ) -> Unit,
    onDeleteVideo: (String) -> Unit,
    onUploadVideo: (
        title: String,
        desc: String,
        category: String,
        videoUri: Uri?,
        thumbUri: Uri?,
        directVideoUrl: String,
        directThumbUrl: String,
        cloudName: String,
        uploadPreset: String,
        onSuccess: () -> Unit
    ) -> Unit,
    onClearUploadError: () -> Unit,
    openUploadInitially: Boolean = false,
    onConsumeOpenUpload: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showUploadDialog by remember { mutableStateOf(false) }
    var playingVideo by remember { mutableStateOf<Video?>(null) }
    var editingVideo by remember { mutableStateOf<Video?>(null) }

    LaunchedEffect(openUploadInitially) {
        if (openUploadInitially) {
            onClearUploadError()
            showUploadDialog = true
            onConsumeOpenUpload()
        }
    }

    fun openVideoUrl(url: String) {
        val safeUrl = url.trim().ifBlank { "https://youtube.com/@harshgamer-113" }
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(safeUrl)))
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("videos_tab"),
        containerColor = Color.Transparent,
        floatingActionButton = {
            if (uiState.currentUser != null) {
                ExtendedFloatingActionButton(
                    onClick = {
                        onClearUploadError()
                        showUploadDialog = true
                    },
                    icon = { Icon(Icons.Filled.CloudUpload, contentDescription = null) },
                    text = { Text("Upload Video", style = MaterialTheme.typography.labelLarge) },
                    containerColor = HgRed,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.testTag("upload_video_fab")
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 92.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                CyberSectionHeader(
                    title = "Harshgamer113 Videos",
                    subtitle = "Gameplay clutches, sensitivity guides & creator uploads",
                    actionLabel = if (uiState.isAdmin) "Admin ON" else "Admin",
                    onActionClick = onToggleAdminMode
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, HgRed.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(148.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_hero_arena),
                            contentDescription = "Harshgamer113 YouTube channel banner",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.background.copy(alpha = 0.94f),
                                            MaterialTheme.colorScheme.background.copy(alpha = 0.65f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CyberStatusBadge(
                                    label = "YOUTUBE • @HARSHGAMER-113",
                                    accentColor = HgRed,
                                    icon = Icons.Filled.PlayArrow
                                )
                                if (uiState.isAdmin) {
                                    CyberStatusBadge(
                                        label = "ADMIN",
                                        accentColor = MaterialTheme.colorScheme.tertiary,
                                        icon = Icons.Filled.AdminPanelSettings
                                    )
                                }
                            }
                            Text(
                                text = "Watch in-app MP4s or upload clips up to 100MB to Cloudinary.",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Button(
                                onClick = { openVideoUrl("https://youtube.com/@harshgamer-113") },
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("videos_open_channel_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = HgRed,
                                    contentColor = MaterialTheme.colorScheme.onSecondary
                                )
                            ) {
                                Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("OPEN YOUTUBE CHANNEL", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = uiState.videoSearchQuery,
                    onValueChange = onSearchQueryChange,
                    label = { Text("Search videos, creators or categories") },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (uiState.videoSearchQuery.isNotBlank()) {
                            IconButton(
                                onClick = { onSearchQueryChange("") },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("video_search_input")
                )
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(VIDEO_CATEGORIES) { cat ->
                        FilterChip(
                            selected = uiState.selectedVideoCategory == cat,
                            onClick = { onSelectCategory(cat) },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("video_filter_${cat.lowercase().replace(" ", "_")}")
                        )
                    }
                }
            }

            if (uiState.filteredVideos.isEmpty()) {
                item {
                    EmptyCyberState(
                        icon = Icons.Filled.VideoLibrary,
                        title = "No Videos Found",
                        hint = "Clear your search, switch filters, or tap Upload Video to publish a clip."
                    )
                }
            } else {
                items(uiState.filteredVideos, key = { it.id }) { video ->
                    val canManage = uiState.isAdmin ||
                        (uiState.currentUser != null &&
                            video.ownerEmail.equals(uiState.currentUser.email, ignoreCase = true))
                    VideoCard(
                        video = video,
                        canManage = canManage,
                        onWatchClick = {
                            onWatchVideo(video)
                            playingVideo = video
                        },
                        onToggleSave = { onToggleSave(video) },
                        onShareClick = { onShareVideo(video) },
                        onEditClick = { editingVideo = video },
                        onDeleteClick = { onDeleteVideo(video.id) }
                    )
                }
            }
        }
    }

    playingVideo?.let { active ->
        VideoPlayerDialog(
            video = active,
            onDismiss = { playingVideo = null },
            onShare = onShareVideo
        )
    }

    editingVideo?.let { target ->
        EditVideoDialog(
            video = target,
            onDismiss = { editingVideo = null },
            onSave = { title, desc, cat, thumbUrl ->
                onEditVideo(target.id, title, desc, cat, thumbUrl) { res ->
                    if (res.isSuccess) editingVideo = null
                }
            }
        )
    }

    if (showUploadDialog) {
        UploadVideoDialog(
            uploadState = uiState.videoUploadState,
            onDismiss = {
                if (!uiState.videoUploadState.isUploading) {
                    showUploadDialog = false
                }
            },
            onUpload = { title, desc, cat, vUri, tUri, vUrl, tUrl, cloud, preset ->
                onUploadVideo(title, desc, cat, vUri, tUri, vUrl, tUrl, cloud, preset) {
                    showUploadDialog = false
                }
            }
        )
    }
}

@Composable
private fun VideoCard(
    video: Video,
    canManage: Boolean,
    onWatchClick: () -> Unit,
    onToggleSave: () -> Unit,
    onShareClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("video_card_${video.id}"),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (video.thumbUrl.isNotBlank()) {
                AsyncImage(
                    model = video.thumbUrl,
                    contentDescription = "Thumbnail for ${video.title}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(MaterialTheme.shapes.medium)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CyberStatusBadge(
                        label = video.category.uppercase(),
                        accentColor = MaterialTheme.colorScheme.primary
                    )
                    CyberStatusBadge(
                        label = video.viewsLabel,
                        accentColor = MaterialTheme.colorScheme.tertiary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleSave,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("save_video_${video.id}")
                    ) {
                        Icon(
                            imageVector = if (video.isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = "Save video",
                            tint = if (video.isSaved) HgRed else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onShareClick,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("share_video_${video.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Share video",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (canManage) {
                        IconButton(
                            onClick = onEditClick,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("edit_video_${video.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit video",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("delete_video_${video.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteOutline,
                                contentDescription = "Delete video",
                                tint = HgRed
                            )
                        }
                    }
                }
            }

            Text(
                text = video.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "By ${video.uploadedBy} • ${formatUploadDate(video.createdAt)} • ${video.duration}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            if (video.description.isNotBlank()) {
                Text(
                    text = video.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedButton(
                onClick = onWatchClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("watch_video_${video.id}"),
                border = BorderStroke(1.dp, HgRed.copy(alpha = 0.6f))
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = HgRed)
                Spacer(modifier = Modifier.width(6.dp))
                Text("WATCH VIDEO", color = HgRed, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
fun EditVideoDialog(
    video: Video,
    onDismiss: () -> Unit,
    onSave: (title: String, desc: String, category: String, thumbUrl: String) -> Unit
) {
    var title by remember { mutableStateOf(video.title) }
    var description by remember { mutableStateOf(video.description) }
    var selectedCategory by remember { mutableStateOf(video.category.ifBlank { UPLOAD_CATEGORIES.first() }) }
    var thumbUrl by remember { mutableStateOf(video.thumbUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text("EDIT VIDEO DETAILS", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .testTag("edit_video_dialog"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(UPLOAD_CATEGORIES) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) }
                        )
                    }
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Video Title") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_video_title_input")
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = thumbUrl,
                    onValueChange = { thumbUrl = it },
                    label = { Text("Thumbnail URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, description, selectedCategory, thumbUrl) },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("confirm_edit_video_button")
            ) {
                Text("SAVE CHANGES")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun UploadVideoDialog(
    uploadState: VideoUploadState,
    onDismiss: () -> Unit,
    onUpload: (
        title: String,
        desc: String,
        category: String,
        videoUri: Uri?,
        thumbUri: Uri?,
        directVideoUrl: String,
        directThumbUrl: String,
        cloudName: String,
        uploadPreset: String
    ) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("hg113_cloudinary", Context.MODE_PRIVATE) }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(UPLOAD_CATEGORIES.first()) }
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var selectedThumbUri by remember { mutableStateOf<Uri?>(null) }
    var directVideoUrl by remember { mutableStateOf("") }
    var directThumbUrl by remember { mutableStateOf("") }
    var cloudName by remember { mutableStateOf(prefs.getString("cloud_name", "") ?: "") }
    var uploadPreset by remember { mutableStateOf(prefs.getString("upload_preset", "hg_videos") ?: "hg_videos") }

    val videoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) selectedVideoUri = uri
    }

    val thumbPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) selectedThumbUri = uri
    }

    val hasMediaOrUrl = selectedVideoUri != null || directVideoUrl.isNotBlank()
    val canSubmit = !uploadState.isUploading && title.isNotBlank() && hasMediaOrUrl

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text("UPLOAD / PUBLISH VIDEO", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(UPLOAD_CATEGORIES) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) }
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Video Title") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("upload_video_title_input")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            videoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                        },
                        enabled = !uploadState.isUploading,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("pick_video_button")
                    ) {
                        Icon(Icons.Filled.VideoFile, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (selectedVideoUri != null) "MP4 Ready" else "Pick Video")
                    }

                    OutlinedButton(
                        onClick = {
                            thumbPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        enabled = !uploadState.isUploading,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("pick_thumb_button")
                    ) {
                        Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (selectedThumbUri != null) "Thumb Ready" else "Pick Thumb")
                    }
                }

                if (selectedThumbUri != null) {
                    AsyncImage(
                        model = selectedThumbUri,
                        contentDescription = "Selected thumbnail preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(MaterialTheme.shapes.medium)
                    )
                }

                if (selectedVideoUri != null) {
                    CyberStatusBadge(
                        label = "VIDEO SELECTED: ${selectedVideoUri?.lastPathSegment ?: "MP4 CLIP"}",
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        icon = Icons.Filled.VideoFile
                    )
                }

                if (selectedVideoUri != null || selectedThumbUri != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = cloudName,
                            onValueChange = { cloudName = it },
                            label = { Text("Cloudinary Cloud") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = uploadPreset,
                            onValueChange = { uploadPreset = it },
                            label = { Text("Preset") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                OutlinedTextField(
                    value = directVideoUrl,
                    onValueChange = { directVideoUrl = it },
                    label = { Text("Or Paste MP4 / YouTube URL") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("upload_video_url_input")
                )

                OutlinedTextField(
                    value = directThumbUrl,
                    onValueChange = { directThumbUrl = it },
                    label = { Text("Thumbnail URL (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (uploadState.isUploading) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Uploading to Cloudinary... ${uploadState.progressPercent}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        LinearProgressIndicator(
                            progress = { (uploadState.progressPercent / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (!uploadState.errorMessage.isNullOrBlank()) {
                    Text(
                        text = uploadState.errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = HgRed
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    prefs.edit()
                        .putString("cloud_name", cloudName.trim())
                        .putString("upload_preset", uploadPreset.trim())
                        .apply()
                    onUpload(
                        title,
                        description,
                        selectedCategory,
                        selectedVideoUri,
                        selectedThumbUri,
                        directVideoUrl,
                        directThumbUrl,
                        cloudName,
                        uploadPreset
                    )
                },
                enabled = canSubmit,
                modifier = Modifier.testTag("confirm_upload_video_button")
            ) {
                Text(if (selectedVideoUri != null) "UPLOAD & SAVE" else "PUBLISH")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !uploadState.isUploading
            ) {
                Text("Cancel")
            }
        }
    )
}
