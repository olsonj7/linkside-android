package com.linkside.app.ui.navigation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.linkside.app.data.contacts.ContactsHelper
import com.linkside.app.data.location.LocationHelper
import com.linkside.app.data.model.ManualInvite
import com.linkside.app.data.model.User
import com.linkside.app.data.prefs.ProfilePreferences
import com.linkside.app.auth.GoogleSignInHelper
import android.os.Build
import androidx.compose.runtime.DisposableEffect
import com.linkside.app.LinksideApplication
import com.linkside.app.push.PushNotificationHelper
import com.linkside.app.push.PushRoute
import com.linkside.app.push.PushRouter
import com.linkside.app.push.PushTokenManager
import com.linkside.app.ui.auth.LinkEmailScreen
import com.linkside.app.ui.golfers.ContactPickerSheet
import com.linkside.app.ui.golfers.EditGroupScreen
import com.linkside.app.ui.golfers.FriendGroupsScreen
import com.linkside.app.ui.golfers.GolfersScreen
import com.linkside.app.ui.golfers.ManualGolferDialog
import com.linkside.app.ui.ideas.CreateIdeaThreadScreen
import com.linkside.app.ui.ideas.IdeaThreadDetailScreen
import com.linkside.app.ui.ideas.IdeaThreadsScreen
import com.linkside.app.ui.notifications.NotificationsScreen
import com.linkside.app.ui.components.LinksideTopAppBar
import com.linkside.app.ui.components.MentionCandidate
import com.linkside.app.ui.components.ProfileAvatarView
import com.linkside.app.ui.home.HomeScreen
import com.linkside.app.ui.profile.EditProfileScreen
import com.linkside.app.ui.profile.ProfileScreen
import com.linkside.app.ui.teetimes.CreateTeeTimeScreen
import com.linkside.app.ui.teetimes.EditTeeTimeScreen
import com.linkside.app.ui.teetimes.ManageInviteesScreen
import com.linkside.app.ui.teetimes.PlayerOfTheDayScreen
import com.linkside.app.ui.teetimes.RoundSummaryScreen
import com.linkside.app.ui.teetimes.ScorecardViewerScreen
import com.linkside.app.ui.teetimes.TeeTimeChatScreen
import com.linkside.app.ui.teetimes.TeeTimeDetailScreen
import com.linkside.app.ui.tournaments.TournamentDetailScreen
import com.linkside.app.ui.tournaments.TournamentsScreen
import com.linkside.app.ui.trips.TripChatScreen
import com.linkside.app.ui.trips.TripDetailScreen
import com.linkside.app.ui.theme.LinksideColors
import com.linkside.app.viewmodel.AuthViewModel
import java.time.Instant
import com.linkside.app.viewmodel.GolfersViewModel
import com.linkside.app.viewmodel.IdeaThreadViewModel
import com.linkside.app.viewmodel.NotificationsViewModel
import com.linkside.app.viewmodel.TeeTimeViewModel
import com.linkside.app.viewmodel.TournamentViewModel
import com.linkside.app.viewmodel.TripViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTabShell(
    authViewModel: AuthViewModel,
    profilePreferences: ProfilePreferences,
    golfersViewModel: GolfersViewModel,
    teeTimeViewModel: TeeTimeViewModel,
    tripViewModel: TripViewModel,
    ideaThreadViewModel: IdeaThreadViewModel,
    notificationsViewModel: NotificationsViewModel,
    tournamentViewModel: TournamentViewModel,
    onDarkModeChange: (Boolean) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val app = context.applicationContext as LinksideApplication
    val scope = rememberCoroutineScope()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val user = authState.user ?: return
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val homeNav = rememberNavController()
    val golfersNav = rememberNavController()
    val profileNav = rememberNavController()

    val golfersState by golfersViewModel.uiState.collectAsStateWithLifecycle()
    val teeTimeState by teeTimeViewModel.uiState.collectAsStateWithLifecycle()
    val tripState by tripViewModel.uiState.collectAsStateWithLifecycle()
    val ideaState by ideaThreadViewModel.uiState.collectAsStateWithLifecycle()
    val notificationsState by notificationsViewModel.uiState.collectAsStateWithLifecycle()
    val tournamentState by tournamentViewModel.uiState.collectAsStateWithLifecycle()

    var showManualGolfer by remember { mutableStateOf(false) }
    var showContactPicker by remember { mutableStateOf(false) }
    var pickerSelection by remember { mutableStateOf(setOf<String>()) }
    var courseQuery by remember { mutableStateOf("") }
    var profileCourseQuery by remember { mutableStateOf("") }
    // Cached one-shot device coordinate used to bias course search toward nearby
    // courses (mirrors iOS). Null when unavailable or permission was declined.
    var searchLatLng by remember { mutableStateOf<Pair<Double, Double>?>(null) }

    fun refreshSearchLocation() {
        if (!LocationHelper.hasPermission(context)) return
        scope.launch { searchLatLng = LocationHelper.currentLatLng(context) }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) refreshSearchLocation()
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            scope.launch { PushTokenManager.syncWithServer(context, app.linksideRepository) }
        }
    }

    fun navigateFromPush(route: PushRoute) {
        when (route) {
            is PushRoute.RoundSummary -> {
                selectedTab = 0
                homeNav.navigate(Routes.roundSummary(route.teeTimeId)) { launchSingleTop = true }
            }
            is PushRoute.PlayerOfTheDay -> {
                selectedTab = 0
                // Match iOS: potd taps land on round recap share flow.
                homeNav.navigate(Routes.roundSummary(route.teeTimeId)) { launchSingleTop = true }
            }
            is PushRoute.TeeTime -> {
                selectedTab = 0
                homeNav.navigate(Routes.teeTimeDetail(route.id)) { launchSingleTop = true }
            }
            is PushRoute.Trip -> {
                selectedTab = 0
                homeNav.navigate(Routes.tripDetail(route.id)) { launchSingleTop = true }
            }
            is PushRoute.Tournament -> {
                selectedTab = 0
                homeNav.navigate(Routes.tournamentDetail(route.id)) { launchSingleTop = true }
            }
            is PushRoute.IdeaThread -> {
                selectedTab = 0
                homeNav.navigate(Routes.ideaThreadDetail(route.id)) { launchSingleTop = true }
            }
        }
        notificationsViewModel.loadNotifications()
    }

    DisposableEffect(user.id) {
        val listener: (PushRoute) -> Unit = { navigateFromPush(it) }
        PushRouter.addListener(listener)
        PushRouter.consumePending()?.let { route -> navigateFromPush(route) }
        onDispose { PushRouter.removeListener(listener) }
    }

    fun ensureSearchLocation() {
        if (LocationHelper.hasPermission(context)) {
            refreshSearchLocation()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    val now = Instant.now()
    // Only show upcoming declined events in the profile; past ones drop off.
    val declinedTeeTimes = teeTimeState.teeTimes
        .filter { it.isActiveDeclined(user) && it.parsedInstant()?.isAfter(now) == true }
        .sortedBy { it.parsedInstant() ?: Instant.MAX }
    val declinedTrips = tripViewModel.declinedTrips(user)
        .filter { it.parsedEnd()?.isAfter(now) == true }
    val withdrawnTournaments = tournamentViewModel.withdrawnTournaments()
    val previousTeeTimes = teeTimeViewModel.previousTeeTimes(user)

    LaunchedEffect(selectedTab) {
        if (selectedTab == 0) {
            authViewModel.refreshUser()
        }
        if (selectedTab == 1) {
            golfersViewModel.syncFromServer()
        }
        if (selectedTab == 2) {
            // Keep Profile declined lists in sync after RSVPs made on Home.
            teeTimeViewModel.loadTeeTimes()
            tripViewModel.loadTrips()
            tournamentViewModel.loadTournaments()
        }
    }

    // If a nested NavHost is popped past its start destination, content goes blank
    // (scaffold green shows through). Restore the tab root.
    val homeEntry by homeNav.currentBackStackEntryAsState()
    val golfersEntry by golfersNav.currentBackStackEntryAsState()
    val profileEntry by profileNav.currentBackStackEntryAsState()
    LaunchedEffect(selectedTab, homeEntry, golfersEntry, profileEntry) {
        when (selectedTab) {
            0 -> if (homeEntry == null) {
                homeNav.navigate(Routes.HomeMain) { launchSingleTop = true }
            }
            1 -> if (golfersEntry == null) {
                golfersNav.navigate("golfers_main") { launchSingleTop = true }
            }
            2 -> if (profileEntry == null) {
                profileNav.navigate("profile_main") { launchSingleTop = true }
            }
        }
    }

    LaunchedEffect(user.id) {
        teeTimeViewModel.currentUser = user
        tripViewModel.currentUser = user
        authViewModel.refreshUser()
        golfersViewModel.syncFromServer()
        teeTimeViewModel.loadTeeTimes()
        tripViewModel.loadTrips()
        ideaThreadViewModel.loadThreads()
        notificationsViewModel.loadNotifications()
        tournamentViewModel.loadTournaments()
        if (profilePreferences.pushNotificationsEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !PushNotificationHelper.canPostNotifications(context)
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                PushTokenManager.syncWithServer(context, app.linksideRepository)
            }
        }
    }

    LaunchedEffect(teeTimeState.teeTimes, user.id) {
        teeTimeViewModel.loadScorecardsForCurrentRounds(user)
    }

    LaunchedEffect(ideaState.errorMessage, notificationsState.errorMessage, tournamentState.errorMessage) {
        ideaState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            ideaThreadViewModel.clearError()
        }
        notificationsState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            notificationsViewModel.clearError()
        }
        tournamentState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            tournamentViewModel.clearError()
        }
    }

    LaunchedEffect(golfersState.errorMessage, teeTimeState.errorMessage, tripState.errorMessage) {
        golfersState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            golfersViewModel.clearError()
        }
        teeTimeState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            teeTimeViewModel.clearError()
        }
        tripState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            tripViewModel.clearError()
        }
    }

    LaunchedEffect(courseQuery) {
        if (courseQuery.length < 2) {
            teeTimeViewModel.clearCourseSearch()
            return@LaunchedEffect
        }
        delay(350)
        teeTimeViewModel.searchCourses(courseQuery, searchLatLng?.first, searchLatLng?.second)
    }

    LaunchedEffect(profileCourseQuery) {
        if (profileCourseQuery.length < 2) {
            return@LaunchedEffect
        }
        delay(350)
        teeTimeViewModel.searchCourses(profileCourseQuery)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            val contacts = ContactsHelper.loadContacts(context)
            golfersViewModel.loadDeviceContacts(contacts)
            pickerSelection = golfersState.golfers.map { it.phone }.toSet()
            showContactPicker = true
        }
    }

    fun openContactPicker() {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS,
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            val contacts = ContactsHelper.loadContacts(context)
            golfersViewModel.loadDeviceContacts(contacts)
            pickerSelection = golfersState.golfers.map { it.phone }.toSet()
            showContactPicker = true
        } else {
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    if (showManualGolfer) {
        ManualGolferDialog(
            onDismiss = { showManualGolfer = false },
            onAdd = { golfersViewModel.addManualGolfer(it) },
        )
    }

    if (showContactPicker) {
        ContactPickerSheet(
            contacts = golfersState.deviceContacts.ifEmpty { golfersState.golfers },
            selectedPhones = pickerSelection,
            contactStatuses = golfersState.contactStatuses,
            onToggle = { phone ->
                pickerSelection = if (pickerSelection.contains(phone)) pickerSelection - phone else pickerSelection + phone
            },
            onDone = {
                val selected = golfersState.deviceContacts.filter { pickerSelection.contains(it.phone) }
                if (selected.isNotEmpty()) golfersViewModel.saveGolfers(selected)
                showContactPicker = false
            },
            onDismiss = { showContactPicker = false },
        )
    }

    Scaffold(
        modifier = modifier,
        containerColor = LinksideColors.Primary,
        bottomBar = {
            NavigationBar(
                containerColor = LinksideColors.Primary,
                contentColor = LinksideColors.TextSecondary,
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Home") },
                    colors = navItemColors(),
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.People, contentDescription = null) },
                    label = { Text("Golfers") },
                    colors = navItemColors(),
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        ProfileAvatarView(
                            name = user.displayName,
                            remoteUrl = user.avatarUrl,
                            size = 24.dp,
                        )
                    },
                    label = { Text("Profile") },
                    colors = navItemColors(),
                )
            }
        },
    ) { padding ->
        when (selectedTab) {
            0 -> NavHost(
                navController = homeNav,
                startDestination = Routes.HomeMain,
                modifier = Modifier.padding(padding),
            ) {
                composable(Routes.HomeMain) {
                    // Consume system/gesture back at the tab root so Home can't be popped
                    // out of the NavHost (blank green content area).
                    BackHandler(enabled = selectedTab == 0) { }
                    HomeScreen(
                        user = user,
                        nextUpTeeTime = teeTimeViewModel.nextUpTeeTime(user),
                        currentRounds = teeTimeViewModel.currentRoundTeeTimes(user),
                        scorecardsByTeeTime = teeTimeState.teeTimeScorecards,
                        teeTimes = teeTimeState.teeTimes,
                        trips = tripViewModel.upcomingTrips(user),
                        groups = golfersState.groups,
                        ideaThreads = ideaState.threads.take(3),
                        openTournaments = tournamentState.tournaments.filter { it.isOpen && !it.isWithdrawn },
                        unreadNotifications = notificationsViewModel.unreadCount,
                        isLoading = teeTimeState.isLoading,
                        isTripsLoading = tripState.isLoading,
                        onRefresh = {
                            authViewModel.refreshUser()
                            teeTimeViewModel.loadTeeTimes()
                            tripViewModel.loadTrips()
                            golfersViewModel.syncFromServer()
                            ideaThreadViewModel.loadThreads()
                            notificationsViewModel.loadNotifications()
                            tournamentViewModel.loadTournaments()
                        },
                        onCreateTeeTime = { homeNav.navigate(Routes.CreateTeeTime) },
                        onTeeTimeClick = { id -> homeNav.navigate(Routes.teeTimeDetail(id)) },
                        onTripClick = { id -> homeNav.navigate(Routes.tripDetail(id)) },
                        onFriendGroups = { homeNav.navigate(Routes.FriendGroups) },
                        onEditGroup = { group -> homeNav.navigate(Routes.editGroup(group.id)) },
                        onIdeaThreads = { homeNav.navigate(Routes.IdeaThreads) },
                        onIdeaThreadClick = { id -> homeNav.navigate(Routes.ideaThreadDetail(id)) },
                        onTournaments = { homeNav.navigate(Routes.Tournaments) },
                        onTournamentClick = { id -> homeNav.navigate(Routes.tournamentDetail(id)) },
                        onNotifications = { homeNav.navigate(Routes.Notifications) },
                        golferCount = golfersState.golfers.size,
                        onAddGolfers = { selectedTab = 1 },
                    )
                }
                composable(Routes.CreateTeeTime) {
                    val latestAuth by authViewModel.uiState.collectAsStateWithLifecycle()
                    LaunchedEffect(Unit) {
                        courseQuery = ""
                        teeTimeViewModel.clearCourseSearch()
                        golfersViewModel.syncFromServer()
                        // Ask for (or refresh) location so course search prefers nearby courses.
                        ensureSearchLocation()
                    }
                    CreateTeeTimeScreen(
                        savedGolfers = golfersState.golfers,
                        friendGroups = golfersState.groups,
                        contactStatuses = golfersState.contactStatuses,
                        favoriteCourses = latestAuth.user?.favoriteCourses.orEmpty(),
                        defaultGroupSize = profilePreferences.defaultGroupSize.coerceIn(2, 4),
                        courseResults = teeTimeState.courseSearchResults,
                        isSearching = teeTimeState.isSearchingCourses,
                        isLoading = teeTimeState.isLoading,
                        onBack = { homeNav.safePopBack(Routes.HomeMain) },
                        onSearchCourses = { query -> courseQuery = query },
                        onClearCourseSearch = { teeTimeViewModel.clearCourseSearch() },
                        onToggleFavoriteCourse = authViewModel::toggleFavoriteCourse,
                        onCreate = { courseName, courseId, date, golfersNeeded, invites, timeMode, timeWindows, playFormat, greenFee, holesCount, roundName, sendInvites ->
                            teeTimeViewModel.createTeeTime(
                                courseName,
                                courseId,
                                date,
                                golfersNeeded,
                                invites,
                                timeMode,
                                timeWindows,
                                playFormat,
                                greenFee,
                                holesCount,
                                roundName,
                                sendInvites,
                            ) { _, manuals ->
                                if (manuals.isNotEmpty()) {
                                    openManualInviteSms(context, manuals)
                                }
                                homeNav.safePopBack(Routes.HomeMain)
                            }
                        },
                    )
                }
                composable(
                    route = Routes.TeeTimeDetail,
                    arguments = listOf(navArgument("id") { type = NavType.StringType }),
                ) { entry ->
                    val id = entry.arguments?.getString("id").orEmpty().decodeRoute()
                    val teeTime = teeTimeState.teeTimes.firstOrNull { it.id == id }
                    LaunchedEffect(id) {
                        if (teeTime == null) teeTimeViewModel.refreshTeeTime(id)
                        teeTimeViewModel.loadTeeTimeDetail(id)
                    }
                    if (teeTime != null) {
                        TeeTimeDetailScreen(
                            teeTime = teeTime,
                            user = user,
                            photos = teeTimeState.teeTimePhotos[id].orEmpty(),
                            myScore = teeTimeState.roundScores[id],
                            scorecards = teeTimeState.teeTimeScorecards[id].orEmpty(),
                            courseWebsiteUrl = teeTime.courseId?.let { teeTimeState.courseWebsites[it] },
                            isLoading = teeTimeState.isLoading,
                            isUploadingPhoto = teeTimeState.isUploadingPhoto,
                            isSavingScore = teeTimeState.isSavingScore,
                            onBack = { homeNav.safePopBack(Routes.HomeMain) },
                            onRsvp = { status ->
                                teeTimeViewModel.updateRsvp(
                                    teeTimeId = id,
                                    phone = user.phone,
                                    status = status,
                                    userId = user.id,
                                )
                            },
                            onOpenChat = { homeNav.navigate(Routes.teeTimeChat(id)) },
                            onUploadPhoto = { bytes, mime -> teeTimeViewModel.uploadPhoto(id, bytes, mime) },
                            onSaveScore = { score -> teeTimeViewModel.saveRoundScore(id, score) },
                            onEdit = { homeNav.navigate(Routes.editTeeTime(id)) },
                            onManageInvitees = { homeNav.navigate(Routes.manageInvitees(id)) },
                            onSendPendingInvites = {
                                teeTimeViewModel.sendPendingInvites(id) { manuals ->
                                    if (manuals.isNotEmpty()) {
                                        openManualInviteSms(context, manuals)
                                    }
                                }
                            },
                            onViewScorecards = { homeNav.navigate(Routes.scorecards(id)) },
                            onShareRound = { homeNav.navigate(Routes.roundSummary(id)) },
                            onCancelTeeTime = {
                                teeTimeViewModel.deleteTeeTime(id) {
                                    homeNav.safePopBack(Routes.HomeMain)
                                }
                            },
                            onBumpInvite = { invite ->
                                bumpInvitee(context, teeTimeViewModel, teeTime, invite, user.displayName)
                            },
                            onToggleInviteAccess = { invite ->
                                invite.userId?.let { teeTimeViewModel.toggleInviteAccess(id, it) }
                            },
                            onSendLinksideInvite = { invite ->
                                invite.phone?.takeIf { it.isNotBlank() }?.let { phone ->
                                    teeTimeViewModel.prepareLinksideInvite(phone, invite.name, user.displayName) { manual ->
                                        openManualInviteSms(context, listOf(manual))
                                    }
                                }
                            },
                            onRemoveInvite = { invite ->
                                teeTimeViewModel.removeInvite(id, invite.userId, invite.phone)
                            },
                        )
                    } else {
                        LoadingWithBack(onBack = { homeNav.safePopBack(Routes.HomeMain) })
                    }
                }
                composable(
                    route = Routes.EditTeeTime,
                    arguments = listOf(navArgument("id") { type = NavType.StringType }),
                ) { entry ->
                    val id = entry.arguments?.getString("id").orEmpty().decodeRoute()
                    val teeTime = teeTimeState.teeTimes.firstOrNull { it.id == id }
                    LaunchedEffect(id) {
                        teeTimeViewModel.clearError()
                        if (teeTime == null) teeTimeViewModel.refreshTeeTime(id)
                    }
                    if (teeTime != null) {
                        EditTeeTimeScreen(
                            teeTime = teeTime,
                            isLoading = teeTimeState.isLoading,
                            errorMessage = teeTimeState.errorMessage,
                            onBack = { homeNav.safePopBack(Routes.HomeMain) },
                            onSave = { date, golfersNeeded, timeMode, timeWindows, playFormat, greenFee, holesCount, roundName ->
                                teeTimeViewModel.updateTeeTime(
                                    id = id,
                                    date = date,
                                    golfersNeeded = golfersNeeded,
                                    timeMode = timeMode,
                                    timeWindows = timeWindows,
                                    playFormat = playFormat,
                                    greenFee = greenFee,
                                    holesCount = holesCount,
                                    roundName = roundName,
                                ) {
                                    homeNav.safePopBack(Routes.HomeMain)
                                }
                            },
                        )
                    } else {
                        LoadingWithBack(onBack = { homeNav.safePopBack(Routes.HomeMain) })
                    }
                }
                composable(
                    route = Routes.TeeTimeChat,
                    arguments = listOf(navArgument("id") { type = NavType.StringType }),
                ) { entry ->
                    val id = entry.arguments?.getString("id").orEmpty()
                    val teeTime = teeTimeState.teeTimes.firstOrNull { it.id == id }
                    LaunchedEffect(id) {
                        if (teeTime == null) teeTimeViewModel.refreshTeeTime(id)
                    }
                    val current = teeTimeState.teeTimes.firstOrNull { it.id == id }
                    if (current != null) {
                        TeeTimeChatScreen(
                            courseName = current.courseName,
                            teeTimeId = id,
                            user = user,
                            messages = teeTimeState.teeTimeMessages[id].orEmpty(),
                            isSending = teeTimeState.isSendingMessage,
                            mentionCandidates = mentionCandidatesFrom(current.invites, user.id),
                            onBack = { homeNav.safePopBack(Routes.HomeMain) },
                            onLoad = { teeTimeViewModel.loadMessages(id) },
                            onStartPolling = { teeTimeViewModel.startChatPolling(id) },
                            onStopPolling = { teeTimeViewModel.stopChatPolling() },
                            onSend = { text, mentions -> teeTimeViewModel.sendMessage(id, text, mentions) },
                            onToggleReaction = { messageId, emoji ->
                                teeTimeViewModel.toggleReaction(id, messageId, emoji)
                            },
                        )
                    } else {
                        LoadingWithBack(onBack = { homeNav.safePopBack(Routes.HomeMain) })
                    }
                }
                composable(
                    route = Routes.TripDetail,
                    arguments = listOf(navArgument("id") { type = NavType.StringType }),
                ) { entry ->
                    val id = entry.arguments?.getString("id").orEmpty()
                    val trip = tripViewModel.trip(id)
                    LaunchedEffect(id) {
                        if (trip == null) tripViewModel.refreshTrip(id)
                        tripViewModel.loadTripDetail(id)
                    }
                    val currentTrip = tripViewModel.trip(id)
                    if (currentTrip != null) {
                        TripDetailScreen(
                            trip = currentTrip,
                            user = user,
                            teeTimes = tripState.tripTeeTimes[id].orEmpty(),
                            photos = tripState.tripPhotos[id].orEmpty(),
                            announcements = tripState.tripAnnouncements[id].orEmpty(),
                            isLoading = tripState.isLoading,
                            isUploadingPhoto = tripState.isUploadingPhoto,
                            isPostingAnnouncement = tripState.isPostingAnnouncement,
                            onBack = { homeNav.safePopBack(Routes.HomeMain) },
                            onRsvp = { status -> tripViewModel.rsvpTrip(id, status) },
                            onToggleDeposit = { invite, paid ->
                                tripViewModel.setDepositPaid(id, invite.phone, invite.userId, paid)
                            },
                            onOpenChat = { homeNav.navigate(Routes.tripChat(id)) },
                            onTeeTimeClick = { teeTimeId -> homeNav.navigate(Routes.teeTimeDetail(teeTimeId)) },
                            onUploadPhoto = { bytes, mime -> tripViewModel.uploadPhoto(id, bytes, mime) },
                            onPostAnnouncement = { message -> tripViewModel.postAnnouncement(id, message) },
                        )
                    } else {
                        LoadingWithBack(onBack = { homeNav.safePopBack(Routes.HomeMain) })
                    }
                }
                composable(
                    route = Routes.TripChat,
                    arguments = listOf(navArgument("id") { type = NavType.StringType }),
                ) { entry ->
                    val id = entry.arguments?.getString("id").orEmpty()
                    val trip = tripViewModel.trip(id)
                    LaunchedEffect(id) {
                        if (trip == null) tripViewModel.refreshTrip(id)
                    }
                    val currentTrip = tripViewModel.trip(id)
                    if (currentTrip != null) {
                        TripChatScreen(
                            tripName = currentTrip.name,
                            tripId = id,
                            user = user,
                            messages = tripState.tripMessages[id].orEmpty(),
                            isSending = tripState.isSendingMessage,
                            isCreatingPoll = tripState.isCreatingPoll,
                            tripCreatorId = currentTrip.creatorId,
                            mentionCandidates = mentionCandidatesFrom(currentTrip.invites, user.id),
                            onBack = { homeNav.safePopBack(Routes.HomeMain) },
                            onLoad = { tripViewModel.loadMessages(id) },
                            onStartPolling = { tripViewModel.startChatPolling(id) },
                            onStopPolling = { tripViewModel.stopChatPolling() },
                            onSend = { text, mentions -> tripViewModel.sendMessage(id, text, mentions) },
                            onToggleReaction = { messageId, emoji ->
                                tripViewModel.toggleReaction(id, messageId, emoji)
                            },
                            onCreatePoll = { q, opts, multi -> tripViewModel.createPoll(id, q, opts, multi) },
                            onVotePoll = { pollId, ids -> tripViewModel.votePoll(id, pollId, ids) },
                            onClosePoll = { pollId -> tripViewModel.closePoll(id, pollId) },
                            onDeletePoll = { pollId -> tripViewModel.deletePoll(id, pollId) },
                        )
                    } else {
                        LoadingWithBack(onBack = { homeNav.safePopBack(Routes.HomeMain) })
                    }
                }
                composable(Routes.Notifications) {
                    val notificationsUi by notificationsViewModel.uiState.collectAsStateWithLifecycle()
                    NotificationsScreen(
                        notifications = notificationsUi.notifications,
                        isLoading = notificationsUi.isLoading,
                        onBack = { homeNav.safePopBack(Routes.HomeMain) },
                        onMarkAllRead = { notificationsViewModel.markAllRead() },
                        onNotificationClick = { notification ->
                            notificationsViewModel.markAllRead()
                            val refId = notification.refId
                            when {
                                // Post-round: Player of the Day copy → trophy share card;
                                // otherwise the round summary share screen (IG/FB).
                                refId != null && notification.isRoundRecap() -> {
                                    val potd = notification.title.contains("Player of the Day", ignoreCase = true)
                                    homeNav.navigate(
                                        if (potd) Routes.playerOfTheDay(refId)
                                        else Routes.roundSummary(refId),
                                    )
                                }
                                // Message / poll notifications jump straight into the chat.
                                refId != null && notification.type == "tee_time_message" ->
                                    homeNav.navigate(Routes.teeTimeChat(refId))
                                refId != null && (notification.type == "trip_message" || notification.type == "trip_poll") ->
                                    homeNav.navigate(Routes.tripChat(refId))
                                refId != null && notification.isIdeaThreadRelated() ->
                                    homeNav.navigate(Routes.ideaThreadDetail(refId))
                                notification.isTeeTimeRelated() && refId != null ->
                                    homeNav.navigate(Routes.teeTimeDetail(refId))
                                notification.isTripRelated() && refId != null ->
                                    homeNav.navigate(Routes.tripDetail(refId))
                                notification.isTournamentRelated() && refId != null ->
                                    homeNav.navigate(Routes.tournamentDetail(refId))
                            }
                        },
                        onDelete = { notificationsViewModel.deleteNotification(it) },
                    )
                }
                composable(Routes.IdeaThreads) {
                    IdeaThreadsScreen(
                        threads = ideaState.threads,
                        isLoading = ideaState.isLoading,
                        onBack = { homeNav.safePopBack(Routes.HomeMain) },
                        onThreadClick = { id -> homeNav.navigate(Routes.ideaThreadDetail(id)) },
                        onCreateClick = { homeNav.navigate(Routes.CreateIdeaThread) },
                    )
                }
                composable(Routes.CreateIdeaThread) {
                    LaunchedEffect(Unit) { golfersViewModel.syncFromServer() }
                    CreateIdeaThreadScreen(
                        golfers = golfersState.golfers,
                        isCreating = ideaState.isCreating,
                        onBack = { homeNav.safePopBack(Routes.HomeMain) },
                        onCreate = { name, phones ->
                            ideaThreadViewModel.createThread(name, phones) {
                                homeNav.safePopBack(Routes.HomeMain)
                                homeNav.navigate(Routes.ideaThreadDetail(it.id))
                            }
                        },
                    )
                }
                composable(
                    route = Routes.IdeaThreadDetail,
                    arguments = listOf(navArgument("id") { type = NavType.StringType }),
                ) { entry ->
                    val id = entry.arguments?.getString("id").orEmpty()
                    val thread = ideaState.threads.firstOrNull { it.id == id }
                    LaunchedEffect(id) {
                        if (thread == null) ideaThreadViewModel.loadThreads()
                    }
                    val current = ideaState.threads.firstOrNull { it.id == id }
                    if (current != null) {
                        IdeaThreadDetailScreen(
                            thread = current,
                            user = user,
                            messages = ideaState.threadMessages[id].orEmpty(),
                            isSending = ideaState.isSendingMessage,
                            isCreatingPoll = ideaState.isCreatingPoll,
                            onBack = { homeNav.safePopBack(Routes.HomeMain) },
                            onLoad = { ideaThreadViewModel.loadMessages(id) },
                            onStartPolling = { ideaThreadViewModel.startChatPolling(id) },
                            onStopPolling = { ideaThreadViewModel.stopChatPolling() },
                            onSend = { text -> ideaThreadViewModel.sendMessage(id, text) },
                            onCreatePoll = { q, opts, multi -> ideaThreadViewModel.createPoll(id, q, opts, multi) },
                            onVotePoll = { pollId, ids -> ideaThreadViewModel.votePoll(id, pollId, ids) },
                            onClosePoll = { pollId -> ideaThreadViewModel.closePoll(id, pollId) },
                            onDeletePoll = { pollId -> ideaThreadViewModel.deletePoll(id, pollId) },
                        )
                    } else {
                        LoadingWithBack(onBack = { homeNav.safePopBack(Routes.HomeMain) })
                    }
                }
                friendGroupRoutes(
                    nav = homeNav,
                    rootRoute = Routes.HomeMain,
                    user = user,
                    golfersViewModel = golfersViewModel,
                    context = context,
                )
                composable(
                    route = Routes.ManageInvitees,
                    arguments = listOf(navArgument("id") { type = NavType.StringType }),
                ) { entry ->
                    val id = entry.arguments?.getString("id").orEmpty().decodeRoute()
                    val teeTime = teeTimeState.teeTimes.firstOrNull { it.id == id }
                    LaunchedEffect(id) {
                        if (teeTime == null) teeTimeViewModel.refreshTeeTime(id)
                        golfersViewModel.syncFromServer()
                    }
                    if (teeTime != null) {
                        val tripId = teeTime.tripId
                        val trip = tripId?.let { tripViewModel.trip(it) }
                        LaunchedEffect(tripId) {
                            if (!tripId.isNullOrBlank() && trip == null) {
                                tripViewModel.refreshTrip(tripId)
                            }
                        }
                        val tripRoster = trip?.invites?.mapNotNull { invite ->
                            val phone = invite.phone?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                            val parts = invite.name.trim().split(Regex("\\s+"), limit = 2)
                            Friend(
                                phone = phone,
                                firstName = parts.getOrElse(0) { invite.name },
                                lastName = parts.getOrElse(1) { "" },
                            )
                        }
                        ManageInviteesScreen(
                            teeTime = teeTime,
                            groups = golfersState.groups,
                            savedGolfers = golfersState.golfers,
                            tripRoster = tripRoster,
                            isLoading = teeTimeState.isLoading,
                            onBack = { homeNav.safePopBack(Routes.HomeMain) },
                            onRemove = { invite ->
                                teeTimeViewModel.removeInvite(id, invite.userId, invite.phone)
                            },
                            onAdd = { friends ->
                                val notify = !teeTime.hasPendingInvites
                                teeTimeViewModel.addInvites(id, friends, notify = notify) { manuals ->
                                    if (manuals.isNotEmpty()) {
                                        openManualInviteSms(context, manuals)
                                    }
                                }
                            },
                        )
                    } else {
                        LoadingWithBack(onBack = { homeNav.safePopBack(Routes.HomeMain) })
                    }
                }
                composable(
                    route = Routes.Scorecards,
                    arguments = listOf(navArgument("id") { type = NavType.StringType }),
                ) { entry ->
                    val id = entry.arguments?.getString("id").orEmpty().decodeRoute()
                    val teeTime = teeTimeState.teeTimes.firstOrNull { it.id == id }
                    LaunchedEffect(id) {
                        if (teeTime == null) teeTimeViewModel.refreshTeeTime(id)
                        teeTimeViewModel.loadTeeTimeDetail(id)
                    }
                    ScorecardViewerScreen(
                        courseName = teeTime?.courseName ?: "Scorecards",
                        scorecards = teeTimeState.teeTimeScorecards[id].orEmpty(),
                        onBack = { homeNav.safePopBack(Routes.HomeMain) },
                        onShareRound = { homeNav.navigate(Routes.roundSummary(id)) },
                        onPlayerOfTheDay = { homeNav.navigate(Routes.playerOfTheDay(id)) },
                    )
                }
                composable(
                    route = Routes.RoundSummary,
                    arguments = listOf(navArgument("id") { type = NavType.StringType }),
                ) { entry ->
                    val id = entry.arguments?.getString("id").orEmpty().decodeRoute()
                    val teeTime = teeTimeState.teeTimes.firstOrNull { it.id == id }
                    LaunchedEffect(id) {
                        teeTimeViewModel.refreshTeeTime(id)
                    }
                    if (teeTime != null) {
                        RoundSummaryScreen(
                            teeTime = teeTime,
                            summary = teeTimeState.roundSummaries[id],
                            scorecards = teeTimeState.teeTimeScorecards[id].orEmpty(),
                            isLoading = teeTimeState.isLoadingRoundSummary,
                            errorMessage = teeTimeState.errorMessage,
                            onBack = { homeNav.safePopBack(Routes.HomeMain) },
                            onLoad = { teeTimeViewModel.loadRoundSummary(id) },
                            onOpenPlayerOfTheDay = { homeNav.navigate(Routes.playerOfTheDay(id)) },
                        )
                    } else {
                        LoadingWithBack(onBack = { homeNav.safePopBack(Routes.HomeMain) })
                    }
                }
                composable(
                    route = Routes.PlayerOfTheDay,
                    arguments = listOf(navArgument("id") { type = NavType.StringType }),
                ) { entry ->
                    val id = entry.arguments?.getString("id").orEmpty().decodeRoute()
                    val teeTime = teeTimeState.teeTimes.firstOrNull { it.id == id }
                    LaunchedEffect(id) {
                        teeTimeViewModel.refreshTeeTime(id)
                        teeTimeViewModel.loadTeeTimeDetail(id)
                    }
                    if (teeTime != null) {
                        PlayerOfTheDayScreen(
                            teeTime = teeTime,
                            scorecards = teeTimeState.teeTimeScorecards[id].orEmpty(),
                            onBack = { homeNav.safePopBack(Routes.HomeMain) },
                        )
                    } else {
                        LoadingWithBack(onBack = { homeNav.safePopBack(Routes.HomeMain) })
                    }
                }
                composable(Routes.Tournaments) {
                    LaunchedEffect(Unit) { tournamentViewModel.loadTournaments() }
                    TournamentsScreen(
                        tournaments = tournamentState.tournaments.filter { !it.isWithdrawn },
                        isLoading = tournamentState.isLoading,
                        onBack = { homeNav.safePopBack(Routes.HomeMain) },
                        onTournamentClick = { tid -> homeNav.navigate(Routes.tournamentDetail(tid)) },
                    )
                }
                composable(
                    route = Routes.TournamentDetail,
                    arguments = listOf(navArgument("id") { type = NavType.StringType }),
                ) { entry ->
                    val id = entry.arguments?.getString("id").orEmpty().decodeRoute()
                    LaunchedEffect(id) { tournamentViewModel.loadTournament(id) }
                    val tournament = tournamentState.selected?.takeIf { it.id == id }
                        ?: tournamentState.tournaments.firstOrNull { it.id == id }
                    if (tournament != null) {
                        TournamentDetailScreen(
                            tournament = tournament,
                            participants = tournamentState.participants,
                            currentUserId = user.id,
                            isLoading = tournamentState.isLoading,
                            isRegistering = tournamentState.isRegistering,
                            isWithdrawing = tournamentState.isWithdrawing,
                            onBack = { homeNav.safePopBack(Routes.HomeMain) },
                            onRegister = { teamName ->
                                tournamentViewModel.register(id, teamName = teamName) {
                                    Toast.makeText(context, "You’re registered!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onWithdraw = {
                                tournamentViewModel.withdraw(id, user.phone ?: user.id) {
                                    Toast.makeText(context, "You’ve withdrawn.", Toast.LENGTH_SHORT).show()
                                }
                            },
                        )
                    } else {
                        LoadingWithBack(onBack = { homeNav.safePopBack(Routes.HomeMain) })
                    }
                }
            }

            1 -> NavHost(
                navController = golfersNav,
                startDestination = "golfers_main",
                modifier = Modifier.padding(padding),
            ) {
                composable("golfers_main") {
                    BackHandler(enabled = selectedTab == 1) { }
                    GolfersScreen(
                        golfers = golfersState.golfers,
                        contactStatuses = golfersState.contactStatuses,
                        isLoading = golfersState.isLoading,
                        isPreparingInvite = golfersState.isPreparingInvite,
                        inviteError = golfersState.inviteError,
                        onClearInviteError = golfersViewModel::clearInviteError,
                        onRefresh = {
                            golfersViewModel.syncFromServer()
                        },
                        onOpenGroups = { golfersNav.navigate(Routes.FriendGroups) },
                        onAddFromContacts = { openContactPicker() },
                        onAddManual = { showManualGolfer = true },
                        onRemove = { golfersViewModel.removeGolfer(it) },
                        onInviteToApp = { friend ->
                            golfersViewModel.prepareAppInvite(
                                friend = friend,
                                hostName = user.displayName,
                            ) { phone, message ->
                                val smsUri = Uri.parse("smsto:${Uri.encode(phone)}")
                                val intent = Intent(Intent.ACTION_SENDTO, smsUri).apply {
                                    putExtra("sms_body", message)
                                }
                                runCatching {
                                    context.startActivity(intent)
                                }.onFailure {
                                    Toast.makeText(
                                        context,
                                        "Could not open Messages. Please text ${friend.fullName} manually.",
                                        Toast.LENGTH_LONG,
                                    ).show()
                                }
                            }
                        },
                    )
                }
                friendGroupRoutes(
                    nav = golfersNav,
                    rootRoute = "golfers_main",
                    user = user,
                    golfersViewModel = golfersViewModel,
                    context = context,
                )
            }

            else -> NavHost(
                navController = profileNav,
                startDestination = "profile_main",
                modifier = Modifier.padding(padding),
            ) {
                composable("profile_main") {
                    BackHandler(enabled = selectedTab == 2) { }
                    ProfileScreen(
                        user = user,
                        profilePreferences = profilePreferences,
                        courseSearchResults = teeTimeState.courseSearchResults,
                        isSearchingCourses = teeTimeState.isSearchingCourses,
                        isUploadingAvatar = authState.isUploadingAvatar,
                        declinedTeeTimes = declinedTeeTimes,
                        declinedTrips = declinedTrips,
                        withdrawnTournaments = withdrawnTournaments,
                        previousTeeTimes = previousTeeTimes,
                        roundScores = teeTimeState.roundScores,
                        onDarkModeChange = onDarkModeChange,
                        onEditProfile = { profileNav.navigate("edit_profile") },
                        onSearchCourses = { query -> profileCourseQuery = query },
                        onAddFavoriteCourse = authViewModel::addFavoriteCourse,
                        onRemoveFavoriteCourse = authViewModel::removeFavoriteCourse,
                        onUploadAvatar = authViewModel::uploadAvatar,
                        onDeleteAvatar = authViewModel::deleteAvatar,
                        onDeclinedTeeTimeClick = { id -> profileNav.navigate(Routes.teeTimeDetail(id)) },
                        onDeclinedTripClick = { id -> profileNav.navigate(Routes.tripDetail(id)) },
                        onWithdrawnTournamentClick = { id -> profileNav.navigate(Routes.tournamentDetail(id)) },
                        onPreviousTeeTimeClick = { id -> profileNav.navigate(Routes.teeTimeDetail(id)) },
                        onLinkEmail = { profileNav.navigate(Routes.LinkEmail) },
                        onLinkGoogle = {
                            scope.launch {
                                GoogleSignInHelper.signIn(context)
                                    .onSuccess { token ->
                                        authViewModel.linkGoogle(token) {
                                            Toast.makeText(context, "Google account linked", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    .onFailure { error ->
                                        if (error !is androidx.credentials.exceptions.GetCredentialCancellationException) {
                                            Toast.makeText(
                                                context,
                                                error.message ?: "Google link failed",
                                                Toast.LENGTH_LONG,
                                            ).show()
                                        }
                                    }
                            }
                        },
                        isLinkingGoogle = authState.isLoading,
                        onSignOut = onSignOut,
                    )
                }
                composable(Routes.LinkEmail) {
                    LinkEmailScreen(
                        isLoading = authState.isLoading,
                        onBack = { profileNav.safePopBack("profile_main") },
                        onLink = { email, password ->
                            authViewModel.linkEmail(email, password) {
                                Toast.makeText(context, "Email linked", Toast.LENGTH_SHORT).show()
                                profileNav.safePopBack("profile_main")
                            }
                        },
                    )
                }
                composable(
                    route = Routes.TournamentDetail,
                    arguments = listOf(navArgument("id") { type = NavType.StringType }),
                ) { entry ->
                    val id = entry.arguments?.getString("id").orEmpty().decodeRoute()
                    LaunchedEffect(id) { tournamentViewModel.loadTournament(id) }
                    val tournament = tournamentState.selected?.takeIf { it.id == id }
                        ?: tournamentState.tournaments.firstOrNull { it.id == id }
                    if (tournament != null) {
                        TournamentDetailScreen(
                            tournament = tournament,
                            participants = tournamentState.participants,
                            currentUserId = user.id,
                            isLoading = tournamentState.isLoading,
                            isRegistering = tournamentState.isRegistering,
                            isWithdrawing = tournamentState.isWithdrawing,
                            onBack = { profileNav.safePopBack("profile_main") },
                            onRegister = { teamName ->
                                tournamentViewModel.register(id, teamName = teamName) {
                                    Toast.makeText(context, "You’re registered!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onWithdraw = {
                                tournamentViewModel.withdraw(id, user.phone ?: user.id) {
                                    Toast.makeText(context, "You’ve withdrawn.", Toast.LENGTH_SHORT).show()
                                }
                            },
                        )
                    } else {
                        LoadingWithBack(onBack = { profileNav.safePopBack("profile_main") })
                    }
                }
                composable(
                    route = Routes.TripDetail,
                    arguments = listOf(navArgument("id") { type = NavType.StringType }),
                ) { entry ->
                    val id = entry.arguments?.getString("id").orEmpty().decodeRoute()
                    val trip = tripViewModel.trip(id)
                    LaunchedEffect(id) {
                        if (trip == null) tripViewModel.refreshTrip(id)
                        tripViewModel.loadTripDetail(id)
                    }
                    val currentTrip = tripViewModel.trip(id)
                    if (currentTrip != null) {
                        TripDetailScreen(
                            trip = currentTrip,
                            user = user,
                            teeTimes = tripState.tripTeeTimes[id].orEmpty(),
                            photos = tripState.tripPhotos[id].orEmpty(),
                            announcements = tripState.tripAnnouncements[id].orEmpty(),
                            isLoading = tripState.isLoading,
                            isUploadingPhoto = tripState.isUploadingPhoto,
                            isPostingAnnouncement = tripState.isPostingAnnouncement,
                            onBack = { profileNav.safePopBack("profile_main") },
                            onRsvp = { status -> tripViewModel.rsvpTrip(id, status) },
                            onToggleDeposit = { invite, paid ->
                                tripViewModel.setDepositPaid(id, invite.phone, invite.userId, paid)
                            },
                            onOpenChat = { profileNav.navigate(Routes.tripChat(id)) },
                            onTeeTimeClick = { teeTimeId -> profileNav.navigate(Routes.teeTimeDetail(teeTimeId)) },
                            onUploadPhoto = { bytes, mime -> tripViewModel.uploadPhoto(id, bytes, mime) },
                            onPostAnnouncement = { message -> tripViewModel.postAnnouncement(id, message) },
                        )
                    } else {
                        LoadingWithBack(onBack = { profileNav.safePopBack("profile_main") })
                    }
                }
                composable(
                    route = Routes.TripChat,
                    arguments = listOf(navArgument("id") { type = NavType.StringType }),
                ) { entry ->
                    val id = entry.arguments?.getString("id").orEmpty().decodeRoute()
                    val trip = tripViewModel.trip(id)
                    LaunchedEffect(id) {
                        if (trip == null) tripViewModel.refreshTrip(id)
                    }
                    val currentTrip = tripViewModel.trip(id)
                    if (currentTrip != null) {
                        TripChatScreen(
                            tripName = currentTrip.name,
                            tripId = id,
                            user = user,
                            messages = tripState.tripMessages[id].orEmpty(),
                            isSending = tripState.isSendingMessage,
                            isCreatingPoll = tripState.isCreatingPoll,
                            tripCreatorId = currentTrip.creatorId,
                            mentionCandidates = mentionCandidatesFrom(currentTrip.invites, user.id),
                            onBack = { profileNav.safePopBack("profile_main") },
                            onLoad = { tripViewModel.loadMessages(id) },
                            onStartPolling = { tripViewModel.startChatPolling(id) },
                            onStopPolling = { tripViewModel.stopChatPolling() },
                            onSend = { text, mentions -> tripViewModel.sendMessage(id, text, mentions) },
                            onToggleReaction = { messageId, emoji ->
                                tripViewModel.toggleReaction(id, messageId, emoji)
                            },
                            onCreatePoll = { q, opts, multi -> tripViewModel.createPoll(id, q, opts, multi) },
                            onVotePoll = { pollId, ids -> tripViewModel.votePoll(id, pollId, ids) },
                            onClosePoll = { pollId -> tripViewModel.closePoll(id, pollId) },
                            onDeletePoll = { pollId -> tripViewModel.deletePoll(id, pollId) },
                        )
                    } else {
                        LoadingWithBack(onBack = { profileNav.safePopBack("profile_main") })
                    }
                }
                composable(
                    route = Routes.TeeTimeDetail,
                    arguments = listOf(navArgument("id") { type = NavType.StringType }),
                ) { entry ->
                    val id = entry.arguments?.getString("id").orEmpty().decodeRoute()
                    val teeTime = teeTimeState.teeTimes.firstOrNull { it.id == id }
                    LaunchedEffect(id) {
                        if (teeTime == null) teeTimeViewModel.refreshTeeTime(id)
                        teeTimeViewModel.loadTeeTimeDetail(id)
                    }
                    if (teeTime != null) {
                        TeeTimeDetailScreen(
                            teeTime = teeTime,
                            user = user,
                            photos = teeTimeState.teeTimePhotos[id].orEmpty(),
                            myScore = teeTimeState.roundScores[id],
                            scorecards = teeTimeState.teeTimeScorecards[id].orEmpty(),
                            courseWebsiteUrl = teeTime.courseId?.let { teeTimeState.courseWebsites[it] },
                            isLoading = teeTimeState.isLoading,
                            isUploadingPhoto = teeTimeState.isUploadingPhoto,
                            isSavingScore = teeTimeState.isSavingScore,
                            onBack = { profileNav.safePopBack("profile_main") },
                            onRsvp = { status ->
                                teeTimeViewModel.updateRsvp(
                                    teeTimeId = id,
                                    phone = user.phone,
                                    status = status,
                                    userId = user.id,
                                )
                            },
                            onOpenChat = { profileNav.navigate(Routes.teeTimeChat(id)) },
                            onUploadPhoto = { bytes, mime -> teeTimeViewModel.uploadPhoto(id, bytes, mime) },
                            onSaveScore = { score -> teeTimeViewModel.saveRoundScore(id, score) },
                            onEdit = null,
                            onSendPendingInvites = {
                                teeTimeViewModel.sendPendingInvites(id) { manuals ->
                                    if (manuals.isNotEmpty()) {
                                        openManualInviteSms(context, manuals)
                                    }
                                }
                            },
                            onViewScorecards = { profileNav.navigate(Routes.scorecards(id)) },
                            onShareRound = { profileNav.navigate(Routes.roundSummary(id)) },
                            onBumpInvite = { invite ->
                                bumpInvitee(context, teeTimeViewModel, teeTime, invite, user.displayName)
                            },
                            onToggleInviteAccess = { invite ->
                                invite.userId?.let { teeTimeViewModel.toggleInviteAccess(id, it) }
                            },
                            onSendLinksideInvite = { invite ->
                                invite.phone?.takeIf { it.isNotBlank() }?.let { phone ->
                                    teeTimeViewModel.prepareLinksideInvite(phone, invite.name, user.displayName) { manual ->
                                        openManualInviteSms(context, listOf(manual))
                                    }
                                }
                            },
                            onRemoveInvite = { invite ->
                                teeTimeViewModel.removeInvite(id, invite.userId, invite.phone)
                            },
                        )
                    } else {
                        LoadingWithBack(onBack = { profileNav.safePopBack("profile_main") })
                    }
                }
                composable(
                    route = Routes.Scorecards,
                    arguments = listOf(navArgument("id") { type = NavType.StringType }),
                ) { entry ->
                    val id = entry.arguments?.getString("id").orEmpty().decodeRoute()
                    val teeTime = teeTimeState.teeTimes.firstOrNull { it.id == id }
                    LaunchedEffect(id) {
                        if (teeTime == null) teeTimeViewModel.refreshTeeTime(id)
                        teeTimeViewModel.loadTeeTimeDetail(id)
                    }
                    ScorecardViewerScreen(
                        courseName = teeTime?.courseName ?: "Scorecards",
                        scorecards = teeTimeState.teeTimeScorecards[id].orEmpty(),
                        onBack = { profileNav.safePopBack("profile_main") },
                        onShareRound = { profileNav.navigate(Routes.roundSummary(id)) },
                        onPlayerOfTheDay = { profileNav.navigate(Routes.playerOfTheDay(id)) },
                    )
                }
                composable(
                    route = Routes.RoundSummary,
                    arguments = listOf(navArgument("id") { type = NavType.StringType }),
                ) { entry ->
                    val id = entry.arguments?.getString("id").orEmpty().decodeRoute()
                    val teeTime = teeTimeState.teeTimes.firstOrNull { it.id == id }
                    LaunchedEffect(id) {
                        teeTimeViewModel.refreshTeeTime(id)
                    }
                    if (teeTime != null) {
                        RoundSummaryScreen(
                            teeTime = teeTime,
                            summary = teeTimeState.roundSummaries[id],
                            scorecards = teeTimeState.teeTimeScorecards[id].orEmpty(),
                            isLoading = teeTimeState.isLoadingRoundSummary,
                            errorMessage = teeTimeState.errorMessage,
                            onBack = { profileNav.safePopBack("profile_main") },
                            onLoad = { teeTimeViewModel.loadRoundSummary(id) },
                            onOpenPlayerOfTheDay = { profileNav.navigate(Routes.playerOfTheDay(id)) },
                        )
                    } else {
                        LoadingWithBack(onBack = { profileNav.safePopBack("profile_main") })
                    }
                }
                composable(
                    route = Routes.PlayerOfTheDay,
                    arguments = listOf(navArgument("id") { type = NavType.StringType }),
                ) { entry ->
                    val id = entry.arguments?.getString("id").orEmpty().decodeRoute()
                    val teeTime = teeTimeState.teeTimes.firstOrNull { it.id == id }
                    LaunchedEffect(id) {
                        teeTimeViewModel.refreshTeeTime(id)
                        teeTimeViewModel.loadTeeTimeDetail(id)
                    }
                    if (teeTime != null) {
                        PlayerOfTheDayScreen(
                            teeTime = teeTime,
                            scorecards = teeTimeState.teeTimeScorecards[id].orEmpty(),
                            onBack = { profileNav.safePopBack("profile_main") },
                        )
                    } else {
                        LoadingWithBack(onBack = { profileNav.safePopBack("profile_main") })
                    }
                }
                composable(
                    route = Routes.TeeTimeChat,
                    arguments = listOf(navArgument("id") { type = NavType.StringType }),
                ) { entry ->
                    val id = entry.arguments?.getString("id").orEmpty().decodeRoute()
                    val teeTime = teeTimeState.teeTimes.firstOrNull { it.id == id }
                    LaunchedEffect(id) {
                        if (teeTime == null) teeTimeViewModel.refreshTeeTime(id)
                    }
                    val current = teeTimeState.teeTimes.firstOrNull { it.id == id }
                    if (current != null) {
                        TeeTimeChatScreen(
                            courseName = current.courseName,
                            teeTimeId = id,
                            user = user,
                            messages = teeTimeState.teeTimeMessages[id].orEmpty(),
                            isSending = teeTimeState.isSendingMessage,
                            mentionCandidates = mentionCandidatesFrom(current.invites, user.id),
                            onBack = { profileNav.safePopBack("profile_main") },
                            onLoad = { teeTimeViewModel.loadMessages(id) },
                            onStartPolling = { teeTimeViewModel.startChatPolling(id) },
                            onStopPolling = { teeTimeViewModel.stopChatPolling() },
                            onSend = { text, mentions -> teeTimeViewModel.sendMessage(id, text, mentions) },
                            onToggleReaction = { messageId, emoji ->
                                teeTimeViewModel.toggleReaction(id, messageId, emoji)
                            },
                        )
                    } else {
                        LoadingWithBack(onBack = { profileNav.safePopBack("profile_main") })
                    }
                }
                composable("edit_profile") {
                    EditProfileScreen(
                        user = user,
                        isLoading = authState.isLoading,
                        onBack = { profileNav.safePopBack("profile_main") },
                        onSave = { firstName, lastName, address, city, state, zipCode, handicapText ->
                            authViewModel.updateProfileExtended(
                                firstName = firstName,
                                lastName = lastName,
                                address = address,
                                city = city,
                                state = state,
                                zipCode = zipCode,
                                handicapText = handicapText,
                            ) {
                                profileNav.safePopBack("profile_main")
                            }
                        },
                    )
                }
            }
        }
    }
}


/**
 * Opens the Messages app for the first manual invite SMS; toasts if more remain.
 */
/**
 * Bumps an invitee for their RSVP. Linkside users get a server-side push/in-app
 * notification; non-Linkside golfers are bumped by opening a pre-filled SMS.
 * Mirrors iOS TeeTimeDetailView.sendBump.
 */
private fun bumpInvitee(
    context: android.content.Context,
    viewModel: com.linkside.app.viewmodel.TeeTimeViewModel,
    teeTime: com.linkside.app.data.model.TeeTime,
    invite: com.linkside.app.data.model.Invite,
    hostName: String,
) {
    if (!invite.userId.isNullOrBlank()) {
        viewModel.bumpInvitee(teeTime.id, invite.userId, invite.phone) { success ->
            Toast.makeText(
                context,
                if (success) "Reminder sent to ${invite.name}" else "Couldn’t send reminder. Please try again.",
                if (success) Toast.LENGTH_SHORT else Toast.LENGTH_LONG,
            ).show()
        }
    } else if (!invite.phone.isNullOrBlank()) {
        val body = "\u26F3 $hostName here — are you in for golf at ${teeTime.courseName} on " +
            "${teeTime.formattedDate()}? Reply YES, NO, or MAYBE!"
        openManualInviteSms(
            context,
            listOf(ManualInvite(phone = invite.phone!!, name = invite.name, message = body)),
        )
    }
}

private fun openManualInviteSms(context: android.content.Context, invites: List<ManualInvite>) {
    val pending = invites.filter { it.message.isNotBlank() && it.phone.isNotBlank() }
    if (pending.isEmpty()) return
    val first = pending.first()
    val smsUri = Uri.parse("smsto:${Uri.encode(first.phone)}")
    val intent = Intent(Intent.ACTION_SENDTO, smsUri).apply {
        putExtra("sms_body", first.message)
    }
    runCatching {
        context.startActivity(intent)
    }.onFailure {
        Toast.makeText(
            context,
            "Could not open Messages. Please text ${first.name} manually.",
            Toast.LENGTH_LONG,
        ).show()
    }
    if (pending.size > 1) {
        Toast.makeText(
            context,
            "Invite sent draft opened. ${pending.size - 1} more invite(s) still need a text.",
            Toast.LENGTH_LONG,
        ).show()
    }
}

/**
 * Chat @mention candidates from an invite list: named participants (excluding
 * self), de-duplicated by name. SMS-only invitees keep a null userId so they can
 * be tagged visually without receiving a push. Mirrors iOS mentionCandidates.
 */
private fun mentionCandidatesFrom(
    invites: List<com.linkside.app.data.model.Invite>,
    myId: String,
): List<MentionCandidate> {
    val seen = mutableSetOf<String>()
    return invites.mapNotNull { inv ->
        val name = inv.name.trim()
        if (name.isEmpty()) return@mapNotNull null
        if (inv.userId != null && inv.userId == myId) return@mapNotNull null
        if (!seen.add(name.lowercase())) return@mapNotNull null
        MentionCandidate(userId = inv.userId, name = name)
    }
}

/**
 * Pops one level, but never removes [rootRoute] from the stack.
 * A second tap after returning home (same spot as the back button) used to
 * call popBackStack() on the root and leave the NavHost empty / solid green.
 */
private fun NavHostController.safePopBack(rootRoute: String) {
    val currentRoute = currentDestination?.route
    if (currentRoute == null || currentRoute == rootRoute) return
    if (previousBackStackEntry == null) return
    popBackStack()
}

@Composable
private fun LoadingWithBack(
    onBack: () -> Unit,
    title: String? = null,
) {
    Scaffold(
        containerColor = LinksideColors.Primary,
        topBar = { LinksideTopAppBar(title = title, onBack = onBack) },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = LinksideColors.Accent)
        }
    }
}

private fun NavGraphBuilder.friendGroupRoutes(
    nav: NavHostController,
    rootRoute: String,
    user: User,
    golfersViewModel: GolfersViewModel,
    context: android.content.Context,
) {
    composable(Routes.FriendGroups) {
        val golfersState by golfersViewModel.uiState.collectAsStateWithLifecycle()
        LaunchedEffect(Unit) { golfersViewModel.syncFromServer() }
        FriendGroupsScreen(
            groups = golfersState.groups,
            canCreateGroup = golfersViewModel.canCreateGroup(user),
            onBack = { nav.safePopBack(rootRoute) },
            onCreateGroup = {
                if (golfersViewModel.canCreateGroup(user)) {
                    nav.navigate(Routes.editGroup(null))
                } else {
                    Toast.makeText(context, "Bronze accounts are limited to 3 friend groups.", Toast.LENGTH_LONG).show()
                }
            },
            onEditGroup = { group -> nav.navigate(Routes.editGroup(group.id)) },
            onDeleteGroup = { golfersViewModel.deleteGroup(it) },
        )
    }
    composable(
        route = Routes.EditGroup,
        arguments = listOf(navArgument("id") { type = NavType.StringType }),
    ) { entry ->
        val golfersState by golfersViewModel.uiState.collectAsStateWithLifecycle()
        val rawId = entry.arguments?.getString("id").orEmpty().decodeRoute()
        val isNew = rawId.isBlank() || rawId.equals("new", ignoreCase = true)
        val group = if (isNew) {
            null
        } else {
            golfersState.groups.firstOrNull { it.id.equals(rawId, ignoreCase = true) }
        }
        LaunchedEffect(rawId) { golfersViewModel.syncFromServer() }
        EditGroupScreen(
            group = group,
            savedGolfers = golfersState.golfers,
            contactStatuses = golfersState.contactStatuses,
            isLoadingGolfers = golfersState.isLoading,
            isSaving = golfersState.isSavingGroup,
            onRefreshGolfers = { golfersViewModel.syncFromServer() },
            onBack = { nav.safePopBack(rootRoute) },
            onSave = { name, members ->
                when {
                    !isNew && group == null -> {
                        Toast.makeText(context, "Group not found. Pull to refresh and try again.", Toast.LENGTH_LONG).show()
                    }
                    group == null -> {
                        if (!golfersViewModel.canCreateGroup(user)) {
                            Toast.makeText(
                                context,
                                "Bronze accounts are limited to 3 friend groups.",
                                Toast.LENGTH_LONG,
                            ).show()
                            return@EditGroupScreen
                        }
                        golfersViewModel.createGroup(name, members) { nav.safePopBack(rootRoute) }
                    }
                    else -> {
                        golfersViewModel.updateGroup(group.copy(name = name, members = members)) {
                            nav.safePopBack(rootRoute)
                        }
                    }
                }
            },
        )
    }
}

@Composable
private fun navItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = LinksideColors.AccentLabel,
    selectedTextColor = LinksideColors.AccentLabel,
    unselectedIconColor = LinksideColors.TextTertiary,
    unselectedTextColor = LinksideColors.TextTertiary,
    indicatorColor = LinksideColors.AccentChipBackground,
)
