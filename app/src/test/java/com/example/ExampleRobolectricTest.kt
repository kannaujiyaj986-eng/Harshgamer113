package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.HarshgamerDatabase
import com.example.data.repository.AuthRepository
import com.example.data.repository.ChatRepository
import com.example.data.repository.HarshgamerRepository
import com.example.data.repository.LocalGamerUser
import com.example.data.repository.VideoRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var database: HarshgamerDatabase
    private lateinit var repository: HarshgamerRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var videoRepository: VideoRepository
    private lateinit var chatRepository: ChatRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, HarshgamerDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val dao = database.harshgamerDao()
        repository = HarshgamerRepository(dao)
        authRepository = AuthRepository(context, dao)
        videoRepository = VideoRepository(dao)
        chatRepository = ChatRepository(dao) {
            authRepository.currentUser.value ?: LocalGamerUser("Tester113", "tester@harshgamer.app", "tester113")
        }
        authRepository.logout()
    }

    @After
    fun tearDown() {
        authRepository.logout()
        database.close()
    }

    @Test
    fun appNameMatchesHarshgamer113() {
        val appName = context.getString(R.string.app_name)
        assertEquals("Harshgamer113", appName)
    }

    @Test
    fun repositorySeedsVideosChatsLoadoutsAndPlaceholderAi() = runTest {
        repository.ensureSeeded()
        assertTrue(repository.loadouts.first().isNotEmpty())
        assertTrue(videoRepository.getVideos().isNotEmpty())
        assertTrue(chatRepository.messages("global").first().isNotEmpty())

        chatRepository.send("Ready for Erangel scrim!", "scrims")
        assertTrue(chatRepository.messages("scrims").first().any { it.text == "Ready for Erangel scrim!" })

        val aiResult = repository.generateAiStudioIdeas("BGMI 4-finger clutch", "Video Titles")
        assertTrue(aiResult.isSuccess)
        assertTrue(aiResult.getOrNull().orEmpty().contains("Sample Placeholder Response"))
    }

    @Test
    fun chatRepositoryReportBlockUnblockAndAskQuestionWork() = runTest {
        repository.ensureSeeded()
        val globalMsg = chatRepository.messages("global").first().first { !it.isFromCurrentUser }

        chatRepository.reportMessage(globalMsg, "Spam or Ads")
        assertEquals(1, database.harshgamerDao().getReportCount())

        chatRepository.blockUser(globalMsg.senderId, globalMsg.senderName)
        assertTrue(chatRepository.blockedList().contains(globalMsg.senderId))

        chatRepository.unblockUser(globalMsg.senderId)
        assertFalse(chatRepository.blockedList().contains(globalMsg.senderId))

        chatRepository.askQuestion("How do I join Tonight's Night Clash?")
        val questions = chatRepository.myQuestions()
        assertEquals(1, questions.size)
        assertEquals("How do I join Tonight's Night Clash?", questions.first()["question"])
    }

    @Test
    fun videoRepositoryOwnerIsolationEditDeleteAndAdminCheckWork() = runTest {
        videoRepository.saveVideo(
            title = "120 FPS TDM Clutch",
            desc = "Handcam gameplay",
            category = "Montages",
            videoUrl = "https://res.cloudinary.com/demo/video/upload/sample.mp4",
            thumbUrl = "https://example.com/thumb.jpg",
            uploadedBy = "AlphaPlayer",
            ownerEmail = "alpha@harshgamer.app"
        )
        val saved = videoRepository.getVideos().first { it.title == "120 FPS TDM Clutch" }
        videoRepository.incrementViews(saved.id)
        val afterView = videoRepository.getVideos().first { it.id == saved.id }
        assertEquals(1L, afterView.views)

        assertTrue(videoRepository.isAdmin("squad113@harshgamer.app", false))
        assertFalse(videoRepository.isAdmin("player@example.com", false))

        // Cross-user edit/delete must be rejected with SecurityException
        val foreignEdit = videoRepository.updateVideoDetails(
            videoId = saved.id,
            currentUserEmail = "intruder@example.com",
            isAdmin = false,
            title = "Hacked Title",
            desc = "",
            category = "Montages",
            thumbUrl = ""
        )
        assertTrue(foreignEdit.isFailure)
        assertTrue(foreignEdit.exceptionOrNull() is SecurityException)

        val foreignDelete = videoRepository.deleteVideo(
            id = saved.id,
            currentUserEmail = "intruder@example.com",
            isAdmin = false
        )
        assertTrue(foreignDelete.isFailure)
        assertTrue(foreignDelete.exceptionOrNull() is SecurityException)

        // Owner edit and delete succeed
        val ownerEdit = videoRepository.updateVideoDetails(
            videoId = saved.id,
            currentUserEmail = "alpha@harshgamer.app",
            isAdmin = false,
            title = "120 FPS Erangel Clutch",
            desc = "Updated desc",
            category = "Montages",
            thumbUrl = "https://example.com/thumb2.jpg"
        )
        assertTrue(ownerEdit.isSuccess)
        assertEquals("120 FPS Erangel Clutch", videoRepository.getVideos().first { it.id == saved.id }.title)

        val ownerDelete = videoRepository.deleteVideo(
            id = saved.id,
            currentUserEmail = "alpha@harshgamer.app",
            isAdmin = false
        )
        assertTrue(ownerDelete.isSuccess)
        assertFalse(videoRepository.getVideos().any { it.id == saved.id })
    }

    @Test
    fun localAuthSignupLoginResetChangePasswordAndRememberMeWork() = runTest {
        authRepository.ensureSeedCreatorAccount()
        assertFalse(authRepository.isLoggedIn)

        val signupRes = authRepository.signup(
            displayName = "HarshFan",
            email = "fan@harshgamer.app",
            password = "secret123",
            username = "harsh_fan_113",
            rememberMe = false
        )
        assertTrue(signupRes.isSuccess)
        assertTrue(authRepository.isLoggedIn)
        assertFalse(authRepository.rememberLogin.value)

        // New instance should NOT auto-login when rememberMe was false
        val reloadedAuth = AuthRepository(context, database.harshgamerDao())
        assertFalse(reloadedAuth.isLoggedIn)

        authRepository.logout()
        assertFalse(authRepository.isLoggedIn)

        val resetRes = authRepository.resetPassword("fan@harshgamer.app", "newpass456")
        assertTrue(resetRes.isSuccess)

        val loginRes = authRepository.login("fan@harshgamer.app", "newpass456", rememberMe = true)
        assertTrue(loginRes.isSuccess)
        assertEquals("HarshFan", authRepository.currentUser.value?.displayName)
        assertEquals("harsh_fan_113", authRepository.currentUser.value?.username)

        val changeRes = authRepository.changePassword("newpass456", "finalpass789")
        assertTrue(changeRes.isSuccess)
        authRepository.logout()
        assertTrue(authRepository.login("fan@harshgamer.app", "finalpass789").isSuccess)
    }

    @Test
    fun profileUpdateEnforcesOwnerIsolation() = runTest {
        authRepository.signup("Bravo", "bravo@harshgamer.app", "pass1234", "bravo113")
        val denied = repository.updateProfile(
            ownerEmail = "bravo@harshgamer.app",
            currentUserEmail = "attacker@harshgamer.app",
            username = "hacked",
            fullName = "Hacked",
            gamerTag = "Hacked",
            mainGame = "BGMI",
            role = "Sniper",
            deviceName = "PC",
            targetKd = "9.9",
            bio = "Unauthorized"
            ,
            avatarUri = "",
            coverUri = ""
        )
        assertTrue(denied.isFailure)
        assertTrue(denied.exceptionOrNull() is SecurityException)

        val allowed = repository.updateProfile(
            ownerEmail = "bravo@harshgamer.app",
            currentUserEmail = "bravo@harshgamer.app",
            username = "bravo_pro",
            fullName = "Bravo Gaming",
            gamerTag = "HG_Bravo",
            mainGame = "BGMI",
            role = "IGL",
            deviceName = "120FPS Tablet",
            targetKd = "6.2 K/D",
            bio = "Squad 113 Sniper",
            avatarUri = "file:///avatars/bravo.jpg",
            coverUri = "file:///covers/bravo_bg.jpg"
        )
        assertTrue(allowed.isSuccess)
        val stored = repository.observePlayerProfile("bravo@harshgamer.app").first()
        assertEquals("Bravo Gaming", stored?.fullName)
        assertEquals("bravo_pro", stored?.username)
        assertEquals("file:///avatars/bravo.jpg", stored?.avatarUri)
        assertEquals("file:///covers/bravo_bg.jpg", stored?.coverUri)
    }
}
