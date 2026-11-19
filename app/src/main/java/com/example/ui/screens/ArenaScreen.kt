package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.LoadoutEntity
import com.example.ui.HarshgamerUiState
import com.example.ui.components.CyberSectionHeader
import com.example.ui.components.CyberStatusBadge
import com.example.ui.components.LoadoutCard
import com.example.ui.components.StatMetricTile
import com.example.ui.theme.HgRed
import com.example.ui.theme.JetBrainsMonoFontFamily

@Composable
fun ArenaScreen(
    uiState: HarshgamerUiState,
    onCopyUid: (String) -> Unit,
    onCopyLoadout: (LoadoutEntity) -> Unit,
    onToggleFavorite: (LoadoutEntity) -> Unit,
    onNavigateToLoadouts: () -> Unit,
    onNavigateToScrims: () -> Unit,
    onNavigateToVideosUpload: () -> Unit = {},
    onNavigateToChannel: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val creatorUid = stringResource(R.string.creator_uid_value)
    val featuredLoadouts = uiState.loadouts.take(3)
    val registeredScrimsCount = uiState.scrims.count { it.isRegistered }
    val welcomeName = uiState.currentUser?.displayName ?: uiState.playerProfile?.gamerTag ?: "Gamer"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("arena_screen"),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            HeroCreatorSection(
                welcomeName = welcomeName,
                creatorUid = creatorUid,
                onCopyUid = { onCopyUid(creatorUid) },
                onOpenYoutube = {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://youtube.com/@harshgamer-113"))
                    )
                }
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatMetricTile(
                    label = "AVG KILLS",
                    value = String.format("%.1f", uiState.careerStats.avgKills),
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatMetricTile(
                    label = "WIN RATE",
                    value = "${uiState.careerStats.winRatePercent}%",
                    accentColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
                StatMetricTile(
                    label = "SCRIMS",
                    value = "$registeredScrimsCount/${uiState.scrims.size}",
                    accentColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            QuickDashboardActions(
                onUploadVideo = onNavigateToVideosUpload,
                onOpenChannel = onNavigateToChannel,
                onOpenProfile = onNavigateToProfile,
                onOpenSettings = onNavigateToSettings
            )
        }

        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CyberSectionHeader(
                    title = "Community Scrims",
                    subtitle = "Custom lobbies hosted for Squad 113",
                    actionLabel = stringResource(R.string.featured_scrim_cta),
                    onActionClick = onNavigateToScrims
                )
                TournamentSpotlightCard(
                    activeScrimsCount = uiState.scrims.size,
                    registeredCount = registeredScrimsCount,
                    onClick = onNavigateToScrims
                )
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CyberSectionHeader(
                    title = stringResource(R.string.featured_loadouts_title),
                    subtitle = "Tap copy to import sensitivity codes",
                    actionLabel = stringResource(R.string.view_all_action),
                    onActionClick = onNavigateToLoadouts,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(featuredLoadouts, key = { it.id }) { loadout ->
                        LoadoutCard(
                            loadout = loadout,
                            onCopyCode = onCopyLoadout,
                            onToggleFavorite = onToggleFavorite,
                            modifier = Modifier.width(308.dp)
                        )
                    }
                }
            }
        }

        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CyberSectionHeader(
                    title = stringResource(R.string.pro_rig_title),
                    subtitle = "Recommended competitive hardware & HUD specs"
                )
                BattleRigSpecsCard()
            }
        }
    }
}

@Composable
private fun HeroCreatorSection(
    welcomeName: String,
    creatorUid: String,
    onCopyUid: () -> Unit,
    onOpenYoutube: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_hero_arena),
                contentDescription = stringResource(R.string.cd_hero_banner),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.background.copy(alpha = 0.65f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            )
            CyberStatusBadge(
                label = stringResource(R.string.hero_live_badge),
                accentColor = MaterialTheme.colorScheme.tertiary,
                icon = Icons.Filled.Bolt,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-36).dp)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            width = 2.5.dp,
                            brush = Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    HgRed
                                )
                            ),
                            shape = CircleShape
                        )
                        .padding(3.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_harsh_avatar),
                        contentDescription = stringResource(R.string.cd_creator_avatar),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "HARSHGAMER113",
                            style = MaterialTheme.typography.headlineLarge,
                            color = HgRed
                        )
                        Icon(
                            imageVector = Icons.Filled.Verified,
                            contentDescription = "Verified Creator",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Welcome, $welcomeName",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Button(
                onClick = onOpenYoutube,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("visit_youtube_channel_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HgRed,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("VISIT YOUTUBE CHANNEL", style = MaterialTheme.typography.labelLarge)
            }

            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.creator_uid_label).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = creatorUid,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    OutlinedButton(
                        onClick = onCopyUid,
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("copy_creator_uid_button"),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.copy_uid_action),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TournamentSpotlightCard(
    activeScrimsCount: Int,
    registeredCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("arena_scrim_spotlight_card"),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(176.dp)
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
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background.copy(alpha = 0.94f),
                                MaterialTheme.colorScheme.background.copy(alpha = 0.68f),
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
                CyberStatusBadge(
                    label = "$activeScrimsCount OPEN LOBBIES • $registeredCount JOINED",
                    accentColor = MaterialTheme.colorScheme.secondary,
                    icon = Icons.Filled.EmojiEvents
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "NIGHT CLASH CUSTOM ROOMS",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Lock your squad slot, log match K/D, and claim MVP spotlight.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = onClick,
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("arena_open_scrims_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Text("ENTER TOURNAMENT HUB", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun BattleRigSpecsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RigSpecRow(
                icon = Icons.Filled.Gamepad,
                label = "Control Claw",
                value = "4-Finger + Peek Hold"
            )
            RigSpecRow(
                icon = Icons.Filled.ScreenRotation,
                label = "Gyroscope",
                value = "Always On • 335% Red Dot"
            )
            RigSpecRow(
                icon = Icons.Filled.Speed,
                label = "Frame Target",
                value = "120 FPS Smooth / Extreme+"
            )
            RigSpecRow(
                icon = Icons.Filled.HeadsetMic,
                label = "Audio Tuning",
                value = "Ultra Stereo • Footstep EQ"
            )
        }
    }
}

@Composable
private fun RigSpecRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun QuickDashboardActions(
    onUploadVideo: () -> Unit,
    onOpenChannel: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CyberSectionHeader(
            title = "Creator & Hub Shortcuts",
            subtitle = "Quick access to uploads, channel & account tools"
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onUploadVideo,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("arena_quick_upload_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HgRed,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Icon(Icons.Filled.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Upload Video", style = MaterialTheme.typography.labelLarge)
            }
            OutlinedButton(
                onClick = onOpenChannel,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("arena_quick_channel_button"),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Filled.VideoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("My Channel", style = MaterialTheme.typography.labelLarge)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onOpenProfile,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("arena_quick_profile_button"),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("My Profile", style = MaterialTheme.typography.labelLarge)
            }
            OutlinedButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("arena_quick_settings_button"),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Icon(Icons.Filled.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Settings", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
