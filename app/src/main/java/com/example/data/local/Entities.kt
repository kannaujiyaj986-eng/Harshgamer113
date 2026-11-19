package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "loadouts")
data class LoadoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val game: String,
    val code: String,
    val cameraSens: Int,
    val adsSens: Int,
    val gyroSens: Int,
    val clawStyle: String,
    val notes: String,
    val isFavorite: Boolean = false,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "scrims")
data class ScrimEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val game: String,
    val mode: String,
    val mapName: String,
    val scheduleLabel: String,
    val prizeLabel: String,
    val maxSlots: Int,
    val filledSlots: Int,
    val isRegistered: Boolean = false,
    val registeredIgn: String = "",
    val registeredSquad: String = "",
    val registeredRole: String = ""
)

@Entity(tableName = "match_logs")
data class MatchLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val game: String,
    val mode: String,
    val mapName: String,
    val kills: Int,
    val placement: Int,
    val damage: Int,
    val notes: String,
    val playedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "squad_notes")
data class SquadNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String,
    val content: String,
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
    @PrimaryKey val ownerEmail: String,
    val username: String,
    val fullName: String,
    val gamerTag: String,
    val mainGame: String,
    val role: String,
    val deviceName: String,
    val targetKd: String,
    val bio: String,
    val avatarUri: String = "",
    val coverUri: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_accounts")
data class UserAccountEntity(
    @PrimaryKey val email: String,
    val displayName: String,
    val username: String = "",
    val passwordHash: String,
    val passwordSalt: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "video_items")
data class VideoItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String,
    val duration: String,
    val viewsLabel: String,
    val youtubeUrl: String,
    val thumbUrl: String = "",
    val viewsCount: Long = 0L,
    val uploadedBy: String = "Harshgamer113",
    val ownerEmail: String = "squad113@harshgamer.app",
    val description: String,
    val isSaved: Boolean = false,
    val publishedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roomId: String = "global",
    val senderId: String = "",
    val senderName: String,
    val senderRole: String,
    val message: String,
    val isFromCurrentUser: Boolean = false,
    val sentAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reported_messages")
data class ReportedMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val messageId: String,
    val reportedUid: String,
    val reportedText: String,
    val reason: String,
    val reporterUid: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "blocked_users",
    primaryKeys = ["ownerUid", "blockedUid"]
)
data class BlockedUserEntity(
    val ownerUid: String,
    val blockedUid: String,
    val blockedName: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "help_questions")
data class HelpQuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uid: String,
    val name: String,
    val question: String,
    val answer: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "ai_prompt_logs")
data class AiPromptLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val prompt: String,
    val response: String,
    val category: String,
    val createdAt: Long = System.currentTimeMillis()
)
