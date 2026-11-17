package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.audio.GameSoundEffect
import com.example.audio.SoundAndHapticsManager
import com.example.data.CareerSaveRepository
import com.example.data.CareerSaveSummary
import com.example.data.FootballLifeDatabase
import com.example.data.SettingsRepository
import com.example.data.UserSettings
import com.example.engine.CareerEngine
import com.example.engine.LiveMatchUiState
import com.example.engine.MatchPhase
import com.example.engine.MatchSimulationEngine
import com.example.engine.RealPlayerPreset
import com.example.engine.TacticalDecisionOption
import com.example.engine.TrainingDrillDefinition
import com.example.engine.WorldDataCatalog
import com.example.model.CareerSaveState
import com.example.model.DynamicStoryChoice
import com.example.model.GraphicsQuality
import com.example.model.LifestylePurchaseItem
import com.example.model.LifestyleVenueAction
import com.example.model.PlayerAppearance
import com.example.model.Position
import com.example.model.PreferredFoot
import com.example.model.TransferOffer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class RootScreen {
    INTRO_SPLASH,
    MAIN_MENU,
    SAVE_SLOTS,
    SETTINGS_MENU,
    CAREER_PATH_CHOICE,
    REAL_PLAYER_PICKER,
    CUSTOM_PLAYER_CREATOR,
    LEAGUE_AND_CLUB_PICKER,
    CAREER_HUB_SHELL
}

enum class HubTab(val labelTr: String) {
    HAYATIM("Hayatım"),
    TAKVIM("Takvim"),
    LIG("Lig"),
    KARIYER("Kariyer"),
    TELEFON("Telefon")
}

enum class HubOverlayScreen {
    NONE,
    LIVE_MATCH_FLOW,
    TRAINING_CENTER,
    SOCIAL_LIFE_VENUES,
    TRANSFER_AND_CONTRACT,
    PLAYER_PROFILE_ARCHIVE,
    POST_MATCH_LOCKER_ROOM,
    COACH_MEETING,
    ACTIVE_PHONE_CALL
}

data class CustomPlayerDraft(
    val firstName: String = "Arda",
    val lastName: String = "Kaya",
    val birthYear: Int = 2006,
    val country: String = "Türkiye",
    val city: String = "İstanbul",
    val nationality: String = "Türkiye",
    val kitName: String = "KAYA",
    val kitNumber: Int = 10,
    val primaryPosition: Position = Position.AM,
    val secondaryPositions: List<Position> = listOf(Position.WG, Position.CM),
    val preferredFoot: PreferredFoot = PreferredFoot.RIGHT,
    val heightCm: Int = 179,
    val weightKg: Int = 73,
    val appearance: PlayerAppearance = PlayerAppearance(),
    val backgroundStory: String = "Altyapı Akademisi Çıkışlı"
)

class FootballLifeViewModel(
    private val saveRepository: CareerSaveRepository,
    private val settingsRepository: SettingsRepository,
    private val soundManager: SoundAndHapticsManager
) : ViewModel() {

    private val matchEngine = MatchSimulationEngine()
    private var matchLoopJob: Job? = null

    val saveSummaries: StateFlow<List<CareerSaveSummary>> = saveRepository.saveSummariesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userSettings: StateFlow<UserSettings> = settingsRepository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    private val _rootScreen = MutableStateFlow(RootScreen.INTRO_SPLASH)
    val rootScreen: StateFlow<RootScreen> = _rootScreen.asStateFlow()

    private val _activeHubTab = MutableStateFlow(HubTab.HAYATIM)
    val activeHubTab: StateFlow<HubTab> = _activeHubTab.asStateFlow()

    private val _overlayScreen = MutableStateFlow(HubOverlayScreen.NONE)
    val overlayScreen: StateFlow<HubOverlayScreen> = _overlayScreen.asStateFlow()

    private val _targetSlotForNewCareer = MutableStateFlow(1)
    val targetSlotForNewCareer: StateFlow<Int> = _targetSlotForNewCareer.asStateFlow()

    private val _selectedRealPreset = MutableStateFlow<RealPlayerPreset?>(null)
    val selectedRealPreset: StateFlow<RealPlayerPreset?> = _selectedRealPreset.asStateFlow()

    private val _customDraft = MutableStateFlow(CustomPlayerDraft())
    val customDraft: StateFlow<CustomPlayerDraft> = _customDraft.asStateFlow()

    private val _selectedLeagueIdForSetup = MutableStateFlow("TR_BAL")
    val selectedLeagueIdForSetup: StateFlow<String> = _selectedLeagueIdForSetup.asStateFlow()

    private val _activeCareer = MutableStateFlow<CareerSaveState?>(null)
    val activeCareer: StateFlow<CareerSaveState?> = _activeCareer.asStateFlow()

    private val _liveMatchState = MutableStateFlow<LiveMatchUiState?>(null)
    val liveMatchState: StateFlow<LiveMatchUiState?> = _liveMatchState.asStateFlow()

    private val _toastBanner = MutableStateFlow<String?>(null)
    val toastBanner: StateFlow<String?> = _toastBanner.asStateFlow()

    private val _aiTypingTargetId = MutableStateFlow<String?>(null)
    val aiTypingTargetId: StateFlow<String?> = _aiTypingTargetId.asStateFlow()

    private val _aiConnectionStatus = MutableStateFlow<String?>(null)
    val aiConnectionStatus: StateFlow<String?> = _aiConnectionStatus.asStateFlow()

    private val _activePhoneCallId = MutableStateFlow<String?>(null)
    val activePhoneCallId: StateFlow<String?> = _activePhoneCallId.asStateFlow()

    private var returnToHubAfterSettings: Boolean = false

    fun openSettingsFromHub() {
        playSfx(GameSoundEffect.MENU_TAP)
        returnToHubAfterSettings = true
        _rootScreen.value = RootScreen.SETTINGS_MENU
    }

    fun closeSettingsScreen() {
        playSfx(GameSoundEffect.MENU_TAP)
        if (returnToHubAfterSettings && _activeCareer.value != null) {
            returnToHubAfterSettings = false
            _rootScreen.value = RootScreen.CAREER_HUB_SHELL
        } else {
            returnToHubAfterSettings = false
            _rootScreen.value = RootScreen.MAIN_MENU
        }
    }

    fun dismissToast() {
        _toastBanner.value = null
    }

    fun showToast(message: String) {
        if (message.isNotBlank()) {
            _toastBanner.value = message
        }
    }

    fun playSfx(effect: GameSoundEffect) {
        val cfg = userSettings.value
        soundManager.playEffect(effect, cfg.soundEffectsEnabled, cfg.hapticsEnabled)
    }

    fun completeIntroSplash() {
        _rootScreen.value = RootScreen.MAIN_MENU
    }

    fun navigateToRoot(screen: RootScreen) {
        playSfx(GameSoundEffect.MENU_TAP)
        _rootScreen.value = screen
    }

    fun selectHubTab(tab: HubTab) {
        playSfx(GameSoundEffect.MENU_TAP)
        _overlayScreen.value = HubOverlayScreen.NONE
        _activeHubTab.value = tab
    }

    fun openOverlay(overlay: HubOverlayScreen) {
        playSfx(GameSoundEffect.MENU_TAP)
        _overlayScreen.value = overlay
    }

    fun closeOverlay() {
        playSfx(GameSoundEffect.MENU_TAP)
        _overlayScreen.value = HubOverlayScreen.NONE
    }

    fun handleBackNavigation(): Boolean {
        if (_rootScreen.value == RootScreen.CAREER_HUB_SHELL) {
            if (_overlayScreen.value != HubOverlayScreen.NONE) {
                _overlayScreen.value = HubOverlayScreen.NONE
                return true
            }
            if (_activeHubTab.value != HubTab.HAYATIM) {
                _activeHubTab.value = HubTab.HAYATIM
                return true
            }
            _rootScreen.value = RootScreen.MAIN_MENU
            return true
        }
        return when (_rootScreen.value) {
            RootScreen.SETTINGS_MENU -> {
                closeSettingsScreen()
                true
            }
            RootScreen.SAVE_SLOTS,
            RootScreen.CAREER_PATH_CHOICE -> {
                _rootScreen.value = RootScreen.MAIN_MENU
                true
            }
            RootScreen.REAL_PLAYER_PICKER,
            RootScreen.CUSTOM_PLAYER_CREATOR -> {
                _rootScreen.value = RootScreen.CAREER_PATH_CHOICE
                true
            }
            RootScreen.LEAGUE_AND_CLUB_PICKER -> {
                _rootScreen.value = if (_selectedRealPreset.value != null) {
                    RootScreen.REAL_PLAYER_PICKER
                } else {
                    RootScreen.CUSTOM_PLAYER_CREATOR
                }
                true
            }
            else -> false
        }
    }

    // Career Slot Operations
    fun startNewCareerFlow(preferredSlotId: Int? = null) {
        playSfx(GameSoundEffect.MENU_TAP)
        val occupied = saveSummaries.value.map { it.slotId }.toSet()
        val chosenSlot = preferredSlotId ?: (1..3).firstOrNull { it !in occupied } ?: 1
        _targetSlotForNewCareer.value = chosenSlot
        _selectedRealPreset.value = null
        _rootScreen.value = RootScreen.CAREER_PATH_CHOICE
    }

    fun continueMostRecentCareer() {
        playSfx(GameSoundEffect.MENU_TAP)
        viewModelScope.launch {
            val loaded = saveRepository.loadMostRecentCareer()
            if (loaded != null) {
                val enriched = com.example.ai.DialogueEngine.ensureExtendedNpcs(loaded)
                _activeCareer.value = enriched
                _activeHubTab.value = HubTab.HAYATIM
                _overlayScreen.value = HubOverlayScreen.NONE
                _rootScreen.value = RootScreen.CAREER_HUB_SHELL
                showToast("${enriched.player.fullName} kariyerine kaldığın yerden devam ediliyor.")
            } else {
                showToast("Kayıtlı kariyer bulunamadı. Yeni bir kariyer başlatın.")
            }
        }
    }

    fun loadCareerSlot(slotId: Int) {
        playSfx(GameSoundEffect.MENU_TAP)
        viewModelScope.launch {
            val loaded = saveRepository.loadCareerSlot(slotId)
            if (loaded != null) {
                settingsRepository.setLastActiveSlot(slotId)
                val enriched = com.example.ai.DialogueEngine.ensureExtendedNpcs(loaded)
                _activeCareer.value = enriched
                _activeHubTab.value = HubTab.HAYATIM
                _overlayScreen.value = HubOverlayScreen.NONE
                _rootScreen.value = RootScreen.CAREER_HUB_SHELL
                showToast("Kayıt Yuvası $slotId yüklendi: ${enriched.player.fullName}")
            }
        }
    }

    fun deleteCareerSlot(slotId: Int) {
        playSfx(GameSoundEffect.MENU_TAP)
        viewModelScope.launch {
            saveRepository.deleteCareerSlot(slotId)
            if (_activeCareer.value?.saveSlotId == slotId) {
                _activeCareer.value = null
            }
            showToast("Kayıt Yuvası $slotId silindi.")
        }
    }

    // Setup & Player Creation
    fun updateCustomDraft(transform: (CustomPlayerDraft) -> CustomPlayerDraft) {
        _customDraft.update(transform)
    }

    fun chooseRealPlayerPreset(preset: RealPlayerPreset, startImmediatelyAtCurrentClub: Boolean) {
        playSfx(GameSoundEffect.MENU_TAP)
        _selectedRealPreset.value = preset
        _selectedLeagueIdForSetup.value = preset.defaultLeagueId
        if (startImmediatelyAtCurrentClub) {
            finalizeNewCareerWithClub(preset.defaultLeagueId, preset.defaultClubId)
        } else {
            _rootScreen.value = RootScreen.LEAGUE_AND_CLUB_PICKER
        }
    }

    fun proceedFromCustomCreatorToClubPicker() {
        playSfx(GameSoundEffect.MENU_TAP)
        _selectedRealPreset.value = null
        _rootScreen.value = RootScreen.LEAGUE_AND_CLUB_PICKER
    }

    fun selectLeagueForSetup(leagueId: String) {
        playSfx(GameSoundEffect.MENU_TAP)
        _selectedLeagueIdForSetup.value = leagueId
    }

    fun finalizeNewCareerWithClub(leagueId: String, clubId: String) {
        playSfx(GameSoundEffect.TRAINING_LEVEL_UP)
        val slotId = _targetSlotForNewCareer.value
        val preset = _selectedRealPreset.value
        val draft = _customDraft.value

        val newState = if (preset != null) {
            CareerEngine.createNewCareerFromRealPreset(
                slotId = slotId,
                preset = preset,
                overrideLeagueId = leagueId,
                overrideClubId = clubId
            )
        } else {
            CareerEngine.createNewCareerFromCustomPlayer(
                slotId = slotId,
                firstName = draft.firstName,
                lastName = draft.lastName,
                birthYear = draft.birthYear,
                country = draft.country,
                city = draft.city,
                nationality = draft.nationality,
                kitName = draft.kitName,
                kitNumber = draft.kitNumber,
                primaryPosition = draft.primaryPosition,
                secondaryPositions = draft.secondaryPositions,
                preferredFoot = draft.preferredFoot,
                heightCm = draft.heightCm,
                weightKg = draft.weightKg,
                appearance = draft.appearance,
                backgroundStory = draft.backgroundStory,
                selectedLeagueId = leagueId,
                selectedClubId = clubId
            )
        }

        val enrichedNewState = com.example.ai.DialogueEngine.ensureExtendedNpcs(newState)
        _activeCareer.value = enrichedNewState
        _liveMatchState.value = null
        _activeHubTab.value = HubTab.HAYATIM
        _overlayScreen.value = HubOverlayScreen.NONE
        _rootScreen.value = RootScreen.CAREER_HUB_SHELL

        persistCareerState(enrichedNewState)
        showToast("${enrichedNewState.player.fullName}, ${enrichedNewState.currentClub.name} tesislerine katıldı! İlk günün başladı.")
    }

    // Core Gameplay Actions
    fun runTrainingDrill(drill: TrainingDrillDefinition) {
        val current = _activeCareer.value ?: return
        val (updated, feedback) = CareerEngine.performTrainingSession(current, drill)
        if (updated != current) {
            playSfx(GameSoundEffect.TRAINING_LEVEL_UP)
            val synced = updated.copy(currentHour = (updated.currentHour + 4).coerceAtMost(21))
            _activeCareer.value = synced
            persistCareerState(synced)
        } else {
            playSfx(GameSoundEffect.MENU_TAP)
        }
        showToast(feedback)
    }

    fun advanceDay() {
        val current = _activeCareer.value ?: return
        val (updated, feedback) = CareerEngine.advanceToNextDay(current)
        if (updated != current) {
            playSfx(GameSoundEffect.MENU_TAP)
            val resetClock = updated.copy(currentHour = 9)
            _activeCareer.value = resetClock
            persistCareerState(resetClock)
        }
        showToast(feedback)
    }

    fun advanceSynchronizedTime() {
        val current = _activeCareer.value ?: return
        val (updated, feedback) = CareerEngine.advanceSynchronizedTime(current)
        if (updated != current) {
            playSfx(GameSoundEffect.MENU_TAP)
            _activeCareer.value = updated
            persistCareerState(updated)
        }
        showToast(feedback)
    }

    fun advanceUntilNextMatchDay() {
        var current = _activeCareer.value ?: return
        var safetySteps = 0
        while (current.todayUserFixture == null && safetySteps < 6) {
            val (next, _) = CareerEngine.advanceToNextDay(current)
            if (next == current) break
            current = next
            safetySteps++
        }
        playSfx(GameSoundEffect.MENU_TAP)
        val synced = current.copy(currentHour = 17)
        _activeCareer.value = synced
        persistCareerState(synced)
        val match = synced.todayUserFixture
        if (match != null) {
            showToast("Maç gününe gelindi: ${match.homeClubName} vs ${match.awayClubName}!")
        } else {
            showToast("Takvim ilerletildi: ${CareerEngine.formatGameDate(synced.currentDayIndex)}")
        }
    }

    fun performSocialAction(action: LifestyleVenueAction) {
        val current = _activeCareer.value ?: return
        val (updated, msg) = CareerEngine.executeSocialVenueAction(current, action)
        if (updated != current) {
            playSfx(GameSoundEffect.TRAINING_LEVEL_UP)
            val synced = updated.copy(currentHour = (updated.currentHour + 3).coerceAtMost(21))
            _activeCareer.value = synced
            persistCareerState(synced)
        } else {
            playSfx(GameSoundEffect.MENU_TAP)
        }
        showToast(msg)
    }

    fun buyLifestyleItem(item: LifestylePurchaseItem) {
        val current = _activeCareer.value ?: return
        val (updated, msg) = CareerEngine.purchaseLifestyleItem(current, item)
        if (updated != current) {
            playSfx(GameSoundEffect.TRANSFER_OFFER)
            _activeCareer.value = updated
            persistCareerState(updated)
        } else {
            playSfx(GameSoundEffect.MENU_TAP)
        }
        showToast(msg)
    }

    fun sendNpcMessage(
        npcId: String,
        text: String,
        channel: com.example.ai.DialogueChannelType = com.example.ai.DialogueChannelType.WHATSAPP_CHAT,
        topicContext: String? = null
    ) {
        val stateBefore = _activeCareer.value ?: return
        val clean = text.trim()
        if (clean.isEmpty()) return

        // 1. Optimistic UI update so user bubble appears immediately
        val (optimisticState, _) = com.example.ai.DialogueEngine.appendOptimisticUserNpcMessage(stateBefore, npcId, clean)
        _activeCareer.value = optimisticState
        playSfx(GameSoundEffect.PHONE_MESSAGE)

        val cfg = userSettings.value
        _aiTypingTargetId.value = npcId

        viewModelScope.launch {
            try {
                val result = com.example.ai.DialogueEngine.generateAndApplyNpcDialogue(
                    stateBeforeUserMsg = stateBefore,
                    currentStateWithUserMsg = _activeCareer.value ?: optimisticState,
                    npcId = npcId,
                    userText = clean,
                    channel = channel,
                    topicContext = topicContext,
                    apiKeyInput = cfg.geminiApiKey,
                    modelId = cfg.geminiModel,
                    aiEnabled = cfg.aiDialoguesEnabled
                )
                playSfx(GameSoundEffect.PHONE_MESSAGE)
                _activeCareer.value = result.updatedState
                persistCareerState(result.updatedState)
                result.toastSummary?.let { showToast(it) }
            } finally {
                if (_aiTypingTargetId.value == npcId) {
                    _aiTypingTargetId.value = null
                }
            }
        }
    }

    fun sendCoachMeetingMessage(text: String) {
        sendNpcMessage(
            npcId = "NPC_MANAGER",
            text = text,
            channel = com.example.ai.DialogueChannelType.COACH_OFFICE_MEETING,
            topicContext = "Teknik Direktör Odasında Yüz Yüze Görüşme"
        )
    }

    fun markNpcMessagesRead(npcId: String) {
        val current = _activeCareer.value ?: return
        val updatedNpcs = current.npcs.map {
            if (it.id == npcId) it.copy(unreadCount = 0) else it
        }
        val updated = current.copy(npcs = updatedNpcs)
        _activeCareer.value = updated
        persistCareerState(updated)
    }

    fun submitPressOrSocialStatement(text: String, tone: String) {
        val current = _activeCareer.value ?: return
        val (updated, msg) = CareerEngine.publishPressStatementOrSocialPost(current, text, tone)
        playSfx(GameSoundEffect.PHONE_MESSAGE)
        _activeCareer.value = updated
        persistCareerState(updated)
        showToast(msg)
    }

    fun completePostMatchInterview(result: com.example.engine.interview.InterviewSessionResult) {
        val current = _activeCareer.value ?: return
        val (updated, msg) = com.example.engine.interview.InterviewEngine.applyInterviewSessionResult(current, result)
        playSfx(GameSoundEffect.PHONE_MESSAGE)
        _activeCareer.value = updated
        persistCareerState(updated)
        showToast(msg)
    }

    fun resolveStoryDilemma(choice: DynamicStoryChoice) {
        val current = _activeCareer.value ?: return
        val (updated, msg) = CareerEngine.resolveDynamicDilemma(current, choice)
        playSfx(GameSoundEffect.TRAINING_LEVEL_UP)
        _activeCareer.value = updated
        persistCareerState(updated)
        showToast(msg)
    }

    fun negotiateOffer(offerId: String) {
        val current = _activeCareer.value ?: return
        val (updated, msg) = CareerEngine.negotiateTransferOffer(current, offerId)
        playSfx(GameSoundEffect.TRANSFER_OFFER)
        _activeCareer.value = updated
        persistCareerState(updated)
        showToast(msg)
    }

    fun acceptTransferOffer(offer: TransferOffer) {
        val current = _activeCareer.value ?: return
        val (updated, msg) = CareerEngine.acceptTransferOffer(current, offer)
        playSfx(GameSoundEffect.GOAL_CELEBRATION)
        _activeCareer.value = updated
        persistCareerState(updated)
        showToast(msg)
    }

    fun askAgentForTransferOffers() {
        val current = _activeCareer.value ?: return
        val (updated, msg) = CareerEngine.requestTransferSearchFromAgent(current)
        playSfx(GameSoundEffect.PHONE_MESSAGE)
        _activeCareer.value = updated
        persistCareerState(updated)
        showToast(msg)
    }

    // 2D Hybrid Match Lifecycle
    fun prepareMatchForTodayOrNext() {
        val current = _activeCareer.value ?: return
        val fixture = current.todayUserFixture ?: current.nextUnplayedUserFixture ?: return

        // If the user clicks Play Match from the Match tab and isn't on match day yet, advance to match day first
        val readyCareer = if (current.currentDayIndex < fixture.dayIndex) {
            var temp = current
            while (temp.currentDayIndex < fixture.dayIndex) {
                val (stepped, _) = CareerEngine.advanceToNextDay(temp)
                if (stepped == temp) break
                temp = stepped
            }
            _activeCareer.value = temp
            persistCareerState(temp)
            temp
        } else {
            current
        }

        val targetFixture = readyCareer.todayUserFixture ?: readyCareer.nextUnplayedUserFixture ?: return
        val homeClub = readyCareer.clubs.find { it.id == targetFixture.homeClubId } ?: readyCareer.currentClub
        val awayClub = readyCareer.clubs.find { it.id == targetFixture.awayClubId } ?: readyCareer.clubs.last()

        val initialMatch = matchEngine.createInitialMatchState(
            fixture = targetFixture,
            homeClub = homeClub,
            awayClub = awayClub,
            userPlayer = readyCareer.player,
            userClubId = readyCareer.contract.clubId,
            startingStatus = readyCareer.nextMatchStartingStatus
        )
        _liveMatchState.value = initialMatch
        _overlayScreen.value = HubOverlayScreen.LIVE_MATCH_FLOW
    }

    // PhoneOS 2.0 & Career Archive Interactive Actions
    fun toggleLikeInstaPost(postId: String) {
        val current = _activeCareer.value ?: return
        val updated = CareerEngine.toggleLikeInstaPost(current, postId)
        playSfx(GameSoundEffect.MENU_TAP)
        _activeCareer.value = updated
        persistCareerState(updated)
    }

    fun toggleSaveInstaPost(postId: String) {
        val current = _activeCareer.value ?: return
        val updated = CareerEngine.toggleSaveInstaPost(current, postId)
        playSfx(GameSoundEffect.MENU_TAP)
        _activeCareer.value = updated
        persistCareerState(updated)
    }

    fun toggleLikeInstaComment(postId: String, commentId: String) {
        val current = _activeCareer.value ?: return
        val updated = CareerEngine.toggleLikeInstaComment(current, postId, commentId)
        playSfx(GameSoundEffect.MENU_TAP)
        _activeCareer.value = updated
        persistCareerState(updated)
    }

    fun commentOnInstaPost(postId: String, commentText: String) {
        val current = _activeCareer.value ?: return
        val updated = CareerEngine.commentOnInstaPost(current, postId, commentText)
        playSfx(GameSoundEffect.PHONE_MESSAGE)
        _activeCareer.value = updated
        persistCareerState(updated)
        showToast("Yorumun Instagram gönderisine eklendi.")
    }

    fun acceptInstaDmRequest(threadId: String) {
        val current = _activeCareer.value ?: return
        val (updated, msg) = CareerEngine.acceptInstaDmRequest(current, threadId)
        playSfx(GameSoundEffect.PHONE_MESSAGE)
        _activeCareer.value = updated
        persistCareerState(updated)
        showToast(msg)
    }

    fun deleteInstaDmRequest(threadId: String) {
        val current = _activeCareer.value ?: return
        val (updated, msg) = CareerEngine.deleteInstaDmRequest(current, threadId)
        playSfx(GameSoundEffect.MENU_TAP)
        _activeCareer.value = updated
        persistCareerState(updated)
        showToast(msg)
    }

    fun sendInstaDmMessage(threadId: String, text: String) {
        val stateBefore = _activeCareer.value ?: return
        val clean = text.trim()
        if (clean.isEmpty()) return

        val (optimisticState, _) = com.example.ai.DialogueEngine.appendOptimisticUserInstaDm(stateBefore, threadId, clean)
        _activeCareer.value = optimisticState
        playSfx(GameSoundEffect.PHONE_MESSAGE)

        val cfg = userSettings.value
        _aiTypingTargetId.value = threadId

        viewModelScope.launch {
            try {
                val result = com.example.ai.DialogueEngine.generateAndApplyInstaDmDialogue(
                    stateBeforeUserMsg = stateBefore,
                    currentStateWithUserMsg = _activeCareer.value ?: optimisticState,
                    threadId = threadId,
                    userText = clean,
                    apiKeyInput = cfg.geminiApiKey,
                    modelId = cfg.geminiModel,
                    aiEnabled = cfg.aiDialoguesEnabled
                )
                playSfx(GameSoundEffect.PHONE_MESSAGE)
                _activeCareer.value = result.updatedState
                persistCareerState(result.updatedState)
                result.toastSummary?.let { showToast(it) }
            } finally {
                if (_aiTypingTargetId.value == threadId) {
                    _aiTypingTargetId.value = null
                }
            }
        }
    }

    fun viewAndReactToStory(storyId: String, react: Boolean) {
        val current = _activeCareer.value ?: return
        val updated = CareerEngine.viewAndReactToStory(current, storyId, react)
        playSfx(GameSoundEffect.MENU_TAP)
        _activeCareer.value = updated
        persistCareerState(updated)
        if (react) {
            showToast("Hikayeye beğeni ve tepki gönderildi! (+1 Moral)")
        }
    }

    fun shareGalleryPhotoToInsta(photoId: String, caption: String) {
        val current = _activeCareer.value ?: return
        val (updated, msg) = CareerEngine.shareGalleryPhotoToInsta(current, photoId, caption)
        playSfx(GameSoundEffect.PHONE_MESSAGE)
        _activeCareer.value = updated
        persistCareerState(updated)
        showToast(msg)
    }

    fun captureCameraSceneToGallery(sceneType: String, album: String, title: String, caption: String) {
        val current = _activeCareer.value ?: return
        val (updated, msg) = CareerEngine.captureCameraSceneToGallery(current, sceneType, album, title, caption)
        playSfx(GameSoundEffect.TRAINING_LEVEL_UP)
        _activeCareer.value = updated
        persistCareerState(updated)
        showToast(msg)
    }

    fun toggleLikeXPost(postId: String) {
        val current = _activeCareer.value ?: return
        val updated = CareerEngine.toggleLikeXPost(current, postId)
        playSfx(GameSoundEffect.MENU_TAP)
        _activeCareer.value = updated
        persistCareerState(updated)
    }

    fun toggleRepostXPost(postId: String) {
        val current = _activeCareer.value ?: return
        val updated = CareerEngine.toggleRepostXPost(current, postId)
        playSfx(GameSoundEffect.MENU_TAP)
        _activeCareer.value = updated
        persistCareerState(updated)
    }

    fun publishUserXPost(content: String) {
        val current = _activeCareer.value ?: return
        val (updated, msg) = CareerEngine.publishUserXPost(current, content)
        playSfx(GameSoundEffect.PHONE_MESSAGE)
        _activeCareer.value = updated
        persistCareerState(updated)
        showToast(msg)
    }

    fun completePhoneCall(callId: String, chosenResponse: String) {
        val current = com.example.ai.DialogueEngine.ensureExtendedNpcs(_activeCareer.value ?: return)
        val call = current.callRecords.find { it.id == callId }
        val targetNpcId = call?.npcId ?: "NPC_AGENT"
        val updatedCalls = current.callRecords.map {
            if (it.id == callId) it.copy(completed = true) else it
        }
        val stateWithCallMarked = current.copy(callRecords = updatedCalls)
        _activeCareer.value = stateWithCallMarked
        sendNpcMessage(
            npcId = targetNpcId,
            text = chosenResponse,
            channel = com.example.ai.DialogueChannelType.VOICE_PHONE_CALL,
            topicContext = call?.topic ?: "Telefon Görüşmesi"
        )
    }

    fun initiateCallWithNpc(npcId: String) {
        val current = com.example.ai.DialogueEngine.ensureExtendedNpcs(_activeCareer.value ?: return)
        val (updated, newCall) = CareerEngine.initiateCallWithNpc(current, npcId)
        playSfx(GameSoundEffect.PHONE_MESSAGE)
        _activeCareer.value = updated
        persistCareerState(updated)
        if (newCall != null) {
            _activePhoneCallId.value = newCall.id
            _overlayScreen.value = HubOverlayScreen.ACTIVE_PHONE_CALL
            showToast("${newCall.callerName} aranıyor...")
        }
    }

    fun openInteractiveCallOverlay(callId: String? = null) {
        playSfx(GameSoundEffect.MENU_TAP)
        if (callId != null) {
            _activePhoneCallId.value = callId
        }
        _overlayScreen.value = HubOverlayScreen.ACTIVE_PHONE_CALL
    }

    fun endActivePhoneCall(callId: String?) {
        val current = _activeCareer.value ?: return
        if (callId != null) {
            val updatedCalls = current.callRecords.map {
                if (it.id == callId) it.copy(completed = true) else it
            }
            val updated = current.copy(callRecords = updatedCalls)
            _activeCareer.value = updated
            persistCareerState(updated)
        }
        _activePhoneCallId.value = null
        _overlayScreen.value = HubOverlayScreen.NONE
    }

    fun testGeminiConnection(apiKey: String, modelId: String) {
        viewModelScope.launch {
            _aiConnectionStatus.value = "Bağlantı test ediliyor..."
            val res = com.example.ai.GeminiClient.testConnection(apiKey, modelId)
            _aiConnectionStatus.value = "${res.statusTitleTr}: ${res.detailMessageTr}"
        }
    }

    fun updatePhoneWallpaper(wallpaperId: String) {
        val current = _activeCareer.value ?: return
        val updated = CareerEngine.updatePhoneWallpaper(current, wallpaperId)
        playSfx(GameSoundEffect.MENU_TAP)
        _activeCareer.value = updated
        persistCareerState(updated)
        showToast("Telefon duvar kağıdı güncellendi.")
    }

    fun extendCurrentContract() {
        val current = _activeCareer.value ?: return
        val (updated, msg) = CareerEngine.extendCurrentContract(current)
        playSfx(GameSoundEffect.TRANSFER_OFFER)
        _activeCareer.value = updated
        persistCareerState(updated)
        showToast(msg)
    }

    fun startLiveMatchKickOff() {
        val currentMatch = _liveMatchState.value ?: return
        playSfx(GameSoundEffect.WHISTLE_KICKOFF)
        _liveMatchState.value = matchEngine.startKickOff(currentMatch)
        startMatchLoopTicker()
    }

    fun startSecondHalf() {
        val career = _activeCareer.value ?: return
        val currentMatch = _liveMatchState.value ?: return
        playSfx(GameSoundEffect.WHISTLE_KICKOFF)
        _liveMatchState.value = matchEngine.resumeSecondHalf(currentMatch, career.player)
        startMatchLoopTicker()
    }

    fun setMatchSpeed(multiplier: Int) {
        val currentMatch = _liveMatchState.value ?: return
        playSfx(GameSoundEffect.MENU_TAP)
        _liveMatchState.value = currentMatch.copy(speedMultiplier = multiplier.coerceIn(1, 3))
    }

    fun skipToNextMatchMoment() {
        val career = _activeCareer.value ?: return
        val current = _liveMatchState.value ?: return
        playSfx(GameSoundEffect.MENU_TAP)
        val skipped = matchEngine.skipToNextPlayableMoment(current, career.player)
        _liveMatchState.value = skipped
        if (skipped.phase == MatchPhase.LIVE_PLAY) {
            startMatchLoopTicker()
        } else if (skipped.phase == MatchPhase.FULL_TIME) {
            playSfx(GameSoundEffect.WHISTLE_FULLTIME)
        }
    }

    fun selectCriticalMatchOption(option: TacticalDecisionOption) {
        val career = _activeCareer.value ?: return
        val currentMatch = _liveMatchState.value ?: return
        playSfx(GameSoundEffect.BALL_KICK)
        val resolved = matchEngine.resolveCriticalDecision(currentMatch, option, career.player)
        if (resolved.phase == MatchPhase.GOAL_CELEBRATION) {
            playSfx(GameSoundEffect.GOAL_CELEBRATION)
        }
        _liveMatchState.value = resolved
        startMatchLoopTicker()
    }

    fun finishMatchAndCommitResults() {
        matchLoopJob?.cancel()
        val career = _activeCareer.value ?: return
        val matchState = _liveMatchState.value ?: return
        val report = matchState.pendingReport ?: matchEngine.buildFinalMatchReport(matchState, career.player)

        playSfx(GameSoundEffect.WHISTLE_FULLTIME)
        val updatedCareer = CareerEngine.applyCompletedMatch(career, report)
        _activeCareer.value = updatedCareer
        _liveMatchState.value = null
        _overlayScreen.value = HubOverlayScreen.POST_MATCH_LOCKER_ROOM
        persistCareerState(updatedCareer)
    }

    private fun startMatchLoopTicker() {
        matchLoopJob?.cancel()
        matchLoopJob = viewModelScope.launch {
            while (true) {
                val currentBeforeDelay = _liveMatchState.value ?: break
                val frameDelay = if (currentBeforeDelay.phase == MatchPhase.ACTION_RESOLUTION || currentBeforeDelay.phase == MatchPhase.GOAL_CELEBRATION) 28L else 42L
                delay(frameDelay)
                val career = _activeCareer.value ?: break
                val current = _liveMatchState.value ?: break
                if (current.phase in listOf(
                        MatchPhase.PRE_MATCH,
                        MatchPhase.CRITICAL_DECISION,
                        MatchPhase.HALF_TIME,
                        MatchPhase.FULL_TIME
                    )
                ) {
                    break
                }
                val prevScore = current.homeScore + current.awayScore
                val stepped = matchEngine.stepAnimationFrame(current, career.player)
                if (stepped.homeScore + stepped.awayScore > prevScore) {
                    playSfx(GameSoundEffect.GOAL_CELEBRATION)
                }
                _liveMatchState.value = stepped
                if (stepped.phase == MatchPhase.FULL_TIME) {
                    playSfx(GameSoundEffect.WHISTLE_FULLTIME)
                    break
                }
            }
        }
    }

    // Settings Operations
    fun setAmbienceEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.updateAmbience(enabled) }
    }

    fun setSfxEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.updateSfx(enabled) }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.updateHaptics(enabled) }
    }

    fun setGraphicsQuality(quality: GraphicsQuality) {
        viewModelScope.launch { settingsRepository.updateGraphicsQuality(quality) }
    }

    fun setSkipIntro(skip: Boolean) {
        viewModelScope.launch { settingsRepository.updateSkipIntro(skip) }
    }

    fun setLocalAccountName(name: String) {
        viewModelScope.launch {
            settingsRepository.updateAccountName(name)
            showToast("Kariyer profili güncellendi: $name")
        }
    }

    fun setGeminiApiKey(key: String) {
        viewModelScope.launch {
            settingsRepository.updateGeminiApiKey(key)
            showToast("Gemini API anahtarı kaydedildi.")
        }
    }

    fun setGeminiModel(model: String) {
        viewModelScope.launch {
            settingsRepository.updateGeminiModel(model)
            showToast("Gemini modeli güncellendi: $model")
        }
    }

    fun setAiDialoguesEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateAiDialoguesEnabled(enabled)
        }
    }

    private fun persistCareerState(state: CareerSaveState) {
        viewModelScope.launch {
            val dateLabel = CareerEngine.formatGameDate(state.currentDayIndex, state.currentSeasonYear)
            saveRepository.saveCareer(state, dateLabel)
            settingsRepository.setLastActiveSlot(state.saveSlotId)
        }
    }

    override fun onCleared() {
        matchLoopJob?.cancel()
        super.onCleared()
    }

    class Factory(private val appContext: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = FootballLifeDatabase.getInstance(appContext)
            val saveRepo = CareerSaveRepository(db.careerSaveDao())
            val settingsRepo = SettingsRepository(appContext)
            val soundMgr = SoundAndHapticsManager(appContext)
            return FootballLifeViewModel(saveRepo, settingsRepo, soundMgr) as T
        }
    }
}
