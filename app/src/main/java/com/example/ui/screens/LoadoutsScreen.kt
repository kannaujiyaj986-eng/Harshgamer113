package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.LoadoutEntity
import com.example.ui.HarshgamerUiState
import com.example.ui.components.CyberSectionHeader
import com.example.ui.components.EmptyCyberState
import com.example.ui.components.LoadoutCard

private val GAME_FILTERS = listOf("All", "BGMI", "Free Fire", "CODM", "Valorant")

@Composable
fun LoadoutsScreen(
    uiState: HarshgamerUiState,
    onSelectGame: (String) -> Unit,
    onToggleFavoritesOnly: () -> Unit,
    onCopyCode: (LoadoutEntity) -> Unit,
    onToggleFavorite: (LoadoutEntity) -> Unit,
    onDeleteLoadout: (Long) -> Unit,
    onCreateLoadout: (
        title: String,
        game: String,
        code: String,
        cameraSens: Int,
        adsSens: Int,
        gyroSens: Int,
        clawStyle: String,
        notes: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("loadouts_screen"),
        containerColor = Color.Transparent,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = {
                    Text(
                        text = stringResource(R.string.new_loadout_fab),
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("new_loadout_fab")
            )
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
                    title = stringResource(R.string.loadouts_header_title),
                    subtitle = stringResource(R.string.loadouts_header_subtitle)
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = uiState.favoritesOnly,
                            onClick = onToggleFavoritesOnly,
                            label = { Text("Favorites") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Favorite,
                                    contentDescription = null,
                                    tint = if (uiState.favoritesOnly) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.secondary
                            ),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("filter_favorites_chip")
                        )
                    }

                    items(GAME_FILTERS) { game ->
                        val selected = uiState.selectedGameFilter == game
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectGame(game) },
                            label = { Text(game) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("filter_game_${game.lowercase().replace(" ", "_")}")
                        )
                    }
                }
            }

            if (uiState.filteredLoadouts.isEmpty()) {
                item {
                    EmptyCyberState(
                        icon = Icons.Filled.Tune,
                        title = stringResource(R.string.empty_loadouts_title),
                        hint = stringResource(R.string.empty_loadouts_hint)
                    )
                }
            } else {
                items(uiState.filteredLoadouts, key = { it.id }) { loadout ->
                    LoadoutCard(
                        loadout = loadout,
                        onCopyCode = onCopyCode,
                        onToggleFavorite = onToggleFavorite,
                        onDelete = { onDeleteLoadout(it.id) }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateLoadoutDialog(
            onDismiss = { showCreateDialog = false },
            onSave = { title, game, code, cam, ads, gyro, claw, notes ->
                onCreateLoadout(title, game, code, cam, ads, gyro, claw, notes)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun CreateLoadoutDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, Int, Int, Int, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedGame by remember { mutableStateOf("BGMI") }
    var code by remember { mutableStateOf("") }
    var camText by remember { mutableStateOf("120") }
    var adsText by remember { mutableStateOf("105") }
    var gyroText by remember { mutableStateOf("320") }
    var clawStyle by remember { mutableStateOf("4-Finger Gyro") }
    var notes by remember { mutableStateOf("") }

    val gameChoices = listOf("BGMI", "Free Fire", "CODM", "Valorant")
    val canSave = title.isNotBlank() && code.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "NEW SENSITIVITY LOADOUT",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(gameChoices) { game ->
                        FilterChip(
                            selected = selectedGame == game,
                            onClick = { selectedGame = game },
                            label = { Text(game) }
                        )
                    }
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Loadout Title") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_loadout_title")
                )
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Share / Crosshair Code") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_loadout_code")
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = camText,
                        onValueChange = { camText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Cam") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = adsText,
                        onValueChange = { adsText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("ADS") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = gyroText,
                        onValueChange = { gyroText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Gyro") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = clawStyle,
                    onValueChange = { clawStyle = it },
                    label = { Text("Claw / DPI Style") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Tactical Notes (Optional)") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        title,
                        selectedGame,
                        code,
                        camText.toIntOrNull() ?: 100,
                        adsText.toIntOrNull() ?: 100,
                        gyroText.toIntOrNull() ?: 0,
                        clawStyle.ifBlank { "Custom HUD" },
                        notes
                    )
                },
                enabled = canSave,
                modifier = Modifier.testTag("save_loadout_button")
            ) {
                Text("SAVE PRESET")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
