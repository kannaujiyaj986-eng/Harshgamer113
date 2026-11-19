package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AiPromptLogEntity
import com.example.data.local.BlockedUserEntity
import com.example.data.local.HarshgamerDatabase
import com.example.data.local.HelpQuestionEntity
import com.example.data.local.LoadoutEntity
import com.example.data.local.MatchLogEntity
import com.example.data.local.PlayerProfileEntity
import com.example.data.local.ScrimEntity
import com.example.data.local.SquadNoteEntity
import com.example.data.repository.AuthRepository
import com.example.data.repository.ChatRepository
import com.example.data.repository.HarshgamerRepository
import com.example.data.repository.LocalGamerUser
import com.example.data.repository.Message
import com.example.data.repository.Video
import com.example.data.repository.VideoRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CareerStats(
    val totalMatches: Int = 0,
    val avgKills: Double = 0.0,
    val winCount: Int = 0,
    val winRatePercent: Int = 0,
    val bestKills: Int = 0
)

data class VideoUploadState(
    val isUploading: Boolean = false,
    val progressPercent: Int = 0,
    val errorMessage: String? = null
)

data class HarshgamerUiState(
    val currentUser: LocalGamerUser? = null,
    val rememberLogin: Boolean = true,
    val isAdmin: Boolean = false,
    val loadouts: List<LoadoutEntity> = emptyList(),
    val filteredLoadouts: List<LoadoutEntity> = emptyList(),
    val selectedGameFilter: String = "All",
    val favoritesOnly: Boolean = false,
    val scrims: List<ScrimEntity> = emptyList(),
    val matchLogs: List<MatchLogEntity> = emptyList(),
    val careerStats: CareerStats = CareerStats(),
    val squadNotes: List<SquadNoteEntity> = emptyList(),
    val filteredNotes: List<SquadNoteEntity> = emptyList(),
    val selectedNoteCategory: String = "All",
    val playerProfile: PlayerProfileEntity? = null,
    val videos: List<Video> = emptyList(),
    val filteredVideos: List<Video> = emptyList(),
    val myUploadedVideos: List<Video> = emptyList(),
    val myTotalViews: Long = 0L,
    val selectedVideoCategory: String = "All",
    val videoSearchQuery: String = "",
    val videoUploadState: VideoUploadState = VideoUploadState(),
    val selectedChatRoom: String = "global",
    val chatMessages: List<Message> = emptyList(),
    val blockedUsers: List<BlockedUserEntity> = emptyList(),
    val helpQuestions: List<HelpQuestionEntity> = emptyList(),
    val aiLogs: List<AiPromptLogEntity> = emptyList(),
    val isAiGenerating: Boolean = false,
    val isLiveGeminiEnabled: Boolean = false,
    val isReady: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class HarshgamerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HarshgamerRepository
    val authRepository: AuthRepository
    val videoRepository: VideoRepository
    val chatRepository: ChatRepository

    private val selectedGameFilter = MutableStateFlow("All")
    private val favoritesOnly = MutableStateFlow(false)
    private val selectedNoteCategory = MutableStateFlow("All")
    private val selectedVideoCategory = MutableStateFlow("All")
    private val videoSearchQuery = MutableStateFlow("")
    private val selectedChatRoom = MutableStateFlow("global")
    private val adminModeOverride = MutableStateFlow(false)
    private val videoUploadState = MutableStateFlow(VideoUploadState())
    private val isAiGenerating = MutableStateFlow(false)
    private val seeded = MutableStateFlow(false)

    init {
        val dao = HarshgamerDatabase.getInstance(application).harshgamerDao()
        repository = HarshgamerRepository(dao)
        authRepository = AuthRepository(application, dao)
        videoRepository = VideoRepository(dao)
        chatRepository = ChatRepository(dao) { authRepository.currentUser.value }
        viewModelScope.launch {
            authRepository.ensureSeedCreatorAccount()
            repository.ensureSeeded()
            seeded.value = true
        }
    }

    private data class FilterState(
        val game: String,
        val favoritesOnly: Boolean,
        val noteCategory: String,
        val videoCategory: String,
        val videoSearch: String,
        val chatRoom: String,
        val adminOverride: Boolean,
        val uploadState: VideoUploadState,
        val aiGenerating: Boolean,
        val seeded: Boolean,
        val rememberLogin: Boolean
    )

    private val filterPartA = combine(
        selectedGameFilter,
        favoritesOnly,
        selectedNoteCategory,
        selectedVideoCategory,
        videoSearchQuery
    ) { game, fav, noteCat, videoCat, search ->
        listOf(game, fav.toString(), noteCat, videoCat, search)
    }

    private val filterPartB = combine(
        selectedChatRoom,
        adminModeOverride,
        videoUploadState,
        isAiGenerating,
        seeded
    ) { chatRoom, admin, upload, aiGen, isSeeded ->
        FilterState(
            game = "",
            favoritesOnly = false,
            noteCategory = "",
            videoCategory = "",
            videoSearch = "",
            chatRoom = chatRoom,
            adminOverride = admin,
            uploadState = upload,
            aiGenerating = aiGen,
            seeded = isSeeded,
            rememberLogin = true
        )
    }

    private val filterFlow = combine(
        filterPartA,
        filterPartB,
        authRepository.rememberLogin
    ) { a, b, rememberMe ->
        b.copy(
            game = a[0],
            favoritesOnly = a[1].toBoolean(),
            noteCategory = a[2],
            videoCategory = a[3],
            videoSearch = a[4],
            rememberLogin = rememberMe
        )
    }

    private data class CoreSnapshot(
        val loadouts: List<LoadoutEntity>,
        val scrims: List<ScrimEntity>,
        val matches: List<MatchLogEntity>,
        val notes: List<SquadNoteEntity>,
        val profile: PlayerProfileEntity?
    )

    private val activeProfileFlow = authRepository.currentUser.flatMapLatest { user ->
        if (user == null) flowOf(null)
        else repository.observePlayerProfile(user.email)
    }

    private val coreFlow = combine(
        repository.loadouts,
        repository.scrims,
        repository.matchLogs,
        repository.squadNotes,
        activeProfileFlow
    ) { loadouts, scrims, matches, notes, profile ->
        CoreSnapshot(loadouts, scrims, matches, notes, profile)
    }

    private data class CommunitySnapshot(
        val user: LocalGamerUser?,
        val videos: List<Video>,
        val chats: List<Message>,
        val blockedUsers: List<BlockedUserEntity>,
        val helpQuestions: List<HelpQuestionEntity>,
        val aiLogs: List<AiPromptLogEntity>
    )

    private val roomMessagesFlow = selectedChatRoom.flatMapLatest { roomId ->
        chatRepository.messages(roomId)
    }

    private val blockedUsersFlow = authRepository.currentUser.flatMapLatest {
        chatRepository.observeBlockedUsers()
    }

    private val helpQuestionsFlow = authRepository.currentUser.flatMapLatest {
        chatRepository.observeMyQuestions()
    }

    private val chatBundleFlow = combine(
        roomMessagesFlow,
        blockedUsersFlow,
        helpQuestionsFlow
    ) { chats, blocked, questions ->
        Triple(chats, blocked, questions)
    }

    private val communityFlow = combine(
        authRepository.currentUser,
        videoRepository.observeVideos(),
        chatBundleFlow,
        repository.aiLogs
    ) { user, videos, chatBundle, aiLogs ->
        CommunitySnapshot(
            user = user,
            videos = videos,
            chats = chatBundle.first,
            blockedUsers = chatBundle.second,
            helpQuestions = chatBundle.third,
            aiLogs = aiLogs
        )
    }

    val uiState: StateFlow<HarshgamerUiState> = combine(coreFlow, communityFlow, filterFlow) { core, community, filters ->
        val filteredLoadouts = core.loadouts.filter { item ->
            val gameOk = filters.game == "All" || item.game.equals(filters.game, ignoreCase = true)
            val favOk = !filters.favoritesOnly || item.isFavorite
            gameOk && favOk
        }
        val filteredNotes = core.notes.filter { note ->
            filters.noteCategory == "All" || note.category.equals(filters.noteCategory, ignoreCase = true)
        }
        val userEmail = community.user?.email?.trim()?.lowercase().orEmpty()
        val query = filters.videoSearch.trim()
        val myUploadedVideos = if (userEmail.isBlank()) emptyList() else {
            community.videos.filter { it.ownerEmail.equals(userEmail, ignoreCase = true) }
        }
        val myTotalViews = myUploadedVideos.sumOf { it.views }

        val filteredVideos = community.videos.filter { video ->
            val catOk = when (filters.videoCategory) {
                "All" -> true
                "Saved" -> video.isSaved
                "My Videos" -> userEmail.isNotBlank() && video.ownerEmail.equals(userEmail, ignoreCase = true)
                else -> video.category.equals(filters.videoCategory, ignoreCase = true)
            }
            val searchOk = query.isBlank() ||
                video.title.contains(query, ignoreCase = true) ||
                video.description.contains(query, ignoreCase = true) ||
                video.category.contains(query, ignoreCase = true) ||
                video.uploadedBy.contains(query, ignoreCase = true)
            catOk && searchOk
        }
        val blockedSet = community.blockedUsers.map { it.blockedUid }.toSet()
        val visibleChats = community.chats.filterNot { it.senderId in blockedSet }

        val total = core.matches.size
        val avgKills = if (total > 0) core.matches.sumOf { it.kills }.toDouble() / total else 0.0
        val wins = core.matches.count { it.placement == 1 }
        val winRate = if (total > 0) ((wins * 100.0) / total).toInt() else 0
        val bestKills = core.matches.maxOfOrNull { it.kills } ?: 0
        val isAdmin = videoRepository.isAdmin(community.user?.email, filters.adminOverride)

        HarshgamerUiState(
            currentUser = community.user,
            rememberLogin = filters.rememberLogin,
            isAdmin = isAdmin,
            loadouts = core.loadouts,
            filteredLoadouts = filteredLoadouts,
            selectedGameFilter = filters.game,
            favoritesOnly = filters.favoritesOnly,
            scrims = core.scrims,
            matchLogs = core.matches,
            careerStats = CareerStats(
                totalMatches = total,
                avgKills = avgKills,
                winCount = wins,
                winRatePercent = winRate,
                bestKills = bestKills
            ),
            squadNotes = core.notes,
            filteredNotes = filteredNotes,
            selectedNoteCategory = filters.noteCategory,
            playerProfile = core.profile,
            videos = community.videos,
            filteredVideos = filteredVideos,
            myUploadedVideos = myUploadedVideos,
            myTotalViews = myTotalViews,
            selectedVideoCategory = filters.videoCategory,
            videoSearchQuery = filters.videoSearch,
            videoUploadState = filters.uploadState,
            selectedChatRoom = filters.chatRoom,
            chatMessages = visibleChats,
            blockedUsers = community.blockedUsers,
            helpQuestions = community.helpQuestions,
            aiLogs = community.aiLogs,
            isAiGenerating = filters.aiGenerating,
            isLiveGeminiEnabled = repository.isLiveGeminiEnabled,
            isReady = filters.seeded
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = HarshgamerUiState(
            currentUser = authRepository.currentUser.value,
            rememberLogin = authRepository.rememberLogin.value
        )
    )

    fun setGameFilter(game: String) { selectedGameFilter.value = game }
    fun toggleFavoritesOnly() { favoritesOnly.value = !favoritesOnly.value }
    fun setNoteCategoryFilter(category: String) { selectedNoteCategory.value = category }
    fun setVideoCategoryFilter(category: String) { selectedVideoCategory.value = category }
    fun setVideoSearchQuery(query: String) { videoSearchQuery.value = query }
    fun setChatRoom(roomId: String) { selectedChatRoom.value = roomId }
    fun toggleAdminMode() { adminModeOverride.value = !adminModeOverride.value }
    fun setRememberLogin(enabled: Boolean) { authRepository.setRememberLogin(enabled) }

    fun clearVideoUploadError() {
        videoUploadState.value = videoUploadState.value.copy(errorMessage = null)
    }

    fun uploadAndSaveVideo(
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
    ) {
        viewModelScope.launch {
            videoUploadState.value = VideoUploadState(isUploading = true, progressPercent = 0, errorMessage = null)
            try {
                val user = uiState.value.currentUser
                    ?: throw IllegalStateException("Please log in before uploading videos.")
                val appCtx = getApplication<Application>()
                val finalVideoUrl = if (videoUri != null) {
                    videoRepository.uploadToCloudinary(
                        ctx = appCtx,
                        uri = videoUri,
                        cloudName = cloudName,
                        uploadPreset = uploadPreset,
                        onProgress = { pct ->
                            videoUploadState.value = videoUploadState.value.copy(progressPercent = pct)
                        }
                    )
                } else {
                    directVideoUrl.trim()
                }

                val finalThumbUrl = if (thumbUri != null) {
                    if (cloudName.isNotBlank()) {
                        videoRepository.uploadToCloudinary(
                            ctx = appCtx,
                            uri = thumbUri,
                            cloudName = cloudName,
                            uploadPreset = uploadPreset,
                            onProgress = {}
                        )
                    } else {
                        videoRepository.persistPickedMediaLocally(
                            ctx = appCtx,
                            uri = thumbUri,
                            folderName = "thumbs",
                            expectedKind = "image",
                            maxBytes = 10L * 1024 * 1024
                        )
                    }
                } else {
                    directThumbUrl.trim()
                }

                val uploader = uiState.value.playerProfile?.fullName?.ifBlank { user.displayName } ?: user.displayName
                videoRepository.saveVideo(
                    title = title,
                    desc = desc,
                    category = category,
                    videoUrl = finalVideoUrl,
                    thumbUrl = finalThumbUrl,
                    uploadedBy = uploader,
                    ownerEmail = user.email
                )
                videoUploadState.value = VideoUploadState()
                onSuccess()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                videoUploadState.value = VideoUploadState(
                    isUploading = false,
                    progressPercent = 0,
                    errorMessage = e.localizedMessage ?: "Video upload failed."
                )
            }
        }
    }

    fun editVideoDetails(
        videoId: String,
        title: String,
        desc: String,
        category: String,
        thumbUrl: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val email = uiState.value.currentUser?.email.orEmpty()
            val res = videoRepository.updateVideoDetails(
                videoId = videoId,
                currentUserEmail = email,
                isAdmin = uiState.value.isAdmin,
                title = title,
                desc = desc,
                category = category,
                thumbUrl = thumbUrl
            )
            onResult(res)
        }
    }

    fun deleteVideo(id: String, onResult: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            val email = uiState.value.currentUser?.email.orEmpty()
            val res = videoRepository.deleteVideo(id, email, uiState.value.isAdmin)
            onResult(res)
        }
    }

    fun incrementVideoViews(id: String) {
        viewModelScope.launch { videoRepository.incrementViews(id) }
    }

    fun toggleVideoSaved(video: Video) {
        viewModelScope.launch { videoRepository.toggleSaved(video) }
    }

    fun updateProfilePhoto(
        uri: Uri,
        isCover: Boolean,
        onResult: (Result<String>) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val user = uiState.value.currentUser
                    ?: throw IllegalStateException("Please log in first.")
                val profile = uiState.value.playerProfile
                val appCtx = getApplication<Application>()
                val localUri = videoRepository.persistPickedMediaLocally(
                    ctx = appCtx,
                    uri = uri,
                    folderName = if (isCover) "covers" else "avatars",
                    expectedKind = "image",
                    maxBytes = 10L * 1024 * 1024
                )
                val res = repository.updateProfile(
                    ownerEmail = user.email,
                    currentUserEmail = user.email,
                    username = profile?.username ?: user.username,
                    fullName = profile?.fullName ?: user.displayName,
                    gamerTag = profile?.gamerTag ?: user.displayName,
                    mainGame = profile?.mainGame ?: "BGMI",
                    role = profile?.role ?: "Assaulter",
                    deviceName = profile?.deviceName ?: "120FPS Pro OLED",
                    targetKd = profile?.targetKd ?: "5.0 K/D",
                    bio = profile?.bio ?: "",
                    avatarUri = if (!isCover) localUri else profile?.avatarUri.orEmpty(),
                    coverUri = if (isCover) localUri else profile?.coverUri.orEmpty()
                )
                if (res.isSuccess) onResult(Result.success(localUri))
                else onResult(Result.failure(res.exceptionOrNull() ?: IllegalStateException("Profile update failed")))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                onResult(Result.failure(e))
            }
        }
    }

    fun updateProfile(
        username: String,
        fullName: String,
        gamerTag: String,
        mainGame: String,
        role: String,
        deviceName: String,
        targetKd: String,
        bio: String,
        onResult: (Result<Unit>) -> Unit = {}
    ) {
        viewModelScope.launch {
            val user = uiState.value.currentUser
            if (user == null) {
                onResult(Result.failure(IllegalStateException("Not authenticated.")))
                return@launch
            }
            val current = uiState.value.playerProfile
            val res = repository.updateProfile(
                ownerEmail = user.email,
                currentUserEmail = user.email,
                username = username,
                fullName = fullName,
                gamerTag = gamerTag,
                mainGame = mainGame,
                role = role,
                deviceName = deviceName,
                targetKd = targetKd,
                bio = bio,
                avatarUri = current?.avatarUri.orEmpty(),
                coverUri = current?.coverUri.orEmpty()
            )
            if (res.isSuccess) {
                authRepository.updateActiveUserDisplay(fullName.ifBlank { gamerTag }, username)
            }
            onResult(res)
        }
    }

    fun changePassword(
        currentPassword: String,
        newPassword: String,
        onResult: (Result<String>) -> Unit
    ) {
        viewModelScope.launch {
            onResult(authRepository.changePassword(currentPassword, newPassword))
        }
    }

    fun toggleLoadoutFavorite(loadout: LoadoutEntity) {
        viewModelScope.launch { repository.toggleFavorite(loadout) }
    }

    fun addLoadout(
        title: String,
        game: String,
        code: String,
        cameraSens: Int,
        adsSens: Int,
        gyroSens: Int,
        clawStyle: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.addLoadout(title, game, code, cameraSens, adsSens, gyroSens, clawStyle, notes)
        }
    }

    fun deleteLoadout(id: Long) {
        viewModelScope.launch { repository.deleteLoadout(id) }
    }

    fun registerForScrim(scrim: ScrimEntity, ign: String, squadName: String, role: String) {
        viewModelScope.launch { repository.registerForScrim(scrim, ign, squadName, role) }
    }

    fun leaveScrim(scrim: ScrimEntity) {
        viewModelScope.launch { repository.leaveScrim(scrim) }
    }

    fun addMatchLog(
        game: String,
        mode: String,
        mapName: String,
        kills: Int,
        placement: Int,
        damage: Int,
        notes: String
    ) {
        viewModelScope.launch {
            repository.addMatchLog(game, mode, mapName, kills, placement, damage, notes)
        }
    }

    fun deleteMatchLog(id: Long) {
        viewModelScope.launch { repository.deleteMatchLog(id) }
    }

    fun addSquadNote(title: String, category: String, content: String) {
        viewModelScope.launch { repository.addSquadNote(title, category, content) }
    }

    fun togglePinNote(note: SquadNoteEntity) {
        viewModelScope.launch { repository.togglePinNote(note) }
    }

    fun deleteSquadNote(id: Long) {
        viewModelScope.launch { repository.deleteSquadNote(id) }
    }

    fun sendChatMessage(message: String) {
        viewModelScope.launch {
            chatRepository.send(message, selectedChatRoom.value)
        }
    }

    fun reportChatMessage(message: Message, reason: String) {
        viewModelScope.launch {
            chatRepository.reportMessage(message, reason)
        }
    }

    fun blockChatUser(uid: String, displayName: String) {
        viewModelScope.launch {
            chatRepository.blockUser(uid, displayName)
        }
    }

    fun unblockChatUser(uid: String) {
        viewModelScope.launch {
            chatRepository.unblockUser(uid)
        }
    }

    fun askHelpQuestion(question: String) {
        viewModelScope.launch {
            chatRepository.askQuestion(question)
        }
    }

    fun generateAiIdeas(prompt: String, category: String, onError: (String) -> Unit) {
        viewModelScope.launch {
            isAiGenerating.value = true
            try {
                val res = repository.generateAiStudioIdeas(prompt, category)
                res.onFailure { err ->
                    onError(err.localizedMessage ?: "Failed to generate ideas.")
                }
            } finally {
                isAiGenerating.value = false
            }
        }
    }

    fun deleteAiLog(id: Long) {
        viewModelScope.launch { repository.deleteAiLog(id) }
    }

    fun logout() {
        authRepository.logout()
    }
}
