package com.example.engine

import com.example.model.Club
import com.example.model.CompletedMatchReport
import com.example.model.LeagueFixture
import com.example.model.MatchEventRecord
import com.example.model.PlayerProfile
import com.example.model.Position
import com.example.model.StartingStatus
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

enum class MatchPhase {
    PRE_MATCH,
    LIVE_PLAY,
    CRITICAL_DECISION,
    ACTION_RESOLUTION,
    GOAL_CELEBRATION,
    HALF_TIME,
    FULL_TIME
}

data class PitchPlayerNode(
    val id: String,
    val number: Int,
    val name: String,
    val position: Position,
    val isHome: Boolean,
    val isUser: Boolean,
    val baseX: Float, // 0..100 (left to right)
    val baseY: Float, // 0..100 (top to bottom)
    val x: Float,
    val y: Float,
    val targetX: Float,
    val targetY: Float,
    val hasBall: Boolean = false
)

data class PitchBallState(
    val x: Float = 50f,
    val y: Float = 50f,
    val targetX: Float = 50f,
    val targetY: Float = 50f,
    val speed: Float = 0.35f,
    val isShotInFlight: Boolean = false
)

data class TacticalDecisionOption(
    val id: String,
    val title: String,
    val subtitle: String,
    val keyAttributeLabel: String,
    val keyAttributeValue: Int,
    val successProbabilityPct: Int,
    val riskLevel: String, // "Düşük Risk", "Dengeli", "Yüksek Risk / Yüksek Ödül"
    val actionType: String // "FINESSE_SHOT", "POWER_SHOT", "THROUGH_PASS", "DRIBBLE_KEEPER", "TACKLE", "CROSS"
)

data class CriticalMomentContext(
    val id: String,
    val minute: Int,
    val title: String,
    val situationDescription: String,
    val ballX: Float,
    val ballY: Float,
    val options: List<TacticalDecisionOption>
)

data class LiveMatchUiState(
    val fixture: LeagueFixture,
    val homeClub: Club,
    val awayClub: Club,
    val userIsHome: Boolean,
    val userStartingStatus: StartingStatus,
    val userOnPitch: Boolean,
    val phase: MatchPhase = MatchPhase.PRE_MATCH,
    val minute: Int = 0,
    val subSecondTick: Int = 0,
    val speedMultiplier: Int = 1, // 1x, 2x, 3x
    val homeScore: Int = 0,
    val awayScore: Int = 0,
    val homePossessionPct: Int = 50,
    val homeShots: Int = 0,
    val awayShots: Int = 0,
    val homeShotsOnTarget: Int = 0,
    val awayShotsOnTarget: Int = 0,
    val homeCorners: Int = 0,
    val awayCorners: Int = 0,
    val homeFouls: Int = 0,
    val awayFouls: Int = 0,
    val userGoals: Int = 0,
    val userAssists: Int = 0,
    val userShots: Int = 0,
    val userPassesCompleted: Int = 0,
    val userPassesAttempted: Int = 0,
    val userDecisionsTotal: Int = 0,
    val userDecisionsSucceeded: Int = 0,
    val userLiveRating: Double = 6.6,
    val userLiveEnergy: Int = 90,
    val players: List<PitchPlayerNode> = emptyList(),
    val ball: PitchBallState = PitchBallState(),
    val cameraFocusX: Float = 50f,
    val cameraFocusY: Float = 50f,
    val cameraZoom: Float = 1.0f,
    val activeDecision: CriticalMomentContext? = null,
    val latestBannerText: String = " Stadyumda son hazırlıklar tamamlandı!",
    val goalScorerBanner: String? = null,
    val commentaryFeed: List<MatchEventRecord> = emptyList(),
    val resolutionTicksRemaining: Int = 0,
    val pendingReport: CompletedMatchReport? = null
)

class MatchSimulationEngine(private val baseSeed: Long = 20261114L) {

    fun createInitialMatchState(
        fixture: LeagueFixture,
        homeClub: Club,
        awayClub: Club,
        userPlayer: PlayerProfile,
        userClubId: String,
        startingStatus: StartingStatus
    ): LiveMatchUiState {
        val userIsHome = fixture.homeClubId == userClubId
        val userStarts = startingStatus == StartingStatus.STARTER
        val nodes = buildInitial22Players(
            homeClub = homeClub,
            awayClub = awayClub,
            userPlayer = userPlayer,
            userIsHome = userIsHome,
            userOnPitch = userStarts
        )
        return LiveMatchUiState(
            fixture = fixture,
            homeClub = homeClub,
            awayClub = awayClub,
            userIsHome = userIsHome,
            userStartingStatus = startingStatus,
            userOnPitch = userStarts,
            phase = MatchPhase.PRE_MATCH,
            minute = 0,
            userLiveEnergy = userPlayer.energy,
            players = nodes,
            ball = PitchBallState(50f, 50f, 50f, 50f),
            latestBannerText = if (userStarts) {
                "İlk 11'de sahaya çıkıyorsun! Maç başlamaya hazır."
            } else {
                "Maça yedek kulübesinde başlıyorsun. Hoca ikinci yarıda seni oyuna alabilir."
            }
        )
    }

    fun startKickOff(state: LiveMatchUiState): LiveMatchUiState {
        val kickOffEvent = MatchEventRecord(
            minute = 1,
            type = "DÜDÜK",
            teamId = state.homeClub.id,
            playerName = "Hakem",
            description = "1' — İlk düdük çaldı ve mücadele başladı!",
            involvesUser = false
        )
        return state.copy(
            phase = MatchPhase.LIVE_PLAY,
            minute = 1,
            subSecondTick = 0,
            cameraZoom = 1.0f,
            cameraFocusX = 50f,
            cameraFocusY = 50f,
            latestBannerText = "1' — Karşılaşma başladı!",
            commentaryFeed = listOf(kickOffEvent)
        )
    }

    fun resumeSecondHalf(state: LiveMatchUiState, userPlayer: PlayerProfile): LiveMatchUiState {
        var nowOnPitch = state.userOnPitch
        val newEvents = state.commentaryFeed.toMutableList()
        var banner = "46' — İkinci yarı başladı!"

        if (!nowOnPitch && state.userStartingStatus != StartingStatus.OUT_OF_SQUAD) {
            nowOnPitch = true
            banner = "46' — Teknik direktör seni ikinci yarı başında oyuna aldı!"
            newEvents.add(
                0,
                MatchEventRecord(
                    minute = 46,
                    type = "DEĞİŞİKLİK",
                    teamId = if (state.userIsHome) state.homeClub.id else state.awayClub.id,
                    playerName = userPlayer.fullName,
                    description = "46' — Oyuncu Değişikliği: ${userPlayer.fullName} ikinci yarı başında sahaya dahil oldu!",
                    involvesUser = true
                )
            )
        }

        val updatedNodes = buildInitial22Players(
            homeClub = state.homeClub,
            awayClub = state.awayClub,
            userPlayer = userPlayer,
            userIsHome = state.userIsHome,
            userOnPitch = nowOnPitch
        )

        return state.copy(
            phase = MatchPhase.LIVE_PLAY,
            minute = 46,
            subSecondTick = 0,
            userOnPitch = nowOnPitch,
            players = updatedNodes,
            ball = PitchBallState(50f, 50f, 50f, 50f),
            cameraZoom = 1.0f,
            cameraFocusX = 50f,
            cameraFocusY = 50f,
            latestBannerText = banner,
            commentaryFeed = newEvents
        )
    }

    /**
     * Advances the 2D pitch simulation by one animation frame (approx 50ms).
     * Moves players and ball smoothly, and advances match minute every 8 ticks.
     */
    fun stepAnimationFrame(
        state: LiveMatchUiState,
        userPlayer: PlayerProfile
    ): LiveMatchUiState {
        when (state.phase) {
            MatchPhase.PRE_MATCH, MatchPhase.CRITICAL_DECISION, MatchPhase.HALF_TIME, MatchPhase.FULL_TIME -> {
                return state
            }
            MatchPhase.ACTION_RESOLUTION, MatchPhase.GOAL_CELEBRATION -> {
                val movedPlayers = interpolatePlayers(state.players, stepFactor = 0.24f)
                val movedBall = interpolateBall(state.ball, stepFactor = 0.28f)
                val remaining = state.resolutionTicksRemaining - 1
                if (remaining > 0) {
                    return state.copy(
                        players = movedPlayers,
                        ball = movedBall,
                        resolutionTicksRemaining = remaining
                    )
                }
                return state.copy(
                    phase = MatchPhase.LIVE_PLAY,
                    players = movedPlayers,
                    ball = movedBall,
                    cameraZoom = 1.0f,
                    cameraFocusX = 50f,
                    cameraFocusY = 50f,
                    goalScorerBanner = null,
                    resolutionTicksRemaining = 0
                )
            }
            MatchPhase.LIVE_PLAY -> {
                val movedPlayers = interpolatePlayers(state.players, stepFactor = 0.18f)
                val movedBall = interpolateBall(state.ball, stepFactor = 0.24f)
                val ticksPerMinute = when (state.speedMultiplier) {
                    3 -> 3
                    2 -> 5
                    else -> 8
                }
                val nextSubTick = state.subSecondTick + 1
                if (nextSubTick < ticksPerMinute) {
                    return state.copy(
                        players = movedPlayers,
                        ball = movedBall,
                        subSecondTick = nextSubTick
                    )
                }
                return advanceMatchMinute(
                    state = state.copy(players = movedPlayers, ball = movedBall, subSecondTick = 0),
                    userPlayer = userPlayer
                )
            }
        }
    }

    fun advanceMatchMinute(
        state: LiveMatchUiState,
        userPlayer: PlayerProfile
    ): LiveMatchUiState {
        val nextMinute = state.minute + 1
        val rng = Random(baseSeed + state.fixture.id.hashCode() + nextMinute * 997L + state.userDecisionsTotal * 31L)

        // Check Half-Time
        if (state.minute == 45 && nextMinute == 46 && state.commentaryFeed.none { it.description.contains("İlk yarı sona erdi") }) {
            val htEvent = MatchEventRecord(
                minute = 45,
                type = "DEVRE",
                teamId = state.homeClub.id,
                playerName = "Hakem",
                description = "45' — İlk yarı sona erdi: ${state.homeClub.shortName} ${state.homeScore} - ${state.awayScore} ${state.awayClub.shortName}",
                involvesUser = false
            )
            return state.copy(
                phase = MatchPhase.HALF_TIME,
                minute = 45,
                latestBannerText = "İlk Yarı Sonucu: ${state.homeScore} - ${state.awayScore}",
                commentaryFeed = listOf(htEvent) + state.commentaryFeed
            )
        }

        // Check Full-Time
        if (nextMinute > 90) {
            val report = buildFinalMatchReport(state, userPlayer)
            val ftEvent = MatchEventRecord(
                minute = 90,
                type = "MAÇ SONU",
                teamId = state.homeClub.id,
                playerName = "Hakem",
                description = "90' — Maç sona erdi! ${state.homeClub.name} ${state.homeScore} - ${state.awayScore} ${state.awayClub.name}",
                involvesUser = false
            )
            return state.copy(
                phase = MatchPhase.FULL_TIME,
                minute = 90,
                cameraZoom = 1.0f,
                latestBannerText = "Maç Sona Erdi: ${state.homeScore} - ${state.awayScore}",
                commentaryFeed = listOf(ftEvent) + state.commentaryFeed,
                pendingReport = report
            )
        }

        // Sub in user at minute 58 if still on bench
        var userOnPitch = state.userOnPitch
        val events = state.commentaryFeed.toMutableList()
        var banner = state.latestBannerText

        if (!userOnPitch && nextMinute == 58 && state.userStartingStatus != StartingStatus.OUT_OF_SQUAD) {
            userOnPitch = true
            banner = "58' — Oyuna giriyorsun! Teknik direktör senden skora katkı bekliyor."
            events.add(
                0,
                MatchEventRecord(
                    minute = 58,
                    type = "DEĞİŞİKLİK",
                    teamId = if (state.userIsHome) state.homeClub.id else state.awayClub.id,
                    playerName = userPlayer.fullName,
                    description = "58' — ${userPlayer.fullName} kenardan oyuna dahil oldu!",
                    involvesUser = true
                )
            )
        }

        // Compute tactical possession shift
        val homeStr = state.homeClub.teamStrength + 2 // small home advantage
        val awayStr = state.awayClub.teamStrength
        val homeProb = (0.50 + (homeStr - awayStr) * 0.01).coerceIn(0.28, 0.72)
        val homeAttacksThisMinute = rng.nextDouble() < homeProb

        val newPossession = ((state.homePossessionPct * 0.85) + (if (homeAttacksThisMinute) 60 else 40) * 0.15)
            .roundToInt().coerceIn(32, 68)

        // Update 22 player target positions and ball carrier
        val (updatedPlayers, updatedBall) = computeTacticalPositionsForMinute(
            players = state.players,
            homeAttacks = homeAttacksThisMinute,
            minute = nextMinute,
            rng = rng
        )

        var homeScore = state.homeScore
        var awayScore = state.awayScore
        var homeShots = state.homeShots
        var awayShots = state.awayShots
        var homeOnTarget = state.homeShotsOnTarget
        var awayOnTarget = state.awayShotsOnTarget
        var homeCorners = state.homeCorners
        var awayCorners = state.awayCorners
        var homeFouls = state.homeFouls
        var awayFouls = state.awayFouls
        var userPassesComp = state.userPassesCompleted
        var userPassesAtt = state.userPassesAttempted
        var userRating = state.userLiveRating
        val userEnergy = if (userOnPitch && nextMinute % 3 == 0) {
            (state.userLiveEnergy - 1).coerceAtLeast(18)
        } else {
            state.userLiveEnergy
        }

        // Passive pass participation for user
        if (userOnPitch && rng.nextInt(100) < 35) {
            userPassesAtt += 1
            val passChance = (58 + userPlayer.attributes.passing / 3).coerceIn(60, 94)
            if (rng.nextInt(100) < passChance) {
                userPassesComp += 1
                userRating = (((userRating + 0.02).coerceAtMost(9.8)) * 10.0).roundToInt() / 10.0
            }
        }

        // Check if a Critical Interactive Moment should trigger for the User!
        // Guaranteed at least 3-4 exciting critical moments per match when user is on pitch
        val isPrimeDecisionMinute = nextMinute in listOf(14, 29, 39, 53, 66, 78, 86)
        val randomDecisionTrigger = rng.nextInt(100) < 6 && state.userDecisionsTotal < 5
        if (userOnPitch && (isPrimeDecisionMinute || randomDecisionTrigger)) {
            val criticalContext = createCriticalMoment(
                minute = nextMinute,
                state = state.copy(players = updatedPlayers, ball = updatedBall),
                userPlayer = userPlayer,
                rng = rng
            )
            // Position user and ball at the critical spot
            val focusedPlayers = updatedPlayers.map { node ->
                if (node.isUser) {
                    node.copy(
                        x = criticalContext.ballX,
                        y = criticalContext.ballY,
                        targetX = criticalContext.ballX,
                        targetY = criticalContext.ballY,
                        hasBall = true
                    )
                } else {
                    node.copy(hasBall = false)
                }
            }
            return state.copy(
                phase = MatchPhase.CRITICAL_DECISION,
                minute = nextMinute,
                userOnPitch = userOnPitch,
                homePossessionPct = newPossession,
                userPassesCompleted = userPassesComp,
                userPassesAttempted = userPassesAtt,
                userLiveRating = userRating,
                userLiveEnergy = userEnergy,
                players = focusedPlayers,
                ball = PitchBallState(
                    x = criticalContext.ballX,
                    y = criticalContext.ballY,
                    targetX = criticalContext.ballX,
                    targetY = criticalContext.ballY
                ),
                cameraFocusX = criticalContext.ballX,
                cameraFocusY = criticalContext.ballY,
                cameraZoom = 1.38f,
                activeDecision = criticalContext,
                latestBannerText = "${nextMinute}' — KRİTİK POZİSYON: ${criticalContext.title}",
                commentaryFeed = events
            )
        }

        // Otherwise check AI team match events (shots, saves, goals, corners, cards)
        val eventRoll = rng.nextInt(100)
        when {
            eventRoll < 7 -> {
                // Shot attempt by attacking team
                val attackingClub = if (homeAttacksThisMinute) state.homeClub else state.awayClub
                val defendingClub = if (homeAttacksThisMinute) state.awayClub else state.homeClub
                val attackerSquad = attackingClub.keySquadMembers.filter { it.position != Position.GK }
                val shooter = attackerSquad.ifEmpty { attackingClub.keySquadMembers }.random(rng)

                if (homeAttacksThisMinute) homeShots++ else awayShots++
                val onTarget = rng.nextInt(100) < 52
                if (onTarget) {
                    if (homeAttacksThisMinute) homeOnTarget++ else awayOnTarget++
                    val goalChance = (22 + (attackingClub.teamStrength - defendingClub.teamStrength)).coerceIn(12, 38)
                    if (rng.nextInt(100) < goalChance) {
                        if (homeAttacksThisMinute) homeScore++ else awayScore++
                        val goalX = if (homeAttacksThisMinute) 97f else 3f
                        val msg = "${nextMinute}' — GOL! ${shooter.name} (${attackingClub.shortName}) ceza sahasından ağları havalandırdı! Skor: $homeScore-$awayScore"
                        events.add(
                            0,
                            MatchEventRecord(
                                minute = nextMinute,
                                type = "GOL",
                                teamId = attackingClub.id,
                                playerName = shooter.name,
                                description = msg,
                                involvesUser = false
                            )
                        )
                        return state.copy(
                            phase = MatchPhase.GOAL_CELEBRATION,
                            minute = nextMinute,
                            homeScore = homeScore,
                            awayScore = awayScore,
                            homeShots = homeShots,
                            awayShots = awayShots,
                            homeShotsOnTarget = homeOnTarget,
                            awayShotsOnTarget = awayOnTarget,
                            homePossessionPct = newPossession,
                            userPassesCompleted = userPassesComp,
                            userPassesAttempted = userPassesAtt,
                            userLiveEnergy = userEnergy,
                            players = updatedPlayers,
                            ball = updatedBall.copy(targetX = goalX, targetY = 50f, isShotInFlight = true),
                            latestBannerText = msg,
                            goalScorerBanner = "GOL! ${shooter.name.uppercase()} ($nextMinute')",
                            commentaryFeed = events,
                            resolutionTicksRemaining = 14
                        )
                    } else {
                        if (homeAttacksThisMinute) homeCorners++ else awayCorners++
                        banner = "${nextMinute}' — ${shooter.name} sert vurdu, kaleci kornere çeldi!"
                        events.add(
                            0,
                            MatchEventRecord(
                                minute = nextMinute,
                                type = "KURTARIŞ",
                                teamId = attackingClub.id,
                                playerName = shooter.name,
                                description = banner,
                                involvesUser = false
                            )
                        )
                    }
                } else {
                    banner = "${nextMinute}' — ${attackingClub.shortName} atağında ${shooter.name}'in şutu auta çıktı."
                }
            }
            eventRoll in 7..10 -> {
                if (homeAttacksThisMinute) awayFouls++ else homeFouls++
                val foulingClub = if (homeAttacksThisMinute) state.awayClub else state.homeClub
                if (rng.nextInt(100) < 28) {
                    val defender = foulingClub.keySquadMembers.random(rng)
                    banner = "${nextMinute}' — SARI KART: ${defender.name} (${foulingClub.shortName}) sert müdahalesi sonrası sarı kart gördü."
                    events.add(
                        0,
                        MatchEventRecord(
                            minute = nextMinute,
                            type = "SARI_KART",
                            teamId = foulingClub.id,
                            playerName = defender.name,
                            description = banner,
                            involvesUser = false
                        )
                    )
                }
            }
        }

        return state.copy(
            minute = nextMinute,
            userOnPitch = userOnPitch,
            homeScore = homeScore,
            awayScore = awayScore,
            homePossessionPct = newPossession,
            homeShots = homeShots,
            awayShots = awayShots,
            homeShotsOnTarget = homeOnTarget,
            awayShotsOnTarget = awayOnTarget,
            homeCorners = homeCorners,
            awayCorners = awayCorners,
            homeFouls = homeFouls,
            awayFouls = awayFouls,
            userPassesCompleted = userPassesComp,
            userPassesAttempted = userPassesAtt,
            userLiveRating = userRating,
            userLiveEnergy = userEnergy,
            players = updatedPlayers,
            ball = updatedBall,
            latestBannerText = banner,
            commentaryFeed = events
        )
    }

    /**
     * Resolves the user's chosen option during a Hybrid 2D Match Critical Moment.
     * Calculates outcome from player attributes, energy, morale, and opponent defense,
     * then animates the ball and updates match score/stats/rating deterministically.
     */
    fun resolveCriticalDecision(
        state: LiveMatchUiState,
        chosenOption: TacticalDecisionOption,
        userPlayer: PlayerProfile
    ): LiveMatchUiState {
        val active = state.activeDecision ?: return state.copy(phase = MatchPhase.LIVE_PLAY)
        val rng = Random(
            baseSeed +
                state.fixture.id.hashCode() +
                state.minute * 131L +
                chosenOption.id.hashCode() +
                state.userDecisionsTotal * 17L
        )
        val roll = rng.nextInt(100)
        val succeeded = roll < chosenOption.successProbabilityPct

        val attackingRight = state.userIsHome
        val targetGoalX = if (attackingRight) 98f else 2f
        val userClub = if (state.userIsHome) state.homeClub else state.awayClub

        var homeScore = state.homeScore
        var awayScore = state.awayScore
        var homeShots = state.homeShots
        var awayShots = state.awayShots
        var homeOnTarget = state.homeShotsOnTarget
        var awayOnTarget = state.awayShotsOnTarget
        var userGoals = state.userGoals
        var userAssists = state.userAssists
        var userShots = state.userShots
        var userPassesComp = state.userPassesCompleted
        var userPassesAtt = state.userPassesAttempted
        var userRating = state.userLiveRating
        var nextPhase = MatchPhase.ACTION_RESOLUTION
        var goalBanner: String? = null
        var bannerText: String
        var ballTargetX: Float
        var ballTargetY: Float

        val newEvents = state.commentaryFeed.toMutableList()

        when (chosenOption.actionType) {
            "FINESSE_SHOT", "POWER_SHOT", "DRIBBLE_KEEPER" -> {
                userShots += 1
                if (state.userIsHome) homeShots += 1 else awayShots += 1

                if (succeeded) {
                    if (state.userIsHome) {
                        homeScore += 1
                        homeOnTarget += 1
                    } else {
                        awayScore += 1
                        awayOnTarget += 1
                    }
                    userGoals += 1
                    userRating = (userRating + 1.1).coerceAtMost(10.0)
                    nextPhase = MatchPhase.GOAL_CELEBRATION
                    ballTargetX = targetGoalX
                    ballTargetY = if (chosenOption.actionType == "FINESSE_SHOT") 44f else 54f
                    bannerText = "${state.minute}' — GOOOL! ${userPlayer.fullName} (${chosenOption.title}) ile topu ağlara gönderdi! ($homeScore-$awayScore)"
                    goalBanner = "GOOOL! ${userPlayer.kitName.uppercase()} (${state.minute}')"
                    newEvents.add(
                        0,
                        MatchEventRecord(
                            minute = state.minute,
                            type = "GOL",
                            teamId = userClub.id,
                            playerName = userPlayer.fullName,
                            description = bannerText,
                            involvesUser = true
                        )
                    )
                } else {
                    val savedByKeeper = roll < (chosenOption.successProbabilityPct + 25)
                    if (savedByKeeper) {
                        if (state.userIsHome) homeOnTarget += 1 else awayOnTarget += 1
                        userRating = (userRating + 0.1).coerceAtMost(10.0)
                        ballTargetX = if (attackingRight) 93f else 7f
                        ballTargetY = 48f
                        bannerText = "${state.minute}' — ${userPlayer.fullName} (${chosenOption.title}) denedi ancak kaleci son anda parmaklarının ucuyla çıkardı!"
                    } else {
                        userRating = (userRating - 0.15).coerceAtLeast(5.2)
                        ballTargetX = if (attackingRight) 99f else 1f
                        ballTargetY = 28f
                        bannerText = "${state.minute}' — ${userPlayer.fullName} sert vurdu fakat top direğin yanından auta çıktı."
                    }
                    newEvents.add(
                        0,
                        MatchEventRecord(
                            minute = state.minute,
                            type = "ŞUT",
                            teamId = userClub.id,
                            playerName = userPlayer.fullName,
                            description = bannerText,
                            involvesUser = true
                        )
                    )
                }
            }

            "THROUGH_PASS", "CROSS" -> {
                userPassesAtt += 1
                if (succeeded) {
                    userPassesComp += 1
                    // Teammate finishes the pass!
                    val finishChance = (54 + (userClub.teamStrength - 50) / 2).coerceIn(45, 78)
                    val teammateScored = rng.nextInt(100) < finishChance
                    if (state.userIsHome) homeShots += 1 else awayShots += 1

                    if (teammateScored) {
                        if (state.userIsHome) {
                            homeScore += 1
                            homeOnTarget += 1
                        } else {
                            awayScore += 1
                            awayOnTarget += 1
                        }
                        userAssists += 1
                        userRating = (userRating + 0.85).coerceAtMost(10.0)
                        nextPhase = MatchPhase.GOAL_CELEBRATION
                        ballTargetX = targetGoalX
                        ballTargetY = 50f
                        bannerText = "${state.minute}' — ASİST VE GOL! ${userPlayer.fullName} harika bir pas çıkardı, takım arkadaşı tek vuruşla golü attı! ($homeScore-$awayScore)"
                        goalBanner = "ASİST: ${userPlayer.kitName.uppercase()} (${state.minute}')"
                        newEvents.add(
                            0,
                            MatchEventRecord(
                                minute = state.minute,
                                type = "ASİST",
                                teamId = userClub.id,
                                playerName = userPlayer.fullName,
                                description = bannerText,
                                involvesUser = true
                            )
                        )
                    } else {
                        userRating = (userRating + 0.30).coerceAtMost(10.0)
                        ballTargetX = if (attackingRight) 90f else 10f
                        ballTargetY = 46f
                        bannerText = "${state.minute}' — ${userPlayer.fullName} kilit pası adrese teslim gönderdi ancak takım arkadaşı kaleciyi geçemedi!"
                        newEvents.add(
                            0,
                            MatchEventRecord(
                                minute = state.minute,
                                type = "KRİTİK_KARAR",
                                teamId = userClub.id,
                                playerName = userPlayer.fullName,
                                description = bannerText,
                                involvesUser = true
                            )
                        )
                    }
                } else {
                    userRating = (userRating - 0.15).coerceAtLeast(5.2)
                    ballTargetX = if (attackingRight) 74f else 26f
                    ballTargetY = 55f
                    bannerText = "${state.minute}' — ${userPlayer.fullName} savunma arkasına pas denedi fakat rakip stoper araya girdi."
                    newEvents.add(
                        0,
                        MatchEventRecord(
                            minute = state.minute,
                            type = "KRİTİK_KARAR",
                            teamId = userClub.id,
                            playerName = userPlayer.fullName,
                            description = bannerText,
                            involvesUser = true
                        )
                    )
                }
            }

            else -> {
                // TACKLE / DEFENSIVE INTERCEPTION
                if (succeeded) {
                    userRating = (userRating + 0.55).coerceAtMost(10.0)
                    ballTargetX = 52f
                    ballTargetY = 48f
                    bannerText = "${state.minute}' — KRİTİK MÜDAHALE! ${userPlayer.fullName} zamanında kayarak tehlikeli atağı kesti ve takımını kontra atağa kaldırdı!"
                } else {
                    userRating = (userRating - 0.25).coerceAtLeast(5.0)
                    ballTargetX = if (attackingRight) 18f else 82f
                    ballTargetY = 50f
                    bannerText = "${state.minute}' — ${userPlayer.fullName} müdahalede gecikti, rakip tehlikeli bölgeden serbest vuruş kazandı."
                }
                newEvents.add(
                    0,
                    MatchEventRecord(
                        minute = state.minute,
                        type = "KRİTİK_KARAR",
                        teamId = userClub.id,
                        playerName = userPlayer.fullName,
                        description = bannerText,
                        involvesUser = true
                    )
                )
            }
        }

        val updatedBall = state.ball.copy(
            targetX = ballTargetX,
            targetY = ballTargetY,
            isShotInFlight = true
        )

        return state.copy(
            phase = nextPhase,
            homeScore = homeScore,
            awayScore = awayScore,
            homeShots = homeShots,
            awayShots = awayShots,
            homeShotsOnTarget = homeOnTarget,
            awayShotsOnTarget = awayOnTarget,
            userGoals = userGoals,
            userAssists = userAssists,
            userShots = userShots,
            userPassesCompleted = userPassesComp,
            userPassesAttempted = userPassesAtt,
            userDecisionsTotal = state.userDecisionsTotal + 1,
            userDecisionsSucceeded = state.userDecisionsSucceeded + (if (succeeded) 1 else 0),
            userLiveRating = (userRating * 10.0).roundToInt() / 10.0,
            userLiveEnergy = (state.userLiveEnergy - 3).coerceAtLeast(10),
            ball = updatedBall,
            activeDecision = null,
            latestBannerText = bannerText,
            goalScorerBanner = goalBanner,
            commentaryFeed = newEvents,
            resolutionTicksRemaining = 16
        )
    }

    private fun createCriticalMoment(
        minute: Int,
        state: LiveMatchUiState,
        userPlayer: PlayerProfile,
        rng: Random
    ): CriticalMomentContext {
        val attackingRight = state.userIsHome
        val oppClub = if (state.userIsHome) state.awayClub else state.homeClub
        val oppDefPenalty = ((oppClub.teamStrength - 55) * 0.45).roundToInt()
        val energyMod = ((state.userLiveEnergy - 65) * 0.2).roundToInt()
        val moraleMod = ((userPlayer.morale - 65) * 0.15).roundToInt()
        val attr = userPlayer.attributes

        val isDefensive = userPlayer.primaryPosition in listOf(Position.CB, Position.FB, Position.DM, Position.GK) &&
            rng.nextInt(100) < 45

        if (isDefensive) {
            val ballX = if (attackingRight) 24f else 76f
            val ballY = (35 + rng.nextInt(30)).toFloat()
            val tackleProb = (52 + (attr.defending * 0.45).roundToInt() + energyMod - oppDefPenalty).coerceIn(25, 88)
            val interceptProb = (54 + (attr.tactics * 0.45).roundToInt() + moraleMod - oppDefPenalty).coerceIn(28, 88)
            val pressProb = (50 + (attr.strength * 0.42).roundToInt() + energyMod - oppDefPenalty).coerceIn(25, 85)

            return CriticalMomentContext(
                id = "CM_DEF_$minute",
                minute = minute,
                title = "${minute}' — Rakip Hızlı Kontra Atakta!",
                situationDescription = "${oppClub.name} forveti ceza sahasına sokulmak üzere. Savunma hattında kritik karar anı!",
                ballX = ballX,
                ballY = ballY,
                options = listOf(
                    TacticalDecisionOption(
                        id = "OPT_INTERCEPT",
                        title = "Pas Açısını Kapat ve Topu Kes",
                        subtitle = "Oyun görüşün ve taktik sezginle pas yolunu kapat.",
                        keyAttributeLabel = "Taktik",
                        keyAttributeValue = attr.tactics,
                        successProbabilityPct = interceptProb,
                        riskLevel = "Düşük Risk",
                        actionType = "TACKLE"
                    ),
                    TacticalDecisionOption(
                        id = "OPT_SLIDE_TACKLE",
                        title = "Zamanlamalı Kayarak Müdahale",
                        subtitle = "Topa doğrudan hamle yap ve hızlı geçiş başlat.",
                        keyAttributeLabel = "Savunma",
                        keyAttributeValue = attr.defending,
                        successProbabilityPct = tackleProb,
                        riskLevel = "Dengeli",
                        actionType = "TACKLE"
                    ),
                    TacticalDecisionOption(
                        id = "OPT_SHOULDER_PRESS",
                        title = "Omuz Omuza Fiziksel Baskı",
                        subtitle = "Rakibin dengesini bozup topu kazan.",
                        keyAttributeLabel = "Fizik",
                        keyAttributeValue = attr.strength,
                        successProbabilityPct = pressProb,
                        riskLevel = "Yüksek Risk / Yüksek Ödül",
                        actionType = "TACKLE"
                    )
                )
            )
        }

        val scenarioIndex = (minute + state.userDecisionsTotal) % 3
        val ballX = if (attackingRight) (78f + rng.nextInt(10)) else (22f - rng.nextInt(10))
        val ballY = (32f + rng.nextInt(36))

        val finesseProb = (36 + (attr.shooting * 0.50).roundToInt() + moraleMod - oppDefPenalty).coerceIn(20, 84)
        val powerProb = (32 + (attr.shooting * 0.42 + attr.strength * 0.12).roundToInt() + energyMod - oppDefPenalty).coerceIn(18, 80)
        val passProb = (48 + (attr.passing * 0.35 + attr.vision * 0.20).roundToInt() + moraleMod - oppDefPenalty).coerceIn(30, 88)
        val dribbleProb = (30 + (attr.dribbling * 0.35 + attr.agility * 0.20).roundToInt() + energyMod - oppDefPenalty).coerceIn(18, 78)

        val title = when (scenarioIndex) {
            0 -> "${minute}' — Savunmanın Arkasına Sarktın!"
            1 -> "${minute}' — Ceza Sahası Yayında Boşluk Buldun!"
            else -> "${minute}' — Kanattan Tehlikeli Bindirme!"
        }
        val desc = when (scenarioIndex) {
            0 -> "Kaleci açıyı kapatmak için kalesini terk ediyor, yanındaki forvet arkadaşın da arka direkte boş!"
            1 -> "${oppClub.shortName} savunması dengesiz yakalandı. Top ayağında ve karar senin!"
            else -> "Rakip beki hızınla geçtin, ceza sahası içinde iki arkadaşın hareketlendi!"
        }

        return CriticalMomentContext(
            id = "CM_ATT_$minute",
            minute = minute,
            title = title,
            situationDescription = desc,
            ballX = ballX,
            ballY = ballY,
            options = listOf(
                TacticalDecisionOption(
                    id = "OPT_FINESSE",
                    title = "Uzak Köşeye Plase Bırak",
                    subtitle = "Kalecinin uzanamayacağı köşeye falsolu vuruş dene.",
                    keyAttributeLabel = "Şut",
                    keyAttributeValue = attr.shooting,
                    successProbabilityPct = finesseProb,
                    riskLevel = "Dengeli",
                    actionType = "FINESSE_SHOT"
                ),
                TacticalDecisionOption(
                    id = "OPT_PASS",
                    title = "Boştaki Takım Arkadaşına Pas Ver",
                    subtitle = "Arka direkteki arkadaşına asist fırsatı yarat.",
                    keyAttributeLabel = "Pas & Görüş",
                    keyAttributeValue = (attr.passing + attr.vision) / 2,
                    successProbabilityPct = passProb,
                    riskLevel = "Düşük Risk",
                    actionType = "THROUGH_PASS"
                ),
                TacticalDecisionOption(
                    id = "OPT_POWER",
                    title = "Üst Köşeye Sert Şut Çek",
                    subtitle = "Kalecinin tepki veremeyeceği sertlikte aban.",
                    keyAttributeLabel = "Şut & Güç",
                    keyAttributeValue = (attr.shooting + attr.strength) / 2,
                    successProbabilityPct = powerProb,
                    riskLevel = "Yüksek Risk / Yüksek Ödül",
                    actionType = "POWER_SHOT"
                ),
                TacticalDecisionOption(
                    id = "OPT_DRIBBLE",
                    title = "Kaleciyi Çalımlamayı Dene",
                    subtitle = "Bire birde kaleciyi yatırıp boş kaleye yürü.",
                    keyAttributeLabel = "Dribbling",
                    keyAttributeValue = attr.dribbling,
                    successProbabilityPct = dribbleProb,
                    riskLevel = "Yüksek Risk / Yüksek Ödül",
                    actionType = "DRIBBLE_KEEPER"
                )
            )
        )
    }

    private fun buildInitial22Players(
        homeClub: Club,
        awayClub: Club,
        userPlayer: PlayerProfile,
        userIsHome: Boolean,
        userOnPitch: Boolean
    ): List<PitchPlayerNode> {
        // 4-3-3 tactical coordinates for Home (left -> right) and Away (right -> left)
        val homeCoords = listOf(
            Position.GK to (6f to 50f),
            Position.CB to (20f to 34f),
            Position.CB to (20f to 66f),
            Position.FB to (24f to 14f),
            Position.FB to (24f to 86f),
            Position.DM to (35f to 50f),
            Position.CM to (43f to 32f),
            Position.AM to (48f to 68f),
            Position.WG to (62f to 18f),
            Position.WG to (62f to 82f),
            Position.ST to (68f to 50f)
        )

        val awayCoords = listOf(
            Position.GK to (94f to 50f),
            Position.CB to (80f to 34f),
            Position.CB to (80f to 66f),
            Position.FB to (76f to 14f),
            Position.FB to (76f to 86f),
            Position.DM to (65f to 50f),
            Position.CM to (57f to 68f),
            Position.AM to (52f to 32f),
            Position.WG to (38f to 18f),
            Position.WG to (38f to 82f),
            Position.ST to (32f to 50f)
        )

        val userSlotIndex = when (userPlayer.primaryPosition) {
            Position.GK -> 0
            Position.CB -> 1
            Position.FB, Position.WB -> 3
            Position.DM -> 5
            Position.CM -> 6
            Position.AM -> 7
            Position.WG -> 8
            Position.ST -> 10
        }

        val homeNodes = homeCoords.mapIndexed { index, (pos, xy) ->
            val isUserHere = userIsHome && userOnPitch && index == userSlotIndex
            val member = homeClub.keySquadMembers.getOrNull(index)
            PitchPlayerNode(
                id = if (isUserHere) userPlayer.id else (member?.id ?: "H_$index"),
                number = if (isUserHere) userPlayer.kitNumber else (member?.kitNumber ?: (index + 1)),
                name = if (isUserHere) userPlayer.kitName else (member?.name ?: "Oyuncu ${index + 1}"),
                position = if (isUserHere) userPlayer.primaryPosition else pos,
                isHome = true,
                isUser = isUserHere,
                baseX = xy.first,
                baseY = xy.second,
                x = xy.first,
                y = xy.second,
                targetX = xy.first,
                targetY = xy.second,
                hasBall = index == 6
            )
        }

        val awayNodes = awayCoords.mapIndexed { index, (pos, xy) ->
            val isUserHere = !userIsHome && userOnPitch && index == userSlotIndex
            val member = awayClub.keySquadMembers.getOrNull(index)
            PitchPlayerNode(
                id = if (isUserHere) userPlayer.id else (member?.id ?: "A_$index"),
                number = if (isUserHere) userPlayer.kitNumber else (member?.kitNumber ?: (index + 1)),
                name = if (isUserHere) userPlayer.kitName else (member?.name ?: "Rakip ${index + 1}"),
                position = if (isUserHere) userPlayer.primaryPosition else pos,
                isHome = false,
                isUser = isUserHere,
                baseX = xy.first,
                baseY = xy.second,
                x = xy.first,
                y = xy.second,
                targetX = xy.first,
                targetY = xy.second,
                hasBall = false
            )
        }

        return homeNodes + awayNodes
    }

    private fun computeTacticalPositionsForMinute(
        players: List<PitchPlayerNode>,
        homeAttacks: Boolean,
        minute: Int,
        rng: Random
    ): Pair<List<PitchPlayerNode>, PitchBallState> {
        // Shift line of play based on attacking team and phase
        val attackProgress = (rng.nextFloat() * 24f) - 4f // -4..+20
        val teamShiftX = if (homeAttacks) (10f + attackProgress) else -(10f + attackProgress)

        val candidatesForBall = players.filter {
            it.isHome == homeAttacks && it.position != Position.GK
        }
        val carrier = candidatesForBall.ifEmpty { players }.random(rng)

        val updatedNodes = players.map { p ->
            if (p.position == Position.GK) {
                val gkX = if (p.isHome) 5.5f else 94.5f
                val gkY = (50f + (rng.nextFloat() - 0.5f) * 8f).coerceIn(42f, 58f)
                p.copy(
                    targetX = gkX,
                    targetY = gkY,
                    hasBall = p.id == carrier.id
                )
            } else {
                val roleMultiplier = when (p.position) {
                    Position.CB -> 0.55f
                    Position.FB, Position.WB -> 0.85f
                    Position.DM, Position.CM -> 0.95f
                    Position.AM, Position.WG, Position.ST -> 1.15f
                    Position.GK -> 0.1f
                }
                val jitterX = (rng.nextFloat() - 0.5f) * 9f
                val jitterY = (rng.nextFloat() - 0.5f) * 12f
                val newTargetX = (p.baseX + teamShiftX * roleMultiplier + jitterX).coerceIn(8f, 92f)
                val newTargetY = (p.baseY + jitterY).coerceIn(8f, 92f)
                p.copy(
                    targetX = newTargetX,
                    targetY = newTargetY,
                    hasBall = p.id == carrier.id
                )
            }
        }

        val activeCarrier = updatedNodes.find { it.id == carrier.id } ?: updatedNodes.first()
        val ball = PitchBallState(
            x = activeCarrier.x,
            y = activeCarrier.y,
            targetX = activeCarrier.targetX,
            targetY = activeCarrier.targetY,
            isShotInFlight = false
        )
        return updatedNodes to ball
    }

    private fun interpolatePlayers(
        players: List<PitchPlayerNode>,
        stepFactor: Float
    ): List<PitchPlayerNode> {
        return players.map { p ->
            val nx = p.x + (p.targetX - p.x) * stepFactor
            val ny = p.y + (p.targetY - p.y) * stepFactor
            p.copy(x = nx, y = ny)
        }
    }

    private fun interpolateBall(
        ball: PitchBallState,
        stepFactor: Float
    ): PitchBallState {
        val nx = ball.x + (ball.targetX - ball.x) * stepFactor
        val ny = ball.y + (ball.targetY - ball.y) * stepFactor
        return ball.copy(x = nx, y = ny)
    }

    fun buildFinalMatchReport(
        state: LiveMatchUiState,
        userPlayer: PlayerProfile
    ): CompletedMatchReport {
        val userClub = if (state.userIsHome) state.homeClub else state.awayClub
        val oppClub = if (state.userIsHome) state.awayClub else state.homeClub
        val userTeamGoals = if (state.userIsHome) state.homeScore else state.awayScore
        val oppTeamGoals = if (state.userIsHome) state.awayScore else state.homeScore

        val minutesPlayed = when {
            state.userStartingStatus == StartingStatus.STARTER -> 90
            state.userOnPitch -> 32
            else -> 0
        }

        val finalRating = if (minutesPlayed == 0) {
            6.0
        } else {
            val teamResultBonus = when {
                userTeamGoals > oppTeamGoals -> 0.3
                userTeamGoals == oppTeamGoals -> 0.0
                else -> -0.2
            }
            ((state.userLiveRating + teamResultBonus).coerceIn(5.0, 10.0) * 10.0).roundToInt() / 10.0
        }

        val managerComment = when {
            minutesPlayed == 0 -> "${userClub.managerName}: \"Bugün taktiksel olarak sana süre veremedim, hafta içi antrenmanlarda kendini hazır tut.\""
            finalRating >= 8.0 -> "${userClub.managerName}: \"Muhteşem bir performans! Sahada tam istediğim liderliği ve kaliteyi gösterdin.\""
            finalRating >= 6.8 -> "${userClub.managerName}: \"Disiplinli ve faydalı oynadın. Bu çizgini korursan formayı bırakmazsın.\""
            else -> "${userClub.managerName}: \"Bugün kararlarında aceleciydin ve beklentimin altında kaldın. Antrenmanda daha çok çalışmalısın.\""
        }

        val teammateComment = when {
            state.userGoals > 0 || state.userAssists > 0 -> "Takım Kaptanı: \"Skora yaptığın katkı soyunma odasında herkesi ateşledi kardeşim!\""
            userTeamGoals > oppTeamGoals -> "Takım Kaptanı: \"Harika bir 3 puan! Emeğine sağlık.\""
            else -> "Takım Kaptanı: \"Kafamızı kaldırıp önümüzdeki haftaya odaklanmalıyız.\""
        }

        val pressHeadline = when {
            state.userGoals >= 2 -> "${userPlayer.fullName} Yıldızlaştı! ${userClub.name} Sahadan ${state.homeScore}-${state.awayScore} Ayrıldı"
            state.userGoals == 1 -> "${userPlayer.fullName} Ağları Havalandırdı: ${state.homeClub.shortName} ${state.homeScore}-${state.awayScore} ${state.awayClub.shortName}"
            state.userAssists >= 1 -> "${userPlayer.fullName}'den Kritik Asist! ${state.homeClub.shortName} ${state.homeScore}-${state.awayScore} ${state.awayClub.shortName}"
            else -> "${state.homeClub.name} ${state.homeScore} - ${state.awayScore} ${state.awayClub.name}: Mücadele Nefes Kesti"
        }

        val energySpent = (92 - state.userLiveEnergy).coerceIn(10, 45)

        return CompletedMatchReport(
            fixtureId = state.fixture.id,
            week = state.fixture.week,
            dateLabel = state.fixture.dateLabel,
            homeClubId = state.homeClub.id,
            homeClubName = state.homeClub.name,
            awayClubId = state.awayClub.id,
            awayClubName = state.awayClub.name,
            homeScore = state.homeScore,
            awayScore = state.awayScore,
            homePossessionPct = state.homePossessionPct,
            homeShots = state.homeShots.coerceAtLeast(state.homeScore + 2),
            awayShots = state.awayShots.coerceAtLeast(state.awayScore + 2),
            homeShotsOnTarget = state.homeShotsOnTarget.coerceAtLeast(state.homeScore + 1),
            awayShotsOnTarget = state.awayShotsOnTarget.coerceAtLeast(state.awayScore + 1),
            userPlayedMinutes = minutesPlayed,
            userStarted = state.userStartingStatus == StartingStatus.STARTER,
            userGoals = state.userGoals,
            userAssists = state.userAssists,
            userShots = state.userShots,
            userPassesCompleted = state.userPassesCompleted,
            userPassesAttempted = state.userPassesAttempted,
            userKeyDecisionsMade = state.userDecisionsTotal,
            userSuccessfulDecisions = state.userDecisionsSucceeded,
            userRating = finalRating,
            managerFeedback = managerComment,
            teammateReaction = teammateComment,
            pressHeadline = pressHeadline,
            energySpent = energySpent,
            wageAndBonusEarnedEur = 0L, // populated by CareerEngine when applying match report
            events = state.commentaryFeed
        )
    }
}
