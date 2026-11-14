package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.CareerArchiveScreen
import com.example.ui.screens.CareerPathChoiceScreen
import com.example.ui.screens.CustomPlayerCreatorScreen
import com.example.ui.screens.IntroSplashScreen
import com.example.ui.screens.LeagueAndClubPickerScreen
import com.example.ui.screens.LeagueAndWorldScreen
import com.example.ui.screens.MainMenuScreen
import com.example.ui.screens.MatchCenterScreen
import com.example.ui.screens.PhoneOsScreen
import com.example.ui.screens.PlayerHubScreen
import com.example.ui.screens.PlayerProfileArchiveScreen
import com.example.ui.screens.PostMatchLockerRoomScreen
import com.example.ui.screens.RealPlayerPickerScreen
import com.example.ui.screens.SaveSlotsScreen
import com.example.ui.screens.SettingsMenuScreen
import com.example.ui.screens.SocialLifeAndStoreScreen
import com.example.ui.screens.TrainingCenterScreen
import com.example.ui.screens.TransferAndContractScreen
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentBright
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BackgroundPrimary
import com.example.ui.theme.BrightGold
import com.example.ui.theme.BrightNeonGreen
import com.example.ui.theme.ChampionshipGold
import com.example.ui.theme.DarkPitchSurface
import com.example.ui.theme.DarkTurfGreen
import com.example.ui.theme.DeepStadiumGreen
import com.example.ui.theme.FootballLifeTheme
import com.example.ui.theme.MatchLossRed
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PrimaryWhite
import com.example.ui.theme.SecondaryMintGray
import com.example.ui.theme.StrokePremium
import com.example.ui.theme.StrokePremiumStrong
import com.example.ui.theme.SurfacePrimary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VibrantFootballGreen
import com.example.viewmodel.FootballLifeViewModel
import com.example.viewmodel.HubOverlayScreen
import com.example.viewmodel.HubTab
import com.example.viewmodel.RootScreen
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FootballLifeTheme {
                FootballLifeApp()
            }
        }
    }
}

@Composable
fun FootballLifeApp(
    viewModel: FootballLifeViewModel = viewModel(
        factory = FootballLifeViewModel.Factory(LocalContext.current.applicationContext)
    )
) {
    val rootScreen by viewModel.rootScreen.collectAsStateWithLifecycle()
    val activeHubTab by viewModel.activeHubTab.collectAsStateWithLifecycle()
    val overlayScreen by viewModel.overlayScreen.collectAsStateWithLifecycle()
    val saveSummaries by viewModel.saveSummaries.collectAsStateWithLifecycle()
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
    val targetSlot by viewModel.targetSlotForNewCareer.collectAsStateWithLifecycle()
    val customDraft by viewModel.customDraft.collectAsStateWithLifecycle()
    val selectedLeagueId by viewModel.selectedLeagueIdForSetup.collectAsStateWithLifecycle()
    val activeCareer by viewModel.activeCareer.collectAsStateWithLifecycle()
    val liveMatch by viewModel.liveMatchState.collectAsStateWithLifecycle()
    val toastBanner by viewModel.toastBanner.collectAsStateWithLifecycle()

    BackHandler(enabled = rootScreen != RootScreen.MAIN_MENU && rootScreen != RootScreen.INTRO_SPLASH) {
        viewModel.handleBackNavigation()
    }

    LaunchedEffect(toastBanner) {
        if (toastBanner != null) {
            delay(3200L)
            viewModel.dismissToast()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (rootScreen) {
            RootScreen.INTRO_SPLASH -> {
                IntroSplashScreen(
                    skipAutomatically = userSettings.skipIntroAnimation,
                    onIntroFinished = { viewModel.completeIntroSplash() }
                )
            }

            RootScreen.MAIN_MENU -> {
                MainMenuScreen(
                    saveSummaries = saveSummaries,
                    settings = userSettings,
                    onStartNewCareer = { viewModel.startNewCareerFlow() },
                    onContinueCareer = { viewModel.continueMostRecentCareer() },
                    onOpenSaveSlots = { viewModel.navigateToRoot(RootScreen.SAVE_SLOTS) },
                    onOpenSettings = { viewModel.navigateToRoot(RootScreen.SETTINGS_MENU) }
                )
            }

            RootScreen.SAVE_SLOTS -> {
                SaveSlotsScreen(
                    saveSummaries = saveSummaries,
                    onBack = { viewModel.navigateToRoot(RootScreen.MAIN_MENU) },
                    onLoadSlot = { viewModel.loadCareerSlot(it) },
                    onCreateInSlot = { viewModel.startNewCareerFlow(it) },
                    onDeleteSlot = { viewModel.deleteCareerSlot(it) }
                )
            }

            RootScreen.SETTINGS_MENU -> {
                SettingsMenuScreen(
                    settings = userSettings,
                    onBack = { viewModel.navigateToRoot(RootScreen.MAIN_MENU) },
                    onToggleAmbience = { viewModel.setAmbienceEnabled(it) },
                    onToggleSfx = { viewModel.setSfxEnabled(it) },
                    onToggleHaptics = { viewModel.setHapticsEnabled(it) },
                    onSelectGraphics = { viewModel.setGraphicsQuality(it) },
                    onToggleSkipIntro = { viewModel.setSkipIntro(it) },
                    onSaveAccountName = { viewModel.setLocalAccountName(it) }
                )
            }

            RootScreen.CAREER_PATH_CHOICE -> {
                CareerPathChoiceScreen(
                    slotId = targetSlot,
                    onBack = { viewModel.navigateToRoot(RootScreen.MAIN_MENU) },
                    onSelectRealPlayerPath = { viewModel.navigateToRoot(RootScreen.REAL_PLAYER_PICKER) },
                    onSelectCustomPlayerPath = { viewModel.navigateToRoot(RootScreen.CUSTOM_PLAYER_CREATOR) }
                )
            }

            RootScreen.REAL_PLAYER_PICKER -> {
                RealPlayerPickerScreen(
                    onBack = { viewModel.navigateToRoot(RootScreen.CAREER_PATH_CHOICE) },
                    onStartAtCurrentClub = { preset ->
                        viewModel.chooseRealPlayerPreset(preset, startImmediatelyAtCurrentClub = true)
                    },
                    onPickCustomClubForPreset = { preset ->
                        viewModel.chooseRealPlayerPreset(preset, startImmediatelyAtCurrentClub = false)
                    }
                )
            }

            RootScreen.CUSTOM_PLAYER_CREATOR -> {
                CustomPlayerCreatorScreen(
                    draft = customDraft,
                    onUpdateDraft = { viewModel.updateCustomDraft(it) },
                    onBack = { viewModel.navigateToRoot(RootScreen.CAREER_PATH_CHOICE) },
                    onProceedToClubSelect = { viewModel.proceedFromCustomCreatorToClubPicker() }
                )
            }

            RootScreen.LEAGUE_AND_CLUB_PICKER -> {
                LeagueAndClubPickerScreen(
                    selectedLeagueId = selectedLeagueId,
                    onSelectLeague = { viewModel.selectLeagueForSetup(it) },
                    onSignWithClub = { leagueId, clubId ->
                        viewModel.finalizeNewCareerWithClub(leagueId, clubId)
                    },
                    onBack = { viewModel.handleBackNavigation() }
                )
            }

            RootScreen.CAREER_HUB_SHELL -> {
                val career = activeCareer
                if (career == null) {
                    viewModel.navigateToRoot(RootScreen.MAIN_MENU)
                } else {
                    Scaffold(
                        contentWindowInsets = WindowInsets.safeDrawing,
                        containerColor = BackgroundPrimary,
                        bottomBar = {
                            Column {
                                HorizontalDivider(color = StrokePremiumStrong, thickness = 1.dp)
                                NavigationBar(
                                    containerColor = SurfacePrimary,
                                    contentColor = TextPrimary,
                                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                                ) {
                                    HubTab.entries.forEach { tab ->
                                        val selected = activeHubTab == tab && overlayScreen == HubOverlayScreen.NONE
                                        val icon = when (tab) {
                                            HubTab.HAYATIM -> Icons.Default.Person
                                            HubTab.TAKVIM -> Icons.Default.CalendarMonth
                                            HubTab.LIG -> Icons.Default.SportsSoccer
                                            HubTab.KARIYER -> Icons.Default.EmojiEvents
                                            HubTab.TELEFON -> Icons.Default.Smartphone
                                        }
                                        val unread = if (tab == HubTab.TELEFON) career.totalUnreadPhoneItems else 0
                                        val matchTodayBadge = (tab == HubTab.HAYATIM || tab == HubTab.TAKVIM) && career.todayUserFixture != null

                                        NavigationBarItem(
                                            selected = selected,
                                            onClick = { viewModel.selectHubTab(tab) },
                                            icon = {
                                                BadgedBox(
                                                    badge = {
                                                        if (unread > 0) {
                                                            Badge(containerColor = MatchLossRed) {
                                                                Text(unread.toString())
                                                            }
                                                        } else if (matchTodayBadge) {
                                                            Badge(containerColor = AccentBright) {
                                                                Text("!")
                                                            }
                                                        }
                                                    }
                                                ) {
                                                    Icon(icon, contentDescription = tab.labelTr)
                                                }
                                            },
                                            label = {
                                                Text(
                                                    text = tab.labelTr,
                                                    fontFamily = OutfitFontFamily,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold
                                                )
                                            },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = BackgroundPrimary,
                                                selectedTextColor = AccentBright,
                                                indicatorColor = AccentPrimary,
                                                unselectedIconColor = TextSecondary,
                                                unselectedTextColor = TextSecondary
                                            ),
                                            modifier = Modifier.testTag("bottom_nav_${tab.name}")
                                        )
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (overlayScreen) {
                                HubOverlayScreen.LIVE_MATCH_FLOW -> {
                                    MatchCenterScreen(
                                        career = career,
                                        liveMatch = liveMatch,
                                        graphicsQuality = userSettings.graphicsQuality,
                                        onPrepareMatch = { viewModel.prepareMatchForTodayOrNext() },
                                        onStartKickOff = { viewModel.startLiveMatchKickOff() },
                                        onResumeSecondHalf = { viewModel.startSecondHalf() },
                                        onChangeSpeed = { viewModel.setMatchSpeed(it) },
                                        onChooseDecisionOption = { viewModel.selectCriticalMatchOption(it) },
                                        onFinishMatch = { viewModel.finishMatchAndCommitResults() }
                                    )
                                }

                                HubOverlayScreen.TRAINING_CENTER -> {
                                    TrainingCenterScreen(
                                        career = career,
                                        onSelectDrill = { viewModel.runTrainingDrill(it) },
                                        onBack = { viewModel.closeOverlay() }
                                    )
                                }

                                HubOverlayScreen.SOCIAL_LIFE_VENUES -> {
                                    SocialLifeAndStoreScreen(
                                        career = career,
                                        onExecuteVenueAction = { viewModel.performSocialAction(it) },
                                        onBuyItem = { viewModel.buyLifestyleItem(it) },
                                        onBack = { viewModel.closeOverlay() }
                                    )
                                }

                                HubOverlayScreen.TRANSFER_AND_CONTRACT -> {
                                    TransferAndContractScreen(
                                        career = career,
                                        onAskAgentForOffers = { viewModel.askAgentForTransferOffers() },
                                        onNegotiateOffer = { viewModel.negotiateOffer(it) },
                                        onAcceptOffer = { viewModel.acceptTransferOffer(it) },
                                        onBack = { viewModel.closeOverlay() }
                                    )
                                }

                                HubOverlayScreen.PLAYER_PROFILE_ARCHIVE -> {
                                    PlayerProfileArchiveScreen(
                                        career = career,
                                        onBack = { viewModel.closeOverlay() }
                                    )
                                }

                                HubOverlayScreen.POST_MATCH_LOCKER_ROOM -> {
                                    PostMatchLockerRoomScreen(
                                        career = career,
                                        onSubmitPressStatement = { text, tone ->
                                            viewModel.submitPressOrSocialStatement(text, tone)
                                        },
                                        onContinueToCareerHub = {
                                            viewModel.closeOverlay()
                                            viewModel.selectHubTab(HubTab.HAYATIM)
                                        }
                                    )
                                }

                                HubOverlayScreen.NONE -> {
                                    when (activeHubTab) {
                                        HubTab.HAYATIM -> {
                                            PlayerHubScreen(
                                                career = career,
                                                onOpenTraining = { viewModel.openOverlay(HubOverlayScreen.TRAINING_CENTER) },
                                                onOpenSocialLife = { viewModel.openOverlay(HubOverlayScreen.SOCIAL_LIFE_VENUES) },
                                                onOpenTransfers = { viewModel.openOverlay(HubOverlayScreen.TRANSFER_AND_CONTRACT) },
                                                onOpenProfileCard = { viewModel.selectHubTab(HubTab.KARIYER) },
                                                onOpenPhone = { viewModel.selectHubTab(HubTab.TELEFON) },
                                                onOpenLeague = { viewModel.selectHubTab(HubTab.LIG) },
                                                onPlayNextMatch = { viewModel.prepareMatchForTodayOrNext() },
                                                onAdvanceNextDay = { viewModel.advanceSynchronizedTime() },
                                                onAdvanceToMatchDay = { viewModel.advanceUntilNextMatchDay() },
                                                onResolveDilemma = { viewModel.resolveStoryDilemma(it) }
                                            )
                                        }

                                        HubTab.TAKVIM -> {
                                            CalendarScreen(
                                                career = career,
                                                onAdvanceNextDay = { viewModel.advanceDay() },
                                                onAdvanceToMatchDay = { viewModel.advanceUntilNextMatchDay() },
                                                onPlayMatchToday = { viewModel.prepareMatchForTodayOrNext() }
                                            )
                                        }

                                        HubTab.LIG -> {
                                            LeagueAndWorldScreen(
                                                career = career,
                                                onOpenTransfers = { viewModel.openOverlay(HubOverlayScreen.TRANSFER_AND_CONTRACT) },
                                                onOpenProfileArchive = { viewModel.selectHubTab(HubTab.KARIYER) },
                                                onPlayUserMatch = { viewModel.prepareMatchForTodayOrNext() }
                                            )
                                        }

                                        HubTab.KARIYER -> {
                                            CareerArchiveScreen(
                                                state = career,
                                                onRequestAgentTransferSearch = { viewModel.askAgentForTransferOffers() },
                                                onNegotiateOffer = { viewModel.negotiateOffer(it) },
                                                onAcceptOffer = { viewModel.acceptTransferOffer(it) },
                                                onExtendContract = { viewModel.extendCurrentContract() }
                                            )
                                        }

                                        HubTab.TELEFON -> {
                                            PhoneOsScreen(
                                                state = career,
                                                userSettings = userSettings,
                                                onSendMessageToNpc = { npcId, msg ->
                                                    viewModel.sendNpcMessage(npcId, msg)
                                                },
                                                onPublishSocialOrPress = { text, tone ->
                                                    viewModel.submitPressOrSocialStatement(text, tone)
                                                },
                                                onToggleLikeInsta = { postId ->
                                                    viewModel.toggleLikeInstaPost(postId)
                                                },
                                                onToggleSaveInsta = { postId ->
                                                    viewModel.toggleSaveInstaPost(postId)
                                                },
                                                onCommentInsta = { postId, comment ->
                                                    viewModel.commentOnInstaPost(postId, comment)
                                                },
                                                onToggleLikeInstaComment = { postId, commentId ->
                                                    viewModel.toggleLikeInstaComment(postId, commentId)
                                                },
                                                onAcceptInstaDmRequest = { threadId ->
                                                    viewModel.acceptInstaDmRequest(threadId)
                                                },
                                                onDeleteInstaDmRequest = { threadId ->
                                                    viewModel.deleteInstaDmRequest(threadId)
                                                },
                                                onSendInstaDmMessage = { threadId, text ->
                                                    viewModel.sendInstaDmMessage(threadId, text)
                                                },
                                                onViewReactStory = { storyId, react ->
                                                    viewModel.viewAndReactToStory(storyId, react)
                                                },
                                                onSharePhotoToInsta = { photoId, caption ->
                                                    viewModel.shareGalleryPhotoToInsta(photoId, caption)
                                                },
                                                onCaptureCameraPhoto = { sceneType, album, title, caption ->
                                                    viewModel.captureCameraSceneToGallery(sceneType, album, title, caption)
                                                },
                                                onToggleLikeXPost = { xPostId ->
                                                    viewModel.toggleLikeXPost(xPostId)
                                                },
                                                onToggleRepostXPost = { xPostId ->
                                                    viewModel.toggleRepostXPost(xPostId)
                                                },
                                                onPublishXPost = { content ->
                                                    viewModel.publishUserXPost(content)
                                                },
                                                onCompleteCall = { callId, option ->
                                                    viewModel.completePhoneCall(callId, option)
                                                },
                                                onInitiateCallWithNpc = { npcId ->
                                                    viewModel.initiateCallWithNpc(npcId)
                                                },
                                                onPurchaseItem = { item ->
                                                    viewModel.buyLifestyleItem(item)
                                                },
                                                onChangeWallpaper = { wallpaperId ->
                                                    viewModel.updatePhoneWallpaper(wallpaperId)
                                                },
                                                onUpdateAmbience = { viewModel.setAmbienceEnabled(it) },
                                                onUpdateSfx = { viewModel.setSfxEnabled(it) },
                                                onUpdateHaptics = { viewModel.setHapticsEnabled(it) },
                                                onUpdateGraphics = { viewModel.setGraphicsQuality(it) },
                                                onBackToHub = { viewModel.selectHubTab(HubTab.HAYATIM) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Toast Notification Banner
        AnimatedVisibility(
            visible = toastBanner != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Surface(
                color = DarkTurfGreen,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, ChampionshipGold),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.dismissToast() }
            ) {
                Text(
                    text = toastBanner ?: "",
                    color = PrimaryWhite,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }
}
