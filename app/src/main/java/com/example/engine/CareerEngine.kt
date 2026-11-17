package com.example.engine

import com.example.model.AttributeHistoryPoint
import com.example.model.CalendarEvent
import com.example.model.CareerAchievement
import com.example.model.CareerSaveState
import com.example.model.ChatMessage
import com.example.model.Club
import com.example.model.CompletedMatchReport
import com.example.model.Contract
import com.example.model.DynamicStoryChoice
import com.example.model.FinancialTransaction
import com.example.model.GalleryPhotoItem
import com.example.model.GameEvent
import com.example.model.InjuryRecord
import com.example.model.InstaCommentItem
import com.example.model.InstaDmThread
import com.example.model.LeagueFixture
import com.example.model.LeagueScorerEntry
import com.example.model.LifestylePurchaseItem
import com.example.model.LifestyleVenueAction
import com.example.model.MarketValuePoint
import com.example.model.NewsEvent
import com.example.model.NpcCharacter
import com.example.model.PhoneCallRecord
import com.example.model.PlayerAppearance
import com.example.model.PlayerAttributes
import com.example.model.PlayerProfile
import com.example.model.Position
import com.example.model.PreferredFoot
import com.example.model.SeasonStatsRecord
import com.example.model.SocialMediaPost
import com.example.model.SquadRole
import com.example.model.StandingRow
import com.example.model.StartingStatus
import com.example.model.StoryItem
import com.example.model.TransferHistoryEntry
import com.example.model.TransferOffer
import com.example.model.XPostItem
import com.example.engine.progression.LeagueDifficultyCalculator
import com.example.engine.progression.RealisticProgressionEngine
import com.example.engine.progression.TransferMarketEngine
import com.example.ui.components.FLFormatters
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random

data class MonthDayCell(
    val dayOfMonth: Int,
    val dayIndex: Int?, // null for padding cells outside the month or before career start
    val isCurrentDay: Boolean = false,
    val isMatchDay: Boolean = false,
    val isTrainingDay: Boolean = false,
    val isSocialOrTransferDay: Boolean = false,
    val events: List<CalendarEvent> = emptyList(),
    val fixture: LeagueFixture? = null
)

data class MonthCalendarGrid(
    val monthIndex: Int, // 0 = Ağustos 2026, 1 = Eylül 2026 ...
    val monthTitleTr: String,
    val cells: List<MonthDayCell> // 42 cells (6 rows x 7 columns, Monday-first)
)

object CareerEngine {

    private val monthNamesTr = listOf(
        "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık",
        "Ocak", "Şubat", "Mart", "Nisan", "Mayıs"
    )
    private val dayNamesTr = listOf(
        "Pazartesi", "Salı", "Çarşamba", "Perşembe", "Cuma", "Cumartesi", "Pazar"
    )

    fun formatGameDate(dayIndex: Int, seasonYear: Int = 2026): String {
        val zeroBased = (dayIndex - 1).coerceAtLeast(0)
        val dayOfMonth = (10 + zeroBased) % 30 + 1
        val monthIdx = ((10 + zeroBased) / 30) % monthNamesTr.size
        val year = if (monthIdx >= 5) seasonYear + 1 else seasonYear
        val weekday = dayNamesTr[zeroBased % 7]
        return "$dayOfMonth ${monthNamesTr[monthIdx]} $year, $weekday"
    }

    fun formatShortDate(dayIndex: Int): String {
        val zeroBased = (dayIndex - 1).coerceAtLeast(0)
        val dayOfMonth = (10 + zeroBased) % 30 + 1
        val monthIdx = ((10 + zeroBased) / 30) % monthNamesTr.size
        return "$dayOfMonth ${monthNamesTr[monthIdx].take(3)}"
    }

    fun createBalancedCustomAttributes(
        primaryPosition: Position,
        clubTier: Int,
        background: String
    ): PlayerAttributes {
        // Alt ligler (Amatör/BAL 38-46 OVR), Süper Lig (68-74 OVR)
        val base = when (clubTier) {
            6 -> 38
            5 -> 45
            4 -> 52
            3 -> 58
            2 -> 64
            else -> 70
        }
        val streetBonus = if (background.contains("Sokak")) 3 else 0
        val physicalBonus = if (background.contains("Atletik")) 3 else 0
        val tacticalBonus = if (background.contains("Akademi")) 3 else 0

        return when (primaryPosition) {
            Position.GK -> PlayerAttributes(
                shooting = 25, passing = base - 4, dribbling = 30, firstTouch = base - 5,
                pace = base - 8, agility = base + 4, stamina = base, strength = base + 2,
                defending = 35, tactics = base + tacticalBonus, vision = base - 2,
                setPieces = 40, goalkeeping = base + 6
            )
            Position.CB -> PlayerAttributes(
                shooting = base - 12, passing = base - 2, dribbling = base - 6, firstTouch = base - 3,
                pace = base - 1, agility = base - 3, stamina = base + 2, strength = base + 6 + physicalBonus,
                defending = base + 6, tactics = base + 4 + tacticalBonus, vision = base - 3,
                setPieces = base - 8, goalkeeping = 15
            )
            Position.FB, Position.WB -> PlayerAttributes(
                shooting = base - 6, passing = base + 1, dribbling = base + 2 + streetBonus, firstTouch = base,
                pace = base + 6 + physicalBonus, agility = base + 4, stamina = base + 6, strength = base - 1,
                defending = base + 2, tactics = base + tacticalBonus, vision = base,
                setPieces = base - 3, goalkeeping = 15
            )
            Position.DM -> PlayerAttributes(
                shooting = base - 4, passing = base + 3, dribbling = base - 1, firstTouch = base + 1,
                pace = base - 1, agility = base, stamina = base + 5 + physicalBonus, strength = base + 4,
                defending = base + 5, tactics = base + 5 + tacticalBonus, vision = base + 2,
                setPieces = base - 2, goalkeeping = 15
            )
            Position.CM -> PlayerAttributes(
                shooting = base, passing = base + 5 + tacticalBonus, dribbling = base + 2, firstTouch = base + 4,
                pace = base, agility = base + 2, stamina = base + 4, strength = base,
                defending = base - 1, tactics = base + 3, vision = base + 5,
                setPieces = base + 1, goalkeeping = 15
            )
            Position.AM -> PlayerAttributes(
                shooting = base + 2, passing = base + 5, dribbling = base + 5 + streetBonus, firstTouch = base + 6,
                pace = base + 1, agility = base + 5, stamina = base - 1, strength = base - 4,
                defending = base - 10, tactics = base + 2 + tacticalBonus, vision = base + 6,
                setPieces = base + 4, goalkeeping = 15
            )
            Position.WG -> PlayerAttributes(
                shooting = base + 2, passing = base + 1, dribbling = base + 6 + streetBonus, firstTouch = base + 4,
                pace = base + 7 + physicalBonus, agility = base + 6, stamina = base + 2, strength = base - 3,
                defending = base - 12, tactics = base - 1, vision = base + 1,
                setPieces = base, goalkeeping = 15
            )
            Position.ST -> PlayerAttributes(
                shooting = base + 7, passing = base - 2, dribbling = base + 3 + streetBonus, firstTouch = base + 4,
                pace = base + 4 + physicalBonus, agility = base + 3, stamina = base + 1, strength = base + 4,
                defending = base - 14, tactics = base + 1 + tacticalBonus, vision = base - 1,
                setPieces = base + 2, goalkeeping = 15
            )
        }
    }

    fun createNewCareerFromCustomPlayer(
        slotId: Int,
        firstName: String,
        lastName: String,
        birthYear: Int,
        country: String,
        city: String,
        nationality: String,
        kitName: String,
        kitNumber: Int,
        primaryPosition: Position,
        secondaryPositions: List<Position>,
        preferredFoot: PreferredFoot,
        heightCm: Int,
        weightKg: Int,
        appearance: PlayerAppearance,
        backgroundStory: String,
        selectedLeagueId: String,
        selectedClubId: String,
        customAttributes: PlayerAttributes? = null
    ): CareerSaveState {
        val clubs = WorldDataCatalog.clubsForLeague(selectedLeagueId)
        val club = clubs.find { it.id == selectedClubId } ?: clubs.first()
        val age = (2026 - birthYear).coerceIn(16, 36)
        val attrs = customAttributes ?: createBalancedCustomAttributes(primaryPosition, club.tier, backgroundStory)
        val ovr = attrs.calculateOverall(primaryPosition)
        val potential = (ovr + (28 - age).coerceIn(4, 20)).coerceIn(ovr + 3, 94)

        val baseWage = LeagueDifficultyCalculator.computeRealisticStartingWage(club.tier, ovr)
        val baseMarketValue = LeagueDifficultyCalculator.computeRealisticMarketValue(club.tier, ovr, age)
        val startingBalance = when (club.tier) {
            6 -> 250L
            5 -> 650L
            4 -> 1_800L
            3 -> 4_500L
            2 -> 16_000L
            else -> 65_000L
        }

        val player = PlayerProfile(
            id = "PLAYER_${System.currentTimeMillis()}",
            firstName = firstName.trim().ifEmpty { "Kerem" },
            lastName = lastName.trim().ifEmpty { "Yıldırım" },
            birthDate = "14.05.$birthYear",
            age = age,
            country = country,
            city = city,
            nationality = nationality,
            kitName = kitName.trim().ifEmpty { lastName.uppercase() },
            kitNumber = kitNumber.coerceIn(1, 99),
            primaryPosition = primaryPosition,
            secondaryPositions = secondaryPositions,
            preferredFoot = preferredFoot,
            heightCm = heightCm,
            weightKg = weightKg,
            appearance = appearance,
            attributes = attrs,
            potential = potential,
            energy = 94,
            morale = 80,
            form = 70,
            marketValueEur = baseMarketValue,
            followersCount = if (club.tier >= 4) 680 else 4_200,
            fanSupport = 56,
            backgroundStory = backgroundStory,
            isRealPlayer = false,
            dataSourceInfo = "Özel Oluşturulmuş Oyuncu Profili (2026/27)",
            currentSeasonStats = SeasonStatsRecord(
                seasonYear = 2026,
                clubId = club.id,
                clubName = club.name,
                leagueName = club.leagueName
            )
        )

        val role = if (ovr >= club.teamStrength) SquadRole.STARTER else SquadRole.PROSPECT
        val contract = Contract(
            clubId = club.id,
            clubName = club.name,
            weeklyWageEur = baseWage,
            matchAppearanceBonusEur = baseWage / 3,
            goalBonusEur = baseWage / 2,
            startSeasonYear = 2026,
            endSeasonYear = 2028,
            role = role
        )

        return initializeWorldForPlayer(
            slotId = slotId,
            player = player,
            contract = contract,
            leagueId = selectedLeagueId,
            clubs = clubs,
            startingBalanceEur = startingBalance
        )
    }

    fun createNewCareerFromRealPreset(
        slotId: Int,
        preset: RealPlayerPreset,
        overrideLeagueId: String? = null,
        overrideClubId: String? = null
    ): CareerSaveState {
        val targetLeagueId = overrideLeagueId ?: preset.defaultLeagueId
        val clubs = WorldDataCatalog.clubsForLeague(targetLeagueId)
        val club = clubs.find { it.id == (overrideClubId ?: preset.defaultClubId) } ?: clubs.first()

        val player = PlayerProfile(
            id = preset.id,
            firstName = preset.firstName,
            lastName = preset.lastName,
            birthDate = preset.birthDate,
            age = preset.age,
            country = preset.nationality,
            city = preset.birthCity,
            nationality = preset.nationality,
            kitName = preset.kitName,
            kitNumber = preset.kitNumber,
            primaryPosition = preset.primaryPosition,
            secondaryPositions = preset.secondaryPositions,
            preferredFoot = preset.preferredFoot,
            heightCm = preset.heightCm,
            weightKg = preset.weightKg,
            appearance = preset.appearance,
            attributes = preset.attributes,
            potential = preset.potential,
            energy = 94,
            morale = 84,
            form = 78,
            marketValueEur = preset.marketValueEur,
            followersCount = 485_000,
            fanSupport = 76,
            backgroundStory = preset.careerSummary,
            isRealPlayer = true,
            dataSourceInfo = preset.verifiedSeasonSource,
            currentSeasonStats = SeasonStatsRecord(
                seasonYear = 2026,
                clubId = club.id,
                clubName = club.name,
                leagueName = club.leagueName
            ),
            careerArchive = preset.archiveSeasons
        )

        val contract = Contract(
            clubId = club.id,
            clubName = club.name,
            weeklyWageEur = preset.weeklyWageEur,
            matchAppearanceBonusEur = (preset.weeklyWageEur * 0.20).toLong(),
            goalBonusEur = (preset.weeklyWageEur * 0.30).toLong(),
            startSeasonYear = 2026,
            endSeasonYear = preset.contractEndYear,
            role = SquadRole.STAR
        )

        return initializeWorldForPlayer(
            slotId = slotId,
            player = player,
            contract = contract,
            leagueId = targetLeagueId,
            clubs = clubs,
            startingBalanceEur = preset.weeklyWageEur * 4
        )
    }

    private fun initializeWorldForPlayer(
        slotId: Int,
        player: PlayerProfile,
        contract: Contract,
        leagueId: String,
        clubs: List<Club>,
        startingBalanceEur: Long
    ): CareerSaveState {
        val club = clubs.find { it.id == contract.clubId } ?: clubs.first()
        val fixtures = generateLeagueSchedule(leagueId, clubs)
        val standings = clubs.map { c ->
            StandingRow(
                clubId = c.id,
                clubName = c.name,
                shortName = c.shortName,
                primaryColorHex = c.primaryColorHex
            )
        }
        val scorers = buildInitialScorerPool(clubs, player)
        val npcs = buildInitialNpcs(player, club, contract)
        val calendarEvents = buildCalendarFromFixtures(fixtures, club.id)
        val day1Date = formatGameDate(1)

        val initialTx = FinancialTransaction(
            id = "TX_INIT_1",
            dayIndex = 1,
            dateLabel = day1Date,
            category = "SÖZLEŞME",
            amountEur = startingBalanceEur,
            description = "${club.name} imza parası ve başlangıç hesap bakiyesi",
            balanceAfterEur = startingBalanceEur
        )

        val initialNews = listOf(
            NewsEvent(
                id = "NEWS_INIT_1",
                sourceGameEventId = "EVT_CAREER_START",
                dayIndex = 1,
                dateLabel = day1Date,
                category = "KULÜP",
                headline = "${player.fullName}, ${club.name} İle Yeni Sezon Hazırlıklarına Başladı!",
                body = "${club.leagueName} ekiplerinden ${club.name}'da teknik direktör ${club.managerName}, ${player.age} yaşındaki ${player.primaryPosition.titleTr} oyuncusu ${player.fullName}'ın antrenman temposunu yakından takip ediyor.",
                relatedClubName = club.name,
                fanReaction = "Destekleyici"
            )
        )

        val initialSocial = listOf(
            SocialMediaPost(
                id = "SOC_INIT_1",
                dayIndex = 1,
                dateLabel = day1Date,
                authorName = "${club.name} Taraftar Platformu",
                authorHandle = "@${club.shortName.lowercase()}_tribun",
                isUserPost = false,
                content = "Yeni sezon başlıyor! ${player.fullName} (#${player.kitNumber}) bu sezon sahada neler yapacak merakla bekliyoruz! ⚽🔥",
                likesCount = 342,
                commentsCount = 48,
                sentiment = "Destekleyici",
                topFanComments = listOf(
                    "İlk maçta sahada görmek istiyoruz!",
                    "Antrenmanlarda çok hırslı görünüyor, yolu açık olsun."
                )
            )
        )

        val initialTrust = if (player.isRealPlayer || player.overallRating >= club.teamStrength) 68 else 52
        val startingStatus = if (initialTrust >= 58) StartingStatus.STARTER else StartingStatus.BENCH

        val now = System.currentTimeMillis()
        val baseState = CareerSaveState(
            saveSlotId = slotId,
            saveVersion = 3,
            careerTitle = "${player.fullName} • ${club.shortName}",
            createdAtMs = now,
            updatedAtMs = now,
            currentDayIndex = 1,
            currentSeasonYear = 2026,
            currentLeagueId = leagueId,
            player = player,
            contract = contract,
            balanceEur = startingBalanceEur,
            managerTrust = initialTrust,
            teamChemistry = 62,
            nextMatchStartingStatus = startingStatus,
            trainedToday = false,
            socialActionCountToday = 0,
            pressCompletedForLastMatch = true,
            clubs = clubs,
            standings = standings,
            fixtures = fixtures,
            topScorers = scorers,
            npcs = npcs,
            calendarEvents = calendarEvents,
            transactions = listOf(initialTx),
            newsEvents = initialNews,
            socialPosts = initialSocial,
            eventHistory = listOf(
                GameEvent(
                    id = "EVT_CAREER_START",
                    dayIndex = 1,
                    timestampMs = now,
                    eventType = "CAREER_CREATED",
                    contextDescription = "${player.fullName} kariyeri ${club.name} kulübünde başlatıldı.",
                    appliedEffects = "Başlangıç Bakiye: ${FLFormatters.formatCurrency(startingBalanceEur)}, Güven: $initialTrust"
                )
            )
        )
        return ensureVersion3EnrichedState(baseState)
    }

    /**
     * Generates a 14-week double round-robin schedule for an 8-club league.
     * Matches occur every 3 days (Day 2, Day 5, Day 8, Day 11, ...),
     * so on Day 1 the user arrives at the training ground, trains, gets family message,
     * and on Day 2 the first match squad is announced and played!
     */
    private fun generateLeagueSchedule(leagueId: String, clubs: List<Club>): List<LeagueFixture> {
        val n = clubs.size
        if (n < 2) return emptyList()
        val fixtures = mutableListOf<LeagueFixture>()
        val rotating = clubs.drop(1).toMutableList()
        val fixed = clubs.first()
        val roundsFirstHalf = n - 1

        for (round in 0 until roundsFirstHalf * 2) {
            val weekNumber = round + 1
            val matchDayIndex = 2 + round * 3 // Day 2, 5, 8, 11, 14...
            val dateLabel = formatGameDate(matchDayIndex)
            val isSecondHalf = round >= roundsFirstHalf

            val pairs = mutableListOf<Pair<Club, Club>>()
            val firstOpponent = rotating.last()
            if ((round % 2 == 0) xor isSecondHalf) {
                pairs.add(fixed to firstOpponent)
            } else {
                pairs.add(firstOpponent to fixed)
            }

            for (i in 0 until (n / 2) - 1) {
                val c1 = rotating[i]
                val c2 = rotating[rotating.size - 2 - i]
                if (isSecondHalf) {
                    pairs.add(c2 to c1)
                } else {
                    pairs.add(c1 to c2)
                }
            }

            pairs.forEachIndexed { idx, (home, away) ->
                fixtures.add(
                    LeagueFixture(
                        id = "${leagueId}_W${weekNumber}_M$idx",
                        leagueId = leagueId,
                        week = weekNumber,
                        dayIndex = matchDayIndex,
                        dateLabel = dateLabel,
                        kickOffTime = if (idx % 2 == 0) "20:00" else "17:00",
                        homeClubId = home.id,
                        homeClubName = home.name,
                        awayClubId = away.id,
                        awayClubName = away.name,
                        stadiumName = home.stadiumName
                    )
                )
            }

            // Rotate
            val last = rotating.removeAt(rotating.lastIndex)
            rotating.add(0, last)
        }
        return fixtures
    }

    private fun buildInitialScorerPool(clubs: List<Club>, userPlayer: PlayerProfile): List<LeagueScorerEntry> {
        val list = mutableListOf(
            LeagueScorerEntry(
                playerId = userPlayer.id,
                playerName = userPlayer.fullName,
                clubId = userPlayer.currentSeasonStats.clubId,
                clubName = userPlayer.currentSeasonStats.clubName,
                goals = 0,
                assists = 0,
                matches = 0,
                averageRating = 6.8,
                isUserPlayer = true
            )
        )
        clubs.forEach { club ->
            val attackers = club.keySquadMembers.filter {
                it.position in listOf(Position.ST, Position.WG, Position.AM)
            }.take(2)
            attackers.forEach { member ->
                list.add(
                    LeagueScorerEntry(
                        playerId = member.id,
                        playerName = member.name,
                        clubId = club.id,
                        clubName = club.shortName,
                        goals = 0,
                        assists = 0,
                        matches = 0,
                        averageRating = 6.8,
                        isUserPlayer = false
                    )
                )
            }
        }
        return list
    }

    private fun buildInitialNpcs(
        player: PlayerProfile,
        club: Club,
        contract: Contract
    ): List<NpcCharacter> {
        val day1Date = formatShortDate(1)
        val captain = club.keySquadMembers.find { it.isCaptain } ?: club.keySquadMembers.first()
        return listOf(
            NpcCharacter(
                id = "NPC_MANAGER",
                name = club.managerName,
                roleTitle = "Teknik Direktör",
                avatarColorHex = "#20C773",
                personality = "Disiplinli & Taktik Odaklı (${club.managerTacticalStyle})",
                affinity = 55,
                trust = if (player.isRealPlayer) 70 else 52,
                currentMood = "Gözlemci",
                shortTermGoal = "İlk hafta maçına hazır bir kadro çıkarmak",
                longTermGoal = "${club.name} ile sezonu üst sıralarda tamamlamak",
                importantMemories = listOf("Kulüpteki ilk gününde tesislere katıldın."),
                quickReplyOptions = listOf(
                    "Hocam, antrenmanda elimden gelenin en iyisini yapacağım.",
                    "İlk maçta ilk 11'de görev almaya hazırım hocam.",
                    "Taktik sisteminizde benden özel beklentiniz nedir?"
                ),
                messages = listOf(
                    ChatMessage(
                        id = "MSG_MGR_1",
                        senderName = club.managerName,
                        isFromUser = false,
                        text = "Aramıza hoş geldin ${player.firstName}. Bugün tesislerdeki ilk günün. Yarına ilk lig maçımız var; bugünkü antrenman performansın kadro kararımı doğrudan etkileyecek.",
                        dayIndex = 1,
                        timeLabel = "$day1Date • 09:30",
                        emotionTag = "Ciddi"
                    )
                ),
                unreadCount = 1
            ),
            NpcCharacter(
                id = "NPC_FAMILY",
                name = "Babam (Hasan ${player.lastName})",
                roleTitle = "Aile",
                avatarColorHex = "#E6B84D",
                personality = "Destekleyici & Gururlu",
                affinity = 92,
                trust = 95,
                currentMood = "Gururlu",
                shortTermGoal = "Senin ilk maçında sahada başarılı olmanı görmek",
                longTermGoal = "Ailenin gururu olarak sağlıklı bir kariyer sürdürmen",
                importantMemories = listOf("${club.name} sözleşmeni imzaladığın gün."),
                quickReplyOptions = listOf(
                    "Çok sağ ol baba, dualarınızı eksik etmeyin.",
                    "Tesisler harika, bugün sıkı bir antrenman yapacağım.",
                    "İlk maaşımla size güzel bir sürpriz yapacağım."
                ),
                messages = listOf(
                    ChatMessage(
                        id = "MSG_FAM_1",
                        senderName = "Babam",
                        isFromUser = false,
                        text = "Oğlum, annenle beraber formanın fotoğrafına bakıp duruyoruz. ${club.name} senin için büyük bir başlangıç. Kendine dikkat et, akşam antrenman sonrası haber ver.",
                        dayIndex = 1,
                        timeLabel = "$day1Date • 10:15",
                        emotionTag = "Duygusal"
                    )
                ),
                unreadCount = 1
            ),
            NpcCharacter(
                id = "NPC_AGENT",
                name = contract.agentName,
                roleTitle = "Menajer",
                avatarColorHex = "#5DADE2",
                personality = "Stratejik & Piyasa Odaklı",
                affinity = 68,
                trust = 72,
                currentMood = "Motif",
                shortTermGoal = "İlk maçlarda vitrine çıkmanı sağlamak",
                longTermGoal = "Seni üst liglere ve yüksek maaşlı sözleşmelere taşımak",
                importantMemories = listOf("${club.name} ile haftalık ${contract.weeklyWageEur} € sözleşme imzalandı."),
                quickReplyOptions = listOf(
                    "Sözleşme şartlarımdan memnunum, sahaya odaklanacağım.",
                    "Üst lig kulüplerinin gözlemcileri maçlarımızı izliyor mu?",
                    "Sponsorluk fırsatları için çalışma yapabilir misin?"
                ),
                messages = listOf(
                    ChatMessage(
                        id = "MSG_AGT_1",
                        senderName = contract.agentName,
                        isFromUser = false,
                        text = "${player.firstName}, ${club.name} sözleşmeni resmiyete döktük. Haftalık ${contract.weeklyWageEur} € maaş ve maç başı ${contract.matchAppearanceBonusEur} € prim alacaksın. Sahada parlarsan devre arasında teklifler yağar!",
                        dayIndex = 1,
                        timeLabel = "$day1Date • 11:00",
                        emotionTag = "Profesyonel"
                    )
                ),
                unreadCount = 1
            ),
            NpcCharacter(
                id = "NPC_CAPTAIN",
                name = "${captain.name} (#${captain.kitNumber})",
                roleTitle = "Takım Kaptanı",
                avatarColorHex = "#2ECC71",
                personality = "Lider & Birleştirici",
                affinity = 60,
                trust = 60,
                currentMood = "Samimi",
                shortTermGoal = "Soyunma odasında uyumu yüksek tutmak",
                longTermGoal = "Sezon sonunda kupaya uzanmak",
                importantMemories = listOf("Soyunma odasında seni takımla tanıştırdı."),
                quickReplyOptions = listOf(
                    "Eyvallah kaptan, sahada her şeyimi vereceğim.",
                    "Takımın oyun planına hızla uyum sağlamak istiyorum.",
                    "Maçtan sonra takım yemeği organize edelim mi?"
                ),
                messages = listOf(
                    ChatMessage(
                        id = "MSG_CAP_1",
                        senderName = captain.name,
                        isFromUser = false,
                        text = "Soyunma odasına hoş geldin kardeşim. Bir şeye ihtiyacın olursa çekinme. Yarınki maçta birlikte savaşacağız!",
                        dayIndex = 1,
                        timeLabel = "$day1Date • 11:40",
                        emotionTag = "Dostça"
                    )
                ),
                unreadCount = 0
            )
        )
    }

    private fun buildCalendarFromFixtures(
        fixtures: List<LeagueFixture>,
        userClubId: String
    ): List<CalendarEvent> {
        val events = mutableListOf<CalendarEvent>()
        val userFixtures = fixtures.filter { it.homeClubId == userClubId || it.awayClubId == userClubId }

        for (day in 1..45) {
            val dateLabel = formatGameDate(day)
            val matchOnDay = userFixtures.find { it.dayIndex == day }
            if (matchOnDay != null) {
                val isHome = matchOnDay.homeClubId == userClubId
                val opponentName = if (isHome) matchOnDay.awayClubName else matchOnDay.homeClubName
                val venueTag = if (isHome) "İç Saha (${matchOnDay.stadiumName})" else "Deplasman (${matchOnDay.stadiumName})"
                events.add(
                    CalendarEvent(
                        id = "CAL_MATCH_${matchOnDay.id}",
                        dayIndex = day,
                        dateLabel = dateLabel,
                        timeLabel = matchOnDay.kickOffTime,
                        location = venueTag,
                        eventType = "MAÇ",
                        title = "Hafta ${matchOnDay.week}: ${matchOnDay.homeClubName} vs ${matchOnDay.awayClubName}",
                        description = "Rakip: $opponentName • $venueTag",
                        opponentClubId = if (isHome) matchOnDay.awayClubId else matchOnDay.homeClubId,
                        fixtureId = matchOnDay.id,
                        completed = matchOnDay.played
                    )
                )
            } else {
                val nextMatch = userFixtures.find { it.dayIndex == day + 1 }
                if (nextMatch != null) {
                    events.add(
                        CalendarEvent(
                            id = "CAL_TACTIC_$day",
                            dayIndex = day,
                            dateLabel = dateLabel,
                            timeLabel = "15:00",
                            location = "Kulüp Tesisleri",
                            eventType = "ANTRENMAN",
                            title = "Maç Öncesi Taktik Prova & Kadro Açıklaması",
                            description = "Yarınki ${nextMatch.homeClubName} - ${nextMatch.awayClubName} maçı öncesi son idman."
                        )
                    )
                } else {
                    events.add(
                        CalendarEvent(
                            id = "CAL_TRAIN_$day",
                            dayIndex = day,
                            dateLabel = dateLabel,
                            timeLabel = "11:00",
                            location = "Kulüp Antrenman Sahası",
                            eventType = "ANTRENMAN",
                            title = "Gelişim Antrenmanı & Kondisyon",
                            description = "Bireysel ve takım gelişim programı."
                        )
                    )
                }
            }
        }
        return events
    }

    /**
     * Executes a Training Drill and updates player sub-attributes, overall, energy, morale,
     * manager trust, calendar, and Evening Family message on Day 1!
     */
    fun performTrainingSession(
        state: CareerSaveState,
        drill: TrainingDrillDefinition
    ): Pair<CareerSaveState, String> {
        val (progressedState, feedback) = RealisticProgressionEngine.resolveTrainingSession(state, drill)
        if (!progressedState.trainedToday) {
            return progressedState to feedback
        }

        val p = progressedState.player
        val injuredNow = p.injuryDaysRemaining > state.player.injuryDaysRemaining
        val trustDelta = if (drill.id == "DRILL_RECOVERY") 2 else if (injuredNow) 0 else 4
        val newTrust = (state.managerTrust + trustDelta).coerceIn(0, 100)
        val newStartingStatus = computeStartingStatus(p, state.currentClub, newTrust)

        val updatedCalendar = state.calendarEvents.map { ev ->
            if (ev.dayIndex == state.currentDayIndex && ev.eventType == "ANTRENMAN") {
                ev.copy(
                    completed = true,
                    outcomeSummary = "${drill.title} tamamlandı"
                )
            } else {
                ev
            }
        }

        val shortDate = formatShortDate(state.currentDayIndex)
        val updatedNpcs = state.npcs.map { npc ->
            when (npc.id) {
                "NPC_FAMILY" -> if (state.currentDayIndex == 1) {
                    val eveningMsg = ChatMessage(
                        id = "MSG_FAM_EVENING_${state.currentDayIndex}",
                        senderName = npc.name,
                        isFromUser = false,
                        text = "İyi akşamlar oğlum! İlk antrenmanın nasıl geçti? Yarınki maç kadrosunda yer alacak mısın, çok heyecanlıyız!",
                        dayIndex = state.currentDayIndex,
                        timeLabel = "$shortDate • 19:45",
                        emotionTag = "Heyecanlı"
                    )
                    npc.copy(
                        messages = npc.messages + eveningMsg,
                        unreadCount = npc.unreadCount + 1
                    )
                } else npc
                "NPC_MANAGER" -> {
                    val mgrText = if (injuredNow) {
                        "Geçmiş olsun ${p.firstName}, idmanda talihsiz bir sakatlık yaşadın. Sağlık heyetinin programına harfiyen uy."
                    } else {
                        "Bugünkü '${drill.title}' çalışmasındaki disiplininden memnun kaldım. Kadro durumun: ${newStartingStatus.labelTr}."
                    }
                    val mgrMsg = ChatMessage(
                        id = "MSG_MGR_TRN_${state.currentDayIndex}",
                        senderName = npc.name,
                        isFromUser = false,
                        text = mgrText,
                        dayIndex = state.currentDayIndex,
                        timeLabel = "$shortDate • 17:30",
                        emotionTag = if (injuredNow) "Endişeli" else "Memnun",
                        impactSummary = if (injuredNow) "Sakatlık Yaşandı" else "Teknik Direktör Güveni +$trustDelta"
                    )
                    npc.copy(
                        trust = newTrust,
                        messages = npc.messages + mgrMsg,
                        unreadCount = npc.unreadCount + 1
                    )
                }
                else -> npc
            }
        }

        val gameEvent = GameEvent(
            id = "EVT_TRAIN_D${state.currentDayIndex}_${System.currentTimeMillis()}",
            dayIndex = state.currentDayIndex,
            timestampMs = System.currentTimeMillis(),
            eventType = "TRAINING_COMPLETED",
            contextDescription = drill.title,
            appliedEffects = feedback
        )

        val dateLbl = formatShortDate(state.currentDayIndex)
        val updatedAttrHistory = state.attributeHistory + AttributeHistoryPoint(
            dayIndex = state.currentDayIndex,
            dateLabel = dateLbl,
            overall = p.overallRating,
            pace = p.attributes.cardPace,
            shooting = p.attributes.cardShooting,
            passing = p.attributes.cardPassing,
            dribbling = p.attributes.cardDribbling,
            defending = p.attributes.cardDefending,
            physical = p.attributes.cardPhysical,
            averageMatchRating = p.currentSeasonStats.averageRating
        )

        val updatedInjuries = if (injuredNow) {
            listOf(
                InjuryRecord(
                    id = "INJ_D${state.currentDayIndex}",
                    dayIndex = state.currentDayIndex,
                    dateLabel = formatGameDate(state.currentDayIndex, state.currentSeasonYear),
                    injuryType = "Antrenman Kas Zorlanması (${drill.title})",
                    recoveryDays = p.injuryDaysRemaining,
                    matchesMissed = 0
                )
            ) + state.injuryHistory
        } else state.injuryHistory

        return progressedState.copy(
            updatedAtMs = System.currentTimeMillis(),
            managerTrust = newTrust,
            nextMatchStartingStatus = newStartingStatus,
            npcs = updatedNpcs,
            calendarEvents = updatedCalendar,
            attributeHistory = updatedAttrHistory,
            injuryHistory = updatedInjuries,
            eventHistory = listOf(gameEvent) + state.eventHistory
        ) to feedback
    }

    /**
     * Applies a completed match report once and only once, simulates the rest of the league week,
     * updates standings, top scorers, player stats, contract bonuses, news, and social media.
     */
    fun applyCompletedMatch(
        state: CareerSaveState,
        report: CompletedMatchReport
    ): CareerSaveState {
        // Guard against duplicate match reward or replay
        val targetFixture = state.fixtures.find { it.id == report.fixtureId }
        if (targetFixture == null || targetFixture.played) {
            return state
        }
        val eventKey = "EVT_MATCH_${report.fixtureId}"
        if (state.eventHistory.any { it.id == eventKey }) {
            return state
        }

        val week = targetFixture.week
        val rng = Random(20261114L + week * 409L + state.currentLeagueId.hashCode())

        // 1. Update fixtures for this week (User's fixture + AI vs AI fixtures in same week)
        val clubMap = state.clubs.associateBy { it.id }
        val aiGoalLogs = mutableListOf<Triple<String, Int, Int>>() // clubId, goals, assists

        val updatedFixtures = state.fixtures.map { fx ->
            when {
                fx.id == report.fixtureId -> {
                    fx.copy(
                        played = true,
                        homeScore = report.homeScore,
                        awayScore = report.awayScore,
                        goalSummary = report.events.filter { it.type == "GOL" }.map { it.description }
                    )
                }
                fx.week == week && !fx.played -> {
                    val hStr = clubMap[fx.homeClubId]?.teamStrength ?: 60
                    val aStr = clubMap[fx.awayClubId]?.teamStrength ?: 60
                    val hAdv = (hStr + 3 - aStr).coerceIn(-15, 15)
                    val hGoals = max(0, (rng.nextInt(3) + (if (hAdv > 4) 1 else 0) + (if (rng.nextInt(100) < 25) 1 else 0)))
                    val aGoals = max(0, (rng.nextInt(3) + (if (hAdv < -4) 1 else 0)))
                    aiGoalLogs.add(Triple(fx.homeClubId, hGoals, max(0, hGoals - 1)))
                    aiGoalLogs.add(Triple(fx.awayClubId, aGoals, max(0, aGoals - 1)))
                    fx.copy(
                        played = true,
                        homeScore = hGoals,
                        awayScore = aGoals
                    )
                }
                else -> fx
            }
        }

        // 2. Recompute League Standings deterministically from all played fixtures
        val updatedStandings = recomputeStandingsFromFixtures(state.clubs, updatedFixtures)

        // 3. Update Player Season Stats, Energy, Morale, Form, Market Value, Followers
        val p = state.player
        val oldStats = p.currentSeasonStats
        val playedAny = report.userPlayedMinutes > 0
        val newSeasonStats = oldStats.copy(
            appearances = oldStats.appearances + (if (playedAny) 1 else 0),
            starts = oldStats.starts + (if (report.userStarted) 1 else 0),
            minutesPlayed = oldStats.minutesPlayed + report.userPlayedMinutes,
            goals = oldStats.goals + report.userGoals,
            assists = oldStats.assists + report.userAssists,
            totalRatingSum = oldStats.totalRatingSum + (if (playedAny) report.userRating else 0.0)
        )

        // Match performance progression bonus via RealisticProgressionEngine
        val (progPlayer, matchAttrDesc) = RealisticProgressionEngine.resolveMatchProgression(
            player = p,
            club = state.currentClub,
            report = report
        )
        val updatedAttributes = progPlayer.attributes

        val userIsHome = report.homeClubId == state.contract.clubId
        val userWon = if (userIsHome) report.homeScore > report.awayScore else report.awayScore > report.homeScore
        val userDrew = report.homeScore == report.awayScore

        val moraleDelta = when {
            report.userGoals > 0 -> +12
            userWon -> +8
            userDrew -> +1
            else -> -6
        }
        val formDelta = progPlayer.form - p.form
        val trustDelta = ((report.userRating - 6.4) * 7.0).roundToInt() + (if (userWon) 3 else -1)

        val tierProfile = LeagueDifficultyCalculator.getProfileForTier(state.currentClub.tier)
        val baseValUnit = (tierProfile.marketValueMinEur * 0.10).toLong().coerceAtLeast(500L)
        val valueDelta = (report.userGoals * (baseValUnit * 3)) +
            (report.userAssists * (baseValUnit * 2)) +
            (if (report.userRating >= 7.5) baseValUnit else 0L)
        val followersGain = (report.userGoals * (tierProfile.scoutVisibilityFactor * 450).roundToInt().coerceAtLeast(15)) +
            (report.userAssists * (tierProfile.scoutVisibilityFactor * 220).roundToInt().coerceAtLeast(10)) +
            (if (userWon) (tierProfile.scoutVisibilityFactor * 120).roundToInt().coerceAtLeast(15) else 5)

        val updatedPlayer = p.copy(
            attributes = updatedAttributes,
            energy = (p.energy - report.energySpent).coerceIn(12, 100),
            morale = (p.morale + moraleDelta).coerceIn(15, 100),
            form = progPlayer.form,
            marketValueEur = (p.marketValueEur + valueDelta).coerceIn(tierProfile.marketValueMinEur, tierProfile.marketValueMaxEur * 3),
            followersCount = p.followersCount + followersGain,
            fanSupport = (p.fanSupport + (if (report.userRating >= 7.0) 5 else -2)).coerceIn(10, 100),
            currentSeasonStats = newSeasonStats
        )

        // 4. Update Top Scorers & Assists Leaderboard
        val updatedScorers = state.topScorers.map { entry ->
            if (entry.isUserPlayer) {
                entry.copy(
                    goals = newSeasonStats.goals,
                    assists = newSeasonStats.assists,
                    matches = newSeasonStats.appearances,
                    averageRating = newSeasonStats.averageRating
                )
            } else {
                val clubLog = aiGoalLogs.find { it.first == entry.clubId }
                val addG = if (clubLog != null && clubLog.second > 0 && rng.nextBoolean()) 1 else 0
                val addA = if (clubLog != null && clubLog.third > 0 && rng.nextBoolean()) 1 else 0
                entry.copy(
                    goals = entry.goals + addG,
                    assists = entry.assists + addA,
                    matches = entry.matches + 1
                )
            }
        }.sortedWith(compareByDescending<LeagueScorerEntry> { it.goals }.thenByDescending { it.assists })

        // 5. Economy: Pay Match Appearance Bonus + Goal Bonus
        val appearanceEarned = if (playedAny) state.contract.matchAppearanceBonusEur else 0L
        val goalEarned = report.userGoals * state.contract.goalBonusEur
        val totalBonusEarned = appearanceEarned + goalEarned
        val newBalance = state.balanceEur + totalBonusEarned
        val dateLabel = formatGameDate(state.currentDayIndex, state.currentSeasonYear)

        val newTransactions = state.transactions.toMutableList()
        if (totalBonusEarned > 0L) {
            newTransactions.add(
                0,
                FinancialTransaction(
                    id = "TX_MATCH_${report.fixtureId}",
                    dayIndex = state.currentDayIndex,
                    dateLabel = dateLabel,
                    category = "MAÇ_PRİMİ",
                    amountEur = totalBonusEarned,
                    description = "Hafta $week Maç ve Gol Primi (${report.homeClubName} ${report.homeScore}-${report.awayScore} ${report.awayClubName})",
                    balanceAfterEur = newBalance
                )
            )
        }

        // 6. News, Social Media, and NPC Messages (with clean Turkish rating format)
        val ratingStr = FLFormatters.formatRating(report.userRating)
        val newNews = NewsEvent(
            id = "NEWS_MATCH_${report.fixtureId}",
            sourceGameEventId = eventKey,
            dayIndex = state.currentDayIndex,
            dateLabel = dateLabel,
            category = "MAÇ SONUCU",
            headline = report.pressHeadline,
            body = "Ligin $week. haftasında ${report.homeClubName} ile ${report.awayClubName} karşı karşıya geldi (${report.homeScore}-${report.awayScore}). ${updatedPlayer.fullName} maçı $ratingStr reyting, ${report.userGoals} gol ve ${report.userAssists} asist ile tamamladı.",
            relatedClubName = state.currentClub.name,
            fanReaction = if (report.userRating >= 7.0) "Destekleyici" else "Tarafsız"
        )

        val postMatchInstaComments = if (report.userGoals > 0 || report.userRating >= 7.3) {
            listOf(
                InstaCommentItem(
                    id = "IC_MATCH_${report.fixtureId}_1",
                    authorHandle = "433",
                    authorName = "433",
                    isVerified = true,
                    avatarHex = "#FF3B30",
                    text = "Maçın yıldızı sahnede! ($ratingStr Reyting) 🔥⚽",
                    timeLabel = "5d",
                    likesCount = 840 + report.userGoals * 310
                ),
                InstaCommentItem(
                    id = "IC_MATCH_${report.fixtureId}_2",
                    authorHandle = "transfermarkt_tr",
                    authorName = "Transfermarkt TR",
                    isVerified = true,
                    avatarHex = "#00A6FF",
                    text = "Güncel piyasa değeri ${FLFormatters.formatMarketValue(updatedPlayer.marketValueEur)} seviyesine yükseldi 📈",
                    timeLabel = "8d",
                    likesCount = 420
                ),
                InstaCommentItem(
                    id = "IC_MATCH_${report.fixtureId}_3",
                    authorHandle = "${state.currentClub.shortName.lowercase()}_ultras",
                    authorName = "${state.currentClub.shortName} Tribün",
                    isVerified = false,
                    avatarHex = state.currentClub.primaryColorHex,
                    text = "${updatedPlayer.kitName} bugün sahada resmen ders verdi! Tribünler seninle gurur duyuyor 👏🔥",
                    timeLabel = "12d",
                    likesCount = 195
                )
            )
        } else {
            listOf(
                InstaCommentItem(
                    id = "IC_MATCH_${report.fixtureId}_1",
                    authorHandle = "${state.currentClub.shortName.lowercase()}_taraftar",
                    authorName = "${state.currentClub.shortName} Sevdalısı",
                    isVerified = false,
                    avatarHex = state.currentClub.primaryColorHex,
                    text = "Gelecek hafta daha skorer ve baskılı bir oyun bekliyoruz! Pes etmek yok 💪",
                    timeLabel = "6d",
                    likesCount = 74
                ),
                InstaCommentItem(
                    id = "IC_MATCH_${report.fixtureId}_2",
                    authorHandle = "futbol_analiz_tr",
                    authorName = "Futbol Analiz",
                    isVerified = true,
                    avatarHex = "#1D9BF0",
                    text = "${updatedPlayer.fullName} maçı $ratingStr reytingle tamamladı. Antrenman temposunu artırması kritik.",
                    timeLabel = "10d",
                    likesCount = 48
                )
            )
        }

        val fanPost = SocialMediaPost(
            id = "SOC_MATCH_${report.fixtureId}",
            dayIndex = state.currentDayIndex,
            dateLabel = dateLabel,
            authorName = "${state.currentClub.name} Resmi",
            authorHandle = "@${state.currentClub.shortName.lowercase()}resmi",
            isUserPost = false,
            content = "MAÇ SONUCU | ${report.homeClubName} ${report.homeScore} - ${report.awayScore} ${report.awayClubName}\n⭐ ${updatedPlayer.fullName}: $ratingStr Reyting | ${report.userGoals} Gol | ${report.userAssists} Asist",
            likesCount = 920 + followersGain * 3,
            commentsCount = postMatchInstaComments.size + 42 + report.userGoals * 24,
            sentiment = if (report.userRating >= 7.0) "Destekleyici" else "Eleştirel",
            topFanComments = postMatchInstaComments.map { "${it.authorHandle}: ${it.text}" },
            instaComments = postMatchInstaComments,
            isVerifiedAuthor = true,
            locationLabel = state.currentClub.stadiumName,
            imageSceneType = "STADIUM"
        )

        val shortDate = formatShortDate(state.currentDayIndex)
        val newTrust = (state.managerTrust + trustDelta).coerceIn(10, 100)
        val captainName = state.currentClub.keySquadMembers.firstOrNull { it.isCaptain }?.name ?: "Kaptan"

        val updatedNpcs = state.npcs.map { npc ->
            when (npc.id) {
                "NPC_MANAGER" -> {
                    val msg = ChatMessage(
                        id = "MSG_MGR_POST_${report.fixtureId}",
                        senderName = npc.name,
                        isFromUser = false,
                        text = report.managerFeedback.substringAfter(": \"").removeSuffix("\""),
                        dayIndex = state.currentDayIndex,
                        timeLabel = "21:45",
                        emotionTag = if (report.userRating >= 7.0) "Gururlu" else "Eleştirel",
                        impactSummary = "Maç Reytingi: $ratingStr • Güven ${if (trustDelta >= 0) "+$trustDelta" else "$trustDelta"}"
                    )
                    npc.copy(
                        trust = newTrust,
                        quickReplyOptions = listOf(
                            "Hocam gelecek maçta da İlk 11'de hazırım!",
                            "Eksiklerimi antrenmanda kapatacağım hocam.",
                            "Duran toplarda sorumluluk almak istiyorum."
                        ),
                        messages = npc.messages + msg,
                        unreadCount = npc.unreadCount + 1
                    )
                }
                "NPC_AGENT" -> {
                    val msg = ChatMessage(
                        id = "MSG_AGT_POST_${report.fixtureId}",
                        senderName = npc.name,
                        isFromUser = false,
                        text = if (report.userGoals > 0 || report.userAssists > 0 || report.userRating >= 7.3) {
                            "Harika maç çıkardın ${updatedPlayer.firstName}! Tribündeki gözlemciler adını not etti. Transfermarkt piyasa değerin ${FLFormatters.formatMarketValue(updatedPlayer.marketValueEur)} seviyesine yükseldi, kulüplerden teklif isteyelim mi?"
                        } else {
                            "Maçı tribünden takip ettim ${updatedPlayer.firstName} ($ratingStr reyting). Moralini bozma, önümüzdeki hafta skora katkı yaparsan transfer piyasasını hareketlendiririz."
                        },
                        dayIndex = state.currentDayIndex,
                        timeLabel = "22:00",
                        emotionTag = "Heyecanlı",
                        impactSummary = if (totalBonusEarned > 0) "Maç & Gol Primi: +${FLFormatters.formatCurrency(totalBonusEarned)}" else null
                    )
                    npc.copy(
                        quickReplyOptions = listOf(
                            "Üst liglerden resmi transfer teklifi ara.",
                            "Kulüple maaş zammı ve prim görüşmesi yapalım.",
                            "Sponsorluk ve marka anlaşmalarını hızlandıralım."
                        ),
                        messages = npc.messages + msg,
                        unreadCount = npc.unreadCount + 1
                    )
                }
                "NPC_FAMILY" -> {
                    val msg = ChatMessage(
                        id = "MSG_FAM_POST_${report.fixtureId}",
                        senderName = npc.name,
                        isFromUser = false,
                        text = if (report.userGoals > 0) {
                            "Aslan oğlum benim! Attığın golde annenle havalara uçtuk! ${report.homeClubName} ${report.homeScore}-${report.awayScore} ${report.awayClubName} maçının kahramanı oldun, ayağına taş değmesin ❤️⚽"
                        } else if (userWon) {
                            "Tebrikler evladım! ${report.homeScore}-${report.awayScore} galibiyetinizi televizyondan gururla izledik. İyi dinlen!"
                        } else {
                            "Canın sağ olsun oğlum, futbolda her sonuç var (${report.homeScore}-${report.awayScore}). Sen çalışmaya devam et, biz hep senin yanındayız ❤️"
                        },
                        dayIndex = state.currentDayIndex,
                        timeLabel = "22:10",
                        emotionTag = "Sevgi Dolu"
                    )
                    npc.copy(
                        quickReplyOptions = listOf(
                            "Dualarınız yeter baba, iyi ki varsınız!",
                            "Kazandığım primden size güzel bir sürpriz gönderiyorum.",
                            "Haftaya tribüne bekliyorum baba!"
                        ),
                        messages = npc.messages + msg,
                        unreadCount = npc.unreadCount + 1
                    )
                }
                "NPC_TEAM_GROUP" -> {
                    val msg = ChatMessage(
                        id = "MSG_GRP_POST_${report.fixtureId}",
                        senderName = captainName,
                        isFromUser = false,
                        text = if (userWon) {
                            "Soyunma odası yıkılıyor beyler! ${report.homeScore}-${report.awayScore} aldık maçı! ${if (report.userGoals > 0) "${updatedPlayer.firstName} golün efsaneydi kardeşim! 🔥⚽" else "Herkesin ayağına sağlık! 💪"}"
                        } else {
                            "Beyler ${report.homeScore}-${report.awayScore} bitti ama kafaları kaldırmak yok, yarın idmanda kenetlenip telafi edeceğiz!"
                        },
                        dayIndex = state.currentDayIndex,
                        timeLabel = "22:15",
                        emotionTag = "Takım Ruhu"
                    )
                    npc.copy(
                        quickReplyOptions = listOf(
                            "Takımca harika mücadele ettik beyler! 🔥",
                            "Yarın idman çıkışı yemek benden, moral depolayalım!",
                            "Gelecek hafta sahayı dar edeceğiz!"
                        ),
                        messages = npc.messages + msg,
                        unreadCount = npc.unreadCount + 1
                    )
                }
                else -> npc
            }
        }

        // 7. Mark calendar event completed
        val updatedCalendar = state.calendarEvents.map { ev ->
            if (ev.fixtureId == report.fixtureId) {
                ev.copy(
                    completed = true,
                    outcomeSummary = "Skor: ${report.homeScore}-${report.awayScore} (Reyting: $ratingStr)"
                )
            } else ev
        }

        // 8. Generate Realistic Transfer Offer if player meets strict performance & transfer window criteria
        val updatedOffers = TransferMarketEngine.maybeGenerateRealisticOffer(
            state = state,
            player = updatedPlayer,
            currentClub = state.currentClub,
            dayIndex = state.currentDayIndex
        )

        val enrichedReport = report.copy(wageAndBonusEarnedEur = totalBonusEarned)
        val nextStarting = computeStartingStatus(updatedPlayer, state.currentClub, newTrust)

        // 9. Update 2.0 Market Value History, Attribute History, Achievements, Stories, XPosts & Gallery
        val updatedMarketHistory = state.marketValueHistory + MarketValuePoint(
            dayIndex = state.currentDayIndex,
            dateLabel = shortDate,
            valueEur = updatedPlayer.marketValueEur,
            clubName = state.currentClub.shortName,
            reason = "Hafta $week (${report.homeClubName} ${report.homeScore}-${report.awayScore} ${report.awayClubName})"
        )

        val updatedAttrHistory = state.attributeHistory + AttributeHistoryPoint(
            dayIndex = state.currentDayIndex,
            dateLabel = shortDate,
            overall = updatedPlayer.overallRating,
            pace = updatedPlayer.attributes.cardPace,
            shooting = updatedPlayer.attributes.cardShooting,
            passing = updatedPlayer.attributes.cardPassing,
            dribbling = updatedPlayer.attributes.cardDribbling,
            defending = updatedPlayer.attributes.cardDefending,
            physical = updatedPlayer.attributes.cardPhysical,
            averageMatchRating = updatedPlayer.currentSeasonStats.averageRating
        )

        val newAchievements = state.achievements.toMutableList()
        if (newSeasonStats.appearances == 1 && newAchievements.none { it.id == "ACH_DEBUT" }) {
            newAchievements.add(
                CareerAchievement(
                    id = "ACH_DEBUT",
                    title = "Profesyonel İlk Maç",
                    description = "${state.currentClub.name} formasıyla ilk resmi maçına çıktın.",
                    dateLabel = dateLabel,
                    category = "KARİYER_TAŞI"
                )
            )
        }
        if (report.userGoals > 0 && newAchievements.none { it.id == "ACH_FIRST_GOAL" }) {
            newAchievements.add(
                CareerAchievement(
                    id = "ACH_FIRST_GOAL",
                    title = "Kariyerin İlk Golü",
                    description = "${report.homeClubName} - ${report.awayClubName} maçında fileleri havalandırdın.",
                    dateLabel = dateLabel,
                    category = "BİREYSEL"
                )
            )
        }
        if (report.userRating >= 8.0 && newAchievements.none { it.id == "ACH_MOTM_W$week" }) {
            newAchievements.add(
                CareerAchievement(
                    id = "ACH_MOTM_W$week",
                    title = "Haftanın Yıldız Performansı ($ratingStr)",
                    description = "Hafta $week karşılaşmasında üstün oyunla maçın yıldızı seçildin.",
                    dateLabel = dateLabel,
                    category = "BİREYSEL"
                )
            )
        }

        val matchGalleryPhoto = GalleryPhotoItem(
            id = "GAL_MATCH_${report.fixtureId}",
            dayIndex = state.currentDayIndex,
            dateLabel = dateLabel,
            album = if (report.userGoals > 0 || userWon) "Kutlamalar" else "Maçlar",
            title = "Hafta $week: ${report.homeClubName} ${report.homeScore}-${report.awayScore} ${report.awayClubName}",
            caption = "${updatedPlayer.fullName} sahada ($ratingStr reyting, ${report.userGoals} gol, ${report.userAssists} asist).",
            sceneDrawableType = if (userWon) "LOCKER" else "STADIUM"
        )

        val matchStory = StoryItem(
            id = "STORY_MATCH_${report.fixtureId}",
            authorName = state.currentClub.shortName + " Medya",
            authorRole = "Resmi Kulüp Hikayesi",
            avatarHex = state.currentClub.primaryColorHex,
            title = "Maç Sonu: ${report.homeScore}-${report.awayScore}",
            caption = "${updatedPlayer.fullName} bugünkü mücadeleyi $ratingStr reytingle tamamladı!",
            sceneType = "STADIUM",
            dayIndex = state.currentDayIndex
        )

        val matchXPost = XPostItem(
            id = "XP_MATCH_${report.fixtureId}",
            dayIndex = state.currentDayIndex,
            dateLabel = shortDate,
            authorName = "OptaAnaliz TR",
            handle = "@OptaAnaliz_TR",
            category = "YORUMCU",
            isVerified = true,
            text = "$ratingStr - ${updatedPlayer.fullName} (${state.currentClub.shortName}), ${report.homeClubName} - ${report.awayClubName} maçında ${report.userPassesCompleted}/${report.userPassesAttempted} pas isabeti ve ${report.userGoals} gol katkısıyla oynadı. Odak.",
            likes = 410 + followersGain,
            reposts = 62 + report.userGoals * 25
        )

        val baseMatchXPosts = listOf(matchXPost) + state.xPosts
        val finalXPosts = TransferMarketEngine.maybeGenerateTransferRumor(
            state = state.copy(xPosts = baseMatchXPosts),
            player = updatedPlayer,
            club = state.currentClub,
            dayIndex = state.currentDayIndex
        )

        val updatedInstaDmThreads = if (report.userGoals > 0 || report.userRating >= 7.2) {
            maybeGenerateFameDmRequest(state.instaDmThreads, updatedPlayer, state.currentClub, state.currentDayIndex)
        } else {
            state.instaDmThreads
        }

        return state.copy(
            updatedAtMs = System.currentTimeMillis(),
            currentHour = 21,
            player = updatedPlayer,
            balanceEur = newBalance,
            managerTrust = newTrust,
            nextMatchStartingStatus = nextStarting,
            pressCompletedForLastMatch = false,
            standings = updatedStandings,
            fixtures = updatedFixtures,
            topScorers = updatedScorers,
            npcs = updatedNpcs,
            calendarEvents = updatedCalendar,
            transactions = newTransactions,
            newsEvents = listOf(newNews) + state.newsEvents,
            socialPosts = listOf(fanPost) + state.socialPosts,
            transferOffers = updatedOffers,
            lastMatchReport = enrichedReport,
            marketValueHistory = updatedMarketHistory,
            attributeHistory = updatedAttrHistory,
            achievements = newAchievements,
            galleryPhotos = listOf(matchGalleryPhoto) + state.galleryPhotos,
            stories = listOf(matchStory) + state.stories,
            xPosts = finalXPosts,
            instaDmThreads = updatedInstaDmThreads,
            eventHistory = listOf(
                GameEvent(
                    id = eventKey,
                    dayIndex = state.currentDayIndex,
                    timestampMs = System.currentTimeMillis(),
                    eventType = "MATCH_COMPLETED",
                    contextDescription = "${report.homeClubName} ${report.homeScore}-${report.awayScore} ${report.awayClubName}",
                    appliedEffects = "Reyting: $ratingStr, Gol: ${report.userGoals}, Asist: ${report.userAssists}, Prim: +${FLFormatters.formatCurrency(totalBonusEarned)}"
                )
            ) + state.eventHistory
        )
    }

    fun recomputeStandingsFromFixtures(
        clubs: List<Club>,
        fixtures: List<LeagueFixture>
    ): List<StandingRow> {
        val playedFixtures = fixtures.filter { it.played && !it.isCupMatch }
        val rows = clubs.map { club ->
            var p = 0
            var w = 0
            var d = 0
            var l = 0
            var gf = 0
            var ga = 0
            val form = mutableListOf<String>()

            playedFixtures.forEach { fx ->
                if (fx.homeClubId == club.id) {
                    p++
                    gf += fx.homeScore
                    ga += fx.awayScore
                    when {
                        fx.homeScore > fx.awayScore -> { w++; form.add("G") }
                        fx.homeScore == fx.awayScore -> { d++; form.add("B") }
                        else -> { l++; form.add("M") }
                    }
                } else if (fx.awayClubId == club.id) {
                    p++
                    gf += fx.awayScore
                    ga += fx.homeScore
                    when {
                        fx.awayScore > fx.homeScore -> { w++; form.add("G") }
                        fx.awayScore == fx.homeScore -> { d++; form.add("B") }
                        else -> { l++; form.add("M") }
                    }
                }
            }
            StandingRow(
                clubId = club.id,
                clubName = club.name,
                shortName = club.shortName,
                primaryColorHex = club.primaryColorHex,
                played = p,
                won = w,
                drawn = d,
                lost = l,
                goalsFor = gf,
                goalsAgainst = ga,
                points = w * 3 + d,
                recentForm = form.takeLast(5)
            )
        }
        return rows.sortedWith(
            compareByDescending<StandingRow> { it.points }
                .thenByDescending { it.goalDifference }
                .thenByDescending { it.goalsFor }
                .thenBy { it.clubName }
        )
    }

    /**
     * Advances game time by 1 day.
     * Enforces central time progression, passive energy recovery, weekly salary + investment payouts,
     * squad announcement before match day, and dynamic career dilemmas.
     */
    fun advanceToNextDay(state: CareerSaveState): Pair<CareerSaveState, String> {
        val unplayedToday = state.todayUserFixture
        if (unplayedToday != null) {
            return state to "Bugün maç günün (${unplayedToday.homeClubName} vs ${unplayedToday.awayClubName})! Sonraki güne geçmeden önce maçı oynamalısın."
        }

        // Penalty if player skipped training on a non-match day without injury
        val skippedTrainingPenalty = if (!state.trainedToday && state.player.injuryDaysRemaining == 0) -2 else 0
        val nextDay = state.currentDayIndex + 1
        val nextDateLabel = formatGameDate(nextDay, state.currentSeasonYear)
        val shortDate = formatShortDate(nextDay)

        // Calculate passive energy recovery from home + owned lifestyle items
        val ownedItems = WorldDataCatalog.lifestylePurchaseItems.filter { it.id in state.ownedLifestyleItemIds }
        val passiveEnergyBonus = ownedItems.sumOf { it.passiveEnergyRecoveryBonus }
        val passiveMoraleBonus = (ownedItems.sumOf { it.passiveMoraleBonus } / 4).coerceAtMost(5)
        val passiveDividendEur = ownedItems.sumOf { it.weeklyDividendEur }

        val recoveredEnergy = (state.player.energy + 18 + passiveEnergyBonus).coerceIn(15, 100)
        val newInjuryDays = max(0, state.player.injuryDaysRemaining - 1)
        val updatedPlayer = state.player.copy(
            energy = recoveredEnergy,
            morale = (state.player.morale + passiveMoraleBonus).coerceIn(20, 100),
            injuryDaysRemaining = newInjuryDays,
            trainingCountThisWeek = if (nextDay % 7 == 1) 0 else state.player.trainingCountThisWeek
        )

        val newTrust = (state.managerTrust + skippedTrainingPenalty).coerceIn(10, 100)
        val newStartingStatus = computeStartingStatus(updatedPlayer, state.currentClub, newTrust)

        // Weekly wage & passive dividend payout every 7th day
        var newBalance = state.balanceEur
        val newTxList = state.transactions.toMutableList()
        if (nextDay % 7 == 0) {
            val totalWeeklyIncome = state.contract.weeklyWageEur + passiveDividendEur
            newBalance += totalWeeklyIncome
            newTxList.add(
                0,
                FinancialTransaction(
                    id = "TX_WAGE_D$nextDay",
                    dayIndex = nextDay,
                    dateLabel = nextDateLabel,
                    category = "MAAŞ",
                    amountEur = totalWeeklyIncome,
                    description = "${state.currentClub.name} Haftalık Maaş" +
                        if (passiveDividendEur > 0) " + Yatırım Getirisi ($passiveDividendEur €)" else "",
                    balanceAfterEur = newBalance
                )
            )
        }

        // Check if today is Match Day -> Manager announces Starting XI in messages!
        val updatedNpcs = state.npcs.toMutableList()
        val matchToday = state.fixtures.find {
            !it.played && it.dayIndex == nextDay &&
                (it.homeClubId == state.contract.clubId || it.awayClubId == state.contract.clubId)
        }

        if (matchToday != null) {
            val oppName = if (matchToday.homeClubId == state.contract.clubId) matchToday.awayClubName else matchToday.homeClubName
            val statusMsg = when (newStartingStatus) {
                StartingStatus.STARTER -> "Bugünkü $oppName maçında İLK 11'de sahaya çıkıyorsun! Taktik disiplini elden bırakma."
                StartingStatus.BENCH -> "Bugünkü $oppName maçına yedek kulübesinde başlayacaksın. İkinci yarıda oyunun kaderini değiştirmek için hazır ol."
                StartingStatus.OUT_OF_SQUAD -> "Kondisyon veya sakatlık durumun sebebiyle bugünkü maç kadrosunda yer almıyorsun."
            }
            val mgrIdx = updatedNpcs.indexOfFirst { it.id == "NPC_MANAGER" }
            if (mgrIdx >= 0) {
                val mgr = updatedNpcs[mgrIdx]
                updatedNpcs[mgrIdx] = mgr.copy(
                    messages = mgr.messages + ChatMessage(
                        id = "MSG_SQUAD_D$nextDay",
                        senderName = mgr.name,
                        isFromUser = false,
                        text = statusMsg,
                        dayIndex = nextDay,
                        timeLabel = "$shortDate • 10:00",
                        emotionTag = "Kadro Açıklaması"
                    ),
                    unreadCount = mgr.unreadCount + 1
                )
            }
        }

        // Check for dynamic story dilemma on specific days
        val dilemmas = WorldDataCatalog.buildDynamicDilemmas(state.currentClub.name, state.currentClub.managerName)
        val triggeredDilemma = state.activeDilemma ?: dilemmas.find { it.dayIndex == nextDay }

        val dayTransitionNote = buildString {
            append("$nextDateLabel gününe geçildi. Enerji: %$recoveredEnergy")
            if (matchToday != null) {
                append(" • BUGÜN MAÇ GÜNÜ! (${newStartingStatus.labelTr})")
            }
        }

        val activeOffers = state.transferOffers.filter { it.expiresDayIndex >= nextDay }

        return state.copy(
            updatedAtMs = System.currentTimeMillis(),
            currentDayIndex = nextDay,
            player = updatedPlayer,
            balanceEur = newBalance,
            managerTrust = newTrust,
            nextMatchStartingStatus = newStartingStatus,
            trainedToday = false,
            socialActionCountToday = 0,
            npcs = updatedNpcs,
            transactions = newTxList,
            transferOffers = activeOffers,
            activeDilemma = triggeredDilemma
        ) to dayTransitionNote
    }

    fun executeSocialVenueAction(
        state: CareerSaveState,
        action: LifestyleVenueAction
    ): Pair<CareerSaveState, String> {
        if (state.socialActionCountToday >= 2) {
            return state to "Bugün yeterince sosyal etkinliğe katıldın. Dinlenmeli veya sonraki güne geçmelisin."
        }
        if (state.balanceEur < action.costEur) {
            return state to "Bakiye yetersiz! Bu etkinlik için ${action.costEur} € gerekiyor (Mevcut: ${state.balanceEur} €)."
        }

        val dateLabel = formatGameDate(state.currentDayIndex, state.currentSeasonYear)
        val newBalance = state.balanceEur - action.costEur
        val newTx = if (action.costEur > 0) {
            listOf(
                FinancialTransaction(
                    id = "TX_SOC_${System.currentTimeMillis()}",
                    dayIndex = state.currentDayIndex,
                    dateLabel = dateLabel,
                    category = "SOSYAL_HARCAMA",
                    amountEur = -action.costEur,
                    description = "${action.venueName}: ${action.title}",
                    balanceAfterEur = newBalance
                )
            ) + state.transactions
        } else {
            state.transactions
        }

        val p = state.player
        val updatedAttr = if (action.attributeBonusType == "PHY") {
            p.attributes.copy(strength = (p.attributes.strength + 1).coerceAtMost(99))
        } else {
            p.attributes
        }
        val updatedPlayer = p.copy(
            attributes = updatedAttr,
            energy = (p.energy + action.energyDelta).coerceIn(5, 100),
            morale = (p.morale + action.moraleDelta).coerceIn(10, 100),
            followersCount = (p.followersCount + action.followersDelta).coerceAtLeast(0)
        )

        val newTrust = (state.managerTrust + action.managerTrustDelta).coerceIn(5, 100)
        val updatedNpcs = state.npcs.map { npc ->
            if (action.targetNpcRole != null && npc.roleTitle.contains(action.targetNpcRole, ignoreCase = true)) {
                npc.copy(
                    affinity = (npc.affinity + action.relationshipDelta).coerceIn(0, 100),
                    trust = (npc.trust + action.relationshipDelta / 2).coerceIn(0, 100),
                    importantMemories = (listOf("${formatShortDate(state.currentDayIndex)}: ${action.title}") + npc.importantMemories).take(6)
                )
            } else {
                npc
            }
        }

        val outcomeMsg = "${action.venueName} — ${action.title} tamamlandı! " +
            "Enerji ${if (action.energyDelta >= 0) "+${action.energyDelta}" else "${action.energyDelta}"}, " +
            "Moral ${if (action.moraleDelta >= 0) "+${action.moraleDelta}" else "${action.moraleDelta}"}" +
            if (action.costEur > 0) ", Harcama: -${action.costEur} €" else ""

        return state.copy(
            updatedAtMs = System.currentTimeMillis(),
            player = updatedPlayer,
            balanceEur = newBalance,
            managerTrust = newTrust,
            nextMatchStartingStatus = computeStartingStatus(updatedPlayer, state.currentClub, newTrust),
            socialActionCountToday = state.socialActionCountToday + 1,
            npcs = updatedNpcs,
            transactions = newTx,
            eventHistory = listOf(
                GameEvent(
                    id = "EVT_SOC_${System.currentTimeMillis()}",
                    dayIndex = state.currentDayIndex,
                    timestampMs = System.currentTimeMillis(),
                    eventType = "SOCIAL_ACTION",
                    contextDescription = "${action.venueName}: ${action.title}",
                    appliedEffects = outcomeMsg
                )
            ) + state.eventHistory
        ) to outcomeMsg
    }

    fun purchaseLifestyleItem(
        state: CareerSaveState,
        item: LifestylePurchaseItem
    ): Pair<CareerSaveState, String> {
        if (item.id in state.ownedLifestyleItemIds) {
            return state to "${item.name} zaten sahip olduğun varlıklar arasında."
        }
        if (state.balanceEur < item.priceEur) {
            return state to "Yetersiz bakiye! ${item.name} için ${item.priceEur} € gerekiyor (Mevcut: ${state.balanceEur} €)."
        }

        val newBalance = state.balanceEur - item.priceEur
        val dateLabel = formatGameDate(state.currentDayIndex, state.currentSeasonYear)
        val tx = FinancialTransaction(
            id = "TX_BUY_${item.id}_${System.currentTimeMillis()}",
            dayIndex = state.currentDayIndex,
            dateLabel = dateLabel,
            category = item.category,
            amountEur = -item.priceEur,
            description = "Satın Alım: ${item.name}",
            balanceAfterEur = newBalance
        )
        val updatedPlayer = state.player.copy(
            morale = (state.player.morale + item.passiveMoraleBonus).coerceIn(0, 100),
            followersCount = state.player.followersCount + 250
        )

        return state.copy(
            updatedAtMs = System.currentTimeMillis(),
            player = updatedPlayer,
            balanceEur = newBalance,
            ownedLifestyleItemIds = state.ownedLifestyleItemIds + item.id,
            transactions = listOf(tx) + state.transactions
        ) to "${item.name} satın alındı! (-${item.priceEur} €)"
    }

    /**
     * Handles both preset dialogue choices and custom free-text messages written by the user to any NPC in WhatsApp.
     * Uses multi-topic natural Turkish contextual analysis so any free-text message receives a realistic, personalized response.
     */
    fun sendDialogueMessageToNpc(
        state: CareerSaveState,
        npcId: String,
        userText: String
    ): CareerSaveState {
        val cleanText = userText.trim()
        if (cleanText.isEmpty()) return state
        val targetNpc = state.npcs.find { it.id == npcId } ?: return state
        val clockStr = state.formattedClockTime

        val userMsg = ChatMessage(
            id = "MSG_USR_${System.currentTimeMillis()}",
            senderName = state.player.firstName,
            isFromUser = true,
            text = cleanText,
            dayIndex = state.currentDayIndex,
            timeLabel = clockStr
        )

        val lower = cleanText.lowercase()
        val isRespectfulOrHardworking = listOf(
            "hocam", "hazırım", "çalış", "elimden", "teşekkür", "sağ ol", "baba", "kaptan",
            "takım", "odak", "saygı", "sözleşme", "destek", "antrenman", "inşallah", "eyvallah", "abi"
        ).any { lower.contains(it) }

        val isAggressiveOrDisrespectful = listOf(
            "saçma", "oynatmıyorsun", "istifa", "nefret", "umrumda değil", "bıktım", "kötü", "rezalet", "aptal"
        ).any { lower.contains(it) }

        val isGreeting = listOf("selam", "merhaba", "nasılsın", "naber", "günaydın", "iyi akşamlar", "alo").any { lower.contains(it) }
        val isQuestion = cleanText.contains("?") || listOf("ne zaman", "nasıl", "kim", "neden", "mi ", "mı ", "mu ", "mü ").any { lower.contains(it) }

        val affinityDelta = when {
            isAggressiveOrDisrespectful -> -6
            isRespectfulOrHardworking -> +5
            else -> +2
        }
        val trustDelta = when {
            isAggressiveOrDisrespectful -> -5
            isRespectfulOrHardworking -> +4
            else -> +1
        }

        val nextMatch = state.todayUserFixture ?: state.nextUnplayedUserFixture
        val nextOpponent = nextMatch?.let {
            if (it.homeClubId == state.contract.clubId) it.awayClubName else it.homeClubName
        } ?: "rakibimiz"

        var updatedPlayer = state.player
        var updatedBalance = state.balanceEur
        var updatedContract = state.contract
        var updatedOffers = state.transferOffers
        var updatedTransactions = state.transactions
        var updatedXPosts = state.xPosts
        var updatedNews = state.newsEvents
        var updatedTeamChemistry = state.teamChemistry
        var extraImpactNote: String? = null
        var forcedStartingStatus: StartingStatus? = null

        val replyText = when (targetNpc.id) {
            "NPC_MANAGER" -> when {
                isAggressiveOrDisrespectful -> {
                    forcedStartingStatus = StartingStatus.BENCH
                    extraImpactNote = "Kadro Durumu: Yedek Kulübesi (Disiplin Uyarısı)"
                    "Bu üslubun ${state.currentClub.name} disiplinine hiç uymuyor ${state.player.firstName}! Seni bu haftaki maçta yedek kulübesine çekiyorum. Cevabını lafla değil idman sahasında ver!"
                }
                lower.contains("11") || lower.contains("forma") || lower.contains("oyna") || lower.contains("kadro") || lower.contains("hazırım") -> {
                    if (state.managerTrust >= 48 && state.player.energy >= 55 && state.player.injuryDaysRemaining == 0) {
                        forcedStartingStatus = StartingStatus.STARTER
                        extraImpactNote = "Kadro Kararı: İLK 11'e Alındın!"
                        "Gözlerindeki o hırsı ve idman verilerini (%${state.player.energy} kondisyon) beğeniyorum ${state.player.firstName}! $nextOpponent maçında seni İLK 11 listesine yazdım. Çık ve formanın hakkını ver!"
                    } else {
                        "Formayı istemen güzel ${state.player.firstName} ama İlk 11 için güvenimi (%${state.managerTrust}/48) ve kondisyonunu (%${state.player.energy}/55) yukarı çekmelisin. Bugün tesislerde ekstra antrenman yap!"
                    }
                }
                lower.contains("duran top") || lower.contains("frikik") || lower.contains("penaltı") || lower.contains("sorumluluk") || lower.contains("taktik") -> {
                    updatedPlayer = updatedPlayer.copy(
                        attributes = updatedPlayer.attributes.copy(
                            tactics = (updatedPlayer.attributes.tactics + 1).coerceAtMost(99)
                        ),
                        morale = (updatedPlayer.morale + 3).coerceAtMost(100)
                    )
                    extraImpactNote = "Taktik +1 • Moral +3"
                    "Sorumluluk almanı seviyorum ${state.player.firstName}! $nextOpponent maçında hücum organizasyonlarında ve duran toplarda merkezde sen olacaksın."
                }
                lower.contains("yorgun") || lower.contains("dinlen") || lower.contains("izin") || lower.contains("ağrı") -> {
                    updatedPlayer = updatedPlayer.copy(
                        energy = (updatedPlayer.energy + 8).coerceAtMost(100)
                    )
                    extraImpactNote = "Rejenerasyon Programı: Enerji +8"
                    "Fizyoterapistlerle konuştum ${state.player.firstName}, sana bugün özel yenilenme programı uyguluyoruz (+8 Enerji). Maç gününe zinde çıkmanı istiyorum."
                }
                lower.contains("antrenman") || lower.contains("idman") || lower.contains("çalış") ->
                    "Tesislerdeki ekstra çalışmalarını yardımcı hocalarımız raporluyor. Reytingin (${updatedPlayer.overallRating}) yükseldikçe sahadaki liderliğin de artacak."
                isGreeting ->
                    "Merhaba ${state.player.firstName}. Tesislerde $nextOpponent maçı video analizlerini inceliyorum. Sen de zihinsel olarak maça odaklan."
                isQuestion ->
                    "Sorduğun konuyu teknik ekiple değerlendiriyoruz ${state.player.firstName}. Şu an tek odağımız $nextOpponent karşısında alacağımız 3 puan."
                else ->
                    "Anlaşıldı ${state.player.firstName}. \"$cleanText\" yaklaşımını sahada da görmek istiyorum. Disiplini bozmadan devam!"
            }
            "NPC_FAMILY" -> when {
                lower.contains("hediye") || lower.contains("sürpriz") || lower.contains("gönder") || lower.contains("destek") -> {
                    if (updatedBalance >= 1_500L) {
                        val giftAmount = 1_500L
                        updatedBalance -= giftAmount
                        updatedPlayer = updatedPlayer.copy(morale = (updatedPlayer.morale + 10).coerceAtMost(100))
                        updatedTransactions = listOf(
                            FinancialTransaction(
                                id = "TX_FAM_${System.currentTimeMillis()}",
                                dayIndex = state.currentDayIndex,
                                dateLabel = formatGameDate(state.currentDayIndex, state.currentSeasonYear),
                                category = "YAŞAM_TARZI",
                                amountEur = -giftAmount,
                                description = "Aileye Destek & Sürpriz Hediye",
                                balanceAfterEur = updatedBalance
                            )
                        ) + updatedTransactions
                        extraImpactNote = "Aile Desteği (-1.500 €) • Moral +10"
                        "Canım oğlum ne gerek vardı! Gönderdiğin sürpriz annenle bizi mutluluktan ağlattı. Allah ayağına taş değdirmesin, seninle gurur duyuyoruz! ❤️🙏"
                    } else {
                        "Düşünmen yeter aslan oğlum! Sen önce kendi birikimini yap, senin sağlığın bize en büyük hediye ❤️"
                    }
                }
                lower.contains("maç") || lower.contains("gol") || lower.contains("forma") || lower.contains("tribün") -> {
                    updatedPlayer = updatedPlayer.copy(morale = (updatedPlayer.morale + 4).coerceAtMost(100))
                    extraImpactNote = "Aile Desteği: Moral +4"
                    "Annenle formalarımızı hazırladık, $nextOpponent maçında gözümüz kulağımız sende evladım. Sahaya çıkınca dualarımız seninle!"
                }
                lower.contains("yorgun") || lower.contains("zor") || lower.contains("moral") || lower.contains("stres") -> {
                    updatedPlayer = updatedPlayer.copy(morale = (updatedPlayer.morale + 6).coerceAtMost(100))
                    extraImpactNote = "Moral +6"
                    "Futbolculuk sabır işidir aslan oğlum. Moralini yüksek tut, uykuna ve beslenmene dikkat et. Biz her zaman arkandayız."
                }
                isGreeting ->
                    "Hoş geldin gözümüzün nuru! Annen de tam seni soruyordu, tesislerde her şey yolunda mı?"
                else ->
                    "Canım evladım, mesajın bizi çok mutlu etti. Mahallede herkes senin ${state.currentClub.name} formasıyla yaptıklarını konuşuyor, gururumuzsun! ❤️"
            }
            "NPC_AGENT" -> when {
                lower.contains("transfer") || lower.contains("teklif") || lower.contains("kulüp") || lower.contains("avrupa") || lower.contains("gözlemci") || lower.contains("ara") -> {
                    val window = TransferMarketEngine.getTransferWindowStatus(state.currentDayIndex)
                    if (!window.isOpen) {
                        "Transfer penceresi şu an kapalı (${window.titleTr}) ${state.player.firstName}. Resmi teklif alamayız ancak scoutlarla ve kulüp temsilcileriyle sürekli dirsek temasındayım. Sen sahadaki performansına odaklan, pencere açıldığında dosyaları açacağız! 💼"
                    } else {
                        val eligibility = TransferMarketEngine.evaluateTransferEligibility(state, state.player, state.currentClub)
                        if (!eligibility.isEligible) {
                            "Piyasadaki kulüplerle nabız yokladım ${state.player.firstName}. Ancak scoutların resmi teklif sunması için: ${eligibility.reasonMessage}"
                        } else {
                            if (updatedOffers.isEmpty()) {
                                updatedOffers = TransferMarketEngine.maybeGenerateRealisticOffer(
                                    state = state,
                                    player = updatedPlayer,
                                    currentClub = state.currentClub,
                                    dayIndex = state.currentDayIndex
                                )
                            }
                            val topOffer = updatedOffers.firstOrNull()
                            if (topOffer != null) {
                                extraImpactNote = "Resmi Teklif: ${topOffer.clubName} (${FLFormatters.formatWage(topOffer.offeredWeeklyWageEur)})"
                                "Hemen kulüplerle temasa geçtim ${state.player.firstName}! Masamızda şu an **${topOffer.clubName}** (${topOffer.leagueName}) kulübünden haftalık **${FLFormatters.formatCurrency(topOffer.offeredWeeklyWageEur)}** maaşlı resmi teklif var! Alt bardan 'Kariyer > Sözleşme & Teklif' sekmesine girip hemen imzalayabilir veya pazarlık yapabilirsin! 💼🔥"
                            } else {
                                "Üst lig kulüpleriyle görüştüm ${state.player.firstName}. Performansını beğeniyorlar ancak henüz resmi bir sözleşme sunmadılar. Bir sonraki maçta fark yaratırsan teklif masaya gelecek!"
                            }
                        }
                    }
                }
                lower.contains("maaş") || lower.contains("zam") || lower.contains("sözleşme") || lower.contains("kontrat") || lower.contains("prim") -> {
                    if (state.managerTrust >= 55) {
                        val raisedWage = (updatedContract.weeklyWageEur * 1.10).toLong()
                        updatedContract = updatedContract.copy(weeklyWageEur = raisedWage)
                        extraImpactNote = "Maaş İyileştirmesi: ${FLFormatters.formatWage(raisedWage)}"
                        "Kulüp yönetimiyle masaya oturdum ${state.player.firstName}! Hocanın sana olan güvenini (%${state.managerTrust}) koz olarak kullandım ve haftalık maaşını ${FLFormatters.formatCurrency(raisedWage)} seviyesine yükselttirdim! 💸✍️"
                    } else {
                        "Yönetimle zam için nabız yokladım ancak teknik direktör güveninin (%${state.managerTrust}/55) biraz daha yükselmesini bekliyorlar. $nextOpponent maçında iyi oynarsan masaya yumruğumuzu vururuz!"
                    }
                }
                lower.contains("sponsor") || lower.contains("reklam") || lower.contains("instagram") || lower.contains("takipçi") ->
                    "Instagram'da ${updatedPlayer.followersCount} takipçiye ulaştın! DM kutundaki İstekler bölümünü mutlaka kontrol et, global markaları kabul edip mesajlaştığında doğrudan banka hesabına sponsorluk ödemesi yatacak."
                isGreeting ->
                    "Selam yıldızım! Tam da bir sportif direktörle senin istatistiklerini (${updatedPlayer.currentSeasonStats.goals}G / ${updatedPlayer.currentSeasonStats.assists}A) konuşuyordum. Ne var ne yok?"
                else ->
                    "Kesinlikle katılıyorum ${state.player.firstName}. Sen çimlerde şovunu yap, saha dışı tüm kontrat ve PR operasyonunu ben yönetiyorum! 💼⚽"
            }
            "NPC_TEAM_GROUP" -> {
                val captain = state.currentClub.keySquadMembers.firstOrNull { it.isCaptain } ?: state.currentClub.keySquadMembers.first()
                val mate = state.currentClub.keySquadMembers.lastOrNull() ?: captain
                updatedTeamChemistry = (updatedTeamChemistry + 3).coerceAtMost(100)
                updatedPlayer = updatedPlayer.copy(morale = (updatedPlayer.morale + 3).coerceAtMost(100))
                extraImpactNote = "Takım Kimyası +3 • Moral +3"
                when {
                    lower.contains("maç") || lower.contains("kazan") || lower.contains("3 puan") || lower.contains("galibiyet") || lower.contains("saha") ->
                        "${captain.name}: Aynen öyle kardeşim! $nextOpponent maçında sahayı dar edeceğiz! Takım kimyamız (%$updatedTeamChemistry) zirvede 🔥💪"
                    lower.contains("yemek") || lower.contains("kahve") || lower.contains("akşam") || lower.contains("benden") ->
                        "${mate.name}: Kral konuştu! 🥩☕ İdman çıkışı hep beraber gidiyoruz, takımca kenetlenmek bize sahada çok güç katacak!"
                    lower.contains("idman") || lower.contains("antrenman") || lower.contains("koşu") ->
                        "${mate.name}: Hoca bugün kondisyonda pestilimizi çıkardı ama $nextOpponent maçında 90 dakika basacağız! 😂⚽"
                    else ->
                        "${captain.name}: Adamsın ${state.player.firstName}! Soyunma odasında bu enerji oldukça kimse bizi yıkamaz 👏⚽"
                }
            }
            "NPC_PRESS" -> {
                val shortDate = formatShortDate(state.currentDayIndex)
                val pressXp = XPostItem(
                    id = "XP_PRESS_${System.currentTimeMillis()}",
                    dayIndex = state.currentDayIndex,
                    dateLabel = shortDate,
                    authorName = "Ertan Süzgün",
                    handle = "@ertansuzgun",
                    category = "GAZETECİ",
                    isVerified = true,
                    text = "🚨 ÖZEL | ${state.currentClub.name} yıldızı ${updatedPlayer.fullName} bana konuştu: \"$cleanText\" 🔥⚽",
                    likes = 1_450 + updatedPlayer.followersCount / 8,
                    reposts = 210
                )
                val pressNews = NewsEvent(
                    id = "NEWS_WHATSAPP_${System.currentTimeMillis()}",
                    dayIndex = state.currentDayIndex,
                    dateLabel = formatGameDate(state.currentDayIndex, state.currentSeasonYear),
                    category = "RÖPORTAJ",
                    headline = "${updatedPlayer.fullName} Basına Konuştu: \"${cleanText.take(45)}\"",
                    body = "${state.currentClub.name} forması giyen ${updatedPlayer.fullName}, spor muhabiri Ertan Süzgün'e verdiği özel demeçle gündeme oturdu.",
                    relatedClubName = state.currentClub.name
                )
                updatedXPosts = listOf(pressXp) + updatedXPosts
                updatedNews = listOf(pressNews) + updatedNews
                updatedPlayer = updatedPlayer.copy(
                    followersCount = updatedPlayer.followersCount + 340,
                    fanSupport = (updatedPlayer.fanSupport + 3).coerceIn(0, 100)
                )
                extraImpactNote = "X & Haber Manşeti Yayınlandı! (+340 Takipçi, Şöhret +3)"
                when {
                    lower.contains("transfer") || lower.contains("teklif") || lower.contains("ayrıl") || lower.contains("avrupa") ->
                        "Bomba açıklama! 🚨 '${updatedPlayer.fullName} geleceği hakkında konuştu: $cleanText' başlığıyla X'te ve Haberler'de manşetten girdim, gündem sallanıyor! 👀🔥"
                    else ->
                        "Harika demeç ${state.player.firstName}! Sözlerini şimdi X'te (@ertansuzgun) ve Futbol Haberleri'nde özel haber olarak yayınladım, taraftarlar etkileşim yağdırıyor! 🎙️⚡"
                }
            }
            else -> {
                updatedTeamChemistry = (updatedTeamChemistry + 2).coerceAtMost(100)
                when {
                    isAggressiveOrDisrespectful -> "Sakin ol kardeşim, hepimiz ${state.currentClub.shortName} arması için ter döküyoruz."
                    lower.contains("pas") || lower.contains("asist") || lower.contains("gol") || lower.contains("maç") ->
                        "Merak etme kardeşim, $nextOpponent maçında gözüm hep senin koşularında olacak. Birlikte fişi çekeceğiz! ⚽🔥"
                    else -> "Eyvallah kardeşim! Soyunma odasında seninle omuz omuza olmak bize güç veriyor."
                }
            }
        }

        val combinedImpact = buildString {
            append("Yakınlık ${if (affinityDelta >= 0) "+$affinityDelta" else "$affinityDelta"}, Güven ${if (trustDelta >= 0) "+$trustDelta" else "$trustDelta"}")
            if (extraImpactNote != null) {
                append(" • $extraImpactNote")
            }
        }

        val npcReply = ChatMessage(
            id = "MSG_NPC_${System.currentTimeMillis() + 1}",
            senderName = targetNpc.name,
            isFromUser = false,
            text = replyText,
            dayIndex = state.currentDayIndex,
            timeLabel = clockStr,
            emotionTag = if (affinityDelta >= 0) "Olumlu" else "Gergin",
            impactSummary = combinedImpact
        )

        val newManagerTrust = if (targetNpc.id == "NPC_MANAGER") {
            (state.managerTrust + trustDelta).coerceIn(5, 100)
        } else {
            state.managerTrust
        }

        val dynamicQuickReplies = when (targetNpc.id) {
            "NPC_MANAGER" -> listOf(
                "Hocam $nextOpponent maçında İlk 11'de oynamaya hazırım!",
                "Duran toplarda ve hücumda sorumluluk almak istiyorum.",
                "Bugün ekstra bireysel antrenman yapacağım hocam."
            )
            "NPC_AGENT" -> listOf(
                "Bana üst liglerden resmi transfer teklifi bul.",
                "Kulüp yönetimiyle maaş zammı görüşmesi yapalım.",
                "Sponsorluk anlaşmalarımızı değerlendirelim."
            )
            "NPC_FAMILY" -> listOf(
                "Dualarınız yeter baba, maça çok iyi hazırlanıyorum.",
                "Size 1.500 € sürpriz hediye gönderiyorum!",
                "Bu hafta sonu maça bekliyorum baba!"
            )
            "NPC_TEAM_GROUP" -> listOf(
                "$nextOpponent maçını kazanıp 3 puanı alacağız beyler! 🔥",
                "İdman çıkışı yemek benden, takımca moral depolayalım!",
                "Sahada yardımlaşmayı artıralım beyler 💪"
            )
            "NPC_PRESS" -> listOf(
                "Tek hedefimiz ${state.currentClub.name} ile zirveye oynamak!",
                "Performansımla Avrupa kulüplerinin radarında olmak gurur verici.",
                "Taraftarımıza bu hafta gol sözü veriyorum!"
            )
            else -> targetNpc.quickReplyOptions
        }

        val updatedNpcs = state.npcs.map { npc ->
            if (npc.id == npcId) {
                npc.copy(
                    affinity = (npc.affinity + affinityDelta).coerceIn(0, 100),
                    trust = (npc.trust + trustDelta).coerceIn(0, 100),
                    currentMood = if (affinityDelta >= 0) "Memnun" else "Mesafeli",
                    quickReplyOptions = dynamicQuickReplies,
                    messages = npc.messages + userMsg + npcReply,
                    unreadCount = 0
                )
            } else npc
        }

        val computedNextStatus = forcedStartingStatus ?: computeStartingStatus(updatedPlayer, state.currentClub, newManagerTrust)

        return state.copy(
            updatedAtMs = System.currentTimeMillis(),
            player = updatedPlayer,
            balanceEur = updatedBalance,
            contract = updatedContract,
            transferOffers = updatedOffers,
            transactions = updatedTransactions,
            xPosts = updatedXPosts,
            newsEvents = updatedNews,
            teamChemistry = updatedTeamChemistry,
            managerTrust = newManagerTrust,
            nextMatchStartingStatus = computedNextStatus,
            npcs = updatedNpcs
        )
    }

    fun publishPressStatementOrSocialPost(
        state: CareerSaveState,
        statementText: String,
        tone: String // "MÜTEVAZI_TAKIMCI", "İDDİALI_ÖZGÜVENLİ", "TARTIŞMALI_SERT"
    ): Pair<CareerSaveState, String> {
        val text = statementText.trim().ifEmpty { "Takım olarak hedefimize odaklandık, taraftarımıza layık olacağız!" }
        val dateLabel = formatGameDate(state.currentDayIndex, state.currentSeasonYear)

        val (moraleDelta, trustDelta, fanDelta, followersDelta, sentiment) = when (tone) {
            "MÜTEVAZI_TAKIMCI" -> listOf(4, 5, 6, 380, 1)
            "İDDİALI_ÖZGÜVENLİ" -> listOf(7, 2, 8, 640, 1)
            else -> listOf(-2, -7, -6, 820, -1)
        }

        val sentimentLabel = when (sentiment) {
            1 -> "Destekleyici"
            -1 -> "Eleştirel"
            else -> "Tarafsız"
        }

        val fanMultiplier = (state.player.followersCount / 35).coerceAtLeast(45)
        val generatedComments = if (sentiment >= 0) {
            listOf(
                InstaCommentItem(
                    id = "IC_USR_${System.currentTimeMillis()}_1",
                    authorHandle = "${state.currentClub.shortName.lowercase()}_ultras",
                    authorName = "${state.currentClub.shortName} Tribün",
                    isVerified = false,
                    avatarHex = state.currentClub.primaryColorHex,
                    text = "Helal olsun sana! Sahada da aynı hırsı bekliyoruz 🔥👏",
                    timeLabel = "1d",
                    likesCount = 42
                ),
                InstaCommentItem(
                    id = "IC_USR_${System.currentTimeMillis()}_2",
                    authorHandle = "futbol_analiz_tr",
                    authorName = "Futbol Analiz",
                    isVerified = true,
                    avatarHex = "#1D9BF0",
                    text = "Profesyonel duruşuyla fark yaratıyor. Bu sezon çok konuşulacak.",
                    timeLabel = "3d",
                    likesCount = 19
                ),
                InstaCommentItem(
                    id = "IC_USR_${System.currentTimeMillis()}_3",
                    authorHandle = "emre_taraftar19",
                    authorName = "Emre Kaya",
                    isVerified = false,
                    avatarHex = "#10B981",
                    text = "Kral formanın hakkını veriyor! 👑⚽",
                    timeLabel = "5d",
                    likesCount = 11
                )
            )
        } else {
            listOf(
                InstaCommentItem(
                    id = "IC_USR_${System.currentTimeMillis()}_1",
                    authorHandle = "spor_masasi",
                    authorName = "Spor Masası",
                    isVerified = true,
                    avatarHex = "#EF4444",
                    text = "Bu açıklamalar soyunma odasında tansiyonu yükseltebilir 👀",
                    timeLabel = "2d",
                    likesCount = 64
                ),
                InstaCommentItem(
                    id = "IC_USR_${System.currentTimeMillis()}_2",
                    authorHandle = "${state.currentClub.shortName.lowercase()}_sevdalisi",
                    authorName = "Taraftar Sesi",
                    isVerified = false,
                    avatarHex = "#F59E0B",
                    text = "Laf değil sahada gol görmek istiyoruz!",
                    timeLabel = "4d",
                    likesCount = 28
                )
            )
        }

        val newPost = SocialMediaPost(
            id = "SOC_USER_${System.currentTimeMillis()}",
            dayIndex = state.currentDayIndex,
            dateLabel = dateLabel,
            authorName = state.player.fullName,
            authorHandle = "@${state.player.firstName.lowercase()}${state.player.kitNumber}",
            isUserPost = true,
            content = text,
            likesCount = 320 + followersDelta + fanMultiplier,
            commentsCount = generatedComments.size + 24,
            sentiment = sentimentLabel,
            topFanComments = generatedComments.map { "${it.authorHandle}: ${it.text}" },
            instaComments = generatedComments,
            isVerifiedAuthor = true,
            locationLabel = "${state.currentClub.city}, ${state.currentClub.country}"
        )

        val newNews = NewsEvent(
            id = "NEWS_PRESS_${System.currentTimeMillis()}",
            sourceGameEventId = "EVT_PRESS_${state.currentDayIndex}",
            dayIndex = state.currentDayIndex,
            dateLabel = dateLabel,
            category = "RÖPORTAJ",
            headline = "${state.player.fullName} Sosyal Medyadan Paylaştı: \"${text.take(48)}...\"",
            body = "${state.currentClub.name} oyuncusu ${state.player.fullName}'ın paylaşımı kısa sürede binlerce beğeni ve yorum aldı.",
            relatedClubName = state.currentClub.name,
            fanReaction = sentimentLabel
        )

        val updatedPlayer = state.player.copy(
            morale = (state.player.morale + moraleDelta).coerceIn(10, 100),
            fanSupport = (state.player.fanSupport + fanDelta).coerceIn(5, 100),
            followersCount = state.player.followersCount + followersDelta
        )
        val newTrust = (state.managerTrust + trustDelta).coerceIn(5, 100)

        return state.copy(
            updatedAtMs = System.currentTimeMillis(),
            player = updatedPlayer,
            managerTrust = newTrust,
            pressCompletedForLastMatch = true,
            socialPosts = listOf(newPost) + state.socialPosts,
            newsEvents = listOf(newNews) + state.newsEvents
        ) to "Instagram gönderin paylaşıldı! Takipçi +$followersDelta, Taraftar Desteği ${if (fanDelta >= 0) "+$fanDelta" else "$fanDelta"}."
    }

    fun resolveDynamicDilemma(
        state: CareerSaveState,
        choice: DynamicStoryChoice
    ): Pair<CareerSaveState, String> {
        val dilemma = state.activeDilemma ?: return state to ""
        val dateLabel = formatGameDate(state.currentDayIndex, state.currentSeasonYear)
        val newBalance = state.balanceEur + choice.moneyDeltaEur

        val updatedTx = if (choice.moneyDeltaEur != 0L) {
            listOf(
                FinancialTransaction(
                    id = "TX_DIL_${System.currentTimeMillis()}",
                    dayIndex = state.currentDayIndex,
                    dateLabel = dateLabel,
                    category = "SPONSORLUK",
                    amountEur = choice.moneyDeltaEur,
                    description = dilemma.title,
                    balanceAfterEur = newBalance
                )
            ) + state.transactions
        } else state.transactions

        val updatedPlayer = state.player.copy(
            morale = (state.player.morale + choice.moraleDelta).coerceIn(10, 100),
            energy = (state.player.energy + choice.energyDelta).coerceIn(10, 100),
            fanSupport = (state.player.fanSupport + choice.fanSupportDelta).coerceIn(10, 100)
        )
        val newTrust = (state.managerTrust + choice.managerTrustDelta).coerceIn(5, 100)

        val news = NewsEvent(
            id = "NEWS_DIL_${System.currentTimeMillis()}",
            sourceGameEventId = dilemma.id,
            dayIndex = state.currentDayIndex,
            dateLabel = dateLabel,
            category = "KULÜP",
            headline = choice.consequenceHeadline,
            body = dilemma.narrative,
            relatedClubName = state.currentClub.name,
            fanReaction = if (choice.fanSupportDelta >= 0) "Destekleyici" else "Eleştirel"
        )

        return state.copy(
            updatedAtMs = System.currentTimeMillis(),
            player = updatedPlayer,
            balanceEur = newBalance,
            managerTrust = newTrust,
            nextMatchStartingStatus = computeStartingStatus(updatedPlayer, state.currentClub, newTrust),
            transactions = updatedTx,
            newsEvents = listOf(news) + state.newsEvents,
            activeDilemma = null
        ) to choice.consequenceHeadline
    }

    private fun maybeGenerateTransferOffer(
        currentOffers: List<TransferOffer>,
        player: PlayerProfile,
        currentClub: Club,
        dayIndex: Int
    ): List<TransferOffer> {
        return currentOffers
    }

    fun negotiateTransferOffer(
        state: CareerSaveState,
        offerId: String
    ): Pair<CareerSaveState, String> {
        return TransferMarketEngine.negotiateOffer(state, offerId)
    }

    fun acceptTransferOffer(
        state: CareerSaveState,
        offer: TransferOffer
    ): Pair<CareerSaveState, String> {
        val newClubs = if (offer.leagueId == state.currentLeagueId) {
            state.clubs
        } else {
            WorldDataCatalog.clubsForLeague(offer.leagueId)
        }
        val newClub = newClubs.find { it.id == offer.clubId } ?: newClubs.first()
        val newFixtures = if (offer.leagueId == state.currentLeagueId) {
            state.fixtures
        } else {
            generateLeagueSchedule(offer.leagueId, newClubs)
        }
        val newStandings = recomputeStandingsFromFixtures(newClubs, newFixtures)
        val signingBonus = offer.offeredWeeklyWageEur * 2
        val newBalance = state.balanceEur + signingBonus
        val dateLabel = formatGameDate(state.currentDayIndex, state.currentSeasonYear)

        val newContract = Contract(
            clubId = newClub.id,
            clubName = newClub.name,
            weeklyWageEur = offer.offeredWeeklyWageEur,
            matchAppearanceBonusEur = offer.offeredAppearanceBonusEur,
            goalBonusEur = offer.offeredGoalBonusEur,
            startSeasonYear = state.currentSeasonYear,
            endSeasonYear = state.currentSeasonYear + offer.contractYears,
            role = offer.promisedRole
        )

        val archiveList = if (state.player.currentSeasonStats.appearances > 0) {
            state.player.careerArchive + state.player.currentSeasonStats
        } else {
            state.player.careerArchive
        }

        val updatedPlayer = state.player.copy(
            morale = 92,
            followersCount = state.player.followersCount + 2_500,
            marketValueEur = (state.player.marketValueEur * 1.25).toLong(),
            currentSeasonStats = SeasonStatsRecord(
                seasonYear = state.currentSeasonYear,
                clubId = newClub.id,
                clubName = newClub.name,
                leagueName = newClub.leagueName
            ),
            careerArchive = archiveList
        )

        val tx = FinancialTransaction(
            id = "TX_TRANSFER_${System.currentTimeMillis()}",
            dayIndex = state.currentDayIndex,
            dateLabel = dateLabel,
            category = "TRANSFER_İMZA",
            amountEur = signingBonus,
            description = "${newClub.name} Transfer İmza Parası",
            balanceAfterEur = newBalance
        )

        val news = NewsEvent(
            id = "NEWS_TR_${System.currentTimeMillis()}",
            sourceGameEventId = "EVT_TR_${offer.id}",
            dayIndex = state.currentDayIndex,
            dateLabel = dateLabel,
            category = "TRANSFER",
            headline = "RESMİ TRANSFER: ${updatedPlayer.fullName}, ${newClub.name} İle Sözleşme İmzaladı!",
            body = "${updatedPlayer.fullName}, haftalık ${offer.offeredWeeklyWageEur} € maaşla ${newClub.name} kadrosuna katıldı.",
            relatedClubName = newClub.name,
            fanReaction = "Destekleyici"
        )

        val updatedNpcs = state.npcs.map { npc ->
            if (npc.id == "NPC_MANAGER") {
                npc.copy(
                    name = newClub.managerName,
                    personality = "Disiplinli (${newClub.managerTacticalStyle})",
                    trust = 62,
                    messages = npc.messages + ChatMessage(
                        id = "MSG_NEW_MGR_${System.currentTimeMillis()}",
                        senderName = newClub.managerName,
                        isFromUser = false,
                        text = "${newClub.name} ailesine hoş geldin ${updatedPlayer.firstName}! Seni uzun süredir takip ediyorduk, sahada fark yaratacağına inanıyorum.",
                        dayIndex = state.currentDayIndex,
                        timeLabel = "${formatShortDate(state.currentDayIndex)} • 14:00",
                        emotionTag = "Hoş Geldin"
                    ),
                    unreadCount = npc.unreadCount + 1
                )
            } else npc
        }

        val updatedCalendar = buildCalendarFromFixtures(newFixtures, newClub.id)
        val oldClub = state.currentClub
        val shortDate = formatShortDate(state.currentDayIndex)
        val transferRecord = TransferHistoryEntry(
            id = "TR_HIST_${System.currentTimeMillis()}",
            seasonLabel = "${state.currentSeasonYear}/${(state.currentSeasonYear + 1) % 100}",
            dateLabel = dateLabel,
            fromClubName = oldClub.name,
            toClubName = newClub.name,
            toClubShortName = newClub.shortName,
            toClubPrimaryHex = newClub.primaryColorHex,
            transferType = offer.transferType,
            feeEur = if (offer.transferType.contains("Kiralık")) 0L else (updatedPlayer.marketValueEur * 0.85).toLong(),
            marketValueAtTransferEur = updatedPlayer.marketValueEur
        )

        val mvPoint = MarketValuePoint(
            dayIndex = state.currentDayIndex,
            dateLabel = shortDate,
            valueEur = updatedPlayer.marketValueEur,
            clubName = newClub.shortName,
            reason = "${newClub.name} Transferi"
        )

        val trGallery = GalleryPhotoItem(
            id = "GAL_TR_${System.currentTimeMillis()}",
            dayIndex = state.currentDayIndex,
            dateLabel = dateLabel,
            album = "Kutlamalar",
            title = "${newClub.name} İmza Töreni",
            caption = "${updatedPlayer.fullName}, ${newClub.name} ile resmi sözleşmeye imza attı.",
            sceneDrawableType = "TRANSFER"
        )

        val trXPost = XPostItem(
            id = "XP_TR_${System.currentTimeMillis()}",
            dayIndex = state.currentDayIndex,
            dateLabel = shortDate,
            authorName = "Yağız Transfer Gündemi",
            handle = "@transfer_merkezi",
            category = "GAZETECİ",
            isVerified = true,
            text = "🔥 ÖZEL | ${newClub.name}, ${oldClub.name} forması giyen ${updatedPlayer.fullName} ile ${offer.contractYears} yıllık anlaşmaya vardı! Haftalık maaş: ${FLFormatters.formatWage(offer.offeredWeeklyWageEur)}. Resmi imzalar atıldı.",
            likes = 1_840,
            reposts = 412
        )

        return state.copy(
            updatedAtMs = System.currentTimeMillis(),
            careerTitle = "${updatedPlayer.fullName} • ${newClub.shortName}",
            currentLeagueId = offer.leagueId,
            player = updatedPlayer,
            contract = newContract,
            balanceEur = newBalance,
            managerTrust = 62,
            nextMatchStartingStatus = computeStartingStatus(updatedPlayer, newClub, 62),
            clubs = newClubs,
            standings = newStandings,
            fixtures = newFixtures,
            npcs = updatedNpcs,
            calendarEvents = updatedCalendar,
            transactions = listOf(tx) + state.transactions,
            newsEvents = listOf(news) + state.newsEvents,
            transferOffers = state.transferOffers.filterNot { it.id == offer.id },
            transferHistory = listOf(transferRecord) + state.transferHistory,
            marketValueHistory = state.marketValueHistory + mvPoint,
            galleryPhotos = listOf(trGallery) + state.galleryPhotos,
            xPosts = listOf(trXPost) + state.xPosts
        ) to "Tebrikler! ${newClub.name} ile resmi sözleşme imzaladın (+${FLFormatters.formatCurrency(signingBonus)} imza parası)."
    }

    fun requestTransferSearchFromAgent(state: CareerSaveState): Pair<CareerSaveState, String> {
        val window = TransferMarketEngine.getTransferWindowStatus(state.currentDayIndex)
        if (!window.isOpen) {
            val updatedX = TransferMarketEngine.maybeGenerateTransferRumor(
                state = state,
                player = state.player,
                club = state.currentClub,
                dayIndex = state.currentDayIndex
            )
            return state.copy(
                updatedAtMs = System.currentTimeMillis(),
                xPosts = updatedX
            ) to "Menajerin Kemal Soylu: 'Transfer penceresi şu an kapalı (${window.titleTr}). Resmi teklif alamayız ancak scoutlarla ve kulüp temsilcileriyle temas halindeyim, dedikoduları ve ilgiyi sıcak tutuyorum.'"
        }

        val eligibility = TransferMarketEngine.evaluateTransferEligibility(state, state.player, state.currentClub)
        if (!eligibility.isEligible) {
            val updatedX = TransferMarketEngine.maybeGenerateTransferRumor(
                state = state,
                player = state.player,
                club = state.currentClub,
                dayIndex = state.currentDayIndex
            )
            return state.copy(
                updatedAtMs = System.currentTimeMillis(),
                xPosts = updatedX
            ) to "Menajerin Kemal Soylu: '${eligibility.reasonMessage}'"
        }

        val updatedOffers = TransferMarketEngine.maybeGenerateRealisticOffer(
            state = state,
            player = state.player,
            currentClub = state.currentClub,
            dayIndex = state.currentDayIndex
        )
        if (updatedOffers.size == state.transferOffers.size) {
            return state to "Menajerin Kemal Soylu kulüplerle görüştü: 'Kulüpler seni izliyor ancak şu an resmi teklif vermediler. Bir sonraki maçta fark yaratırsan masaya teklif gelecek!'"
        }

        val newOffer = updatedOffers.first()
        val rumorPosts = TransferMarketEngine.maybeGenerateTransferRumor(
            state = state,
            player = state.player,
            club = state.currentClub,
            dayIndex = state.currentDayIndex
        )
        return state.copy(
            updatedAtMs = System.currentTimeMillis(),
            transferOffers = updatedOffers,
            xPosts = rumorPosts
        ) to "Menajerin Kemal Soylu, ${newOffer.clubName} kulübünden resmi transfer teklifi getirdi!"
    }

    private fun computeStartingStatus(
        player: PlayerProfile,
        club: Club,
        managerTrust: Int
    ): StartingStatus {
        if (player.injuryDaysRemaining > 0 || player.energy < 22) {
            return StartingStatus.OUT_OF_SQUAD
        }
        val effectiveScore = managerTrust + (player.overallRating - club.teamStrength) * 2 + (player.form - 65) / 3
        return when {
            effectiveScore >= 54 -> StartingStatus.STARTER
            effectiveScore >= 25 -> StartingStatus.BENCH
            else -> StartingStatus.BENCH
        }
    }

    // =========================================================================
    // FOOTBALL LIFE 2.0 — MIGRATION, MONTHLY CALENDAR GRID & PHONE OS ACTIONS
    // =========================================================================

    fun ensureVersion3EnrichedState(state: CareerSaveState): CareerSaveState {
        val club = state.currentClub
        val p = state.player
        val shortDate = formatShortDate(state.currentDayIndex)
        val fullDate = formatGameDate(state.currentDayIndex, state.currentSeasonYear)

        val marketHistory = state.marketValueHistory.ifEmpty {
            val prevValue = (p.marketValueEur * 0.85).toLong().coerceAtLeast(25_000L)
            listOf(
                MarketValuePoint(
                    dayIndex = 0,
                    dateLabel = "Tem 2026",
                    valueEur = prevValue,
                    clubName = club.shortName,
                    reason = "Sezon Öncesi Değerleme"
                ),
                MarketValuePoint(
                    dayIndex = state.currentDayIndex,
                    dateLabel = shortDate,
                    valueEur = p.marketValueEur,
                    clubName = club.shortName,
                    reason = "${club.name} Kadro Kaydı"
                )
            )
        }

        val transferHist = state.transferHistory.ifEmpty {
            val historyList = mutableListOf<TransferHistoryEntry>()
            p.careerArchive.forEachIndexed { idx, arch ->
                historyList.add(
                    TransferHistoryEntry(
                        id = "TR_ARCH_$idx",
                        seasonLabel = "${arch.seasonYear}/${(arch.seasonYear + 1) % 100}",
                        dateLabel = "1 Tem ${arch.seasonYear}",
                        fromClubName = if (idx == 0) "${p.city} Altyapı" else p.careerArchive[idx - 1].clubName,
                        toClubName = arch.clubName,
                        toClubShortName = arch.clubName.take(3).uppercase(),
                        toClubPrimaryHex = club.primaryColorHex,
                        transferType = if (idx == 0) "Altyapıdan A Takıma" else "Bonservisli Transfer",
                        feeEur = if (idx == 0) 0L else (p.marketValueEur / 2),
                        marketValueAtTransferEur = (p.marketValueEur * 0.7).toLong()
                    )
                )
            }
            historyList.add(
                0,
                TransferHistoryEntry(
                    id = "TR_INIT_CURRENT",
                    seasonLabel = "${state.currentSeasonYear}/${(state.currentSeasonYear + 1) % 100}",
                    dateLabel = "10 Ağu ${state.currentSeasonYear}",
                    fromClubName = p.careerArchive.lastOrNull()?.clubName ?: "${club.name} Akademi",
                    toClubName = club.name,
                    toClubShortName = club.shortName,
                    toClubPrimaryHex = club.primaryColorHex,
                    transferType = if (p.isRealPlayer) "Profesyonel Sözleşme" else "A Takım Sözleşmesi",
                    feeEur = 0L,
                    marketValueAtTransferEur = p.marketValueEur
                )
            )
            historyList
        }

        val attrHistory = state.attributeHistory.ifEmpty {
            listOf(
                AttributeHistoryPoint(
                    dayIndex = 1,
                    dateLabel = "10 Ağu",
                    overall = p.overallRating,
                    pace = p.attributes.cardPace,
                    shooting = p.attributes.cardShooting,
                    passing = p.attributes.cardPassing,
                    dribbling = p.attributes.cardDribbling,
                    defending = p.attributes.cardDefending,
                    physical = p.attributes.cardPhysical,
                    averageMatchRating = p.currentSeasonStats.averageRating
                )
            )
        }

        val achievements = state.achievements.ifEmpty {
            val list = mutableListOf(
                CareerAchievement(
                    id = "ACH_PRO_CONTRACT",
                    title = "Profesyonel İmza",
                    description = "${club.name} ile resmi profesyonel futbolcu sözleşmesi imzalandı.",
                    dateLabel = "10 Ağustos 2026",
                    category = "KARİYER_TAŞI"
                )
            )
            p.careerArchive.flatMap { it.trophiesWon }.distinct().forEachIndexed { idx, trophy ->
                list.add(
                    CareerAchievement(
                        id = "ACH_TROPHY_$idx",
                        title = trophy,
                        description = "Kariyer arşivinde kazanılmış resmi kupa.",
                        dateLabel = "Önceki Sezonlar",
                        category = "KUPA"
                    )
                )
            }
            list
        }

        val stories = state.stories.ifEmpty {
            val captain = club.keySquadMembers.find { it.isCaptain } ?: club.keySquadMembers.first()
            listOf(
                StoryItem(
                    id = "ST_CLUB_1",
                    authorName = "${club.shortName.lowercase()}resmi",
                    authorRole = "Resmi Kulüp Hesabı",
                    avatarHex = club.primaryColorHex,
                    title = "Yeni Sezon Kampı Başladı!",
                    caption = "${club.stadiumName} çimlerinde yeni sezon heyecanı. #${club.shortName}",
                    sceneType = "STADIUM",
                    dayIndex = state.currentDayIndex
                ),
                StoryItem(
                    id = "ST_433",
                    authorName = "433",
                    authorRole = "Futbol Medyası",
                    avatarHex = "#FF3B30",
                    title = "Haftanın Genç Yıldız Adayları ✨",
                    caption = "${p.fullName} (#${p.kitNumber}) bu sezon ${club.name} formasıyla vitrine çıkıyor! 👀🔥",
                    sceneType = "STADIUM",
                    dayIndex = state.currentDayIndex
                ),
                StoryItem(
                    id = "ST_FABRIZIO",
                    authorName = "fabrizioromano",
                    authorRole = "Transfer Gazetecisi",
                    avatarHex = "#1D9BF0",
                    title = "Here We Go! ⏳",
                    caption = "Avrupa scoutları bu sezon ${club.leagueName} maçlarını yakından takip ediyor.",
                    sceneType = "TRANSFER",
                    dayIndex = state.currentDayIndex
                ),
                StoryItem(
                    id = "ST_CAPTAIN_1",
                    authorName = captain.name.lowercase().replace(" ", "_"),
                    authorRole = "Takım Kaptanı",
                    avatarHex = "#20CF79",
                    title = "Antrenman Sonrası",
                    caption = "Soyunma odasında moraller yüksek, maça hazırız! 💪⚽",
                    sceneType = "LOCKER",
                    dayIndex = state.currentDayIndex
                ),
                StoryItem(
                    id = "ST_AGENT_1",
                    authorName = "kemalsoylu_mgmt",
                    authorRole = "Menajer",
                    avatarHex = "#5DADE2",
                    title = "Transfer & Kariyer Masası",
                    caption = "Oyuncumuz ${p.fullName} için bu sezon çok büyük bir vitrin olacak.",
                    sceneType = "TRANSFER",
                    dayIndex = state.currentDayIndex
                )
            )
        }

        val enrichedSocialPosts = if (state.socialPosts.size <= 1 || state.socialPosts.all { it.instaComments.isEmpty() }) {
            listOf(
                SocialMediaPost(
                    id = "SOC_FABRIZIO_1",
                    dayIndex = state.currentDayIndex,
                    dateLabel = "2 saat önce",
                    authorName = "Fabrizio Romano",
                    authorHandle = "@fabrizioromano",
                    isUserPost = false,
                    content = "🚨✨ ${club.name} kadrosunda dikkat çeken genç yetenek: ${p.fullName} (#${p.kitNumber}). Teknik direktör ${club.managerName} oyuncunun potansiyeline (${p.potential}) çok güveniyor. Avrupa kulüplerinin scout ekipleri takipte! 🇹🇷⚽ #HereWeGo",
                    likesCount = 48_920,
                    commentsCount = 614,
                    sentiment = "Destekleyici",
                    topFanComments = listOf(
                        "433: Future star in the making 🔥",
                        "${club.shortName.lowercase()}_ultras: Bizim çocuk! Bu sezon patlama yapacak ❤️"
                    ),
                    instaComments = listOf(
                        InstaCommentItem(
                            id = "IC_FAB_1",
                            authorHandle = "433",
                            authorName = "433",
                            isVerified = true,
                            avatarHex = "#FF3B30",
                            text = "Future star in the making 🔥⚽",
                            timeLabel = "2s",
                            likesCount = 1_420
                        ),
                        InstaCommentItem(
                            id = "IC_FAB_2",
                            authorHandle = "${club.shortName.lowercase()}_ultras",
                            authorName = "${club.shortName} Taraftar",
                            isVerified = false,
                            avatarHex = club.primaryColorHex,
                            text = "Bizim çocuk! Bu sezon ${club.stadiumName}'da tarih yazacak 👏",
                            timeLabel = "1s",
                            likesCount = 318
                        ),
                        InstaCommentItem(
                            id = "IC_FAB_3",
                            authorHandle = "transfermarkt_tr",
                            authorName = "Transfermarkt Türkiye",
                            isVerified = true,
                            avatarHex = "#00A6FF",
                            text = "Güncel piyasa değeri: ${FLFormatters.formatMarketValue(p.marketValueEur)} 📈",
                            timeLabel = "45d",
                            likesCount = 205
                        ),
                        InstaCommentItem(
                            id = "IC_FAB_4",
                            authorHandle = "ardaguler",
                            authorName = "Arda Güler",
                            isVerified = true,
                            avatarHex = "#F59E0B",
                            text = "Yolun açık olsun kardeşim! 💪🇹🇷",
                            timeLabel = "30d",
                            likesCount = 4_890
                        )
                    ),
                    isVerifiedAuthor = true,
                    locationLabel = "${club.city}, ${club.country}",
                    imageSceneType = "TRANSFER"
                ),
                SocialMediaPost(
                    id = "SOC_433_1",
                    dayIndex = state.currentDayIndex,
                    dateLabel = "4 saat önce",
                    authorName = "433",
                    authorHandle = "@433",
                    isUserPost = false,
                    content = "Yeni sezon hazırlıklarında ${p.fullName} antrenman temposuyla göz dolduruyor! Sizce bu sezon kaç gol + asist katkısı yapar? 👇⚽",
                    likesCount = 92_340,
                    commentsCount = 1_280,
                    sentiment = "Destekleyici",
                    topFanComments = listOf(
                        "En az 15 gol katkısı yazar! 🔥",
                        "Sol ayağı / tekniği çok klas."
                    ),
                    instaComments = listOf(
                        InstaCommentItem(
                            id = "IC_433_1",
                            authorHandle = "futbolarena_tr",
                            authorName = "Futbol Arena",
                            isVerified = true,
                            avatarHex = "#10B981",
                            text = "İlk 11'de düzenli oynarsa çift haneli gol ve asist sayısına rahat ulaşır 🎯",
                            timeLabel = "3s",
                            likesCount = 512
                        ),
                        InstaCommentItem(
                            id = "IC_433_2",
                            authorHandle = "kerem_akturkoglu",
                            authorName = "Kerem",
                            isVerified = true,
                            avatarHex = "#EF4444",
                            text = "Başarılar kardeşim, sakatlıksız güzel bir sezon olsun 🙏⚽",
                            timeLabel = "2s",
                            likesCount = 1_190
                        ),
                        InstaCommentItem(
                            id = "IC_433_3",
                            authorHandle = "burak_futbolsever",
                            authorName = "Burak Yılmaz",
                            isVerified = false,
                            avatarHex = "#8B5CF6",
                            text = "İlk hafta maçını sabırsızlıkla bekliyoruz!",
                            timeLabel = "1s",
                            likesCount = 64
                        )
                    ),
                    isVerifiedAuthor = true,
                    locationLabel = club.stadiumName,
                    imageSceneType = "TRAINING"
                ),
                SocialMediaPost(
                    id = "SOC_CLUB_OFFICIAL_1",
                    dayIndex = state.currentDayIndex,
                    dateLabel = "6 saat önce",
                    authorName = "${club.name} Resmi",
                    authorHandle = "@${club.shortName.lowercase()}resmi",
                    isUserPost = false,
                    content = "📸 Takımımız yeni sezonun ilk lig maçı için hazırlıklarını ${club.stadiumName} tesislerinde sürdürdü. #${club.shortName} ⚽💪",
                    likesCount = 14_580,
                    commentsCount = 342,
                    sentiment = "Destekleyici",
                    topFanComments = listOf(
                        "Bu sene o kupa buraya gelecek!",
                        "${p.kitName} ilk 11'de başlasın hocam!"
                    ),
                    instaComments = listOf(
                        InstaCommentItem(
                            id = "IC_CLB_1",
                            authorHandle = "${club.shortName.lowercase()}_12numara",
                            authorName = "12. Numara Tribün",
                            isVerified = false,
                            avatarHex = club.primaryColorHex,
                            text = "${p.kitName} (#${p.kitNumber}) ilk 11'de başlamalı hocam! 🔥",
                            timeLabel = "5s",
                            likesCount = 289
                        ),
                        InstaCommentItem(
                            id = "IC_CLB_2",
                            authorHandle = "trtspor",
                            authorName = "TRT Spor",
                            isVerified = true,
                            avatarHex = "#2563EB",
                            text = "${club.name}'da yeni sezon heyecanı zirvede.",
                            timeLabel = "4s",
                            likesCount = 114
                        )
                    ),
                    isVerifiedAuthor = true,
                    locationLabel = "${club.name} Tesisleri",
                    imageSceneType = "STADIUM"
                )
            )
        } else {
            state.socialPosts
        }

        val xPosts = state.xPosts.ifEmpty {
            listOf(
                XPostItem(
                    id = "XP_INIT_1",
                    dayIndex = state.currentDayIndex,
                    dateLabel = "2s",
                    authorName = "Fabrizio Romano",
                    handle = "@FabrizioRomano",
                    category = "GAZETECİ",
                    isVerified = true,
                    text = "🔎 ${club.name} Teknik Direktörü ${club.managerName}, yeni sezon öncesinde ${p.fullName} (#${p.kitNumber}) ile özel bir görüşme gerçekleştirdi. Genç oyuncunun idman temposu teknik heyetten tam not aldı. 🇹🇷✨",
                    likes = 12_450,
                    reposts = 1_280
                ),
                XPostItem(
                    id = "XP_INIT_2",
                    dayIndex = state.currentDayIndex,
                    dateLabel = "4s",
                    authorName = "Sports Digitale",
                    handle = "@SportsDigitale",
                    category = "YORUMCU",
                    isVerified = true,
                    text = "📊 ÖZEL ANALİZ | ${club.name} bu sezon ${club.managerTacticalStyle} sistemiyle sahada olacak. ${p.primaryPosition.titleTr} bölgesinde ${p.fullName} (OVR ${p.overallRating} -> POT ${p.potential}) takımın kilit ismi olabilir.",
                    likes = 3_840,
                    reposts = 412
                ),
                XPostItem(
                    id = "XP_INIT_3",
                    dayIndex = state.currentDayIndex,
                    dateLabel = "5s",
                    authorName = "OptaCan",
                    handle = "@OptaCan",
                    category = "YORUMCU",
                    isVerified = true,
                    text = "${p.kitNumber} - ${club.name} formasıyla yeni sezona hazırlanan ${p.fullName}, antrenman maçlarında %88 pas isabeti ve yüksek hücum presiyle dikkat çekti. Vizyon.",
                    likes = 2_190,
                    reposts = 215
                ),
                XPostItem(
                    id = "XP_INIT_4",
                    dayIndex = state.currentDayIndex,
                    dateLabel = "6s",
                    authorName = "${club.shortName} Ruhu",
                    handle = "@${club.shortName.lowercase()}_ruhu",
                    category = "TARAFTAR",
                    isVerified = false,
                    text = "Bu sezon ${club.stadiumName} tribünlerinde ${p.kitName} tezahüratları yankılanacak! Maç gününü iple çekiyoruz! ⚽🔥",
                    likes = 845,
                    reposts = 94
                )
            )
        }

        // Enrich WhatsApp NPCs with Team Group Chat and Journalist if not present
        val enrichedNpcs = state.npcs.toMutableList()
        if (enrichedNpcs.none { it.id == "NPC_TEAM_GROUP" }) {
            val captain = club.keySquadMembers.find { it.isCaptain } ?: club.keySquadMembers.first()
            enrichedNpcs.add(
                NpcCharacter(
                    id = "NPC_TEAM_GROUP",
                    name = "${club.shortName} Soyunma Odası ⚽",
                    roleTitle = "Takım WhatsApp Grubu",
                    avatarColorHex = "#00A884",
                    personality = "Takım Arkadaşları & Kaptan",
                    affinity = 72,
                    trust = 70,
                    currentMood = "Enerjik",
                    shortTermGoal = "Haftanın maçını kazanmak",
                    longTermGoal = "Takım ruhunu zirvede tutmak",
                    importantMemories = listOf("Kaptan ${captain.name} seni gruba ekledi."),
                    quickReplyOptions = listOf(
                        "Beyler bu hafta sahada basmadık yer bırakmayalım! 🔥",
                        "İdman sonrası takımca kahveye / yemeğe geçelim mi?",
                        "Herkese iyi antrenmanlar kardeşlerim 💪⚽"
                    ),
                    messages = listOf(
                        ChatMessage(
                            id = "MSG_GRP_1",
                            senderName = captain.name,
                            isFromUser = false,
                            text = "${captain.name}: Beyler yarınki maç öncesi kampa giriyoruz, herkes dinlenmesine dikkat etsin! Hoş geldin aramıza @${p.firstName} 👏⚽",
                            dayIndex = 1,
                            timeLabel = "11:45",
                            emotionTag = "Grup"
                        )
                    ),
                    unreadCount = 1
                )
            )
        }
        if (enrichedNpcs.none { it.id == "NPC_PRESS" }) {
            enrichedNpcs.add(
                NpcCharacter(
                    id = "NPC_PRESS",
                    name = "Yağız Sabuncuoğlu",
                    roleTitle = "Spor Gazetecisi",
                    avatarColorHex = "#1D9BF0",
                    personality = "Özel Haber & Transfer Kulis",
                    affinity = 58,
                    trust = 60,
                    currentMood = "Meraklı",
                    shortTermGoal = "Özel röportaj ve kulis bilgisi almak",
                    longTermGoal = "Transfer gelişmelerini ilk duyuran olmak",
                    importantMemories = listOf("Numaranı menajerin üzerinden aldı."),
                    quickReplyOptions = listOf(
                        "Şu an sadece kulübümün başarısına odaklandım.",
                        "Hedefim bu sezon çift haneli skora ulaşmak.",
                        "Avrupa hedefim var ama önce burada şampiyonluk yaşamak istiyorum."
                    ),
                    messages = listOf(
                        ChatMessage(
                            id = "MSG_PRS_1",
                            senderName = "Yağız Sabuncuoğlu",
                            isFromUser = false,
                            text = "Selam ${p.firstName}, yeni sezonun hayırlı olsun. ${club.name} taraftarı seni çok merak ediyor, ilk maç öncesi kısa bir değerlendirmen olur mu?",
                            dayIndex = 1,
                            timeLabel = "12:15",
                            emotionTag = "Basın"
                        )
                    ),
                    unreadCount = 1
                )
            )
        }

        // Enrich Instagram Direct (DM) Threads & Pending Message Requests ("İstekler")
        val instaDms = state.instaDmThreads.ifEmpty {
            listOf(
                InstaDmThread(
                    id = "IDM_ARDA",
                    senderName = "Arda Güler",
                    senderHandle = "@ardaguler",
                    senderRole = "Real Madrid • Milli Futbolcu",
                    avatarHex = "#F59E0B",
                    isVerified = true,
                    followersLabel = "14,8M takipçi",
                    isPendingRequest = false,
                    unreadCount = 1,
                    messages = listOf(
                        ChatMessage(
                            id = "IDM_MSG_ARDA_1",
                            senderName = "Arda Güler",
                            isFromUser = false,
                            text = "Kardeşim yeni sezonun hayırlı olsun! ${club.name} formasıyla gollerini bekliyorum, sahada başarılar 🇹🇷⚽🔥",
                            dayIndex = 1,
                            timeLabel = "10:30"
                        )
                    )
                ),
                InstaDmThread(
                    id = "IDM_CLUB_MEDIA",
                    senderName = "${club.name} Medya",
                    senderHandle = "@${club.shortName.lowercase()}resmi",
                    senderRole = "Resmi Kulüp Hesabı",
                    avatarHex = club.primaryColorHex,
                    isVerified = true,
                    followersLabel = "485B takipçi",
                    isPendingRequest = false,
                    unreadCount = 0,
                    messages = listOf(
                        ChatMessage(
                            id = "IDM_MSG_CLB_1",
                            senderName = "${club.name} Medya",
                            isFromUser = false,
                            text = "Hoş geldin ${p.firstName}! Maç ve antrenman fotoğraflarını buradan da etiketleyerek paylaşabilirsin 📸",
                            dayIndex = 1,
                            timeLabel = "09:15"
                        )
                    )
                ),
                // PENDING MESSAGE REQUESTS ("İstekler" tab in Instagram Direct!)
                InstaDmThread(
                    id = "IDM_REQ_NIKE",
                    senderName = "Nike Football Türkiye",
                    senderHandle = "@niketurkiye",
                    senderRole = "Resmi Marka Sponsorluğu",
                    avatarHex = "#111827",
                    isVerified = true,
                    followersLabel = "2,4M takipçi",
                    isPendingRequest = true,
                    unreadCount = 1,
                    messages = listOf(
                        ChatMessage(
                            id = "IDM_MSG_NIKE_1",
                            senderName = "Nike Football Türkiye",
                            isFromUser = false,
                            text = "Merhaba ${p.fullName}! Yeni sezon yükselişini yakından takip ediyoruz. Mercurial serisi krampon sponsorluğu ve ortak tanıtım için seninle iletişime geçmek istedik 👟⚡",
                            dayIndex = 1,
                            timeLabel = "11:20"
                        )
                    )
                ),
                InstaDmThread(
                    id = "IDM_REQ_INFLUENCER",
                    senderName = "Elvin Yıldırım",
                    senderHandle = "@elvinyildirim",
                    senderRole = "Moda & Dijital İçerik Üreticisi",
                    avatarHex = "#E1306C",
                    isVerified = true,
                    followersLabel = "420B takipçi",
                    isPendingRequest = true,
                    unreadCount = 1,
                    messages = listOf(
                        ChatMessage(
                            id = "IDM_MSG_ELV_1",
                            senderName = "Elvin Yıldırım",
                            isFromUser = false,
                            text = "Selam ${p.firstName}! ✨ ${club.stadiumName}'daki imza gününde çok karizmatik görünüyordun. Müsait bir akşam kahve içip tanışmak ister misin? ☕😊",
                            dayIndex = 1,
                            timeLabel = "12:05"
                        )
                    )
                ),
                InstaDmThread(
                    id = "IDM_REQ_FABRIZIO",
                    senderName = "Fabrizio Romano",
                    senderHandle = "@fabrizioromano",
                    senderRole = "Futbol Gazetecisi",
                    avatarHex = "#1D9BF0",
                    isVerified = true,
                    followersLabel = "32,5M takipçi",
                    isPendingRequest = true,
                    unreadCount = 1,
                    messages = listOf(
                        ChatMessage(
                            id = "IDM_MSG_FAB_1",
                            senderName = "Fabrizio Romano",
                            isFromUser = false,
                            text = "Hi ${p.firstName}! 🇮🇹⚽ Avrupa scoutları seni soruyor. Kariyer hedeflerinle ilgili özel bir DM röportajı yapmak isterim. Here we go!",
                            dayIndex = 1,
                            timeLabel = "12:40"
                        )
                    )
                )
            )
        }

        val gallery = state.galleryPhotos.ifEmpty {
            listOf(
                GalleryPhotoItem(
                    id = "GAL_INIT_1",
                    dayIndex = 1,
                    dateLabel = fullDate,
                    album = "Kutlamalar",
                    title = "${club.name} Sözleşme İmza Günü",
                    caption = "${p.fullName}, #${p.kitNumber} numaralı ${club.name} formasıyla basına poz verdi.",
                    sceneDrawableType = "TRANSFER"
                ),
                GalleryPhotoItem(
                    id = "GAL_INIT_2",
                    dayIndex = 1,
                    dateLabel = fullDate,
                    album = "Antrenman",
                    title = "Sezonun İlk Saha Antrenmanı",
                    caption = "${club.name} tesislerinde kondisyon ve taktik hazırlık.",
                    sceneDrawableType = "TRAINING"
                ),
                GalleryPhotoItem(
                    id = "GAL_INIT_3",
                    dayIndex = 1,
                    dateLabel = fullDate,
                    album = "Maçlar",
                    title = "${club.stadiumName} Gece Atmosferi",
                    caption = "İlk lig maçı öncesi stadyum ışıkları altında son kontroller.",
                    sceneDrawableType = "STADIUM"
                )
            )
        }

        val calls = state.callRecords.ifEmpty {
            listOf(
                PhoneCallRecord(
                    id = "CALL_INIT_AGENT",
                    npcId = "NPC_AGENT",
                    callerName = state.contract.agentName,
                    callerRole = "Menajer",
                    dayIndex = 1,
                    dateLabel = shortDate,
                    topic = "Sezon Hedefleri & Prim Planlaması",
                    transcript = "Merhaba ${p.firstName}! İlk hafta maçın yaklaşıyor. Sahada göstereceğin performansla hem maç başı ${FLFormatters.formatCurrency(state.contract.matchAppearanceBonusEur)} primini alacaksın hem de üst lig gözlemcilerinin dikkatini çekeceksin. Ne durumdasın?",
                    options = listOf(
                        "Kendimi çok formda hissediyorum, ilk maçta fark yaratacağım.",
                        "Hocanın taktik planına odaklandım, adım adım ilerleyeceğiz.",
                        "Devre arasında üst lig tekliflerini şimdiden takip edelim."
                    ),
                    completed = false
                ),
                PhoneCallRecord(
                    id = "CALL_INIT_FAMILY",
                    npcId = "NPC_FAMILY",
                    callerName = "Babam (Hasan ${p.lastName})",
                    callerRole = "Aile",
                    dayIndex = 1,
                    dateLabel = shortDate,
                    topic = "Aile Desteği & İlk Maç Heyecanı",
                    transcript = "Oğlum sesini duymak istedik. Annenle beraber hafta sonu maça gelmek için hazırlık yapıyoruz. Bir eksiğin, ihtiyacın var mı?",
                    options = listOf(
                        "Her şey yolunda baba, tribünde olmanız bana güç verir!",
                        "Çok sağ ol baba, tesislerde çok iyi ağırlanıyoruz."
                    ),
                    completed = false
                )
            )
        }

        return state.copy(
            saveVersion = 3,
            npcs = enrichedNpcs,
            socialPosts = enrichedSocialPosts,
            marketValueHistory = marketHistory,
            transferHistory = transferHist,
            attributeHistory = attrHistory,
            achievements = achievements,
            stories = stories,
            xPosts = xPosts,
            galleryPhotos = gallery,
            callRecords = calls,
            instaDmThreads = instaDms
        )
    }

    /**
     * Synchronized Time Flow:
     * Pressing the top-right "İLERLE" button advances the clock smoothly through the day
     * (09:00 -> 14:00 -> 19:00 -> next day 09:00) and generates live notifications/messages.
     */
    fun advanceSynchronizedTime(state: CareerSaveState): Pair<CareerSaveState, String> {
        val unplayedToday = state.todayUserFixture
        if (unplayedToday != null && state.currentHour >= 17) {
            return state to "Maç saati geldi (${unplayedToday.homeClubName} vs ${unplayedToday.awayClubName} • ${unplayedToday.kickOffTime})! Sağdaki 'MAÇA ÇIK' butonuna basarak sahaya çıkabilirsin."
        }

        val nextMatch = unplayedToday ?: state.nextUnplayedUserFixture
        val oppName = nextMatch?.let {
            if (it.homeClubId == state.contract.clubId) it.awayClubName else it.homeClubName
        } ?: "rakip"

        return when {
            state.currentHour < 13 -> {
                val newHour = 14
                val updatedNpcs = state.npcs.map { npc ->
                    when {
                        unplayedToday != null && npc.id == "NPC_TEAM_GROUP" -> {
                            val captain = state.currentClub.keySquadMembers.firstOrNull { it.isCaptain }?.name ?: "Kaptan"
                            val msg = ChatMessage(
                                id = "MSG_NOON_MATCH_D${state.currentDayIndex}",
                                senderName = captain,
                                isFromUser = false,
                                text = "Beyler bugün $oppName maçı var! Öğle yemeğinden sonra taktik toplantıda buluşuyoruz, herkes konsantre olsun! 🔥⚽",
                                dayIndex = state.currentDayIndex,
                                timeLabel = "14:00"
                            )
                            if (npc.messages.none { it.id == msg.id }) {
                                npc.copy(messages = npc.messages + msg, unreadCount = npc.unreadCount + 1)
                            } else npc
                        }
                        unplayedToday == null && !state.trainedToday && npc.id == "NPC_MANAGER" && state.currentDayIndex % 2 == 1 -> {
                            val msg = ChatMessage(
                                id = "MSG_NOON_TRN_D${state.currentDayIndex}",
                                senderName = npc.name,
                                isFromUser = false,
                                text = "${state.player.firstName}, saat 14:00 saha idmanı başlıyor. $oppName maçı kadrosu için bugünkü antrenman performansını yakından izleyeceğim.",
                                dayIndex = state.currentDayIndex,
                                timeLabel = "14:00"
                            )
                            if (npc.messages.none { it.id == msg.id }) {
                                npc.copy(messages = npc.messages + msg, unreadCount = npc.unreadCount + 1)
                            } else npc
                        }
                        else -> npc
                    }
                }

                val note = if (!state.trainedToday && unplayedToday == null) {
                    "Saat 14:00 • Öğle seansı. Teknik direktör antrenman sahasında seni bekliyor!"
                } else if (unplayedToday != null) {
                    "Saat 14:00 • Maç günü kampı ve taktik toplantı saati."
                } else {
                    "Saat 14:00 • Taktik analiz ve dinlenme saati."
                }
                state.copy(
                    updatedAtMs = System.currentTimeMillis(),
                    currentHour = newHour,
                    npcs = updatedNpcs
                ) to note
            }
            state.currentHour < 19 -> {
                val newHour = if (unplayedToday != null) 19 else 20
                val fameGain = (state.player.overallRating / 3).coerceAtLeast(18) + (if (state.trainedToday) 15 else 0)
                val updatedPlayer = state.player.copy(
                    followersCount = state.player.followersCount + fameGain
                )
                val maybeNewDmList = maybeGenerateFameDmRequest(state.instaDmThreads, updatedPlayer, state.currentClub, state.currentDayIndex)
                val dmAdded = maybeNewDmList.size > state.instaDmThreads.size

                val updatedNpcs = state.npcs.map { npc ->
                    when {
                        unplayedToday != null && npc.id == "NPC_MANAGER" -> {
                            val msg = ChatMessage(
                                id = "MSG_EVE_MATCH_D${state.currentDayIndex}",
                                senderName = npc.name,
                                isFromUser = false,
                                text = "Stadyuma ulaştık ${state.player.firstName}! $oppName karşısında kadro durumun: ${state.nextMatchStartingStatus.labelTr}. Soyunma odasında son konuşmayı yapıp sahaya çıkıyoruz!",
                                dayIndex = state.currentDayIndex,
                                timeLabel = "19:00"
                            )
                            if (npc.messages.none { it.id == msg.id }) {
                                npc.copy(messages = npc.messages + msg, unreadCount = npc.unreadCount + 1)
                            } else npc
                        }
                        unplayedToday == null && !state.trainedToday && state.player.injuryDaysRemaining == 0 && npc.id == "NPC_MANAGER" -> {
                            val msg = ChatMessage(
                                id = "MSG_EVE_SKIP_D${state.currentDayIndex}",
                                senderName = npc.name,
                                isFromUser = false,
                                text = "Bugün tesislerdeki idmanı tamamlamadın ${state.player.firstName}. İlk 11'de yer bulmak istiyorsan antrenman disiplinini bozmamalısın!",
                                dayIndex = state.currentDayIndex,
                                timeLabel = "20:00",
                                emotionTag = "Uyarı"
                            )
                            if (npc.messages.none { it.id == msg.id }) {
                                npc.copy(messages = npc.messages + msg, unreadCount = npc.unreadCount + 1)
                            } else npc
                        }
                        else -> npc
                    }
                }

                // Trigger a dynamic dilemma in the evening if none is active on even days
                val dilemmas = WorldDataCatalog.buildDynamicDilemmas(state.currentClub.name, state.currentClub.managerName)
                val eveningDilemma = state.activeDilemma ?: dilemmas.find { it.dayIndex == state.currentDayIndex }

                val note = if (unplayedToday != null) {
                    "Saat 19:00 • Stadyum ışıkları yandı! 'MAÇA ÇIK' butonuyla sahaya adım atabilirsin."
                } else if (dmAdded) {
                    "Saat 20:00 • Akşam seansı. Instagram'da yeni bir DM isteğin var! (+$fameGain takipçi)"
                } else {
                    "Saat 20:00 • Akşam saatleri (+$fameGain yeni takipçi)."
                }
                state.copy(
                    updatedAtMs = System.currentTimeMillis(),
                    currentHour = newHour,
                    player = updatedPlayer,
                    npcs = updatedNpcs,
                    instaDmThreads = maybeNewDmList,
                    activeDilemma = eveningDilemma
                ) to note
            }
            else -> {
                val (nextDayState, msg) = advanceToNextDay(state)
                nextDayState.copy(currentHour = 9) to msg
            }
        }
    }

    private fun maybeGenerateFameDmRequest(
        currentThreads: List<InstaDmThread>,
        player: PlayerProfile,
        club: Club,
        dayIndex: Int
    ): List<InstaDmThread> {
        val candidatePool = listOf(
            InstaDmThread(
                id = "IDM_REQ_PUMA_D$dayIndex",
                senderName = "PUMA Football",
                senderHandle = "@pumafootball",
                senderRole = "Global Spor Markası",
                avatarHex = "#10B981",
                isVerified = true,
                followersLabel = "8,1M takipçi",
                isPendingRequest = true,
                unreadCount = 1,
                messages = listOf(
                    ChatMessage(
                        id = "IDM_PUMA_M1_$dayIndex",
                        senderName = "PUMA Football",
                        isFromUser = false,
                        text = "Selam ${player.firstName}! 🐆 Sahadaki hızın ve tekniğin PUMA Future serisiyle tam uyumlu. Özel lansman davetimiz için DM'den dönüşünü bekliyoruz!",
                        dayIndex = dayIndex,
                        timeLabel = "19:30"
                    )
                )
            ),
            InstaDmThread(
                id = "IDM_REQ_SCOUT_D$dayIndex",
                senderName = "Marco Rossi (UEFA Scout)",
                senderHandle = "@marcorossi_scout",
                senderRole = "Avrupa Yetenek Avcısı",
                avatarHex = "#6366F1",
                isVerified = true,
                followersLabel = "94B takipçi",
                isPendingRequest = true,
                unreadCount = 1,
                messages = listOf(
                    ChatMessage(
                        id = "IDM_SCT_M1_$dayIndex",
                        senderName = "Marco Rossi",
                        isFromUser = false,
                        text = "Ciao ${player.firstName}! ${club.name} maçındaki son performans raporunu inceledim. Serie A ve La Liga kulüpleri için radarımızdasın 👀⚽",
                        dayIndex = dayIndex,
                        timeLabel = "19:45"
                    )
                )
            ),
            InstaDmThread(
                id = "IDM_REQ_CELEB_D$dayIndex",
                senderName = "Selin Derin",
                senderHandle = "@selinderin_official",
                senderRole = "Oyuncu & Sunucu",
                avatarHex = "#EC4899",
                isVerified = true,
                followersLabel = "1,1M takipçi",
                isPendingRequest = true,
                unreadCount = 1,
                messages = listOf(
                    ChatMessage(
                        id = "IDM_CEL_M1_$dayIndex",
                        senderName = "Selin Derin",
                        isFromUser = false,
                        text = "Selam ${player.firstName}! 🌟 Spor ödülleri gecesinde seninle de tanışmak çok isterim, başarıların daim olsun!",
                        dayIndex = dayIndex,
                        timeLabel = "20:00"
                    )
                )
            )
        )
        val pick = candidatePool[dayIndex % candidatePool.size]
        if (currentThreads.any { it.senderHandle == pick.senderHandle }) return currentThreads
        return listOf(pick) + currentThreads
    }

    /**
     * Generates a real 7-column Monday-first monthly calendar grid (42 cells = 6 weeks x 7 days).
     */
    fun getMonthCalendarGrid(
        state: CareerSaveState,
        monthOffset: Int
    ): MonthCalendarGrid {
        val safeMonthIdx = monthOffset.coerceIn(0, monthNamesTr.lastIndex)
        val year = if (safeMonthIdx >= 5) state.currentSeasonYear + 1 else state.currentSeasonYear
        val monthTitle = "${monthNamesTr[safeMonthIdx]} $year"

        val userFixtures = state.fixtures.filter {
            it.homeClubId == state.contract.clubId || it.awayClubId == state.contract.clubId
        }

        val firstDayTotalOffset = safeMonthIdx * 30
        val firstWeekdayMon0 = ((firstDayTotalOffset - 10) % 7 + 7) % 7
        val daysInMonth = 30

        val cells = mutableListOf<MonthDayCell>()
        for (cellIdx in 0 until 42) {
            val dayNum = cellIdx - firstWeekdayMon0 + 1
            if (dayNum < 1 || dayNum > daysInMonth) {
                cells.add(MonthDayCell(dayOfMonth = 0, dayIndex = null))
            } else {
                val totalOffset = safeMonthIdx * 30 + (dayNum - 1)
                val mappedDayIndex = totalOffset - 9
                if (mappedDayIndex < 1) {
                    cells.add(
                        MonthDayCell(
                            dayOfMonth = dayNum,
                            dayIndex = null
                        )
                    )
                } else {
                    val dayEvents = state.calendarEvents.filter { it.dayIndex == mappedDayIndex }
                    val dayFixture = userFixtures.find { it.dayIndex == mappedDayIndex }
                    val isMatch = dayFixture != null || dayEvents.any { it.eventType == "MAÇ" }
                    val isTraining = dayEvents.any { it.eventType == "ANTRENMAN" }
                    val isSocial = dayEvents.any { it.eventType !in listOf("MAÇ", "ANTRENMAN") }
                    cells.add(
                        MonthDayCell(
                            dayOfMonth = dayNum,
                            dayIndex = mappedDayIndex,
                            isCurrentDay = mappedDayIndex == state.currentDayIndex,
                            isMatchDay = isMatch,
                            isTrainingDay = isTraining,
                            isSocialOrTransferDay = isSocial,
                            events = dayEvents,
                            fixture = dayFixture
                        )
                    )
                }
            }
        }

        return MonthCalendarGrid(
            monthIndex = safeMonthIdx,
            monthTitleTr = monthTitle,
            cells = cells
        )
    }

    fun getMonthIndexForDay(dayIndex: Int): Int {
        val zeroBased = (dayIndex - 1).coerceAtLeast(0)
        return (((10 + zeroBased) / 30) % monthNamesTr.size).coerceIn(0, monthNamesTr.lastIndex)
    }

    fun toggleLikeInstaPost(state: CareerSaveState, postId: String): CareerSaveState {
        val updated = state.socialPosts.map { post ->
            if (post.id == postId) {
                val nowLiked = !post.likedByUser
                post.copy(
                    likedByUser = nowLiked,
                    likesCount = (post.likesCount + if (nowLiked) 1 else -1).coerceAtLeast(0)
                )
            } else post
        }
        return state.copy(updatedAtMs = System.currentTimeMillis(), socialPosts = updated)
    }

    fun toggleSaveInstaPost(state: CareerSaveState, postId: String): CareerSaveState {
        val updated = state.socialPosts.map { post ->
            if (post.id == postId) post.copy(savedByUser = !post.savedByUser) else post
        }
        return state.copy(updatedAtMs = System.currentTimeMillis(), socialPosts = updated)
    }

    fun toggleLikeInstaComment(state: CareerSaveState, postId: String, commentId: String): CareerSaveState {
        val updated = state.socialPosts.map { post ->
            if (post.id == postId) {
                val newComments = post.instaComments.map { c ->
                    if (c.id == commentId) {
                        val liked = !c.likedByUser
                        c.copy(
                            likedByUser = liked,
                            likesCount = (c.likesCount + if (liked) 1 else -1).coerceAtLeast(0)
                        )
                    } else c
                }
                post.copy(instaComments = newComments)
            } else post
        }
        return state.copy(updatedAtMs = System.currentTimeMillis(), socialPosts = updated)
    }

    fun commentOnInstaPost(state: CareerSaveState, postId: String, commentText: String): CareerSaveState {
        val clean = commentText.trim()
        if (clean.isEmpty()) return state
        val userHandle = "${state.player.firstName.lowercase()}${state.player.kitNumber}"
        val userComment = InstaCommentItem(
            id = "IC_USER_${System.currentTimeMillis()}",
            authorHandle = userHandle,
            authorName = state.player.fullName,
            isVerified = true,
            avatarHex = state.currentClub.primaryColorHex,
            text = clean,
            timeLabel = "Şimdi",
            likesCount = 1
        )
        val autoReply = InstaCommentItem(
            id = "IC_REPLY_${System.currentTimeMillis() + 1}",
            authorHandle = "${state.currentClub.shortName.lowercase()}_taraftar",
            authorName = "${state.currentClub.shortName} Sevdalısı",
            isVerified = false,
            avatarHex = "#10B981",
            text = "@$userHandle Kral yorumlarda! Sahada şovunu bekliyoruz 🔥⚽",
            timeLabel = "Şimdi",
            likesCount = 5
        )
        val updated = state.socialPosts.map { post ->
            if (post.id == postId) {
                post.copy(
                    commentsCount = post.commentsCount + 2,
                    topFanComments = listOf("${state.player.fullName}: $clean") + post.topFanComments,
                    instaComments = listOf(userComment, autoReply) + post.instaComments
                )
            } else post
        }
        return state.copy(
            updatedAtMs = System.currentTimeMillis(),
            player = state.player.copy(followersCount = state.player.followersCount + 35),
            socialPosts = updated
        )
    }

    fun acceptInstaDmRequest(state: CareerSaveState, threadId: String): Pair<CareerSaveState, String> {
        val target = state.instaDmThreads.find { it.id == threadId } ?: return state to "İstek bulunamadı."
        val updatedThreads = state.instaDmThreads.map { th ->
            if (th.id == threadId) th.copy(isPendingRequest = false, unreadCount = 0) else th
        }
        val followersBoost = 180
        return state.copy(
            updatedAtMs = System.currentTimeMillis(),
            player = state.player.copy(
                followersCount = state.player.followersCount + followersBoost,
                morale = (state.player.morale + 2).coerceAtMost(100)
            ),
            instaDmThreads = updatedThreads
        ) to "${target.senderName} (${target.senderHandle}) mesaj isteği kabul edildi! Artık doğrudan mesajlaşabilirsin."
    }

    fun deleteInstaDmRequest(state: CareerSaveState, threadId: String): Pair<CareerSaveState, String> {
        val target = state.instaDmThreads.find { it.id == threadId } ?: return state to "İstek bulunamadı."
        val updatedThreads = state.instaDmThreads.filterNot { it.id == threadId }
        return state.copy(
            updatedAtMs = System.currentTimeMillis(),
            instaDmThreads = updatedThreads
        ) to "${target.senderHandle} mesaj isteği silindi."
    }

    fun sendInstaDmMessage(
        state: CareerSaveState,
        threadId: String,
        userText: String
    ): CareerSaveState {
        val clean = userText.trim()
        if (clean.isEmpty()) return state
        val thread = state.instaDmThreads.find { it.id == threadId } ?: return state
        val clockStr = state.formattedClockTime

        val userMsg = ChatMessage(
            id = "IDM_USR_${System.currentTimeMillis()}",
            senderName = state.player.fullName,
            isFromUser = true,
            text = clean,
            dayIndex = state.currentDayIndex,
            timeLabel = clockStr
        )

        val lower = clean.lowercase()
        val handleLower = thread.senderHandle.lowercase()

        var updatedBalance = state.balanceEur
        var updatedTransactions = state.transactions
        var updatedPlayer = state.player
        var updatedXPosts = state.xPosts
        var impactTag: String? = null

        val replyText = when {
            handleLower.contains("nike") || handleLower.contains("puma") -> when {
                lower.contains("evet") || lower.contains("kabul") || lower.contains("olur") || lower.contains("anlaş") || lower.contains("memnun") || lower.contains("imza") || lower.contains("sponsor") -> {
                    val txId = "TX_SPONSOR_${thread.id}_D${state.currentDayIndex}"
                    val alreadyPaidToday = updatedTransactions.any { it.id == txId }
                    if (!alreadyPaidToday) {
                        val sponsorBonus = 15_000L
                        updatedBalance += sponsorBonus
                        updatedTransactions = listOf(
                            FinancialTransaction(
                                id = txId,
                                dayIndex = state.currentDayIndex,
                                dateLabel = formatGameDate(state.currentDayIndex, state.currentSeasonYear),
                                category = "SPONSORLUK",
                                amountEur = sponsorBonus,
                                description = "${thread.senderName} Krampon & Kampanya Sponsorluğu",
                                balanceAfterEur = updatedBalance
                            )
                        ) + updatedTransactions
                        updatedPlayer = updatedPlayer.copy(
                            followersCount = updatedPlayer.followersCount + 950,
                            fanSupport = (updatedPlayer.fanSupport + 4).coerceAtMost(100),
                            morale = (updatedPlayer.morale + 5).coerceAtMost(100)
                        )
                        impactTag = "Sponsorluk Ödemesi: +15.000 € • +950 Takipçi"
                        "Anlaşmamız resmiyet kazandı ${state.player.firstName}! 👟⚡ **15.000 €** kampanya imza primini banka hesabına yatırdık ve özel üretim kramponlarını kulüp tesislerine kargoladık!"
                    } else {
                        "Harika iş birliğimiz devam ediyor ${state.player.firstName}! 👟⚡ Yeni kramponlarınla sahada gollerini bekliyoruz!"
                    }
                }
                lower.contains("ücret") || lower.contains("para") || lower.contains("bütçe") || lower.contains("ne kadar") ->
                    "Kampanya paketimiz kapsamında hemen **15.000 €** peşin sponsorluk primi ve +950 takipçi etkileşimi sunuyoruz. 'Kabul ediyorum, anlaşalım' yazman yeterli! 💼🔥"
                else ->
                    "Mesajın için teşekkürler ${state.player.firstName}! Senin gibi yükselen bir yıldızla çalışmak istiyoruz. Teklifimizi onaylamak için 'Anlaşalım' yazabilirsin ⚡⚽"
            }
            handleLower.contains("fabrizio") || handleLower.contains("scout") -> {
                val xpId = "XP_FAB_${System.currentTimeMillis()}"
                val fabPost = XPostItem(
                    id = xpId,
                    dayIndex = state.currentDayIndex,
                    dateLabel = formatShortDate(state.currentDayIndex),
                    authorName = thread.senderName,
                    handle = thread.senderHandle,
                    category = "GAZETECİ",
                    isVerified = true,
                    text = "🚨✨ ${state.currentClub.name} genç yıldızı ${updatedPlayer.fullName} ile özel görüştüm: \"$clean\" — Avrupa kulüpleri oyuncuyu yakından izliyor! 👀🔥",
                    likes = 4_200 + updatedPlayer.followersCount / 4,
                    reposts = 680
                )
                updatedXPosts = listOf(fabPost) + updatedXPosts
                updatedPlayer = updatedPlayer.copy(
                    marketValueEur = updatedPlayer.marketValueEur + 25_000L,
                    followersCount = updatedPlayer.followersCount + 520
                )
                impactTag = "X Gündemi • Piyasa Değeri +25B € • +520 Takipçi"
                when {
                    lower.contains("avrupa") || lower.contains("transfer") || lower.contains("hedef") || lower.contains("lig") ->
                        "Bunu hemen özel haber yaptım ${state.player.firstName}! 🇮🇹✨ '${updatedPlayer.fullName} Avrupa hedefini doğruladı' paylaşımım X'te binlerce etkileşim aldı, piyasa değerin de yükseldi! Here we go soon! 👀🔥"
                    else ->
                        "Thanks bro! 👊 Sözlerini X hesabımdan takipçilerimle paylaştım, Avrupa scoutları seni konuşuyor!"
                }
            }
            handleLower.contains("elvin") || handleLower.contains("selin") -> {
                updatedPlayer = updatedPlayer.copy(
                    morale = (updatedPlayer.morale + 6).coerceAtMost(100),
                    followersCount = updatedPlayer.followersCount + 410
                )
                impactTag = "Moral +6 • +410 Takipçi"
                when {
                    lower.contains("kahve") || lower.contains("buluş") || lower.contains("akşam") || lower.contains("olur") || lower.contains("nerede") ->
                        "Harika! 😊✨ İdman programın bittiğinde şehir merkezindeki teras kafede buluşalım o zaman. Maçta da locadan seni destekleyeceğim! ☕❤️"
                    else ->
                        "Çok tatlısın ${state.player.firstName}! ✨ Sahadaki enerjin gerçekten çok etkileyici, bu hafta maçından sonra kutlama kahvesi sözümüz olsun ☕🌟"
                }
            }
            handleLower.contains("arda") -> {
                updatedPlayer = updatedPlayer.copy(
                    morale = (updatedPlayer.morale + 4).coerceAtMost(100),
                    followersCount = updatedPlayer.followersCount + 300
                )
                impactTag = "Moral +4 • +300 Takipçi"
                "İnşallah yakında A Milli Takım kampında da yan yana oynarız kardeşim! 🇹🇷🔥 Çalışmaya devam, yeteneğin çok özel."
            }
            else ->
                "Harikasın ${state.player.firstName}! 🔥 Mesajın için çok teşekkürler, ${state.currentClub.name} formasıyla başarılarının devamını diliyoruz! 👏⚽"
        }

        val replyMsg = ChatMessage(
            id = "IDM_REP_${System.currentTimeMillis() + 1}",
            senderName = thread.senderName,
            isFromUser = false,
            text = replyText,
            dayIndex = state.currentDayIndex,
            timeLabel = clockStr,
            impactSummary = impactTag
        )

        val updatedThreads = state.instaDmThreads.map { th ->
            if (th.id == threadId) {
                th.copy(
                    isPendingRequest = false,
                    unreadCount = 0,
                    messages = th.messages + userMsg + replyMsg
                )
            } else th
        }

        return state.copy(
            updatedAtMs = System.currentTimeMillis(),
            player = updatedPlayer.copy(
                morale = (updatedPlayer.morale + 1).coerceAtMost(100),
                followersCount = updatedPlayer.followersCount + 45
            ),
            balanceEur = updatedBalance,
            transactions = updatedTransactions,
            xPosts = updatedXPosts,
            instaDmThreads = updatedThreads
        )
    }

    fun viewAndReactToStory(state: CareerSaveState, storyId: String, react: Boolean): CareerSaveState {
        val updatedStories = state.stories.map { st ->
            if (st.id == storyId) st.copy(viewed = true, reacted = st.reacted || react) else st
        }
        val moraleBoost = if (react) 1 else 0
        return state.copy(
            updatedAtMs = System.currentTimeMillis(),
            player = state.player.copy(morale = (state.player.morale + moraleBoost).coerceAtMost(100)),
            stories = updatedStories
        )
    }

    fun shareGalleryPhotoToInsta(
        state: CareerSaveState,
        photoId: String,
        captionText: String
    ): Pair<CareerSaveState, String> {
        val photo = state.galleryPhotos.find { it.id == photoId } ?: return state to "Fotoğraf bulunamadı."
        val cleanCaption = captionText.trim().ifEmpty { photo.caption }
        val dateLabel = formatGameDate(state.currentDayIndex, state.currentSeasonYear)
        val followersGain = 320

        val generatedComments = listOf(
            InstaCommentItem(
                id = "IC_GAL_${System.currentTimeMillis()}_1",
                authorHandle = "${state.currentClub.shortName.lowercase()}_tribun",
                authorName = "${state.currentClub.shortName} Taraftar",
                isVerified = false,
                avatarHex = state.currentClub.primaryColorHex,
                text = "Harika kare! Forma çok yakışıyor 🔥👏",
                timeLabel = "1d",
                likesCount = 34
            ),
            InstaCommentItem(
                id = "IC_GAL_${System.currentTimeMillis()}_2",
                authorHandle = "433",
                authorName = "433",
                isVerified = true,
                avatarHex = "#FF3B30",
                text = "Class 📸⚽",
                timeLabel = "2d",
                likesCount = 195
            )
        )

        val newPost = SocialMediaPost(
            id = "SOC_GAL_${System.currentTimeMillis()}",
            dayIndex = state.currentDayIndex,
            dateLabel = dateLabel,
            authorName = state.player.fullName,
            authorHandle = "@${state.player.firstName.lowercase()}${state.player.kitNumber}",
            isUserPost = true,
            content = cleanCaption,
            likesCount = 340 + (state.player.followersCount / 15),
            commentsCount = generatedComments.size + 30,
            sentiment = "Destekleyici",
            topFanComments = listOf(
                "Harika kare! 🔥👏",
                "${state.currentClub.shortName} forması çok yakışıyor!"
            ),
            instaComments = generatedComments,
            isVerifiedAuthor = true,
            locationLabel = state.currentClub.stadiumName,
            imageSceneType = photo.sceneDrawableType
        )

        val updatedGallery = state.galleryPhotos.map {
            if (it.id == photoId) it.copy(sharedOnSocial = true) else it
        }
        val updatedPlayer = state.player.copy(
            followersCount = state.player.followersCount + followersGain,
            morale = (state.player.morale + 3).coerceAtMost(100),
            fanSupport = (state.player.fanSupport + 2).coerceAtMost(100)
        )

        return state.copy(
            updatedAtMs = System.currentTimeMillis(),
            player = updatedPlayer,
            socialPosts = listOf(newPost) + state.socialPosts,
            galleryPhotos = updatedGallery
        ) to "Fotoğraf Instagram akışında paylaşıldı! (+$followersGain takipçi)"
    }

    fun captureCameraSceneToGallery(
        state: CareerSaveState,
        sceneType: String,
        album: String,
        title: String,
        caption: String
    ): Pair<CareerSaveState, String> {
        val dateLabel = formatGameDate(state.currentDayIndex, state.currentSeasonYear)
        val newPhoto = GalleryPhotoItem(
            id = "GAL_CAM_${System.currentTimeMillis()}",
            dayIndex = state.currentDayIndex,
            dateLabel = dateLabel,
            album = album,
            title = title.trim().ifEmpty { "${state.currentClub.shortName} Günlüğü" },
            caption = caption.trim().ifEmpty { "${state.player.fullName} — $dateLabel" },
            sceneDrawableType = sceneType
        )
        return state.copy(
            updatedAtMs = System.currentTimeMillis(),
            galleryPhotos = listOf(newPhoto) + state.galleryPhotos
        ) to "Kare galeriye kaydedildi! İstersen Instagram'da paylaşabilirsin."
    }

    fun toggleLikeXPost(state: CareerSaveState, xPostId: String): CareerSaveState {
        val updated = state.xPosts.map { xp ->
            if (xp.id == xPostId) {
                val nowLiked = !xp.likedByUser
                xp.copy(
                    likedByUser = nowLiked,
                    likes = (xp.likes + if (nowLiked) 1 else -1).coerceAtLeast(0)
                )
            } else xp
        }
        return state.copy(updatedAtMs = System.currentTimeMillis(), xPosts = updated)
    }

    fun toggleRepostXPost(state: CareerSaveState, xPostId: String): CareerSaveState {
        val updated = state.xPosts.map { xp ->
            if (xp.id == xPostId) {
                val nowRp = !xp.repostedByUser
                xp.copy(
                    repostedByUser = nowRp,
                    reposts = (xp.reposts + if (nowRp) 1 else -1).coerceAtLeast(0)
                )
            } else xp
        }
        return state.copy(updatedAtMs = System.currentTimeMillis(), xPosts = updated)
    }

    fun publishUserXPost(state: CareerSaveState, text: String): Pair<CareerSaveState, String> {
        val clean = text.trim()
        if (clean.isEmpty()) return state to "Boş gönderi paylaşılamaz."
        val shortDate = formatShortDate(state.currentDayIndex)
        val followersGain = 180
        val newXp = XPostItem(
            id = "XP_USR_${System.currentTimeMillis()}",
            dayIndex = state.currentDayIndex,
            dateLabel = shortDate,
            authorName = state.player.fullName,
            handle = "@${state.player.firstName.lowercase()}${state.player.kitNumber}",
            category = "OYUNCU",
            isVerified = true,
            text = clean,
            likes = 245 + (state.player.followersCount / 25),
            reposts = 32
        )
        val updatedPlayer = state.player.copy(
            followersCount = state.player.followersCount + followersGain,
            fanSupport = (state.player.fanSupport + 2).coerceAtMost(100)
        )
        return state.copy(
            updatedAtMs = System.currentTimeMillis(),
            player = updatedPlayer,
            xPosts = listOf(newXp) + state.xPosts
        ) to "X gönderin paylaşıldı! (+$followersGain takipçi)"
    }

    fun completePhoneCall(
        state: CareerSaveState,
        callId: String,
        selectedOption: String
    ): Pair<CareerSaveState, String> {
        val call = state.callRecords.find { it.id == callId } ?: return state to "Arama kaydı bulunamadı."
        val updatedCalls = state.callRecords.map {
            if (it.id == callId) it.copy(completed = true) else it
        }
        val updatedState = sendDialogueMessageToNpc(state.copy(callRecords = updatedCalls), call.npcId, selectedOption)
        return updatedState to "${call.callerName} ile telefon görüşmesi tamamlandı."
    }

    fun initiateCallWithNpc(
        state: CareerSaveState,
        npcId: String
    ): Pair<CareerSaveState, PhoneCallRecord?> {
        val npc = state.npcs.find { it.id == npcId } ?: return state to null
        val shortDate = formatShortDate(state.currentDayIndex)
        val newCall = PhoneCallRecord(
            id = "CALL_OUT_${System.currentTimeMillis()}",
            npcId = npc.id,
            callerName = npc.name,
            callerRole = npc.roleTitle,
            dayIndex = state.currentDayIndex,
            dateLabel = shortDate,
            topic = "${npc.roleTitle} ile Birebir Görüşme",
            transcript = when (npc.id) {
                "NPC_MANAGER" -> "Evet ${state.player.firstName}, beni aradın. Antrenman tempon (%${state.player.energy} enerji) ve önümüzdeki maç planımız hakkında ne söylemek istersin?"
                "NPC_AGENT" -> "Selam ${state.player.firstName}! Piyasa değerin şu an ${FLFormatters.formatMarketValue(state.player.marketValueEur)}. Sözleşme veya transfer gündemi için mi aradın?"
                "NPC_FAMILY" -> "Oğlum! Sesini duymak ne güzel. Nasılsın, keyfin ve sağlığın yerinde mi?"
                else -> "Alo kardeşim! Tesislerde misin, bir durum mu var?"
            },
            options = npc.quickReplyOptions.ifEmpty {
                listOf(
                    "Her şey yolunda, durum değerlendirmesi yapmak istedim.",
                    "Önümüzdeki maça tam konsantreyim."
                )
            },
            completed = false
        )
        return state.copy(
            updatedAtMs = System.currentTimeMillis(),
            callRecords = listOf(newCall) + state.callRecords
        ) to newCall
    }

    fun updatePhoneWallpaper(state: CareerSaveState, wallpaperId: String): CareerSaveState {
        return state.copy(
            updatedAtMs = System.currentTimeMillis(),
            phoneWallpaperId = wallpaperId
        )
    }

    fun extendCurrentContract(state: CareerSaveState): Pair<CareerSaveState, String> {
        if (state.managerTrust < 55) {
            return state to "Teknik direktör ve yönetim, sözleşme uzatmak için önce sahada daha yüksek güven (%55+) bekliyor."
        }
        val oldContract = state.contract
        val tierProfile = LeagueDifficultyCalculator.getProfileForTier(state.currentClub.tier)
        val wageIncrease = (oldContract.weeklyWageEur * 0.15).toLong().coerceAtLeast(15L)
        val newWage = (oldContract.weeklyWageEur + wageIncrease).coerceAtMost(tierProfile.weeklyWageMaxEur)
        val updatedContract = oldContract.copy(
            weeklyWageEur = newWage,
            matchAppearanceBonusEur = (oldContract.matchAppearanceBonusEur * 1.12).toLong(),
            goalBonusEur = (oldContract.goalBonusEur * 1.15).toLong(),
            endSeasonYear = (oldContract.endSeasonYear + 1).coerceAtMost(2032)
        )
        return state.copy(
            updatedAtMs = System.currentTimeMillis(),
            contract = updatedContract
        ) to "${state.currentClub.name} ile sözleşmen ${updatedContract.endSeasonYear} yılına kadar uzatıldı! Yeni haftalık maaş: ${FLFormatters.formatWage(newWage)}."
    }
}
