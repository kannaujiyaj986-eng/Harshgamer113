package com.example.data.repository

import com.example.data.local.AiPromptLogEntity
import com.example.data.local.HarshgamerDao
import com.example.data.local.LoadoutEntity
import com.example.data.local.MatchLogEntity
import com.example.data.local.PlayerProfileEntity
import com.example.data.local.ScrimEntity
import com.example.data.local.SquadNoteEntity
import com.example.data.local.VideoItemEntity
import kotlinx.coroutines.flow.Flow

class HarshgamerRepository(
    private val dao: HarshgamerDao,
    private val aiRepository: AiRepository = PlaceholderAiRepository()
) {

    val loadouts: Flow<List<LoadoutEntity>> = dao.observeLoadouts()
    val scrims: Flow<List<ScrimEntity>> = dao.observeScrims()
    val matchLogs: Flow<List<MatchLogEntity>> = dao.observeMatchLogs()
    val squadNotes: Flow<List<SquadNoteEntity>> = dao.observeSquadNotes()
    val aiLogs: Flow<List<AiPromptLogEntity>> = dao.observeAiLogs()

    fun observePlayerProfile(email: String): Flow<PlayerProfileEntity?> =
        dao.observePlayerProfile(email.trim().lowercase())

    val isLiveGeminiEnabled: Boolean
        get() = aiRepository.isLiveGeminiEnabled

    suspend fun ensureSeeded() {
        val now = System.currentTimeMillis()
        if (dao.getLoadoutCount() == 0) {
            dao.insertLoadouts(
                listOf(
                    LoadoutEntity(
                        title = "HG113 Laser M416 Spray",
                        game = "BGMI",
                        code = "7381-9940-1132-8854",
                        cameraSens = 118,
                        adsSens = 104,
                        gyroSens = 335,
                        clawStyle = "4-Finger + Full Gyro",
                        notes = "Zero vertical kick up to 150m with 3x/4x red-dot swap.",
                        isFavorite = true,
                        isDefault = true,
                        createdAt = now - 4000L
                    ),
                    LoadoutEntity(
                        title = "One-Tap Desert Eagle",
                        game = "Free Fire",
                        code = "FF113-HEAD-9940-PRO",
                        cameraSens = 192,
                        adsSens = 178,
                        gyroSens = 0,
                        clawStyle = "3-Finger Drag Shot",
                        notes = "High general drag for crisp close-range headlocks.",
                        isFavorite = true,
                        isDefault = true,
                        createdAt = now - 3000L
                    ),
                    LoadoutEntity(
                        title = "Ranked Rush SMG Kinetic",
                        game = "CODM",
                        code = "CODM-HG113-7741-SLIDE",
                        cameraSens = 145,
                        adsSens = 128,
                        gyroSens = 260,
                        clawStyle = "4-Finger Claw",
                        notes = "Tuned for slide-cancel flicks and tight hipfire tracking.",
                        isFavorite = false,
                        isDefault = true,
                        createdAt = now - 2000L
                    ),
                    LoadoutEntity(
                        title = "Radiant Micro-Flick 1600",
                        game = "Valorant",
                        code = "VAL-0;P;c;5;h;0;0l;4;0o;1",
                        cameraSens = 38,
                        adsSens = 95,
                        gyroSens = 0,
                        clawStyle = "0.24 Sens • 1600 DPI",
                        notes = "Cyan 1-4-2-0 static crosshair with crisp head-level micro-control.",
                        isFavorite = false,
                        isDefault = true,
                        createdAt = now - 1000L
                    )
                )
            )
        }

        if (dao.getScrimCount() == 0) {
            dao.insertScrims(
                listOf(
                    ScrimEntity(
                        title = "Harshgamer113 Night Clash",
                        game = "BGMI",
                        mode = "Squad TPP",
                        mapName = "Erangel",
                        scheduleLabel = "Tonight • 9:00 PM IST",
                        prizeLabel = "MVP Crown + Showcase",
                        maxSlots = 25,
                        filledSlots = 19
                    ),
                    ScrimEntity(
                        title = "113 Lone Wolf Headshot Cup",
                        game = "Free Fire",
                        mode = "Clash Squad 4v4",
                        mapName = "Bermuda",
                        scheduleLabel = "Tomorrow • 7:30 PM IST",
                        prizeLabel = "Squad Spotlight",
                        maxSlots = 16,
                        filledSlots = 11
                    ),
                    ScrimEntity(
                        title = "Cyber Arena SnD Gauntlet",
                        game = "CODM",
                        mode = "5v5 Search & Destroy",
                        mapName = "Standoff",
                        scheduleLabel = "Saturday • 8:00 PM IST",
                        prizeLabel = "Top Fragger Badge",
                        maxSlots = 12,
                        filledSlots = 8
                    )
                )
            )
        }

        if (dao.getMatchLogCount() == 0) {
            dao.insertMatchLogs(
                listOf(
                    MatchLogEntity(
                        game = "BGMI",
                        mode = "Squad Classic",
                        mapName = "Erangel",
                        kills = 14,
                        placement = 1,
                        damage = 2180,
                        notes = "Clutched final 1v3 at Pochinki ridge with 4x M416.",
                        playedAt = now - 86_400_000L
                    ),
                    MatchLogEntity(
                        game = "BGMI",
                        mode = "TDM Warehouse",
                        mapName = "Warehouse",
                        kills = 21,
                        placement = 1,
                        damage = 2850,
                        notes = "Warmup drill using 335 gyro sensitivity.",
                        playedAt = now - 43_200_000L
                    ),
                    MatchLogEntity(
                        game = "Free Fire",
                        mode = "CS Ranked",
                        mapName = "Purgatory",
                        kills = 9,
                        placement = 2,
                        damage = 3420,
                        notes = "Double Desert Eagle opening picks on round 4.",
                        playedAt = now - 14_400_000L
                    )
                )
            )
        }

        if (dao.getSquadNoteCount() == 0) {
            dao.insertSquadNotes(
                listOf(
                    SquadNoteEntity(
                        title = "Erangel Hot-Drop Split",
                        category = "Drop Spot",
                        content = "Pochinki East church compound: IGL takes roof for mid-street info while entry clears lower garage.",
                        isPinned = true,
                        createdAt = now - 5000L
                    ),
                    SquadNoteEntity(
                        title = "Gyro Recoil Warmup (10 min)",
                        category = "Recoil Tip",
                        content = "Practice 60m moving targets in training grounds with no compensator before ranked queue.",
                        isPinned = false,
                        createdAt = now - 2500L
                    ),
                    SquadNoteEntity(
                        title = "Season Goal: Conqueror Push",
                        category = "Goal",
                        content = "Maintain 4.5+ K/D across 40 squad matches with consistent top-5 rotations.",
                        isPinned = false,
                        createdAt = now - 1200L
                    )
                )
            )
        }

        val defaultOwner = "squad113@harshgamer.app"
        if (dao.getPlayerProfileOnce(defaultOwner) == null) {
            dao.upsertPlayerProfile(
                PlayerProfileEntity(
                    ownerEmail = defaultOwner,
                    username = "harshgamer113",
                    fullName = "Harshgamer113",
                    gamerTag = "Harshgamer113",
                    mainGame = "BGMI",
                    role = "IGL • Assaulter",
                    deviceName = "120FPS Pro OLED",
                    targetKd = "5.0 K/D",
                    bio = "Pro Esports Creator • 4-Finger Gyro • Squad 113 Official Hub."
                )
            )
        }

        if (dao.getVideoCount() == 0) {
            dao.insertVideos(
                listOf(
                    VideoItemEntity(
                        title = "1v4 Final Zone Clutch + Zero Recoil 4-Finger Gyro Code",
                        category = "Montages",
                        duration = "12:48",
                        viewsLabel = "48K views",
                        viewsCount = 48_000L,
                        youtubeUrl = "https://youtube.com/@harshgamer-113",
                        uploadedBy = "Harshgamer113",
                        ownerEmail = defaultOwner,
                        description = "Full Erangel ranked breakdown with live hand-cam and updated 335% red-dot gyro sensitivity.",
                        isSaved = true,
                        publishedAt = now - 10_000L
                    ),
                    VideoItemEntity(
                        title = "BGMI & Free Fire Sensitivity Masterclass (120 FPS Settings)",
                        category = "Sensitivity Guides",
                        duration = "15:20",
                        viewsLabel = "72K views",
                        viewsCount = 72_000L,
                        youtubeUrl = "https://youtube.com/@harshgamer-113",
                        uploadedBy = "Harshgamer113",
                        ownerEmail = defaultOwner,
                        description = "Step-by-step ADS, camera, and gyroscope tuning for close-range hipfire and mid-range sprays.",
                        isSaved = false,
                        publishedAt = now - 20_000L
                    ),
                    VideoItemEntity(
                        title = "Harshgamer113 Night Clash Custom Rooms — Live Highlights",
                        category = "Live Streams",
                        duration = "44:15",
                        viewsLabel = "31K views",
                        viewsCount = 31_000L,
                        youtubeUrl = "https://youtube.com/@harshgamer-113",
                        uploadedBy = "Harshgamer113",
                        ownerEmail = defaultOwner,
                        description = "Community squad rotations, Pochinki hot-drops, and final circle callouts.",
                        isSaved = false,
                        publishedAt = now - 30_000L
                    ),
                    VideoItemEntity(
                        title = "Top 5 TDM Drills to Win Every 1v1 Duel",
                        category = "Sensitivity Guides",
                        duration = "09:35",
                        viewsLabel = "64K views",
                        viewsCount = 64_000L,
                        youtubeUrl = "https://youtube.com/@harshgamer-113",
                        uploadedBy = "Harshgamer113",
                        ownerEmail = defaultOwner,
                        description = "Daily 10-minute crosshair placement, crouch-jiggle, and peek timing warmup routine.",
                        isSaved = false,
                        publishedAt = now - 40_000L
                    )
                )
            )
        }

        if (dao.getChatCount() == 0) {
            dao.insertChatMessages(
                listOf(
                    com.example.data.local.ChatMessageEntity(
                        roomId = "global",
                        senderId = "harshgamer113_official",
                        senderName = "Harshgamer113",
                        senderRole = "CREATOR",
                        message = "Welcome to Squad 113 Global Lounge! Drop your IGN or grab the latest M416 gyro code from Home.",
                        isFromCurrentUser = false,
                        sentAt = now - 600_000L
                    ),
                    com.example.data.local.ChatMessageEntity(
                        roomId = "global",
                        senderId = "aarav_igl_uid",
                        senderName = "Aarav_IGL",
                        senderRole = "SQUAD LEAD",
                        message = "Need 1 Assaulter for tonight's 9 PM Erangel Night Clash scrim. Who's ready?",
                        isFromCurrentUser = false,
                        sentAt = now - 300_000L
                    ),
                    com.example.data.local.ChatMessageEntity(
                        roomId = "scrims",
                        senderId = "rohan_clutch_uid",
                        senderName = "RohanClutch",
                        senderRole = "SNIPER",
                        message = "Room ID opens 15 minutes before start. Check Scrims tab to lock your slot!",
                        isFromCurrentUser = false,
                        sentAt = now - 180_000L
                    ),
                    com.example.data.local.ChatMessageEntity(
                        roomId = "bgmi",
                        senderId = "harshgamer113_official",
                        senderName = "Harshgamer113",
                        senderRole = "CREATOR",
                        message = "New 335% Red Dot Gyro spray code is live under Home -> Loadouts!",
                        isFromCurrentUser = false,
                        sentAt = now - 120_000L
                    ),
                    com.example.data.local.ChatMessageEntity(
                        roomId = "freefire",
                        senderId = "vikram_ff_uid",
                        senderName = "Vikram_FF",
                        senderRole = "RUSHER",
                        message = "Anyone running Clash Squad custom tonight? Using the Desert Eagle preset.",
                        isFromCurrentUser = false,
                        sentAt = now - 90_000L
                    )
                )
            )
        }
    }

    suspend fun toggleFavorite(loadout: LoadoutEntity) {
        dao.updateLoadout(loadout.copy(isFavorite = !loadout.isFavorite))
    }

    suspend fun addLoadout(
        title: String,
        game: String,
        code: String,
        cameraSens: Int,
        adsSens: Int,
        gyroSens: Int,
        clawStyle: String,
        notes: String
    ) {
        dao.insertLoadout(
            LoadoutEntity(
                title = title.trim(),
                game = game.trim(),
                code = code.trim(),
                cameraSens = cameraSens,
                adsSens = adsSens,
                gyroSens = gyroSens,
                clawStyle = clawStyle.trim(),
                notes = notes.trim(),
                isFavorite = false,
                isDefault = false
            )
        )
    }

    suspend fun deleteLoadout(id: Long) = dao.deleteLoadoutById(id)

    suspend fun registerForScrim(
        scrim: ScrimEntity,
        ign: String,
        squadName: String,
        role: String
    ) {
        val nextFilled = if (scrim.isRegistered) scrim.filledSlots else (scrim.filledSlots + 1).coerceAtMost(scrim.maxSlots)
        dao.updateScrim(
            scrim.copy(
                isRegistered = true,
                filledSlots = nextFilled,
                registeredIgn = ign.trim(),
                registeredSquad = squadName.trim(),
                registeredRole = role.trim()
            )
        )
    }

    suspend fun leaveScrim(scrim: ScrimEntity) {
        if (!scrim.isRegistered) return
        val nextFilled = (scrim.filledSlots - 1).coerceAtLeast(0)
        dao.updateScrim(
            scrim.copy(
                isRegistered = false,
                filledSlots = nextFilled,
                registeredIgn = "",
                registeredSquad = "",
                registeredRole = ""
            )
        )
    }

    suspend fun addMatchLog(
        game: String,
        mode: String,
        mapName: String,
        kills: Int,
        placement: Int,
        damage: Int,
        notes: String
    ) {
        dao.insertMatchLog(
            MatchLogEntity(
                game = game.trim(),
                mode = mode.trim(),
                mapName = mapName.trim(),
                kills = kills.coerceAtLeast(0),
                placement = placement.coerceAtLeast(1),
                damage = damage.coerceAtLeast(0),
                notes = notes.trim()
            )
        )
    }

    suspend fun deleteMatchLog(id: Long) = dao.deleteMatchLogById(id)

    suspend fun addSquadNote(title: String, category: String, content: String) {
        dao.insertSquadNote(
            SquadNoteEntity(
                title = title.trim(),
                category = category.trim(),
                content = content.trim(),
                isPinned = false
            )
        )
    }

    suspend fun togglePinNote(note: SquadNoteEntity) {
        dao.updateSquadNote(note.copy(isPinned = !note.isPinned))
    }

    suspend fun deleteSquadNote(id: Long) = dao.deleteSquadNoteById(id)

    suspend fun updateProfile(
        ownerEmail: String,
        currentUserEmail: String,
        username: String,
        fullName: String,
        gamerTag: String,
        mainGame: String,
        role: String,
        deviceName: String,
        targetKd: String,
        bio: String,
        avatarUri: String,
        coverUri: String
    ): Result<Unit> {
        val cleanOwner = ownerEmail.trim().lowercase()
        val cleanCaller = currentUserEmail.trim().lowercase()
        if (cleanOwner.isBlank() || cleanOwner != cleanCaller) {
            return Result.failure(SecurityException("Access denied: cannot modify another user's private profile."))
        }
        val cleanFull = fullName.trim().ifBlank { gamerTag.trim() }
        val cleanUser = username.trim().lowercase().replace(Regex("[^a-z0-9_]"), "")
            .ifBlank { cleanFull.lowercase().replace(Regex("[^a-z0-9_]"), "").ifBlank { "gamer113" } }
        val existing = dao.getPlayerProfileOnce(cleanOwner)
        dao.upsertPlayerProfile(
            PlayerProfileEntity(
                ownerEmail = cleanOwner,
                username = cleanUser,
                fullName = cleanFull,
                gamerTag = gamerTag.trim().ifBlank { cleanFull },
                mainGame = mainGame.trim().ifBlank { "BGMI" },
                role = role.trim().ifBlank { "Assaulter" },
                deviceName = deviceName.trim().ifBlank { "120FPS Pro OLED" },
                targetKd = targetKd.trim().ifBlank { "5.0 K/D" },
                bio = bio.trim().take(300),
                avatarUri = avatarUri.ifBlank { existing?.avatarUri.orEmpty() },
                coverUri = coverUri.ifBlank { existing?.coverUri.orEmpty() },
                updatedAt = System.currentTimeMillis()
            )
        )
        return Result.success(Unit)
    }

    suspend fun generateAiStudioIdeas(prompt: String, category: String): Result<String> {
        val result = aiRepository.generateStudioIdeas(prompt, category)
        result.onSuccess { generated ->
            dao.insertAiLog(
                AiPromptLogEntity(
                    prompt = prompt.trim(),
                    response = generated,
                    category = category
                )
            )
        }
        return result
    }

    suspend fun deleteAiLog(id: Long) = dao.deleteAiLogById(id)
}
