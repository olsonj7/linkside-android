package com.linkside.app.ui

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.linkside.app.LinksideApplication
import com.linkside.app.auth.GoogleSignInHelper
import com.linkside.app.ui.auth.EmailAuthScreen
import com.linkside.app.ui.auth.ForgotPasswordScreen
import com.linkside.app.ui.auth.OnboardingScreen
import com.linkside.app.ui.auth.PhoneLoginScreen
import com.linkside.app.ui.auth.VerifyCodeScreen
import com.linkside.app.ui.auth.WelcomeScreen
import com.linkside.app.ui.navigation.MainTabShell
import com.linkside.app.ui.navigation.Routes
import com.linkside.app.ui.navigation.decodeRoute
import com.linkside.app.push.PushTokenManager
import com.linkside.app.ui.splash.SplashScreen
import com.linkside.app.viewmodel.AuthViewModel
import com.linkside.app.viewmodel.GolfersViewModel
import com.linkside.app.viewmodel.IdeaThreadViewModel
import com.linkside.app.viewmodel.NotificationsViewModel
import com.linkside.app.viewmodel.TeeTimeViewModel
import com.linkside.app.viewmodel.TournamentViewModel
import com.linkside.app.viewmodel.TripViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch

@Composable
fun LinksideApp(
    onDarkModeChange: (Boolean) -> Unit = {},
    authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.Factory(
            (LocalContext.current.applicationContext as LinksideApplication).authRepository,
        ),
    ),
) {
    val app = LocalContext.current.applicationContext as LinksideApplication
    val golfersViewModel: GolfersViewModel = viewModel(
        factory = GolfersViewModel.Factory(app.linksideRepository),
    )
    val teeTimeViewModel: TeeTimeViewModel = viewModel(
        factory = TeeTimeViewModel.Factory(app.linksideRepository),
    )
    val tripViewModel: TripViewModel = viewModel(
        factory = TripViewModel.Factory(app.linksideRepository),
    )
    val ideaThreadViewModel: IdeaThreadViewModel = viewModel(
        factory = IdeaThreadViewModel.Factory(app.linksideRepository),
    )
    val notificationsViewModel: NotificationsViewModel = viewModel(
        factory = NotificationsViewModel.Factory(app.linksideRepository),
    )
    val tournamentViewModel: TournamentViewModel = viewModel(
        factory = TournamentViewModel.Factory(app.linksideRepository),
    )

    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    var pendingLinkPhone by remember { mutableStateOf<String?>(null) }

    fun signOutFully() {
        scope.launch {
            PushTokenManager.unregisterFromServer(app.linksideRepository)
            PushTokenManager.clearCache(context)
            golfersViewModel.clearLocalData()
            authViewModel.signOut()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            authViewModel.clearError()
        }
    }

    when {
        uiState.isInitializing -> SplashScreen()

        uiState.isAuthenticated && uiState.user != null -> {
            val user = uiState.user!!
            when {
                user.needsNameEntry -> {
                    OnboardingScreen(
                        isLoading = uiState.isLoading,
                        onContinue = { firstName, lastName ->
                            authViewModel.updateProfile(firstName, lastName) {}
                        },
                        onSignOut = { signOutFully() },
                    )
                }

                user.needsPhoneEntry -> {
                    when (val phone = pendingLinkPhone) {
                        null -> PhoneLoginScreen(
                            isLoading = uiState.isLoading,
                            title = "Add your phone",
                            subtitle = "Linkside uses your number for tee time invites and SMS RSVPs.",
                            onSignOut = {
                                pendingLinkPhone = null
                                signOutFully()
                            },
                            onSendCode = { number ->
                                authViewModel.sendCode(number) { pendingLinkPhone = number }
                            },
                        )
                        else -> VerifyCodeScreen(
                            phone = phone,
                            isLoading = uiState.isLoading,
                            onBack = { pendingLinkPhone = null },
                            onVerify = { code ->
                                authViewModel.linkPhone(phone, code) { pendingLinkPhone = null }
                            },
                        )
                    }
                }

                else -> {
                    MainTabShell(
                        authViewModel = authViewModel,
                        profilePreferences = app.profilePreferences,
                        golfersViewModel = golfersViewModel,
                        teeTimeViewModel = teeTimeViewModel,
                        tripViewModel = tripViewModel,
                        ideaThreadViewModel = ideaThreadViewModel,
                        notificationsViewModel = notificationsViewModel,
                        tournamentViewModel = tournamentViewModel,
                        onDarkModeChange = onDarkModeChange,
                        onSignOut = { signOutFully() },
                    )
                }
            }
        }

        else -> {
            NavHost(
                navController = navController,
                startDestination = Routes.Welcome,
            ) {
                composable(Routes.Welcome) {
                    WelcomeScreen(
                        onPhoneClick = { navController.navigate(Routes.PhoneLogin) },
                        onEmailClick = { navController.navigate(Routes.EmailAuth) },
                        onGoogleClick = {
                            scope.launch {
                                GoogleSignInHelper.signIn(context)
                                    .onSuccess(authViewModel::googleAuth)
                                    .onFailure { error ->
                                        if (error !is androidx.credentials.exceptions.GetCredentialCancellationException) {
                                            Toast.makeText(
                                                context,
                                                error.message ?: "Google sign-in failed",
                                                Toast.LENGTH_LONG,
                                            ).show()
                                        }
                                    }
                            }
                        },
                        isGoogleLoading = uiState.isLoading,
                    )
                }
                composable(Routes.PhoneLogin) {
                    PhoneLoginScreen(
                        isLoading = uiState.isLoading,
                        onBack = { navController.popBackStack() },
                        onSendCode = { phone ->
                            authViewModel.sendCode(phone) {
                                navController.navigate(Routes.verifyCode(phone))
                            }
                        },
                    )
                }
                composable(Routes.EmailAuth) {
                    EmailAuthScreen(
                        isLoading = uiState.isLoading,
                        onBack = { navController.popBackStack() },
                        onSignIn = { email, password ->
                            authViewModel.emailLogin(email, password) {
                                navController.popBackStack(Routes.Welcome, inclusive = true)
                            }
                        },
                        onRegister = { email, password, firstName, lastName, phone, smsConsent, phoneCode ->
                            authViewModel.emailRegister(
                                email,
                                password,
                                firstName,
                                lastName,
                                phone,
                                smsConsent,
                                phoneCode,
                            ) {
                                navController.popBackStack(Routes.Welcome, inclusive = true)
                            }
                        },
                        onSendPhoneCode = { phone, onComplete ->
                            authViewModel.sendPhoneVerificationCode(phone, onComplete)
                        },
                        onForgotPassword = { navController.navigate(Routes.ForgotPassword) },
                    )
                }
                composable(Routes.ForgotPassword) {
                    ForgotPasswordScreen(
                        isLoading = uiState.isLoading,
                        onBack = { navController.popBackStack() },
                        onSendCode = { email, onSent ->
                            authViewModel.forgotPassword(email, onSent)
                        },
                        onReset = { email, code, newPassword ->
                            authViewModel.resetPassword(email, code, newPassword) {
                                Toast.makeText(context, "Password updated. Sign in with your new password.", Toast.LENGTH_LONG).show()
                                navController.popBackStack(Routes.EmailAuth, inclusive = false)
                            }
                        },
                    )
                }
                composable(
                    route = Routes.VerifyCode,
                    arguments = listOf(navArgument("phone") { type = NavType.StringType }),
                ) { entry ->
                    val phone = entry.arguments?.getString("phone")?.decodeRoute().orEmpty()
                    VerifyCodeScreen(
                        phone = phone,
                        isLoading = uiState.isLoading,
                        onBack = { navController.popBackStack() },
                        onVerify = { code ->
                            authViewModel.verifyCode(phone, code) {
                                navController.popBackStack(Routes.Welcome, inclusive = true)
                            }
                        },
                    )
                }
            }
        }
    }
}
