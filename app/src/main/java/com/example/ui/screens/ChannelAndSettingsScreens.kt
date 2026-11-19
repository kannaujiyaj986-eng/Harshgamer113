package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.repository.Video
import com.example.ui.HarshgamerUiState
import com.example.ui.components.CyberSectionHeader
import com.example.ui.components.CyberStatusBadge
import com.example.ui.components.EmptyCyberState
import com.example.ui.components.StatMetricTile
import com.example.ui.theme.HgRed

@Composable
fun MyChannelScreen(
    uiState: HarshgamerUiState,
    onWatchVideo: (Video) -> Unit,
    onEditVideo: (Video) -> Unit,
    onDeleteVideo: (Video) -> Unit,
    onShareVideo: (Video) -> Unit,
    onOpenUpload: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val profile = uiState.playerProfile
    val user = uiState.currentUser
    val creatorName = profile?.fullName?.ifBlank { user?.displayName ?: "Squad 113 Creator" }
        ?: user?.displayName
        ?: "Squad 113 Creator"
    val handle = profile?.username?.ifBlank { user?.username ?: "player113" } ?: "player113"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("my_channel_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("channel_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Column {
                    Text("MY CHANNEL", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        text = "Creator Video Profile & Analytics",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f))
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    ) {
                        if (!profile?.coverUri.isNullOrBlank()) {
                            AsyncImage(
                                model = profile?.coverUri,
                                contentDescription = "Channel cover photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.img_hero_arena),
                                contentDescription = "Default channel banner",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, MaterialTheme.colorScheme.surface)
                                    )
                                )
                        )
                    }

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (!profile?.avatarUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = profile?.avatarUri,
                                    contentDescription = "Creator avatar",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, HgRed, CircleShape)
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.img_harsh_avatar),
                                    contentDescription = "Creator avatar",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, HgRed, CircleShape)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = creatorName,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "@$handle • ${user?.email ?: ""}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        if (!profile?.bio.isNullOrBlank()) {
                            Text(
                                text = profile?.bio.orEmpty(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatMetricTile(
                                label = "TOTAL VIDEOS",
                                value = "${uiState.myUploadedVideos.size}",
                                accentColor = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            StatMetricTile(
                                label = "TOTAL VIEWS",
                                value = "${uiState.myTotalViews}",
                                accentColor = HgRed,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Button(
                            onClick = onOpenUpload,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("channel_upload_video_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = HgRed,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            )
                        ) {
                            Text("UPLOAD NEW VIDEO", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }

        item {
            CyberSectionHeader(
                title = "Uploaded Videos",
                subtitle = "Videos published by $creatorName"
            )
        }

        if (uiState.myUploadedVideos.isEmpty()) {
            item {
                EmptyCyberState(
                    icon = Icons.Filled.VideoLibrary,
                    title = "No Uploaded Videos Yet",
                    hint = "Tap Upload New Video above to publish MP4 clips to your creator channel."
                )
            }
        } else {
            items(uiState.myUploadedVideos, key = { it.id }) { video ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("channel_video_card_${video.id}"),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (video.thumbUrl.isNotBlank()) {
                            AsyncImage(
                                model = video.thumbUrl,
                                contentDescription = video.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(145.dp)
                                    .clip(MaterialTheme.shapes.medium)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CyberStatusBadge(
                                label = "${video.category.uppercase()} • ${video.viewsLabel}",
                                accentColor = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = formatUploadDate(video.createdAt),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = video.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onWatchVideo(video) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("WATCH")
                            }
                            IconButton(
                                onClick = { onShareVideo(video) },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(Icons.Filled.Share, contentDescription = "Share video")
                            }
                            IconButton(
                                onClick = { onEditVideo(video) },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit video")
                            }
                            IconButton(
                                onClick = { onDeleteVideo(video) },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete video", tint = HgRed)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    uiState: HarshgamerUiState,
    onToggleRememberLogin: (Boolean) -> Unit,
    onChangePassword: (current: String, next: String, onDone: (Result<String>) -> Unit) -> Unit,
    onCloudSaved: (String) -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("hg113_cloudinary", Context.MODE_PRIVATE) }

    var cloudName by remember { mutableStateOf(prefs.getString("cloud_name", "") ?: "") }
    var uploadPreset by remember { mutableStateOf(prefs.getString("upload_preset", "hg_videos") ?: "hg_videos") }

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordFeedback by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Column {
                    Text("SETTINGS", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        text = "Session, Cloud Storage & Account Security",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Remember Login", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "Stay signed in when reopening Harshgamer113",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.rememberLogin,
                        onCheckedChange = onToggleRememberLogin,
                        modifier = Modifier.testTag("settings_remember_switch")
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Cloud, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Cloud Video Storage (Cloudinary)", style = MaterialTheme.typography.titleMedium)
                    }
                    Text(
                        text = "Enter your Cloudinary Cloud Name and unsigned upload preset to upload MP4 videos and thumbnails directly to cloud storage.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = cloudName,
                        onValueChange = { cloudName = it },
                        label = { Text("Cloudinary Cloud Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_cloud_name_input")
                    )
                    OutlinedTextField(
                        value = uploadPreset,
                        onValueChange = { uploadPreset = it },
                        label = { Text("Unsigned Upload Preset") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            prefs.edit()
                                .putString("cloud_name", cloudName.trim())
                                .putString("upload_preset", uploadPreset.trim().ifBlank { "hg_videos" })
                                .apply()
                            onCloudSaved("Cloud upload settings saved")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("settings_save_cloud_button")
                    ) {
                        Text("SAVE CLOUD CONFIG", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = HgRed)
                        Text("Change Password", style = MaterialTheme.typography.titleMedium)
                    }
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it; passwordFeedback = null },
                        label = { Text("Current Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it; passwordFeedback = null },
                        label = { Text("New Password (min 6 chars)") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; passwordFeedback = null },
                        label = { Text("Confirm New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (passwordFeedback != null) {
                        Text(
                            text = passwordFeedback!!,
                            color = if (passwordError) HgRed else MaterialTheme.colorScheme.tertiary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            if (newPassword != confirmPassword) {
                                passwordError = true
                                passwordFeedback = "New passwords do not match."
                                return@OutlinedButton
                            }
                            onChangePassword(currentPassword, newPassword) { res ->
                                res.onSuccess { msg ->
                                    passwordError = false
                                    passwordFeedback = msg
                                    currentPassword = ""
                                    newPassword = ""
                                    confirmPassword = ""
                                }.onFailure { err ->
                                    passwordError = true
                                    passwordFeedback = err.localizedMessage ?: "Failed to update password."
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("settings_change_password_button")
                    ) {
                        Text("UPDATE PASSWORD", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        item {
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("settings_logout_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HgRed,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("LOGOUT ACCOUNT", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
