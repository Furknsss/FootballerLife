package com.example.ai

import com.example.engine.CareerEngine
import com.example.engine.WorldDataCatalog
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeoutException

class GeminiAiModernizationTest {

    @Before
    fun setUp() {
        GeminiClient.clearFailoverStatus()
        GeminiClient.testCallInterceptor = null
    }

    @After
    fun tearDown() {
        GeminiClient.clearFailoverStatus()
        GeminiClient.testCallInterceptor = null
    }

    @Test
    fun `test1_geminiModelOptionsContainAll2026ModelsAndLegacyMigration`() {
        // 1. Verify all 5 cutting-edge models exist with expected ids
        val modelIds = GeminiModelOption.entries.map { it.modelId }
        assertTrue(modelIds.contains("gemini-3.8-flash"))
        assertTrue(modelIds.contains("gemini-3.8-pro"))
        assertTrue(modelIds.contains("gemini-4-argon"))
        assertTrue(modelIds.contains("gemini-4-hydrogen"))
        assertTrue(modelIds.contains("gemini-3.8-flash-lite"))

        // 2. Exact lookup
        assertEquals(GeminiModelOption.GEMINI_3_8_FLASH, GeminiModelOption.fromModelId("gemini-3.8-flash"))
        assertEquals(GeminiModelOption.GEMINI_3_8_PRO, GeminiModelOption.fromModelId("gemini-3.8-pro"))
        assertEquals(GeminiModelOption.GEMINI_4_ARGON, GeminiModelOption.fromModelId("gemini-4-argon"))
        assertEquals(GeminiModelOption.GEMINI_4_HYDROGEN, GeminiModelOption.fromModelId("gemini-4-hydrogen"))
        assertEquals(GeminiModelOption.GEMINI_3_8_FLASH_LITE, GeminiModelOption.fromModelId("gemini-3.8-flash-lite"))

        // 3. Graceful legacy migration
        assertEquals(GeminiModelOption.GEMINI_3_8_FLASH, GeminiModelOption.fromModelId("gemini-2.5-flash"))
        assertEquals(GeminiModelOption.GEMINI_3_8_FLASH, GeminiModelOption.fromModelId("gemini-2.0-flash"))
        assertEquals(GeminiModelOption.GEMINI_3_8_PRO, GeminiModelOption.fromModelId("gemini-2.5-pro"))
        assertEquals(GeminiModelOption.GEMINI_3_8_FLASH_LITE, GeminiModelOption.fromModelId("gemini-2.5-flash-lite"))

        // 4. Unknown defaults to Flash
        assertEquals(GeminiModelOption.GEMINI_3_8_FLASH, GeminiModelOption.fromModelId("unknown-model-xyz"))
    }

    @Test
    fun `test2_buildFailoverChainCreatesExpectedHierarchy`() {
        // Frontier model chain: Argon -> Flash -> Flash-Lite
        val argonChain = GeminiClient.buildFailoverChain("gemini-4-argon", autoFailoverEnabled = true)
        assertEquals(listOf("gemini-4-argon", "gemini-3.8-flash", "gemini-3.8-flash-lite"), argonChain)

        // Pro model chain: Pro -> Flash -> Flash-Lite
        val proChain = GeminiClient.buildFailoverChain("gemini-3.8-pro", autoFailoverEnabled = true)
        assertEquals(listOf("gemini-3.8-pro", "gemini-3.8-flash", "gemini-3.8-flash-lite"), proChain)

        // Flash chain: Flash -> Flash-Lite
        val flashChain = GeminiClient.buildFailoverChain("gemini-3.8-flash", autoFailoverEnabled = true)
        assertEquals(listOf("gemini-3.8-flash", "gemini-3.8-flash-lite"), flashChain)

        // Flash-Lite chain: Flash-Lite only
        val liteChain = GeminiClient.buildFailoverChain("gemini-3.8-flash-lite", autoFailoverEnabled = true)
        assertEquals(listOf("gemini-3.8-flash-lite"), liteChain)

        // When autoFailover is disabled, only primary model is returned
        val singleChain = GeminiClient.buildFailoverChain("gemini-4-argon", autoFailoverEnabled = false)
        assertEquals(listOf("gemini-4-argon"), singleChain)
    }

    @Test
    fun `test3_smartFailoverOnHttp429QuotaExhaustion`() = runBlocking {
        val dummyKey = "AIzaSyDUMMYKEY1234567890ABCDEF"

        // Mock interceptor: gemini-4-argon fails with 429 quota exhaustion, gemini-3.8-flash succeeds
        GeminiClient.testCallInterceptor = { modelId, _ ->
            if (modelId == "gemini-4-argon") {
                Result.failure(IllegalStateException("HTTP 429: API kota sınırına ulaşıldı (429)."))
            } else {
                Result.success(
                    """
                    {
                        "replyText": "Hocam taktik tahtasında her şey hazır, maça hazırız!",
                        "emotionTag": "Kararlı",
                        "affinityDelta": 4,
                        "trustDelta": 3
                    }
                    """.trimIndent()
                )
            }
        }

        val result = GeminiClient.generateCharacterDialogue(
            apiKeyInput = dummyKey,
            preferredModelId = "gemini-4-argon",
            systemInstruction = "Test instruction",
            conversationHistory = emptyList(),
            userMessageWithDirective = "Hocam durum nedir?",
            autoFailoverEnabled = true
        )

        assertTrue(result.isSuccess)
        val structured = result.getOrNull()
        assertNotNull(structured)
        assertEquals("Hocam taktik tahtasında her şey hazır, maça hazırız!", structured!!.replyText)
        assertEquals("gemini-3.8-flash", structured.modelUsed)
        assertEquals("gemini-4-argon", structured.modelRequested)
        assertTrue(structured.fallbackOccurred)
        assertNotNull(structured.fallbackNoteTr)
        assertTrue(structured.fallbackNoteTr!!.contains("Gemini 3.8 Flash"))

        // Status flow check
        val failoverStatus = GeminiClient.getLastFailoverStatus()
        assertNotNull(failoverStatus)
        assertTrue(failoverStatus!!.contains("gemini-4-argon"))
        assertTrue(failoverStatus.contains("gemini-3.8-flash"))
    }

    @Test
    fun `test4_smartFailoverOnHttp503AndTimeout`() = runBlocking {
        val dummyKey = "AIzaSyDUMMYKEY1234567890ABCDEF"

        // Mock: Pro hits 503, Flash hits timeout (>5s), Flash-Lite succeeds
        GeminiClient.testCallInterceptor = { modelId, _ ->
            when (modelId) {
                "gemini-3.8-pro" -> Result.failure(IllegalStateException("HTTP 503: Sunucu aşırı yoğun."))
                "gemini-3.8-flash" -> Result.failure(TimeoutException("Zaman aşımı (>5s)"))
                else -> Result.success(
                    """
                    {
                        "replyText": "Yedek motor devrede, her şey yolunda!",
                        "emotionTag": "Sakin"
                    }
                    """.trimIndent()
                )
            }
        }

        val result = GeminiClient.generateCharacterDialogue(
            apiKeyInput = dummyKey,
            preferredModelId = "gemini-3.8-pro",
            systemInstruction = "Test",
            conversationHistory = emptyList(),
            userMessageWithDirective = "Selam",
            autoFailoverEnabled = true
        )

        assertTrue(result.isSuccess)
        val structured = result.getOrNull()
        assertNotNull(structured)
        assertEquals("gemini-3.8-flash-lite", structured!!.modelUsed)
        assertEquals("gemini-3.8-pro", structured.modelRequested)
        assertTrue(structured.fallbackOccurred)
    }

    @Test
    fun `test5_dialogueEngineSeamlesslyFallsBackToOfflineWhenAllModelsFail`() = runBlocking {
        val dummyKey = "AIzaSyDUMMYKEY1234567890ABCDEF"

        // All models fail
        GeminiClient.testCallInterceptor = { _, _ ->
            Result.failure(IllegalStateException("HTTP 503: Tüm sunucular kapalı"))
        }

        val ardaPreset = WorldDataCatalog.realPlayerPresets.first { it.id == "REAL_ARDA_GULER" }
        val baseState = CareerEngine.createNewCareerFromRealPreset(slotId = 1, preset = ardaPreset)
        val coachNpc = baseState.npcs.first { it.id == "NPC_COACH" }

        val (stateWithUser, _) = DialogueEngine.appendOptimisticUserNpcMessage(
            state = baseState,
            npcId = coachNpc.id,
            userText = "Hocam ilk 11'de başlamak istiyorum."
        )

        val result = DialogueEngine.generateAndApplyNpcDialogue(
            stateBeforeUserMsg = baseState,
            currentStateWithUserMsg = stateWithUser,
            npcId = coachNpc.id,
            userText = "Hocam ilk 11'de başlamak istiyorum.",
            apiKeyInput = dummyKey,
            modelId = "gemini-4-argon",
            aiEnabled = true,
            autoFailoverEnabled = true
        )

        // Offline scripted dialogue resolved smoothly without crashing
        assertFalse(result.usedGeminiAi)
        assertNotNull(result.replyMessage.text)
        assertTrue(result.replyMessage.text.isNotBlank())
        assertEquals("offline-scripted", result.structuredResponse.modelUsed)
    }

    @Test
    fun `test6_testConnectionTestsBothPrimaryAndFallback`() = runBlocking {
        val dummyKey = "AIzaSyDUMMYKEY1234567890ABCDEF"

        // Mock test connection
        GeminiClient.testCallInterceptor = { modelId, _ ->
            Result.success("""{"replyText": "Bağlantı başarılı: $modelId", "emotionTag": "Hazır"}""")
        }

        val res = GeminiClient.testConnection(
            apiKeyInput = dummyKey,
            modelId = "gemini-3.8-pro"
        )

        assertTrue(res.success)
        assertEquals("gemini-3.8-pro", res.modelTested)
        assertEquals("gemini-3.8-flash-lite", res.fallbackModelTested)
        assertTrue(res.fallbackSuccess)
        assertTrue(res.detailMessageTr.contains("gemini-3.8-pro"))
        assertTrue(res.detailMessageTr.contains("gemini-3.8-flash-lite"))
    }
}
