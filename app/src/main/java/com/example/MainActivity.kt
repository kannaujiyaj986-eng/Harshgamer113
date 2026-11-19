package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.data.repository.Video
import com.example.ui.HarshgamerUiState
import com.example.ui.HarshgamerViewModel
import com.example.ui.components.CyberStatusBadge
import com.example.ui.navigation.ROUTE_FORGOT
import com.example.ui.navigation.ROUTE_LOGIN
import com.example.ui.navigation.ROUTE_MAIN
import com.example.ui.navigation.ROUTE_SIGNUP
import com.example.ui.screens.AiTab
import com.example.ui.screens.ArenaScreen
import com.example.ui.screens.ChatTab
import com.example.ui.screens.EditVideoDialog
import com.example.ui.screens.ForgotScreen
import com.example.ui.screens.HelpScreen
import com.example.ui.screens.LoadoutsScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MyChannelScreen
import com.example.ui.screens.ScrimsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SignupScreen
import com.example.ui.screens.SquadScreen
import com.example.ui.screens.VideoPlayerDialog
import com.example.ui.screens.VideosTab
import com.example.ui.theme.Harshgamer113Theme
import com.example.ui.theme.HgRed
import kotlinx.coroutines.launch

private data class MainNavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
)

private val MAIN_TABS = listOf(
    MainNavItem("Home", Icons.Filled.Home, Icons.Outlined.Home, "tab_home"),
    MainNavItem("Videos", Icons.Filled.PlayArrow, Icons.Outlined.PlayArrow, "tab_videos"),
    MainNavItem("Chats", Icons.Filled.Email, Icons.Outlined.Email, "tab_chats"),
    MainNavItem("AI Studio", Icons.Filled.Star, Icons.Outlined.Star, "tab_ai_studio"),
    MainNavItem("Profile", Icons.Filled.Person, Icons.Outlined.Person, "tab_profile")
)

private enum class ProfileSubRoute {
    NONE, SQUAD, CHANNEL, SETTINGS, HELP
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            Harshgamer113Theme(darkTheme = true) {
                AppNav()
            }
        }
    }
}

@Composable
fun AppNav(
    viewModel: HarshgamerViewModel = viewModel()
) {
    val repo = viewModel.authRepository
    val nav = rememberNavController()
    val start = remember { if (repo.isLoggedIn) ROUTE_MAIN else ROUTE_LOGIN }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavHost(navController = nav, startDestination = start) {
            composable(ROUTE_LOGIN) {
                LoginScreen(
                    repo = repo,
                    onSuccess = {
                        nav.navigate(ROUTE_MAIN) {
                            popUpTo(ROUTE_LOGIN) { inclusive = true }
                        }
                    },
                    onSignup = { nav.navigate(ROUTE_SIGNUP) },
                    onForgot = { nav.navigate(ROUTE_FORGOT) }
                )
            }
            composable(ROUTE_SIGNUP) {
                SignupScreen(
                    repo = repo,
                    onDone = {
                        nav.navigate(ROUTE_MAIN) {
                            popUpTo(ROUTE_LOGIN) { inclusive = true }
                        }
                    },
                    onBack = { nav.popBackStack() }
                )
            }
            composable(ROUTE_FORGOT) {
                ForgotScreen(
                    repo = repo,
                    onBack = { nav.popBackStack() }
                )
            }
            composable(ROUTE_MAIN) {
                MainScreen(
                    viewModel = viewModel,
                    onLogout = {
                        viewModel.logout()
                        nav.navigate(ROUTE_LOGIN) {
                            popUpTo(ROUTE_MAIN) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun MainScreen(
    viewModel: HarshgamerViewModel,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var tab by remember { mutableIntStateOf(0) }
    var openUploadFromShortcut by remember { mutableStateOf(false) }
    var profileSubRoute by remember { mutableStateOf(ProfileSubRoute.NONE) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val uidCopiedMsg = stringResource(R.string.uid_copied_msg)

    fun showFeedback(message: String) {
        coroutineScope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message)
        }
    }

    fun shareVideo(video: Video) {
        val shareText = "${video.title}\nBy ${video.uploadedBy} on Harshgamer113 Hub\n${video.videoUrl}"
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, video.title)
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(sendIntent, "Share Video"))
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets.statusBars,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (!useRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        windowInsets = WindowInsets.navigationBars,
                        modifier = Modifier.testTag("bottom_navigation_bar")
                    ) {
                        MAIN_TABS.forEachIndexed { index, item ->
                            val selected = tab == index
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    tab = index
                                    if (index == 4) profileSubRoute = ProfileSubRoute.NONE
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.label,
                                        style = MaterialTheme.typography.labelMedium,
                                        maxLines = 1
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag(item.tag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxHeight()
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .testTag("navigation_rail")
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        MAIN_TABS.forEachIndexed { index, item ->
                            val selected = tab == index
                            NavigationRailItem(
                                selected = selected,
                                onClick = {
                                    tab = index
                                    if (index == 4) profileSubRoute = ProfileSubRoute.NONE
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag("rail_${item.tag}")
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 840.dp)
                    ) {
                        when (tab) {
                            0 -> HomeTab(
                                uiState = uiState,
                                viewModel = viewModel,
                                onCopyText = { text, msg ->
                                    clipboardManager.setText(AnnotatedString(text))
                                    showFeedback(msg)
                                },
                                uidCopiedMsg = uidCopiedMsg,
                                onFeedback = ::showFeedback,
                                onNavigateToUpload = {
                                    openUploadFromShortcut = true
                                    tab = 1
                                },
                                onNavigateToChannel = {
                                    profileSubRoute = ProfileSubRoute.CHANNEL
                                    tab = 4
                                },
                                onNavigateToProfile = {
                                    profileSubRoute = ProfileSubRoute.SQUAD
                                    tab = 4
                                },
                                onNavigateToSettings = {
                                    profileSubRoute = ProfileSubRoute.SETTINGS
                                    tab = 4
                                }
                            )
                            1 -> VideosTab(
                                uiState = uiState,
                                onSelectCategory = viewModel::setVideoCategoryFilter,
                                onSearchQueryChange = viewModel::setVideoSearchQuery,
                                onToggleSave = { video ->
                                    viewModel.toggleVideoSaved(video)
                                    showFeedback(if (video.isSaved) "Removed from Saved" else "Video bookmarked")
                                },
                                onToggleAdminMode = {
                                    viewModel.toggleAdminMode()
                                    showFeedback(if (uiState.isAdmin) "Admin tools hidden" else "Creator Admin tools enabled")
                                },
                                onWatchVideo = { video ->
                                    viewModel.incrementVideoViews(video.id)
                                },
                                onShareVideo = ::shareVideo,
                                onEditVideo = { id, title, desc, cat, thumbUrl, onDone ->
                                    viewModel.editVideoDetails(id, title, desc, cat, thumbUrl) { res ->
                                        res.onSuccess { showFeedback("Video updated") }
                                            .onFailure { showFeedback(it.localizedMessage ?: "Edit failed") }
                                        onDone(res)
                                    }
                                },
                                onDeleteVideo = { id ->
                                    viewModel.deleteVideo(id) { res ->
                                        res.onSuccess { showFeedback("Video deleted") }
                                            .onFailure { showFeedback(it.localizedMessage ?: "Delete denied") }
                                    }
                                },
                                onUploadVideo = { title, desc, cat, vUri, tUri, vUrl, tUrl, cloud, preset, onDone ->
                                    viewModel.uploadAndSaveVideo(
                                        title = title,
                                        desc = desc,
                                        category = cat,
                                        videoUri = vUri,
                                        thumbUri = tUri,
                                        directVideoUrl = vUrl,
                                        directThumbUrl = tUrl,
                                        cloudName = cloud,
                                        uploadPreset = preset,
                                        onSuccess = {
                                            onDone()
                                            showFeedback("Video published to Harshgamer113")
                                        }
                                    )
                                },
                                onClearUploadError = viewModel::clearVideoUploadError,
                                openUploadInitially = openUploadFromShortcut,
                                onConsumeOpenUpload = { openUploadFromShortcut = false }
                            )
                            2 -> ChatTab(
                                uiState = uiState,
                                onSelectRoom = viewModel::setChatRoom,
                                onSendMessage = viewModel::sendChatMessage,
                                onReportMessage = { msg, reason ->
                                    viewModel.reportChatMessage(msg, reason)
                                    showFeedback("Report submitted for review")
                                },
                                onBlockUser = { uid, name ->
                                    viewModel.blockChatUser(uid, name)
                                    showFeedback("Blocked $name")
                                },
                                onUnblockUser = { uid ->
                                    viewModel.unblockChatUser(uid)
                                    showFeedback("User unblocked")
                                }
                            )
                            3 -> AiTab(
                                uiState = uiState,
                                onGenerate = viewModel::generateAiIdeas,
                                onCopyResponse = { response ->
                                    clipboardManager.setText(AnnotatedString(response))
                                    showFeedback("AI ideas copied to clipboard")
                                },
                                onDeleteLog = { id ->
                                    viewModel.deleteAiLog(id)
                                    showFeedback("Prompt log removed")
                                }
                            )
                            4 -> ProfileTab(
                                uiState = uiState,
                                viewModel = viewModel,
                                activeSubRoute = profileSubRoute,
                                onSubRouteChange = { profileSubRoute = it },
                                onOpenUploadVideo = {
                                    openUploadFromShortcut = true
                                    tab = 1
                                },
                                onShareVideo = ::shareVideo,
                                onFeedback = ::showFeedback,
                                onLogout = onLogout
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeTab(
    uiState: HarshgamerUiState,
    viewModel: HarshgamerViewModel,
    onCopyText: (String, String) -> Unit,
    uidCopiedMsg: String,
    onFeedback: (String) -> Unit,
    onNavigateToUpload: () -> Unit,
    onNavigateToChannel: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    var homeSection by remember { mutableIntStateOf(0) }
    if (homeSection != 0) {
        BackHandler { homeSection = 0 }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .testTag("home_section_switcher")
        ) {
            listOf("Arena", "Loadouts", "Scrims").forEachIndexed { idx, label ->
                SegmentedButton(
                    selected = homeSection == idx,
                    onClick = { homeSection = idx },
                    shape = SegmentedButtonDefaults.itemShape(index = idx, count = 3),
                    modifier = Modifier.testTag("home_subtab_${label.lowercase()}")
                ) {
                    Text(label, style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (homeSection) {
                0 -> ArenaScreen(
                    uiState = uiState,
                    onCopyUid = { uid -> onCopyText(uid, uidCopiedMsg) },
                    onCopyLoadout = { loadout ->
                        onCopyText(loadout.code, "Sensitivity code copied: ${loadout.code}")
                    },
                    onToggleFavorite = viewModel::toggleLoadoutFavorite,
                    onNavigateToLoadouts = { homeSection = 1 },
                    onNavigateToScrims = { homeSection = 2 },
                    onNavigateToVideosUpload = onNavigateToUpload,
                    onNavigateToChannel = onNavigateToChannel,
                    onNavigateToProfile = onNavigateToProfile,
                    onNavigateToSettings = onNavigateToSettings
                )
                1 -> LoadoutsScreen(
                    uiState = uiState,
                    onSelectGame = viewModel::setGameFilter,
                    onToggleFavoritesOnly = viewModel::toggleFavoritesOnly,
                    onCopyCode = { loadout ->
                        onCopyText(loadout.code, "Sensitivity code copied: ${loadout.code}")
                    },
                    onToggleFavorite = viewModel::toggleLoadoutFavorite,
                    onDeleteLoadout = { id ->
                        viewModel.deleteLoadout(id)
                        onFeedback("Loadout removed")
                    },
                    onCreateLoadout = { title, game, code, cam, ads, gyro, claw, notes ->
                        viewModel.addLoadout(title, game, code, cam, ads, gyro, claw, notes)
                        onFeedback("Saved preset \"$title\"")
                    }
                )
                2 -> ScrimsScreen(
                    uiState = uiState,
                    onRegisterScrim = { scrim, ign, squad, role ->
                        viewModel.registerForScrim(scrim, ign, squad, role)
                        onFeedback("Squad locked in for ${scrim.title}")
                    },
                    onLeaveScrim = { scrim ->
                        viewModel.leaveScrim(scrim)
                        onFeedback("Slot cancelled for ${scrim.title}")
                    },
                    onAddMatchLog = { game, mode, map, kills, placement, damage, notes ->
                        viewModel.addMatchLog(game, mode, map, kills, placement, damage, notes)
                        onFeedback("Match logged (+$kills kills)")
                    },
                    onDeleteMatchLog = { id ->
                        viewModel.deleteMatchLog(id)
                        onFeedback("Match deleted")
                    }
                )
            }
        }
    }
}

@Composable
private fun ProfileTab(
    uiState: HarshgamerUiState,
    viewModel: HarshgamerViewModel,
    activeSubRoute: ProfileSubRoute,
    onSubRouteChange: (ProfileSubRoute) -> Unit,
    onOpenUploadVideo: () -> Unit,
    onShareVideo: (Video) -> Unit,
    onFeedback: (String) -> Unit,
    onLogout: () -> Unit
) {
    var channelPlayingVideo by remember { mutableStateOf<Video?>(null) }
    var channelEditingVideo by remember { mutableStateOf<Video?>(null) }

    when (activeSubRoute) {
        ProfileSubRoute.HELP -> {
            HelpScreen(
                myQuestions = uiState.helpQuestions,
                onAskQuestion = { question ->
                    viewModel.askHelpQuestion(question)
                    onFeedback("Question submitted to Squad 113 Support")
                },
                onBack = { onSubRouteChange(ProfileSubRoute.NONE) }
            )
            return
        }
        ProfileSubRoute.CHANNEL -> {
            MyChannelScreen(
                uiState = uiState,
                onWatchVideo = { video ->
                    viewModel.incrementVideoViews(video.id)
                    channelPlayingVideo = video
                },
                onEditVideo = { video -> channelEditingVideo = video },
                onDeleteVideo = { video ->
                    viewModel.deleteVideo(video.id) { res ->
                        res.onSuccess { onFeedback("Video deleted") }
                            .onFailure { onFeedback(it.localizedMessage ?: "Delete denied") }
                    }
                },
                onShareVideo = onShareVideo,
                onOpenUpload = onOpenUploadVideo,
                onBack = { onSubRouteChange(ProfileSubRoute.NONE) }
            )
            channelPlayingVideo?.let { active ->
                VideoPlayerDialog(
                    video = active,
                    onDismiss = { channelPlayingVideo = null },
                    onShare = onShareVideo
                )
            }
            channelEditingVideo?.let { target ->
                EditVideoDialog(
                    video = target,
                    onDismiss = { channelEditingVideo = null },
                    onSave = { title, desc, cat, thumbUrl ->
                        viewModel.editVideoDetails(target.id, title, desc, cat, thumbUrl) { res ->
                            res.onSuccess {
                                channelEditingVideo = null
                                onFeedback("Video details updated")
                            }.onFailure {
                                onFeedback(it.localizedMessage ?: "Failed to edit video")
                            }
                        }
                    }
                )
            }
            return
        }
        ProfileSubRoute.SETTINGS -> {
            SettingsScreen(
                uiState = uiState,
                onToggleRememberLogin = { enabled ->
                    viewModel.setRememberLogin(enabled)
                    onFeedback(if (enabled) "Remember Login enabled" else "Session will not persist on restart")
                },
                onChangePassword = viewModel::changePassword,
                onCloudSaved = onFeedback,
                onLogout = onLogout,
                onBack = { onSubRouteChange(ProfileSubRoute.NONE) }
            )
            return
        }
        ProfileSubRoute.SQUAD -> {
            BackHandler { onSubRouteChange(ProfileSubRoute.NONE) }
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onSubRouteChange(ProfileSubRoute.NONE) },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("squad_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to profile"
                        )
                    }
                    Text(
                        text = "Back to Profile",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                SquadScreen(
                    uiState = uiState,
                    onSelectNoteCategory = viewModel::setNoteCategoryFilter,
                    onAddNote = { title, category, content ->
                        viewModel.addSquadNote(title, category, content)
                        onFeedback("Strategy note pinned to board")
                    },
                    onTogglePinNote = viewModel::togglePinNote,
                    onDeleteNote = { id ->
                        viewModel.deleteSquadNote(id)
                        onFeedback("Note deleted")
                    },
                    onUpdatePhoto = { uri, isCover ->
                        viewModel.updateProfilePhoto(uri, isCover) { res ->
                            res.onSuccess {
                                onFeedback(if (isCover) "Cover photo updated" else "Profile picture updated")
                            }.onFailure {
                                onFeedback(it.localizedMessage ?: "Failed to update photo")
                            }
                        }
                    },
                    onUpdateProfile = { username, fullName, tag, game, role, device, targetKd, bio ->
                        viewModel.updateProfile(username, fullName, tag, game, role, device, targetKd, bio) { res ->
                            res.onSuccess { onFeedback("Player profile updated") }
                                .onFailure { onFeedback(it.localizedMessage ?: "Profile update failed") }
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
            return
        }
        ProfileSubRoute.NONE -> Unit
    }

    val user = uiState.currentUser
    val profile = uiState.playerProfile
    val displayFullName = profile?.fullName?.ifBlank { user?.displayName ?: "Gamer" } ?: user?.displayName ?: "Gamer"
    val handle = profile?.username?.ifBlank { user?.username ?: "player113" } ?: user?.username ?: "player113"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
            .testTag("profile_tab"),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(6.dp))
        CyberStatusBadge(
            label = "SQUAD 113 ACCOUNT",
            accentColor = MaterialTheme.colorScheme.primary
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!profile?.avatarUri.isNullOrBlank()) {
                    AsyncImage(
                        model = profile?.avatarUri,
                        contentDescription = stringResource(R.string.cd_creator_avatar),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .border(2.dp, HgRed, CircleShape)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.img_harsh_avatar),
                        contentDescription = stringResource(R.string.cd_creator_avatar),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .border(2.dp, HgRed, CircleShape)
                    )
                }
                Text(
                    text = displayFullName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "@$handle • ${user?.email ?: "squad113@harshgamer.app"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                CyberStatusBadge(
                    label = "${uiState.myUploadedVideos.size} VIDEOS • ${uiState.myTotalViews} VIEWS",
                    accentColor = MaterialTheme.colorScheme.tertiary
                )
            }
        }

        Button(
            onClick = { onSubRouteChange(ProfileSubRoute.SQUAD) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("open_squad_hq_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(Icons.Filled.Groups, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("MY PROFILE & SQUAD HQ", style = MaterialTheme.typography.labelLarge)
        }

        OutlinedButton(
            onClick = { onSubRouteChange(ProfileSubRoute.CHANNEL) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("open_my_channel_button"),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Filled.VideoLibrary, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("MY CHANNEL (VIDEO PROFILE)", style = MaterialTheme.typography.labelLarge)
        }

        OutlinedButton(
            onClick = { onSubRouteChange(ProfileSubRoute.SETTINGS) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("open_settings_button"),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Icon(Icons.Filled.Settings, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("SETTINGS & CLOUD STORAGE", style = MaterialTheme.typography.labelLarge)
        }

        OutlinedButton(
            onClick = { onSubRouteChange(ProfileSubRoute.HELP) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("open_help_center_button"),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Icon(Icons.Filled.HelpOutline, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("HELP CENTER", style = MaterialTheme.typography.labelLarge)
        }

        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("logout_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = HgRed,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("LOGOUT", style = MaterialTheme.typography.labelLarge)
        }
    }
}
