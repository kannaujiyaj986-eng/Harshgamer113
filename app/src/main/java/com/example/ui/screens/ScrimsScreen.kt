package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.MatchLogEntity
import com.example.data.local.ScrimEntity
import com.example.ui.HarshgamerUiState
import com.example.ui.components.CyberSectionHeader
import com.example.ui.components.CyberStatusBadge
import com.example.ui.components.EmptyCyberState
import com.example.ui.components.ScrimCard
import com.example.ui.components.StatMetricTile
import com.example.ui.theme.JetBrainsMonoFontFamily

@Composable
fun ScrimsScreen(
    uiState: HarshgamerUiState,
    onRegisterScrim: (ScrimEntity, String, String, String) -> Unit,
    onLeaveScrim: (ScrimEntity) -> Unit,
    onAddMatchLog: (String, String, String, Int, Int, Int, String) -> Unit,
    onDeleteMatchLog: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var scrimToJoin by remember { mutableStateOf<ScrimEntity?>(null) }
    var showLogMatchDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("scrims_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            CyberSectionHeader(
                title = stringResource(R.string.scrims_header_title),
                subtitle = "Lock squad slots or track your match K/D"
            )
        }

        item {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("scrims_segmented_toggle")
            ) {
                SegmentedButton(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    modifier = Modifier.testTag("tab_scrims_button")
                ) {
                    Text(stringResource(R.string.tab_scrims), style = MaterialTheme.typography.labelLarge)
                }
                SegmentedButton(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    modifier = Modifier.testTag("tab_matches_button")
                ) {
                    Text(stringResource(R.string.tab_matches), style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        if (selectedTab == 0) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(136.dp)
                        .clip(MaterialTheme.shapes.large)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_tournament_banner),
                        contentDescription = stringResource(R.string.cd_tournament_banner),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.background.copy(alpha = 0.88f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CyberStatusBadge(
                            label = "HARSHGAMER113 OFFICIAL LOBBIES",
                            accentColor = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Register your squad to reserve your custom room slot.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            items(uiState.scrims, key = { it.id }) { scrim ->
                ScrimCard(
                    scrim = scrim,
                    onJoinClick = { scrimToJoin = it },
                    onLeaveClick = onLeaveScrim
                )
            }
        } else {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatMetricTile(
                            label = "MATCHES",
                            value = uiState.careerStats.totalMatches.toString(),
                            accentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        StatMetricTile(
                            label = "AVG KILLS",
                            value = String.format("%.1f", uiState.careerStats.avgKills),
                            accentColor = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f)
                        )
                        StatMetricTile(
                            label = "BEST KILLS",
                            value = uiState.careerStats.bestKills.toString(),
                            accentColor = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Button(
                        onClick = { showLogMatchDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("log_match_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.log_match_action).uppercase(),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            if (uiState.matchLogs.isEmpty()) {
                item {
                    EmptyCyberState(
                        icon = Icons.Filled.EmojiEvents,
                        title = stringResource(R.string.empty_matches_title),
                        hint = stringResource(R.string.empty_matches_hint)
                    )
                }
            } else {
                items(uiState.matchLogs, key = { it.id }) { match ->
                    MatchLogCard(
                        match = match,
                        onDelete = { onDeleteMatchLog(match.id) }
                    )
                }
            }
        }
    }

    scrimToJoin?.let { targetScrim ->
        ScrimRegistrationDialog(
            scrim = targetScrim,
            defaultIgn = uiState.playerProfile?.gamerTag ?: "",
            defaultRole = uiState.playerProfile?.role ?: "Assaulter",
            onDismiss = { scrimToJoin = null },
            onConfirm = { ign, squad, role ->
                onRegisterScrim(targetScrim, ign, squad, role)
                scrimToJoin = null
            }
        )
    }

    AnimatedVisibility(visible = showLogMatchDialog) {
        LogMatchDialog(
            onDismiss = { showLogMatchDialog = false },
            onSave = { game, mode, mapName, kills, placement, damage, notes ->
                onAddMatchLog(game, mode, mapName, kills, placement, damage, notes)
                showLogMatchDialog = false
            }
        )
    }
}

@Composable
private fun MatchLogCard(
    match: MatchLogEntity,
    onDelete: () -> Unit
) {
    val isVictory = match.placement == 1
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("match_card_${match.id}"),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.dp,
            if (isVictory) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.55f)
            else MaterialTheme.colorScheme.outline
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CyberStatusBadge(
                        label = match.game.uppercase(),
                        accentColor = MaterialTheme.colorScheme.primary
                    )
                    CyberStatusBadge(
                        label = if (isVictory) "#1 VICTORY" else "RANK #${match.placement}",
                        accentColor = if (isVictory) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary,
                        icon = if (isVictory) Icons.Filled.MilitaryTech else null
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("delete_match_${match.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = stringResource(R.string.cd_delete_item),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "${match.mode} • ${match.mapName}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "${match.kills} KILLS",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "•",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${match.damage} DMG",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            if (match.notes.isNotBlank()) {
                Text(
                    text = match.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ScrimRegistrationDialog(
    scrim: ScrimEntity,
    defaultIgn: String,
    defaultRole: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var ign by remember { mutableStateOf(defaultIgn) }
    var squadName by remember { mutableStateOf("Team 113") }
    var selectedRole by remember { mutableStateOf(defaultRole) }
    val roles = listOf("IGL", "Assaulter", "Sniper", "Support")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "JOIN ${scrim.title.uppercase()}",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = ign,
                    onValueChange = { ign = it },
                    label = { Text("In-Game Name (IGN)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_scrim_ign")
                )
                OutlinedTextField(
                    value = squadName,
                    onValueChange = { squadName = it },
                    label = { Text("Squad Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_scrim_squad")
                )
                Text(
                    text = "Squad Role",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(roles) { role ->
                        FilterChip(
                            selected = selectedRole == role,
                            onClick = { selectedRole = role },
                            label = { Text(role) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(ign, squadName, selectedRole) },
                enabled = ign.isNotBlank() && squadName.isNotBlank(),
                modifier = Modifier.testTag("confirm_join_scrim_button")
            ) {
                Text("LOCK SLOT")
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
private fun LogMatchDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, Int, Int, Int, String) -> Unit
) {
    var game by remember { mutableStateOf("BGMI") }
    var mode by remember { mutableStateOf("Squad Classic") }
    var mapName by remember { mutableStateOf("Erangel") }
    var killsText by remember { mutableStateOf("8") }
    var placementText by remember { mutableStateOf("1") }
    var damageText by remember { mutableStateOf("1650") }
    var notes by remember { mutableStateOf("") }

    val games = listOf("BGMI", "Free Fire", "CODM", "Valorant")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text("LOG MATCH RESULT", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(games) { g ->
                        FilterChip(
                            selected = game == g,
                            onClick = { game = g },
                            label = { Text(g) }
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = mode,
                        onValueChange = { mode = it },
                        label = { Text("Mode") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = mapName,
                        onValueChange = { mapName = it },
                        label = { Text("Map") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = killsText,
                        onValueChange = { killsText = it.filter { c -> c.isDigit() } },
                        label = { Text("Kills") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_match_kills")
                    )
                    OutlinedTextField(
                        value = placementText,
                        onValueChange = { placementText = it.filter { c -> c.isDigit() } },
                        label = { Text("Rank #") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = damageText,
                        onValueChange = { damageText = it.filter { c -> c.isDigit() } },
                        label = { Text("Damage") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Highlight / Clutch Note") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        game,
                        mode.ifBlank { "Ranked" },
                        mapName.ifBlank { "Arena" },
                        killsText.toIntOrNull() ?: 0,
                        (placementText.toIntOrNull() ?: 1).coerceAtLeast(1),
                        damageText.toIntOrNull() ?: 0,
                        notes
                    )
                },
                modifier = Modifier.testTag("save_match_button")
            ) {
                Text("SAVE MATCH")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
