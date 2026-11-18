package com.example.engine.match

import com.example.audio.GameSoundEffect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SetPieceAndDramaTest {

    @Test
    fun test1_freeKickWallFiveDefendersConfiguration() {
        val defenders = (0..4).map { idx ->
            WallDefender(
                id = "DEF_$idx",
                index = idx,
                name = "Savunmacı $idx",
                jerseyNumber = idx + 2,
                heightCm = 186,
                basePitchX = 90.0f,
                basePitchY = 46.5f + idx * 1.75f,
                jumpHeightCmMax = 45f
            )
        }
        assertEquals(5, defenders.size)

        val wallConfig = FreeKickWallConfiguration(
            wallCount = 5,
            defenders = defenders,
            sprayLineDistanceM = 9.15f,
            sprayLineStartX = 90.0f,
            sprayLineStartY = 45.0f,
            sprayLineEndX = 90.0f,
            sprayLineEndY = 55.0f,
            shooterJerseyNumber = 7,
            ballPosition = PitchPoint(78f, 50f, 0f)
        )

        assertEquals(5, wallConfig.wallCount)
        assertEquals(7, wallConfig.shooterJerseyNumber)
        assertEquals(9.15f, wallConfig.sprayLineDistanceM, 0.01f)
    }

    @Test
    fun test2_freeKickWallCollisionDetection() {
        val defenders = (0..4).map { idx ->
            WallDefender(
                id = "DEF_$idx",
                index = idx,
                name = "Savunmacı $idx",
                jerseyNumber = idx + 2,
                basePitchX = 90.0f,
                basePitchY = 46.0f + idx * 2.0f
            )
        }
        val wallConfig = FreeKickWallConfiguration(
            defenders = defenders,
            sprayLineDistanceM = 9.15f,
            sprayLineStartX = 90.0f,
            sprayLineStartY = 46.0f,
            sprayLineEndX = 90.0f,
            sprayLineEndY = 54.0f,
            ballPosition = PitchPoint(78f, 50f, 0f)
        )

        // 1. Alçak şut (baraj boyundan alçak, z = 1.2m) -> Baraja çarpmalı
        val lowTrajectory = listOf(
            PitchPoint(82f, 50f, 0.4f),
            PitchPoint(90f, 50f, 1.2f),
            PitchPoint(98f, 50f, 1.5f)
        )
        val (hitLow, msgLow) = SetPiecePhysicsSimulator.checkWallCollision(lowTrajectory, wallConfig, wallJumpScale = 1.0f)
        assertTrue(hitLow)
        assertTrue(msgLow.contains("BARAJA ÇARPTI"))

        // 2. Yüksek falsolu şut (barajı aşan, z = 2.5m) -> Barajı aşmalı
        val highCurveTrajectory = listOf(
            PitchPoint(82f, 50f, 1.2f),
            PitchPoint(90f, 50f, 2.6f),
            PitchPoint(98f, 54f, 2.2f)
        )
        val (hitHigh, msgHigh) = SetPiecePhysicsSimulator.checkWallCollision(highCurveTrajectory, wallConfig, wallJumpScale = 1.0f)
        assertFalse(hitHigh)
        assertTrue(msgHigh.contains("BARAJI AŞTI"))
    }

    @Test
    fun test3_topCorner90TargetRingDetection() {
        val rings = listOf(
            SetPieceTargetRing("90_LEFT", "SOL 90", 98f, 44.5f, 2.2f, 0.85f, 150, true),
            SetPieceTargetRing("90_RIGHT", "SAĞ 90", 98f, 55.5f, 2.2f, 0.85f, 150, true)
        )

        val hitBall = PitchPoint(98f, 55.4f, 2.22f)
        val targetHit = SetPiecePhysicsSimulator.checkTargetRingHit(hitBall, rings)
        assertNotNull(targetHit)
        assertEquals("90_RIGHT", targetHit?.id)
        assertTrue(targetHit?.isTopCorner90 == true)
        assertEquals(150, targetHit?.scoreBonus)
    }

    @Test
    fun test4_penaltyShootoutOutcomesAndCrossbarClang() {
        // 1. Direğe çarpma senaryosu (crossbar hit)
        val (goalPost, crossbarHit, commPost) = SetPiecePhysicsSimulator.resolvePenaltyShootout(
            shotTargetY = 50.0f,
            shotTargetZ = 2.40f, // Üst direk yüksekliği
            keeperDive = PenaltyKeeperDive.BOTTOM_LEFT,
            shotPowerPct = 90
        )
        assertFalse(goalPost)
        assertTrue(crossbarHit)
        assertTrue(commPost.contains("DİREKTEN DÖNDÜ"))

        // 2. Kaleciyi ters köşeye yatıran gol senaryosu
        val (goalScored, _, commGoal) = SetPiecePhysicsSimulator.resolvePenaltyShootout(
            shotTargetY = 45.0f, // Sol köşe
            shotTargetZ = 1.0f,
            keeperDive = PenaltyKeeperDive.BOTTOM_RIGHT, // Kaleci sağa atladı
            shotPowerPct = 85
        )
        assertTrue(goalScored)
        assertTrue(commGoal.contains("GOL"))
    }

    @Test
    fun test5_cornerBoxScrambleResolution() {
        val (result, comm) = SetPiecePhysicsSimulator.resolveCornerScramble(
            boxDuelists = emptyList(),
            userAerialPower = 95
        )
        assertNotNull(comm)
        assertTrue(comm.isNotEmpty())
    }

    @Test
    fun test6_refereeDramaAndMedicalStretcherModels() {
        val yellowCardEvent = RefereeDramaEvent(
            type = RefereeDramaType.YELLOW_CARD,
            refereeName = "Halil Umut Meler",
            targetPlayerName = "Merih Demiral",
            targetPlayerNumber = 3,
            foulDescriptionTr = "Taktik faul ve formadan çekme",
            minute = 42
        )
        assertEquals(RefereeDramaType.YELLOW_CARD, yellowCardEvent.type)
        assertEquals(3, yellowCardEvent.targetPlayerNumber)

        val injuryEvent = MedicalInjuryEvent(
            injuredPlayerName = "Arda Güler",
            injuredJerseyNumber = 10,
            injuredClubName = "Real Madrid",
            bodyPartTr = "Sağ Ayak Bileği",
            requiresStretcher = true,
            currentPhase = MedicalTreatmentPhase.STRETCHER_BOARDING
        )
        assertTrue(injuryEvent.requiresStretcher)
        assertEquals(MedicalTreatmentPhase.STRETCHER_BOARDING, injuryEvent.currentPhase)
    }

    @Test
    fun test7_soundAndHapticsNewAtmosphereEnums() {
        // GameSoundEffect enum değerleri tam ve eksiksiz olmalı
        val requiredSounds = listOf(
            GameSoundEffect.REFEREE_WHISTLE_FOUL,
            GameSoundEffect.REFEREE_WHISTLE_CARD,
            GameSoundEffect.CROSSBAR_METALLIC_HIT,
            GameSoundEffect.CROWD_ROAR_EXPLOSION,
            GameSoundEffect.STADIUM_CHANT_AMBIENCE,
            GameSoundEffect.DEFENSE_WALL_JUMP,
            GameSoundEffect.PENALTY_HEARTBEAT,
            GameSoundEffect.CORNER_AERIAL_DUEL,
            GameSoundEffect.INJURY_STRETCHER_HORN
        )
        assertEquals(9, requiredSounds.size)
    }
}
