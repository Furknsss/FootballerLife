package com.example

import com.example.engine.CareerEngine
import com.example.engine.MatchPhase
import com.example.engine.MatchSimulationEngine
import com.example.engine.WorldDataCatalog
import com.example.model.PlayerAppearance
import com.example.model.Position
import com.example.model.PreferredFoot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `test1_and_test2_customPlayerAndContractCreation`() {
        val state = CareerEngine.createNewCareerFromCustomPlayer(
            slotId = 1,
            firstName = "Eren",
            lastName = "Demir",
            birthYear = 2006,
            country = "Türkiye",
            city = "Eskişehir",
            nationality = "Türkiye",
            kitName = "DEMİR",
            kitNumber = 10,
            primaryPosition = Position.AM,
            secondaryPositions = listOf(Position.CM, Position.WG),
            preferredFoot = PreferredFoot.LEFT,
            heightCm = 178,
            weightKg = 72,
            appearance = PlayerAppearance(),
            backgroundStory = "Altyapı Akademisi Çıkışlı",
            selectedLeagueId = "TR_BAL",
            selectedClubId = "BAL_ESKISEHIR"
        )

        assertEquals("Eren Demir", state.player.fullName)
        assertEquals(Position.AM, state.player.primaryPosition)
        assertEquals("BAL_ESKISEHIR", state.contract.clubId)
        assertEquals("Eskişehir Anadolu SK", state.currentClub.name)
        assertTrue(state.contract.weeklyWageEur > 0L)
        assertTrue(state.balanceEur > 0L)
        assertEquals(8, state.clubs.size)
        assertEquals(8, state.standings.size)
    }

    @Test
    fun `test3_trainingUpdatesAttributesEnergyCalendarAndEveningFamilyMessage`() {
        val initial = CareerEngine.createNewCareerFromCustomPlayer(
            slotId = 1,
            firstName = "Kerem",
            lastName = "Yılmaz",
            birthYear = 2005,
            country = "Türkiye",
            city = "İzmir",
            nationality = "Türkiye",
            kitName = "YILMAZ",
            kitNumber = 8,
            primaryPosition = Position.CM,
            secondaryPositions = emptyList(),
            preferredFoot = PreferredFoot.RIGHT,
            heightCm = 180,
            weightKg = 74,
            appearance = PlayerAppearance(),
            backgroundStory = "Altyapı Akademisi Çıkışlı",
            selectedLeagueId = "TR_BAL",
            selectedClubId = "BAL_IZMIR"
        )

        val passingDrill = WorldDataCatalog.trainingDrills.first { it.id == "DRILL_PASSING" }
        val (afterTrain, msg) = CareerEngine.performTrainingSession(initial, passingDrill)

        assertTrue(msg.contains("başarıyla tamamlandı"))
        assertTrue(afterTrain.trainedToday)
        assertTrue(afterTrain.player.attributes.passing > initial.player.attributes.passing)
        assertTrue(afterTrain.player.energy < initial.player.energy)
        assertTrue(afterTrain.managerTrust > initial.managerTrust)

        // Calendar event for Day 1 training marked completed
        val day1Cal = afterTrain.calendarEvents.first { it.dayIndex == 1 }
        assertTrue(day1Cal.completed)

        // Family evening message sent on Day 1
        val familyNpc = afterTrain.npcs.first { it.id == "NPC_FAMILY" }
        assertTrue(familyNpc.messages.any { it.text.contains("İlk antrenmanın nasıl geçti") })
    }

    @Test
    fun `test4_and_test5_hybrid2dMatchPlayersAndBallMoveAndCriticalDecisionResolves`() {
        val career = CareerEngine.createNewCareerFromCustomPlayer(
            slotId = 1,
            firstName = "Barış",
            lastName = "Kaya",
            birthYear = 2004,
            country = "Türkiye",
            city = "Bursa",
            nationality = "Türkiye",
            kitName = "KAYA",
            kitNumber = 9,
            primaryPosition = Position.ST,
            secondaryPositions = emptyList(),
            preferredFoot = PreferredFoot.RIGHT,
            heightCm = 183,
            weightKg = 78,
            appearance = PlayerAppearance(),
            backgroundStory = "Altyapı Akademisi Çıkışlı",
            selectedLeagueId = "TR_3_LIG",
            selectedClubId = "T3_BURSA"
        )

        val engine = MatchSimulationEngine(baseSeed = 42L)
        val fixture = career.nextUnplayedUserFixture!!
        val home = career.clubs.first { it.id == fixture.homeClubId }
        val away = career.clubs.first { it.id == fixture.awayClubId }

        var match = engine.createInitialMatchState(
            fixture = fixture,
            homeClub = home,
            awayClub = away,
            userPlayer = career.player,
            userClubId = career.contract.clubId,
            startingStatus = com.example.model.StartingStatus.STARTER
        )
        assertEquals(22, match.players.size)

        match = engine.startKickOff(match)
        val initialPositions = match.players.map { it.x to it.y }

        // Step until a critical decision triggers (guaranteed at minute 14)
        var ticks = 0
        while (match.phase != MatchPhase.CRITICAL_DECISION && ticks < 200) {
            match = engine.stepAnimationFrame(match, career.player)
            ticks++
        }

        val movedPositions = match.players.map { it.x to it.y }
        assertNotEquals(initialPositions, movedPositions)
        assertEquals(MatchPhase.CRITICAL_DECISION, match.phase)
        assertNotNull(match.activeDecision)
        assertTrue(match.activeDecision!!.options.isNotEmpty())

        // Choose the pass option ("THROUGH_PASS")
        val passOption = match.activeDecision!!.options.first { it.actionType == "THROUGH_PASS" }
        val afterDecision = engine.resolveCriticalDecision(match, passOption, career.player)
        assertEquals(1, afterDecision.userDecisionsTotal)
        assertTrue(afterDecision.userPassesAttempted >= match.userPassesAttempted)
    }

    @Test
    fun `test6_test7_test11_postMatchUpdatesStandingsStatsNewsAndPreventsDoubleReward`() {
        var career = CareerEngine.createNewCareerFromCustomPlayer(
            slotId = 1,
            firstName = "Mert",
            lastName = "Arslan",
            birthYear = 2005,
            country = "Türkiye",
            city = "Kocaeli",
            nationality = "Türkiye",
            kitName = "ARSLAN",
            kitNumber = 7,
            primaryPosition = Position.WG,
            secondaryPositions = emptyList(),
            preferredFoot = PreferredFoot.RIGHT,
            heightCm = 177,
            weightKg = 71,
            appearance = PlayerAppearance(),
            backgroundStory = "Altyapı Akademisi Çıkışlı",
            selectedLeagueId = "TR_1_LIG",
            selectedClubId = "T1_KOCAELI"
        )

        // Advance from Day 1 to Day 2 (Match Day)
        career = CareerEngine.advanceToNextDay(career).first
        assertEquals(2, career.currentDayIndex)
        val matchFixture = career.todayUserFixture
        assertNotNull(matchFixture)

        val engine = MatchSimulationEngine(baseSeed = 99L)
        val home = career.clubs.first { it.id == matchFixture!!.homeClubId }
        val away = career.clubs.first { it.id == matchFixture!!.awayClubId }
        var live = engine.createInitialMatchState(
            fixture = matchFixture!!,
            homeClub = home,
            awayClub = away,
            userPlayer = career.player,
            userClubId = career.contract.clubId,
            startingStatus = com.example.model.StartingStatus.STARTER
        )
        live = live.copy(
            minute = 90,
            homeScore = 2,
            awayScore = 1,
            userGoals = 1,
            userAssists = 1,
            userLiveRating = 8.4
        )
        val report = engine.buildFinalMatchReport(live, career.player)

        val afterMatch = CareerEngine.applyCompletedMatch(career, report)
        assertEquals(1, afterMatch.player.currentSeasonStats.appearances)
        assertEquals(1, afterMatch.player.currentSeasonStats.goals)
        assertEquals(1, afterMatch.player.currentSeasonStats.assists)
        assertTrue(afterMatch.balanceEur > career.balanceEur)
        assertTrue(afterMatch.standings.all { it.played == 1 })
        assertTrue(afterMatch.newsEvents.first().headline.contains("Mert Arslan"))

        // Test 11: Attempting to apply the same match again must be a no-op (no double rewards!)
        val duplicateAttempt = CareerEngine.applyCompletedMatch(afterMatch, report)
        assertEquals(afterMatch.balanceEur, duplicateAttempt.balanceEur)
        assertEquals(1, duplicateAttempt.player.currentSeasonStats.goals)
        assertEquals(1, duplicateAttempt.standings.first().played)
    }

    @Test
    fun `test8_test12_test14_socialLifeNpcDialogueAndTransferEconomyConsistency`() {
        var career = CareerEngine.createNewCareerFromRealPreset(
            slotId = 2,
            preset = WorldDataCatalog.realPlayerPresets.first { it.id == "REAL_MASON_GREENWOOD" }
        )

        val initialBalance = career.balanceEur
        val dinnerAction = WorldDataCatalog.lifestyleVenueActions.first { it.id == "V_RESTAURANT_TEAM" }
        val (afterSocial, _) = CareerEngine.executeSocialVenueAction(career, dinnerAction)
        assertEquals(initialBalance - dinnerAction.costEur, afterSocial.balanceEur)
        assertTrue(afterSocial.player.morale > career.player.morale)

        // Test 12: Offline contextual NPC dialogue with custom user sentence
        val afterChat = CareerEngine.sendDialogueMessageToNpc(
            afterSocial,
            "NPC_MANAGER",
            "Hocam antrenmanda çok sıkı çalışıyorum ve ilk 11 için hazırım."
        )
        val mgr = afterChat.npcs.first { it.id == "NPC_MANAGER" }
        assertTrue(mgr.trust >= afterSocial.npcs.first { it.id == "NPC_MANAGER" }.trust)

        // Test 14: Request transfer offer, negotiate, and sign
        val (withOffer, _) = CareerEngine.requestTransferSearchFromAgent(afterChat)
        assertTrue(withOffer.transferOffers.isNotEmpty())
        val firstOffer = withOffer.transferOffers.first()
        val (negotiatedState, _) = CareerEngine.negotiateTransferOffer(withOffer, firstOffer.id)
        val process = negotiatedState.livingWorld.transfers.first { it.offerId == firstOffer.id }
        assertTrue(process.rounds.isNotEmpty())
        // Signature now follows agreement, medical and league registration validation.
        val agreed = com.example.engine.TransferEngine.agree(negotiatedState, firstOffer.id).first
        val cleared = com.example.engine.TransferEngine.medical(agreed, firstOffer.id).first
        val (transferredState, _) = com.example.engine.TransferEngine.register(cleared, firstOffer.id)
        assertEquals(firstOffer.clubId, transferredState.contract.clubId)
        assertEquals(process.terms.weeklyWageEur, transferredState.contract.weeklyWageEur)
        assertTrue(transferredState.transferHistory.size >= 2)
        assertTrue(transferredState.marketValueHistory.size >= 2)
    }

    @Test
    fun `test15_footballLife20_formattersNavigationCalendarMigrationAndPhoneOs`() {
        // 1. Clean formatting without floating-point noise
        assertEquals("6,6", com.example.ui.components.FLFormatters.formatRating(6.619999999999))
        assertEquals("%81", com.example.ui.components.FLFormatters.formatPercent(81))
        assertEquals("€1,25 Mn", com.example.ui.components.FLFormatters.formatMarketValue(1_250_000L))
        assertEquals("€64.615 / hafta", com.example.ui.components.FLFormatters.formatWage(64_615L))

        // 2. Mandatory 5 bottom navigation tabs: HAYATIM, TAKVIM, LIG, KARIYER, TELEFON
        val tabNames = com.example.viewmodel.HubTab.entries.map { it.name }
        assertEquals(listOf("HAYATIM", "TAKVIM", "LIG", "KARIYER", "TELEFON"), tabNames)

        // 3. Legacy saveVersion=2 migration to saveVersion=3 preserves player & populates v3 collections
        val ardaPreset = WorldDataCatalog.realPlayerPresets.first { it.id == "REAL_ARDA_GULER" }
        val baseCareer = CareerEngine.createNewCareerFromRealPreset(slotId = 1, preset = ardaPreset)
        val simulatedLegacySave = baseCareer.copy(
            saveVersion = 2,
            marketValueHistory = emptyList(),
            transferHistory = emptyList(),
            achievements = emptyList(),
            stories = emptyList(),
            xPosts = emptyList(),
            galleryPhotos = emptyList(),
            callRecords = emptyList()
        )
        val migrated = CareerEngine.ensureVersion3EnrichedState(simulatedLegacySave)
        assertEquals(3, migrated.saveVersion)
        assertEquals("Arda Güler", migrated.player.fullName)
        assertTrue(migrated.marketValueHistory.size >= 2)
        assertTrue(migrated.transferHistory.isNotEmpty())
        assertTrue(migrated.achievements.isNotEmpty())
        assertTrue(migrated.stories.isNotEmpty())
        assertTrue(migrated.xPosts.isNotEmpty())
        assertTrue(migrated.galleryPhotos.isNotEmpty())
        assertTrue(migrated.callRecords.isNotEmpty())

        // 4. 7-column Monday-first monthly calendar grid
        val monthGrid = CareerEngine.getMonthCalendarGrid(migrated, monthOffset = 0)
        assertTrue(monthGrid.monthTitleTr.startsWith("Ağustos"))
        assertEquals(42, monthGrid.cells.size)
        assertTrue(monthGrid.cells.any { it.isCurrentDay })

        // 5. PhoneOS interactive apps (Camera -> Gallery -> InstaPitch, PitchX, Calls)
        val (afterCamera, _) = CareerEngine.captureCameraSceneToGallery(
            migrated,
            sceneType = "STADIUM",
            album = "Maçlar",
            title = "Maç Günü Hazırlığı",
            caption = "Stadyum atmosferi harika!"
        )
        assertEquals(migrated.galleryPhotos.size + 1, afterCamera.galleryPhotos.size)
        val newestPhoto = afterCamera.galleryPhotos.first()

        val (afterShare, _) = CareerEngine.shareGalleryPhotoToInsta(
            afterCamera,
            photoId = newestPhoto.id,
            captionText = "Taraftarımızla buluşma zamanı!"
        )
        assertTrue(afterShare.player.followersCount > afterCamera.player.followersCount)
        assertTrue(afterShare.socialPosts.first().content.contains("Taraftarımızla"))

        val (afterXPost, _) = CareerEngine.publishUserXPost(afterShare, "Yeni haftaya hazırız! ⚽🔥")
        assertTrue(afterXPost.xPosts.first().text.contains("Yeni haftaya hazırız"))
    }
}

