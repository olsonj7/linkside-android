package com.linkside.app.ui.navigation

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.linkside.app.data.contacts.ContactsHelper
import com.linkside.app.data.model.User
import com.linkside.app.data.prefs.ProfilePreferences
import com.linkside.app.ui.golfers.ContactPickerSheet
import com.linkside.app.ui.golfers.EditGroupScreen
import com.linkside.app.ui.golfers.FriendGroupsScreen
import com.linkside.app.ui.golfers.GolfersScreen
import com.linkside.app.ui.golfers.ManualGolferDialog
import com.linkside.app.ui.components.ProfileAvatarView
import com.linkside.app.ui.home.HomeScreen
import com.linkside.app.ui.profile.EditProfileScreen
import com.linkside.app.ui.profile.ProfileScreen
import com.linkside.app.ui.teetimes.CreateTeeTimeScreen
import com.linkside.app.ui.teetimes.TeeTimeDetailScreen
import com.linkside.app.ui.trips.TripChatScreen
import com.linkside.app.ui.trips.TripDetailScreen
import com.linkside.app.ui.theme.LinksideColors
import com.linkside.app.viewmodel.AuthViewModel
import com.linkside.app.viewmodel.GolfersViewModel
import com.linkside.app.viewmodel.TeeTimeViewModel
import com.linkside.app.viewmodel.TripViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTabShell(
    authViewModel: AuthViewModel,
    profilePreferences: ProfilePreferences,
    golfersViewModel: GolfersViewModel,
    teeTimeViewModel: TeeTimeViewModel,
    tripViewModel: TripViewModel,
    onDarkModeChange: (Boolean) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val user = authState.user ?: return
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val homeNav = rememberNavController()
    val golfersNav = rememberNavController()
    val profileNav = rememberNavController()

    val golfersState by golfersViewModel.uiState.collectAsStateWithLifecycle()
    val teeTimeState by teeTimeViewModel.uiState.collectAsStateWithLifecycle()
    val tripState by tripViewModel.uiState.collectAsStateWithLifecycle()

    var showManualGolfer by remember { mutableStateOf(false) }
    var showContactPicker by remember { mutableStateOf(false) }
    var pickerSelection by remember { mutableStateOf(setOf<String>()) }
    var courseQuery by remember { mutableStateOf("") }
    var profileCourseQuery by remember { mutableStateOf("") }

    LaunchedEffect(selectedTab) {
        if (selectedTab == 0) {
            authViewModel.refreshUser()
        }
    }

    LaunchedEffect(user.id) {
        teeTimeViewModel.currentUser = user
        tripViewModel.currentUser = user
        authViewModel.refreshUser()
        golfersViewModel.syncFromServer()
        teeTimeViewModel.loadTeeTimes()
        tripViewModel.loadTrips()
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
        teeTimeViewModel.searchCourses(courseQuery)
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
                    HomeScreen(
                        user = user,
                        teeTimes = teeTimeState.teeTimes,
                        trips = tripViewModel.upcomingTrips(user),
                        groups = golfersState.groups,
                        isLoading = teeTimeState.isLoading,
                        isTripsLoading = tripState.isLoading,
                        onRefresh = {
                            authViewModel.refreshUser()
                            teeTimeViewModel.loadTeeTimes()
                            tripViewModel.loadTrips()
                            golfersViewModel.syncFromServer()
                        },
                        onCreateTeeTime = { homeNav.navigate(Routes.CreateTeeTime) },
                        onTeeTimeClick = { id -> homeNav.navigate(Routes.teeTimeDetail(id)) },
                        onTripClick = { id -> homeNav.navigate(Routes.tripDetail(id)) },
                        onFriendGroups = { homeNav.navigate(Routes.FriendGroups) },
                        onEditGroup = { group -> homeNav.navigate(Routes.editGroup(group.id)) },
                    )
                }
                composable(Routes.CreateTeeTime) {
                    LaunchedEffect(Unit) {
                        courseQuery = ""
                        teeTimeViewModel.clearCourseSearch()
                        golfersViewModel.syncFromServer()
                    }
                    CreateTeeTimeScreen(
                        savedGolfers = golfersState.golfers,
                        friendGroups = golfersState.groups,
                        defaultGroupSize = profilePreferences.defaultGroupSize.coerceIn(2, 4),
                        courseResults = teeTimeState.courseSearchResults,
                        isSearching = teeTimeState.isSearchingCourses,
                        isLoading = teeTimeState.isLoading,
                        onBack = { homeNav.popBackStack() },
                        onSearchCourses = { query -> courseQuery = query },
                        onClearCourseSearch = { teeTimeViewModel.clearCourseSearch() },
                        onCreate = { courseName, courseId, date, golfersNeeded, invites, timeMode, timeWindows, playFormat, greenFee ->
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
                            ) {
                                homeNav.popBackStack()
                            }
                        },
                    )
                }
                composable(
                    route = Routes.TeeTimeDetail,
                    arguments = listOf(navArgument("id") { type = NavType.StringType }),
                ) { entry ->
                    val id = entry.arguments?.getString("id").orEmpty()
                    val teeTime = teeTimeState.teeTimes.firstOrNull { it.id == id }
                    LaunchedEffect(id) {
                        if (teeTime == null) teeTimeViewModel.refreshTeeTime(id)
                    }
                    if (teeTime != null) {
                        TeeTimeDetailScreen(
                            teeTime = teeTime,
                            user = user,
                            isLoading = teeTimeState.isLoading,
                            onBack = { homeNav.popBackStack() },
                            onRsvp = { status ->
                                val phone = user.phone ?: user.id
                                teeTimeViewModel.updateRsvp(id, phone, status)
                            },
                        )
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = LinksideColors.Accent)
                        }
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
                        val myInvite = currentTrip.myInvite(user)
                        TripDetailScreen(
                            trip = currentTrip,
                            user = user,
                            teeTimes = tripState.tripTeeTimes[id].orEmpty(),
                            photos = tripState.tripPhotos[id].orEmpty(),
                            isLoading = tripState.isLoading,
                            isUploadingPhoto = tripState.isUploadingPhoto,
                            onBack = { homeNav.popBackStack() },
                            onRsvp = { status -> tripViewModel.rsvpTrip(id, status) },
                            onToggleDeposit = { paid ->
                                tripViewModel.setDepositPaid(id, myInvite?.phone, myInvite?.userId ?: user.id, paid)
                            },
                            onToggleBalance = { paid ->
                                tripViewModel.setBalancePaid(id, myInvite?.phone, myInvite?.userId ?: user.id, paid)
                            },
                            onOpenChat = { homeNav.navigate(Routes.tripChat(id)) },
                            onTeeTimeClick = { teeTimeId -> homeNav.navigate(Routes.teeTimeDetail(teeTimeId)) },
                            onUploadPhoto = { bytes, mime -> tripViewModel.uploadPhoto(id, bytes, mime) },
                        )
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = LinksideColors.Accent)
                        }
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
                            onBack = { homeNav.popBackStack() },
                            onLoad = { tripViewModel.loadMessages(id) },
                            onStartPolling = { tripViewModel.startChatPolling(id) },
                            onStopPolling = { tripViewModel.stopChatPolling() },
                            onSend = { text -> tripViewModel.sendMessage(id, text) },
                        )
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = LinksideColors.Accent)
                        }
                    }
                }
                composable(Routes.FriendGroups) {
                    FriendGroupsScreen(
                        groups = golfersState.groups,
                        canCreateGroup = golfersViewModel.canCreateGroup(user),
                        onBack = { homeNav.popBackStack() },
                        onCreateGroup = {
                            if (golfersViewModel.canCreateGroup(user)) {
                                homeNav.navigate(Routes.editGroup(null))
                            } else {
                                Toast.makeText(context, "Bronze accounts are limited to 3 friend groups.", Toast.LENGTH_LONG).show()
                            }
                        },
                        onEditGroup = { group -> homeNav.navigate(Routes.editGroup(group.id)) },
                        onDeleteGroup = { golfersViewModel.deleteGroup(it) },
                    )
                }
                composable(
                    route = Routes.EditGroup,
                    arguments = listOf(navArgument("id") { type = NavType.StringType; defaultValue = "" }),
                ) { entry ->
                    val id = entry.arguments?.getString("id").orEmpty()
                    val group = if (id.isBlank()) null else golfersState.groups.firstOrNull { it.id == id }
                    EditGroupScreen(
                        group = group,
                        savedGolfers = golfersState.golfers,
                        isSaving = golfersState.isSavingGroup,
                        onBack = { homeNav.popBackStack() },
                        onSave = { name, members ->
                            if (group == null) {
                                if (!golfersViewModel.canCreateGroup(user)) {
                                    Toast.makeText(
                                        context,
                                        "Bronze accounts are limited to 3 friend groups.",
                                        Toast.LENGTH_LONG,
                                    ).show()
                                    return@EditGroupScreen
                                }
                                golfersViewModel.createGroup(name, members) { homeNav.popBackStack() }
                            } else {
                                golfersViewModel.updateGroup(group.copy(name = name, members = members)) {
                                    homeNav.popBackStack()
                                }
                            }
                        },
                    )
                }
            }

            1 -> NavHost(
                navController = golfersNav,
                startDestination = "golfers_main",
                modifier = Modifier.padding(padding),
            ) {
                composable("golfers_main") {
                    GolfersScreen(
                        golfers = golfersState.golfers,
                        contactStatuses = golfersState.contactStatuses,
                        isLoading = golfersState.isLoading,
                        onOpenGroups = {
                            selectedTab = 0
                            homeNav.navigate(Routes.FriendGroups)
                        },
                        onAddFromContacts = { openContactPicker() },
                        onAddManual = { showManualGolfer = true },
                        onRemove = { golfersViewModel.removeGolfer(it) },
                    )
                }
            }

            else -> NavHost(
                navController = profileNav,
                startDestination = "profile_main",
                modifier = Modifier.padding(padding),
            ) {
                composable("profile_main") {
                    ProfileScreen(
                        user = user,
                        profilePreferences = profilePreferences,
                        courseSearchResults = teeTimeState.courseSearchResults,
                        isSearchingCourses = teeTimeState.isSearchingCourses,
                        onDarkModeChange = onDarkModeChange,
                        onEditProfile = { profileNav.navigate("edit_profile") },
                        onSearchCourses = { query -> profileCourseQuery = query },
                        onAddFavoriteCourse = authViewModel::addFavoriteCourse,
                        onRemoveFavoriteCourse = authViewModel::removeFavoriteCourse,
                        onSignOut = onSignOut,
                    )
                }
                composable("edit_profile") {
                    EditProfileScreen(
                        user = user,
                        isLoading = authState.isLoading,
                        onBack = { profileNav.popBackStack() },
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
                                profileNav.popBackStack()
                            }
                        },
                    )
                }
            }
        }
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
