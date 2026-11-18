package com.example.engine.social

import com.example.engine.CareerEngine
import com.example.engine.WorldDataCatalog
import com.example.model.CareerSaveState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RomanceAndDatingEngineTest {

    private fun createBaseState(followers: Int = 1_500, rating: Int = 60): CareerSaveState {
        val state = CareerEngine.createNewCareerFromRealPreset(
            slotId = 1,
            preset = WorldDataCatalog.realPlayerPresets.first()
        )
        return state.copy(
            player = state.player.copy(
                followersCount = followers
            )
        )
    }

    @Test
    fun test1_fameTierResolution() {
        val amateurState = createBaseState(followers = 500, rating = 50)
        val amateurTier = RomanceAndDatingEngine.resolvePlayerFameTier(amateurState.player)
        assertEquals(PlayerFameTier.SEMI_PRO, amateurTier) // default preset rating is around 68

        val superstarState = createBaseState(followers = 5_000_000, rating = 90)
        val superstarTier = RomanceAndDatingEngine.resolvePlayerFameTier(superstarState.player)
        assertEquals(PlayerFameTier.GLOBAL_SUPERSTAR, superstarTier)
    }

    @Test
    fun test2_candidatesPoolHasDiverseArchetypes() {
        val candidates = RomanceAndDatingEngine.INITIAL_CANDIDATES
        assertTrue(candidates.size >= 5)

        val archetypes = candidates.map { it.archetype }.toSet()
        assertTrue(archetypes.contains(PartnerArchetype.FITNESS_CREATOR))
        assertTrue(archetypes.contains(PartnerArchetype.SPORTS_PRESENTER))
        assertTrue(archetypes.contains(PartnerArchetype.MUSICIAN))
        assertTrue(archetypes.contains(PartnerArchetype.FASHION_INFLUENCER))
        assertTrue(archetypes.contains(PartnerArchetype.ACTRESS))
        assertTrue(archetypes.contains(PartnerArchetype.SUPERMODEL))
    }

    @Test
    fun test3_datingActionsCoverAllCategories() {
        val actions = RomanceAndDatingEngine.DATING_ACTIONS
        val categories = actions.map { action -> action.category }.toSet()

        assertTrue(categories.contains(DatingInteractionCategory.DINNER))
        assertTrue(categories.contains(DatingInteractionCategory.GIFT))
        assertTrue(categories.contains(DatingInteractionCategory.VACATION))
        assertTrue(categories.contains(DatingInteractionCategory.MATCH_INVITE))
        assertTrue(categories.contains(DatingInteractionCategory.NIGHT_OUT))
    }

    @Test
    fun test4_vipMatchInviteActivatesTribunBonus() {
        val state = createBaseState()
        val partner = RomanceAndDatingEngine.INITIAL_CANDIDATES.first { it.id == "partner_derin_karahan" }
        val matchInviteAction = RomanceAndDatingEngine.DATING_ACTIONS.first { it.id == "act_match_vip" }

        val outcome = RomanceAndDatingEngine.executeInteraction(state, partner, matchInviteAction)

        assertTrue(outcome.success)
        assertTrue(outcome.matchBonusActivated)
        assertTrue(outcome.playerMoraleBonus > 0)
        assertTrue(outcome.resultStoryText.contains("VIP locada"))
    }

    @Test
    fun test5_relationshipStageProgressionAndEngagement() {
        val state = createBaseState()
        val partnerMeeting = RomanceAndDatingEngine.INITIAL_CANDIDATES.first().copy(
            stage = RelationshipStage.MEETING,
            affection = 35
        )

        // Dinner interaction grants affection and advances stage to FLIRTING
        val dinnerAction = RomanceAndDatingEngine.DATING_ACTIONS.first { it.id == "act_dinner_bosphorus" }
        val outcome1 = RomanceAndDatingEngine.executeInteraction(state, partnerMeeting, dinnerAction)
        assertEquals(RelationshipStage.FLIRTING, outcome1.currentStage)
        assertTrue(outcome1.stageAdvanced)

        // Proposal with engagement ring in IN_RELATIONSHIP
        val partnerDating = partnerMeeting.copy(
            stage = RelationshipStage.IN_RELATIONSHIP,
            affection = 90
        )
        val ringAction = RomanceAndDatingEngine.DATING_ACTIONS.first { it.id == "act_gift_engagement_ring" }
        val outcomeRing = RomanceAndDatingEngine.executeInteraction(state, partnerDating, ringAction)
        assertEquals(RelationshipStage.ENGAGED, outcomeRing.currentStage)
        assertTrue(outcomeRing.stageAdvanced)
        assertTrue(outcomeRing.resultStoryText.contains("EVET!"))
    }

    @Test
    fun test6_dramaResolutionAction() {
        val partnerInDrama = RomanceAndDatingEngine.INITIAL_CANDIDATES.first().copy(
            dramaLevel = 60,
            affection = 40
        )

        val (updatedPartner, message) = RomanceAndDatingEngine.resolveDramaAction(partnerInDrama, "FLOWERS")
        assertTrue(updatedPartner.dramaLevel < partnerInDrama.dramaLevel)
        assertTrue(updatedPartner.affection > partnerInDrama.affection)
        assertTrue(message.contains("buzları eritti"))
    }
}
