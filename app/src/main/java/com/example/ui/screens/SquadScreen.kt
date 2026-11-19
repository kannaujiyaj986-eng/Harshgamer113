package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.PlayerProfileEntity
import com.example.data.local.SquadNoteEntity
import com.example.ui.HarshgamerUiState
import com.example.ui.components.CyberSectionHeader
import com.example.ui.components.CyberStatusBadge
import com.example.ui.components.EmptyCyberState
import com.example.ui.components.StatMetricTile
import com.example.ui.theme.HgRed

private val NOTE_CATEGORIES = listOf("All", "Drop Spot", "Recoil Tip", "Callout", "Goal")

@Composable
fun SquadScreen(
    uiState: HarshgamerUiState,
    onSelectNoteCategory: (String) -> Unit,
    onAddNote: (String, String, String) -> Unit,
    onTogglePinNote: (SquadNoteEntity) -> Unit,
    onDeleteNote: (Long) -> Unit,
    onUpdatePhoto: (Uri, Boolean) -> Unit,
    onUpdateProfile: (
        username: String,
        fullName: String,
        gamerTag: String,
        mainGame: String,
        role: String,
        deviceName: String,
        targetKd: String,
        bio: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showAddNoteDialog by remember { mutableStateOf(false) }

    val avatarPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) onUpdatePhoto(uri, false)
    }

    val coverPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) onUpdatePhoto(uri, true)
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("squad_screen"),
        containerColor = Color.Transparent,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddNoteDialog = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = {
                    Text(
                        text = stringResource(R.string.add_note_action),
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.testTag("add_note_fab")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 92.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                CyberSectionHeader(
                    title = stringResource(R.string.squad_header_title),
                    subtitle = "Personal gaming profile, media & tactical playbook"
                )
            }

            item {
                PlayerProfileCard(
                    profile = uiState.playerProfile,
                    fallbackEmail = uiState.currentUser?.email ?: "squad113@harshgamer.app",
                    fallbackName = uiState.currentUser?.displayName ?: "Squad113_Player",
                    fallbackUsername = uiState.currentUser?.username ?: "player113",
                    onPickAvatar = {
                        avatarPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    onPickCover = {
                        coverPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    onEditClick = { showEditProfileDialog = true }
                )
            }

            item {
                CyberSectionHeader(
                    title = "Tactical Strategy Board",
                    subtitle = "Pinned drop spots, callouts & recoil drills"
                )
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(NOTE_CATEGORIES) { cat ->
                        FilterChip(
                            selected = uiState.selectedNoteCategory == cat,
                            onClick = { onSelectNoteCategory(cat) },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("note_filter_${cat.lowercase().replace(" ", "_")}")
                        )
                    }
                }
            }

            if (uiState.filteredNotes.isEmpty()) {
                item {
                    EmptyCyberState(
                        icon = Icons.Filled.Notes,
                        title = stringResource(R.string.empty_notes_title),
                        hint = stringResource(R.string.empty_notes_hint)
                    )
                }
            } else {
                items(uiState.filteredNotes, key = { it.id }) { note ->
                    SquadNoteCard(
                        note = note,
                        onTogglePin = { onTogglePinNote(note) },
                        onDelete = { onDeleteNote(note.id) }
                    )
                }
            }
        }
    }

    if (showEditProfileDialog) {
        EditProfileDialog(
            current = uiState.playerProfile,
            fallbackName = uiState.currentUser?.displayName ?: "Squad113_Striker",
            fallbackUsername = uiState.currentUser?.username ?: "player113",
            onDismiss = { showEditProfileDialog = false },
            onSave = { username, fullName, tag, game, role, device, targetKd, bio ->
                onUpdateProfile(username, fullName, tag, game, role, device, targetKd, bio)
                showEditProfileDialog = false
            }
        )
    }

    if (showAddNoteDialog) {
        AddSquadNoteDialog(
            onDismiss = { showAddNoteDialog = false },
            onSave = { title, category, content ->
                onAddNote(title, category, content)
                showAddNoteDialog = false
            }
        )
    }
}

@Composable
private fun PlayerProfileCard(
    profile: PlayerProfileEntity?,
    fallbackEmail: String,
    fallbackName: String,
    fallbackUsername: String,
    onPickAvatar: () -> Unit,
    onPickCover: () -> Unit,
    onEditClick: () -> Unit
) {
    val displayFullName = profile?.fullName?.ifBlank { fallbackName } ?: fallbackName
    val displayUsername = profile?.username?.ifBlank { fallbackUsername } ?: fallbackUsername
    val displayEmail = profile?.ownerEmail?.ifBlank { fallbackEmail } ?: fallbackEmail

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("player_profile_card"),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(128.dp)
            ) {
                if (!profile?.coverUri.isNullOrBlank()) {
                    AsyncImage(
                        model = profile?.coverUri,
                        contentDescription = "Profile cover photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_arena),
                        contentDescription = "Default profile banner",
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
                OutlinedButton(
                    onClick = onPickCover,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .height(48.dp)
                        .testTag("change_cover_button"),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cover Photo", style = MaterialTheme.typography.labelMedium)
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(modifier = Modifier.size(64.dp)) {
                            if (!profile?.avatarUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = profile?.avatarUri,
                                    contentDescription = stringResource(R.string.cd_creator_avatar),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.img_harsh_avatar),
                                    contentDescription = stringResource(R.string.cd_creator_avatar),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = displayFullName,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "@$displayUsername • ${profile?.gamerTag ?: displayFullName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = displayEmail,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onPickAvatar,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("change_avatar_button"),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                    ) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Change Photo", style = MaterialTheme.typography.labelLarge)
                    }
                    OutlinedButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("edit_profile_button"),
                        border = BorderStroke(1.dp, HgRed.copy(alpha = 0.6f))
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = HgRed)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.edit_profile_action), color = HgRed)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatMetricTile(
                        label = "GAME & ROLE",
                        value = "${profile?.mainGame ?: "BGMI"} • ${profile?.role ?: "Assaulter"}",
                        accentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricTile(
                        label = "DEVICE • K/D",
                        value = "${profile?.deviceName ?: "120FPS"} • ${profile?.targetKd ?: "5.0"}",
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (!profile?.bio.isNullOrBlank()) {
                    Text(
                        text = profile?.bio ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                CyberStatusBadge(
                    label = stringResource(R.string.local_storage_badge),
                    accentColor = MaterialTheme.colorScheme.tertiary,
                    icon = Icons.Filled.Save
                )
            }
        }
    }
}

@Composable
private fun SquadNoteCard(
    note: SquadNoteEntity,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryColor = when (note.category) {
        "Drop Spot" -> MaterialTheme.colorScheme.primary
        "Recoil Tip" -> MaterialTheme.colorScheme.secondary
        "Goal" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("squad_note_card_${note.id}"),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.dp,
            if (note.isPinned) categoryColor.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CyberStatusBadge(
                    label = note.category.uppercase(),
                    accentColor = categoryColor
                )
                Row {
                    IconButton(
                        onClick = onTogglePin,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("pin_note_${note.id}")
                    ) {
                        Icon(
                            imageVector = if (note.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = stringResource(R.string.cd_pin_note),
                            tint = if (note.isPinned) categoryColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("delete_note_${note.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = stringResource(R.string.cd_delete_item),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Text(
                text = note.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EditProfileDialog(
    current: PlayerProfileEntity?,
    fallbackName: String,
    fallbackUsername: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String, String, String) -> Unit
) {
    var fullName by remember { mutableStateOf(current?.fullName?.ifBlank { fallbackName } ?: fallbackName) }
    var username by remember { mutableStateOf(current?.username?.ifBlank { fallbackUsername } ?: fallbackUsername) }
    var gamerTag by remember { mutableStateOf(current?.gamerTag ?: fallbackName) }
    var mainGame by remember { mutableStateOf(current?.mainGame ?: "BGMI") }
    var role by remember { mutableStateOf(current?.role ?: "Assaulter") }
    var deviceName by remember { mutableStateOf(current?.deviceName ?: "120FPS Pro OLED") }
    var targetKd by remember { mutableStateOf(current?.targetKd ?: "5.0 K/D") }
    var bio by remember { mutableStateOf(current?.bio ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("EDIT PERSONAL PROFILE", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_profile_fullname")
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_profile_username")
                    )
                    OutlinedTextField(
                        value = gamerTag,
                        onValueChange = { gamerTag = it },
                        label = { Text("Gamer Tag") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_profile_gamertag")
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = mainGame,
                        onValueChange = { mainGame = it },
                        label = { Text("Main Game") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = role,
                        onValueChange = { role = it },
                        label = { Text("Role") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = deviceName,
                        onValueChange = { deviceName = it },
                        label = { Text("Device") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = targetKd,
                        onValueChange = { targetKd = it },
                        label = { Text("Target K/D") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(username, fullName, gamerTag, mainGame, role, deviceName, targetKd, bio)
                },
                enabled = fullName.isNotBlank() && gamerTag.isNotBlank(),
                modifier = Modifier.testTag("save_profile_button")
            ) {
                Text("SAVE PROFILE")
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
private fun AddSquadNoteDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Drop Spot") }
    var content by remember { mutableStateOf("") }
    val categories = listOf("Drop Spot", "Recoil Tip", "Callout", "Goal")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("ADD STRATEGY NOTE", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_note_title")
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Tactical Details") },
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_note_content")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, category, content) },
                enabled = title.isNotBlank() && content.isNotBlank(),
                modifier = Modifier.testTag("save_note_button")
            ) {
                Text("PIN TO BOARD")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
