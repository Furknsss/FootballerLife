package com.example.ai

import com.example.engine.CareerEngine
import com.example.engine.WorldDataCatalog
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
    fun `test1_geminiModelOptionsContainActiveWorkingModelsAndGracefulMigration`() {
        // 1. Verify active working models from the user's API environment
        val modelIds = GeminiModelOption.entries.map { it.modelId }
        assertTrue(modelIds.contains("gemini-4-hydrogen"))
        assertTrue(modelIds.contains("gemini-3.5-flash-lite"))
        assertTrue(modelIds.contains("gemini-3.8-flash"))
        assertTrue(modelIds.contains("gemini-4-argon"))
        assertTrue(modelIds.contains("gemini-4-carbon"))

        // 2. Exact lookup
        assertEquals(GeminiModelOption.GEMINI_4_HYDROGEN, GeminiModelOption.fromModelId("gemini-4-hydrogen"))
        assertEquals(GeminiModelOption.GEMINI_3_5_FLASH_LITE, GeminiModelOption.fromModelId("gemini-3.5-flash-lite"))
        assertEquals(GeminiModelOption.GEMINI_3_8_FLASH, GeminiModelOption.fromModelId("gemini-3.8-flash"))
        assertEquals(GeminiModelOption.GEMINI_4_ARGON, GeminiModelOption.fromModelId("gemini-4-argon"))
        assertEquals(GeminiModelOption.GEMINI_4_CARBON, GeminiModelOption.fromModelId("gemini-4-carbon"))

        // 3. Graceful migration from aliases
        assertEquals(GeminiModelOption.GEMINI_4_HYDROGEN, GeminiModelOption.fromModelId("hydrogen"))
        assertEquals(GeminiModelOption.GEMINI_4_ARGON, GeminiModelOption.fromModelId("argon"))
        assertEquals(GeminiModelOption.GEMINI_4_CARBON, GeminiModelOption.fromModelId("carbon"))
        assertEquals(GeminiModelOption.GEMINI_3_5_FLASH_LITE, GeminiModelOption.fromModelId("gemini-3.5-flash-lite"))
        assertEquals(GeminiModelOption.GEMINI_3_8_FLASH, GeminiModelOption.fromModelId("gemini-3.8-flash"))

        // 4. Unknown defaults to Hydrogen
        assertEquals(GeminiModelOption.GEMINI_4_HYDROGEN, GeminiModelOption.fromModelId("unknown-model-xyz"))
    }

    @Test
    fun `test2_buildFailoverChainCreatesExpectedHierarchy`() {
        // Argon chain: Argon -> 4 Hydrogen -> 3.5 Flash-Lite -> 3.8 Flash
        val argonChain = GeminiClient.buildFailoverChain("gemini-4-argon", autoFailoverEnabled = true)
        assertEquals(listOf("gemini-4-argon", "gemini-4-hydrogen", "gemini-3.5-flash-lite", "gemini-3.8-flash"), argonChain)

        // Hydrogen chain: 4 Hydrogen -> 3.5 Flash-Lite -> 3.8 Flash -> 4 Argon
        val hydrogenChain = GeminiClient.buildFailoverChain("gemini-4-hydrogen", autoFailoverEnabled = true)
        assertEquals(listOf("gemini-4-hydrogen", "gemini-3.5-flash-lite", "gemini-3.8-flash", "gemini-4-argon"), hydrogenChain)

        // Flash-Lite chain: 3.5 Flash-Lite -> 4 Hydrogen -> 3.8 Flash -> 4 Argon
        val liteChain = GeminiClient.buildFailoverChain("gemini-3.5-flash-lite", autoFailoverEnabled = true)
        assertEquals(listOf("gemini-3.5-flash-lite", "gemini-4-hydrogen", "gemini-3.8-flash", "gemini-4-argon"), liteChain)

        // When autoFailover is disabled, only primary model is returned
        val singleChain = GeminiClient.buildFailoverChain("gemini-4-argon", autoFailoverEnabled = false)
        assertEquals(listOf("gemini-4-argon"), singleChain)
    }

    @Test
    fun `test3_smartFailoverOnHttp429QuotaExhaustion`() = runBlocking {
        val dummyKey = "AIzaSyDUMMYKEY1234567890ABCDEF"

        // Mock interceptor: gemini-4-argon fails with 429 quota exhaustion, gemini-4-hydrogen succeeds
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
        assertEquals("gemini-4-hydrogen", structured.modelUsed)
        assertEquals("gemini-4-argon", structured.modelRequested)
        assertTrue(structured.fallbackOccurred)
        assertNotNull(structured.fallbackNoteTr)
        assertTrue(structured.fallbackNoteTr!!.contains("Gemini 4 Hydrogen"))

        // Status flow check
        val failoverStatus = GeminiClient.getLastFailoverStatus()
        assertNotNull(failoverStatus)
        assertTrue(failoverStatus!!.contains("gemini-4-argon"))
        assertTrue(failoverStatus.contains("gemini-4-hydrogen"))
    }

    @Test
    fun `test4_smartFailoverOnHttp503AndTimeout`() = runBlocking {
        val dummyKey = "AIzaSyDUMMYKEY1234567890ABCDEF"

        // Mock: Argon hits 503, Hydrogen hits timeout (>5s), 3.5 Flash-Lite succeeds
        GeminiClient.testCallInterceptor = { modelId, _ ->
            when (modelId) {
                "gemini-4-argon" -> Result.failure(IllegalStateException("HTTP 503: Sunucu aşırı yoğun."))
                "gemini-4-hydrogen" -> Result.failure(TimeoutException("Zaman aşımı (>5s)"))
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
            preferredModelId = "gemini-4-argon",
            systemInstruction = "Test",
            conversationHistory = emptyList(),
            userMessageWithDirective = "Selam",
            autoFailoverEnabled = true
        )

        assertTrue(result.isSuccess)
        val structured = result.getOrNull()
        assertNotNull(structured)
        assertEquals("gemini-3.5-flash-lite", structured!!.modelUsed)
        assertEquals("gemini-4-argon", structured.modelRequested)
        assertTrue(structured.fallbackOccurred)
    }

    @Test
    fun `test5_antiLoopingAndQualityFilterRejectsRepetitiveAndBlankResponses`() {
        // Blank or placeholder should be rejected
        assertNull(GeminiClient.sanitizeAndValidateDialogueReply(""))
        assertNull(GeminiClient.sanitizeAndValidateDialogueReply("   "))
        assertNull(GeminiClient.sanitizeAndValidateDialogueReply("..."))
        assertNull(GeminiClient.sanitizeAndValidateDialogueReply("null"))
        assertNull(GeminiClient.sanitizeAndValidateDialogueReply("undefined"))

        // Echoing user message should be rejected
        val prompt = "Hocam ilk 11'de miyim?"
        assertNull(GeminiClient.sanitizeAndValidateDialogueReply("Hocam ilk 11'de miyim?", prompt))

        // Consecutive word loop should be deduplicated
        val loopedWords = "hocam hocam hocam hocam taktiğe hazırım"
        val cleanedWordLoop = GeminiClient.sanitizeAndValidateDialogueReply(loopedWords)
        assertNotNull(cleanedWordLoop)
        assertFalse(cleanedWordLoop!!.contains("hocam hocam hocam"))

        // Sentence repetition loop should be deduplicated
        val sentenceLoop = "Maça hazırız. Maça hazırız. Maça hazırız. Sahaya çıkacağız."
        val cleanedSentenceLoop = GeminiClient.sanitizeAndValidateDialogueReply(sentenceLoop)
        assertNotNull(cleanedSentenceLoop)
        val occurrences = cleanedSentenceLoop!!.split("Maça hazırız").size - 1
        assertEquals(1, occurrences)
    }

    @Test
    fun `test6_v1betaPayloadCompliesWithRoleConstraints`() {
        // Test that leading model turns are stripped so contents[0].role is always 'user'
        val historyWithLeadingModelTurn = listOf(
            ConversationTurn(isUser = false, speakerName = "Hoca", text = "Hoş geldin evlat"),
            ConversationTurn(isUser = true, speakerName = "Arda", text = "Sağ olun hocam"),
            ConversationTurn(isUser = false, speakerName = "Hoca", text = "İdmanlara odaklan")
        )

        val payload = GeminiClient.buildRequestPayload(
            systemInstruction = "Sen futbol hocasısın.",
            history = historyWithLeadingModelTurn,
            userPrompt = "Hazırım hocam!",
            temperature = 0.7
        )

        // 1. Verify systemInstruction
        val sysObj = payload.getJSONObject("systemInstruction")
        val sysParts = sysObj.getJSONArray("parts")
        assertEquals("Sen futbol hocasısın.", sysParts.getJSONObject(0).getString("text"))

        // 2. Verify contents
        val contents = payload.getJSONArray("contents")
        assertTrue(contents.length() >= 2)

        // First content turn MUST be 'user'
        assertEquals("user", contents.getJSONObject(0).getString("role"))

        // Last content turn MUST be 'user' and contain userPrompt
        val lastContent = contents.getJSONObject(contents.length() - 1)
        assertEquals("user", lastContent.getString("role"))

        // Verify roles strictly alternate
        for (i in 0 until contents.length() - 1) {
            val role1 = contents.getJSONObject(i).getString("role")
            val role2 = contents.getJSONObject(i + 1).getString("role")
            assertTrue("Roles must strictly alternate: $role1 vs $role2", role1 != role2)
        }

        // 3. Verify generationConfig
        val genConfig = payload.getJSONObject("generationConfig")
        assertEquals(2048, genConfig.getInt("maxOutputTokens"))
        assertEquals("application/json", genConfig.getString("responseMimeType"))
    }

    @Test
    fun `test7_dialogueEngineSeamlesslyFallsBackToOfflineWhenAllModelsFail`() = runBlocking {
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
            modelId = "gemini-4-hydrogen",
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
    fun `test8_testConnectionTestsBothPrimaryAndFallback`() = runBlocking {
        val dummyKey = "AIzaSyDUMMYKEY1234567890ABCDEF"

        // Mock test connection
        GeminiClient.testCallInterceptor = { modelId, _ ->
            Result.success("""{"replyText": "Bağlantı başarılı: $modelId", "emotionTag": "Hazır"}""")
        }

        val res = GeminiClient.testConnection(
            apiKeyInput = dummyKey,
            modelId = "gemini-4-hydrogen"
        )

        assertTrue(res.success)
        assertEquals("gemini-4-hydrogen", res.modelTested)
        assertEquals("gemini-3.5-flash-lite", res.fallbackModelTested)
        assertTrue(res.fallbackSuccess)
        assertTrue(res.detailMessageTr.contains("gemini-4-hydrogen"))
        assertTrue(res.detailMessageTr.contains("gemini-3.5-flash-lite"))
    }
}
