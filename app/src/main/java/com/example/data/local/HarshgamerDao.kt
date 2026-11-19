package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HarshgamerDao {
    @Query("SELECT * FROM loadouts ORDER BY isFavorite DESC, createdAt DESC")
    fun observeLoadouts(): Flow<List<LoadoutEntity>>

    @Query("SELECT COUNT(*) FROM loadouts")
    suspend fun getLoadoutCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoadouts(items: List<LoadoutEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoadout(item: LoadoutEntity)

    @Update
    suspend fun updateLoadout(item: LoadoutEntity)

    @Query("DELETE FROM loadouts WHERE id = :id")
    suspend fun deleteLoadoutById(id: Long)

    @Query("SELECT * FROM scrims ORDER BY isRegistered DESC, id ASC")
    fun observeScrims(): Flow<List<ScrimEntity>>

    @Query("SELECT COUNT(*) FROM scrims")
    suspend fun getScrimCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScrims(items: List<ScrimEntity>)

    @Update
    suspend fun updateScrim(item: ScrimEntity)

    @Query("SELECT * FROM match_logs ORDER BY playedAt DESC")
    fun observeMatchLogs(): Flow<List<MatchLogEntity>>

    @Query("SELECT COUNT(*) FROM match_logs")
    suspend fun getMatchLogCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatchLogs(items: List<MatchLogEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatchLog(item: MatchLogEntity)

    @Query("DELETE FROM match_logs WHERE id = :id")
    suspend fun deleteMatchLogById(id: Long)

    @Query("SELECT * FROM squad_notes ORDER BY isPinned DESC, createdAt DESC")
    fun observeSquadNotes(): Flow<List<SquadNoteEntity>>

    @Query("SELECT COUNT(*) FROM squad_notes")
    suspend fun getSquadNoteCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSquadNotes(items: List<SquadNoteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSquadNote(item: SquadNoteEntity)

    @Update
    suspend fun updateSquadNote(item: SquadNoteEntity)

    @Query("DELETE FROM squad_notes WHERE id = :id")
    suspend fun deleteSquadNoteById(id: Long)

    @Query("SELECT * FROM player_profile WHERE ownerEmail = :email LIMIT 1")
    fun observePlayerProfile(email: String): Flow<PlayerProfileEntity?>

    @Query("SELECT * FROM player_profile WHERE ownerEmail = :email LIMIT 1")
    suspend fun getPlayerProfileOnce(email: String): PlayerProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPlayerProfile(profile: PlayerProfileEntity)

    // Local Auth Accounts
    @Query("SELECT * FROM user_accounts WHERE email = :email LIMIT 1")
    suspend fun findUserByEmail(email: String): UserAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUserAccount(account: UserAccountEntity)

    // Videos
    @Query("SELECT * FROM video_items ORDER BY isSaved DESC, publishedAt DESC")
    fun observeVideos(): Flow<List<VideoItemEntity>>

    @Query("SELECT * FROM video_items ORDER BY publishedAt DESC LIMIT 100")
    suspend fun getVideosOnce(): List<VideoItemEntity>

    @Query("SELECT * FROM video_items WHERE id = :id LIMIT 1")
    suspend fun getVideoById(id: Long): VideoItemEntity?

    @Query("SELECT COUNT(*) FROM video_items")
    suspend fun getVideoCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(items: List<VideoItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(item: VideoItemEntity): Long

    @Update
    suspend fun updateVideo(item: VideoItemEntity)

    @Query("UPDATE video_items SET viewsCount = viewsCount + 1 WHERE id = :id")
    suspend fun incrementVideoViews(id: Long)

    @Query("DELETE FROM video_items WHERE id = :id")
    suspend fun deleteVideoById(id: Long)

    // Community Chat (Multi-room + Reports + Blocking)
    @Query("SELECT * FROM chat_messages WHERE roomId = :roomId ORDER BY sentAt ASC LIMIT 100")
    fun observeChatMessages(roomId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT COUNT(*) FROM chat_messages")
    suspend fun getChatCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessages(items: List<ChatMessageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportedMessageEntity)

    @Query("SELECT COUNT(*) FROM reported_messages")
    suspend fun getReportCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun blockUser(entry: BlockedUserEntity)

    @Query("DELETE FROM blocked_users WHERE ownerUid = :ownerUid AND blockedUid = :blockedUid")
    suspend fun unblockUser(ownerUid: String, blockedUid: String)

    @Query("SELECT * FROM blocked_users WHERE ownerUid = :ownerUid ORDER BY createdAt DESC")
    fun observeBlockedUsers(ownerUid: String): Flow<List<BlockedUserEntity>>

    @Query("SELECT blockedUid FROM blocked_users WHERE ownerUid = :ownerUid")
    suspend fun getBlockedUidsOnce(ownerUid: String): List<String>

    // Help Center Questions
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHelpQuestion(item: HelpQuestionEntity)

    @Query("SELECT * FROM help_questions WHERE uid = :uid ORDER BY createdAt DESC")
    fun observeHelpQuestions(uid: String): Flow<List<HelpQuestionEntity>>

    @Query("SELECT * FROM help_questions WHERE uid = :uid ORDER BY createdAt DESC")
    suspend fun getHelpQuestionsOnce(uid: String): List<HelpQuestionEntity>

    // AI Studio Prompt History
    @Query("SELECT * FROM ai_prompt_logs ORDER BY createdAt DESC")
    fun observeAiLogs(): Flow<List<AiPromptLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAiLog(log: AiPromptLogEntity)

    @Query("DELETE FROM ai_prompt_logs WHERE id = :id")
    suspend fun deleteAiLogById(id: Long)
}
