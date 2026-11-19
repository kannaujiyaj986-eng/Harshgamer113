package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.BlockedUserEntity
import com.example.data.repository.Message
import com.example.ui.HarshgamerUiState
import com.example.ui.components.CyberSectionHeader
import com.example.ui.components.EmptyCyberState
import com.example.ui.theme.HgRed

private data class ChatRoomOption(val id: String, val label: String)

private val CHAT_ROOMS = listOf(
    ChatRoomOption("global", "Global"),
    ChatRoomOption("scrims", "Scrims LFG"),
    ChatRoomOption("bgmi", "BGMI"),
    ChatRoomOption("freefire", "Free Fire")
)

private val REPORT_REASONS = listOf("Spam or Ads", "Abusive Language", "Scam / Phishing", "Off-topic")

@Composable
fun ChatTab(
    uiState: HarshgamerUiState,
    onSelectRoom: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    onReportMessage: (Message, String) -> Unit,
    onBlockUser: (uid: String, displayName: String) -> Unit,
    onUnblockUser: (uid: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var draft by remember { mutableStateOf("") }
    var messageToReport by remember { mutableStateOf<Message?>(null) }
    var showBlockedDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.chatMessages.size, uiState.selectedChatRoom) {
        if (uiState.chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.chatMessages.lastIndex)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .testTag("chat_tab")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CyberSectionHeader(
                title = "Squad 113 Lounge",
                subtitle = "Multi-room community chat",
                actionLabel = if (uiState.blockedUsers.isNotEmpty()) "Blocked (${uiState.blockedUsers.size})" else null,
                onActionClick = if (uiState.blockedUsers.isNotEmpty()) {
                    { showBlockedDialog = true }
                } else null
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(CHAT_ROOMS) { room ->
                    FilterChip(
                        selected = uiState.selectedChatRoom == room.id,
                        onClick = { onSelectRoom(room.id) },
                        label = { Text(room.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("chat_room_${room.id}")
                    )
                }
            }
        }

        if (uiState.chatMessages.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                EmptyCyberState(
                    icon = Icons.Filled.ChatBubbleOutline,
                    title = "Room Is Quiet",
                    hint = "Send the first message in ${CHAT_ROOMS.firstOrNull { it.id == uiState.selectedChatRoom }?.label ?: "this room"}!"
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.chatMessages, key = { it.id }) { msg ->
                    ChatBubble(
                        message = msg,
                        onReportClick = { messageToReport = msg },
                        onBlockClick = { onBlockUser(msg.senderId, msg.senderName) }
                    )
                }
            }
        }

        Surface(
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { if (it.length <= 1000) draft = it },
                        placeholder = { Text("Message Squad 113...") },
                        maxLines = 3,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_message_input")
                    )
                    IconButton(
                        onClick = {
                            if (draft.isNotBlank()) {
                                onSendMessage(draft)
                                draft = ""
                            }
                        },
                        enabled = draft.isNotBlank() && draft.length <= 1000,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("chat_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send message"
                        )
                    }
                }
                if (draft.length > 850) {
                    Text(
                        text = "${draft.length}/1000 characters",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    messageToReport?.let { target ->
        ReportMessageDialog(
            message = target,
            onDismiss = { messageToReport = null },
            onSubmit = { reason ->
                onReportMessage(target, reason)
                messageToReport = null
            }
        )
    }

    if (showBlockedDialog) {
        BlockedUsersDialog(
            blockedUsers = uiState.blockedUsers,
            onUnblock = onUnblockUser,
            onDismiss = { showBlockedDialog = false }
        )
    }
}

@Composable
private fun ChatBubble(
    message: Message,
    onReportClick: () -> Unit,
    onBlockClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val alignment = if (message.isFromCurrentUser) Alignment.End else Alignment.Start
    val bubbleColor = if (message.isFromCurrentUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    val borderColor = when {
        message.isFromCurrentUser -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        message.senderRole == "CREATOR" -> HgRed.copy(alpha = 0.65f)
        else -> MaterialTheme.colorScheme.outline
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 330.dp)
                .testTag("chat_bubble_${message.id}"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = bubbleColor),
            border = BorderStroke(1.dp, borderColor)
        ) {
            Column(
                modifier = Modifier.padding(start = 12.dp, end = 6.dp, top = 8.dp, bottom = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = message.senderName,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (message.senderRole == "CREATOR") HgRed else MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "• ${message.senderRole}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (!message.isFromCurrentUser) {
                        Box {
                            IconButton(
                                onClick = { menuExpanded = true },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("chat_options_${message.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = "Message options",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Report Message") },
                                    leadingIcon = { Icon(Icons.Filled.Flag, contentDescription = null) },
                                    onClick = {
                                        menuExpanded = false
                                        onReportClick()
                                    }
                                )
                                if (message.senderId.isNotBlank()) {
                                    DropdownMenuItem(
                                        text = { Text("Block ${message.senderName}") },
                                        leadingIcon = { Icon(Icons.Filled.Block, contentDescription = null) },
                                        onClick = {
                                            menuExpanded = false
                                            onBlockClick()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(end = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun ReportMessageDialog(
    message: Message,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var selectedReason by remember { mutableStateOf(REPORT_REASONS.first()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("REPORT MESSAGE", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Reporting message from ${message.senderName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                REPORT_REASONS.forEach { reason ->
                    FilterChip(
                        selected = selectedReason == reason,
                        onClick = { selectedReason = reason },
                        label = { Text(reason) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(selectedReason) },
                modifier = Modifier.testTag("confirm_report_button")
            ) {
                Text("SUBMIT REPORT")
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
private fun BlockedUsersDialog(
    blockedUsers: List<BlockedUserEntity>,
    onUnblock: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("BLOCKED USERS", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (blockedUsers.isEmpty()) {
                    Text("No blocked users.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    blockedUsers.forEach { entry ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = entry.blockedName.ifBlank { entry.blockedUid },
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                            OutlinedButton(
                                onClick = { onUnblock(entry.blockedUid) },
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text("Unblock")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}
