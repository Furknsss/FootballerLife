package com.example.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.util.concurrent.TimeoutException

/**
 * Modern 2026 Gemini model options selectable from the in-app Settings screen.
 * Replaces legacy Gemini 2 naming with the cutting-edge Gemini 3.8 and Gemini 4 family.
 */
enum class GeminiModelOption(
    val modelId: String,
    val titleTr: String,
    val descriptionTr: String,
    val badgeTr: String,
    val priorityRank: Int
) {
    GEMINI_3_8_FLASH(
        modelId = "gemini-3.8-flash",
        titleTr = "Gemini 3.8 Flash (Önerilen)",
        descriptionTr = "Anlık WhatsApp/DM mesajlaşmaları, basın demeçleri ve canlı diyaloglar için ultra hızlı varsayılan model.",
        badgeTr = "VARSAYILAN",
        priorityRank = 1
    ),
    GEMINI_3_8_PRO(
        modelId = "gemini-3.8-pro",
        titleTr = "Gemini 3.8 Pro (Derin Akıl & Taktik)",
        descriptionTr = "Derin taktik analizleri, transfer pazarlıkları ve psikolojik karakter nüansları için üstün akıl yürütme gücü.",
        badgeTr = "PRO / AKIL",
        priorityRank = 2
    ),
    GEMINI_4_ARGON(
        modelId = "gemini-4-argon",
        titleTr = "Gemini 4 Argon (Yeni Nesil Frontier)",
        descriptionTr = "Google DeepMind'ın yeni nesil amiral gemisi frontier mimarisi; maksimum bağlamsal kavrayış ve kariyer hafızası.",
        badgeTr = "FRONTIER",
        priorityRank = 3
    ),
    GEMINI_4_HYDROGEN(
        modelId = "gemini-4-hydrogen",
        titleTr = "Gemini 4 Hydrogen (Dinamik Çoklu Görev)",
        descriptionTr = "Dinamik akıl yürütme, maç anı yorumları ve karmaşık kulüp dinamiklerini anında çözümleyen deneysel frontier motor.",
        badgeTr = "DENEKSEL",
        priorityRank = 4
    ),
    GEMINI_3_8_FLASH_LITE(
        modelId = "gemini-3.8-flash-lite",
        titleTr = "Gemini 3.8 Flash-Lite (Hızlı / Düşük Kota)",
        descriptionTr = "Yüksek hacimli mesajlaşmalar, anlık pingler ve minimum kota tüketimi için optimize edilmiş yıldırım hızında model.",
        badgeTr = "HAFİF",
        priorityRank = 5
    );

    companion object {
        val DEFAULT = GEMINI_3_8_FLASH

        fun fromModelId(id: String): GeminiModelOption {
            val trimmed = id.trim()
            // Direct exact match
            entries.find { it.modelId.equals(trimmed, ignoreCase = true) }?.let { return it }

            // Graceful migration from older Gemini 2 ids or partial matching
            return when {
                trimmed.contains("argon", ignoreCase = true) -> GEMINI_4_ARGON
                trimmed.contains("hydrogen", ignoreCase = true) -> GEMINI_4_HYDROGEN
                trimmed.contains("pro", ignoreCase = true) -> GEMINI_3_8_PRO
                trimmed.contains("lite", ignoreCase = true) -> GEMINI_3_8_FLASH_LITE
                else -> GEMINI_3_8_FLASH
            }
        }
    }
}

/**
 * Structured response from Gemini AI dialogue generation, including the character's Turkish reply,
 * side-channel game state impacts, and auto-failover tracking metadata.
 */
data class GeminiDialogueStructuredResponse(
    val replyText: String,
    val emotionTag: String = "Doğal",
    val affinityDelta: Int = 0,
    val trustDelta: Int = 0,
    val moraleDelta: Int = 0,
    val teamChemistryDelta: Int = 0,
    val fanSupportDelta: Int = 0,
    val managerTrustDelta: Int = 0,
    val startingStatusHint: String? = null, // "STARTER", "BENCH", or null
    val newMemoryNote: String? = null,
    val suggestedQuickReplies: List<String> = emptyList(),
    val triggerTransferOffer: Boolean = false,
    val triggerWageRaise: Boolean = false,
    val triggerPressHeadline: String? = null,
    val isFromAi: Boolean = true,
    val modelUsed: String = "",
    val fallbackOccurred: Boolean = false,
    val fallbackNoteTr: String? = null,
    val modelRequested: String = ""
)

/**
 * Result of testing the Gemini API connection from the Settings screen.
 * Tests both the primary model and fallback health.
 */
data class GeminiConnectionTestResult(
    val success: Boolean,
    val statusTitleTr: String,
    val detailMessageTr: String,
    val latencyMs: Long = 0L,
    val modelTested: String = "",
    val fallbackSuccess: Boolean = true,
    val fallbackModelTested: String = "",
    val fallbackLatencyMs: Long = 0L,
    val fallbackDetailTr: String? = null
)

/**
 * Lightweight conversational turn passed to Gemini for multi-turn memory.
 */
data class ConversationTurn(
    val isUser: Boolean,
    val speakerName: String,
    val text: String
)

/**
 * Pure Kotlin + Android SDK (`HttpURLConnection` + `org.json`) client for the official
 * Google Gemini REST `generateContent` API with Smart Auto-Failover & Agent Relay Engine.
 *
 * Security & Reliability Guarantees:
 * - Never logs the API key in logcat, exceptions, or UI messages.
 * - Resolves API key from user-entered Settings first, falling back to `BuildConfig.GEMINI_API_KEY`.
 * - Automatic multi-agent failover:
 *   If the primary model hits 429 (quota exhausted), 503 (overloaded/busy), or latency timeout (>5s),
 *   automatically and invisibly falls back to the next fastest model:
 *   Primary -> Gemini 3.8 Flash -> Gemini 3.8 Flash-Lite -> Offline Intelligent Engine.
 * - Exposes fallback / relay status for monitoring and UI banners.
 */
object GeminiClient {

    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
    const val CALL_TIMEOUT_MS = 5_000L

    // Observable failover status for UI and monitoring
    private val _lastFailoverStatus = MutableStateFlow<String?>(null)
    val lastFailoverStatus: StateFlow<String?> = _lastFailoverStatus

    // Optional interceptor for deterministic unit tests (avoids live network during tests)
    @Volatile
    internal var testCallInterceptor: ((modelId: String, userPrompt: String) -> Result<String>)? = null

    fun getLastFailoverStatus(): String? = _lastFailoverStatus.value

    fun recordFailover(fromModel: String, toModel: String, reason: String) {
        val note = "Model otomatik olarak $fromModel yerine $toModel modeline devredildi ($reason)."
        _lastFailoverStatus.value = note
    }

    fun clearFailoverStatus() {
        _lastFailoverStatus.value = null
    }

    /**
     * Resolves the active Gemini API key without exposing it in logs.
     * Priority:
     * 1. User-entered key in in-app Settings (if non-blank)
     * 2. BuildConfig.GEMINI_API_KEY (injected from .env or local.properties if valid)
     */
    fun resolveApiKey(userEnteredKey: String?): String {
        val cleanedUser = userEnteredKey?.trim().orEmpty()
        if (isValidApiKeyFormat(cleanedUser)) {
            return cleanedUser
        }
        val buildConfigKey = readBuildConfigKeySafely()
        if (isValidApiKeyFormat(buildConfigKey)) {
            return buildConfigKey
        }
        return ""
    }

    /**
     * Returns true if a usable API key is configured either via Settings or BuildConfig.
     */
    fun hasConfiguredApiKey(userEnteredKey: String?): Boolean {
        return resolveApiKey(userEnteredKey).isNotEmpty()
    }

    /**
     * Indicates where the current active API key is coming from (for UI display in Settings).
     */
    fun getApiKeySourceLabelTr(userEnteredKey: String?): String {
        val cleanedUser = userEnteredKey?.trim().orEmpty()
        if (isValidApiKeyFormat(cleanedUser)) {
            return "Uygulama İçi Ayarlar (${maskApiKey(cleanedUser)})"
        }
        val buildConfigKey = readBuildConfigKeySafely()
        if (isValidApiKeyFormat(buildConfigKey)) {
            return ".env / BuildConfig (${maskApiKey(buildConfigKey)})"
        }
        return "Tanımlı Anahtar Yok (Çevrimdışı Senaryo Modu Aktif)"
    }

    /**
     * Masks an API key for safe display in the UI (e.g., "AIza••••••9x2A").
     */
    fun maskApiKey(key: String): String {
        val clean = key.trim()
        if (clean.length <= 8) return "••••••••"
        return "${clean.take(4)}••••••••${clean.takeLast(4)}"
    }

    private fun isValidApiKeyFormat(key: String): Boolean {
        if (key.length < 16) return false
        val upper = key.uppercase()
        if (upper == "MY_GEMINI_API_KEY" || upper == "YOUR_API_KEY" || upper == "NULL" || upper == " PLACEHOLDER") {
            return false
        }
        return true
    }

    private fun readBuildConfigKeySafely(): String {
        return try {
            val field = BuildConfig::class.java.getDeclaredField("GEMINI_API_KEY")
            field.isAccessible = true
            (field.get(null) as? String)?.trim().orEmpty()
        } catch (_: Throwable) {
            ""
        }
    }

    /**
     * Builds the ordered failover model chain based on the user's preferred model and auto-failover setting.
     * Hierarchy: Primary -> Gemini 3.8 Flash -> Gemini 3.8 Flash-Lite
     */
    fun buildFailoverChain(preferredModelId: String, autoFailoverEnabled: Boolean = true): List<String> {
        val primary = GeminiModelOption.fromModelId(preferredModelId).modelId
        if (!autoFailoverEnabled) {
            return listOf(primary)
        }

        val chain = mutableListOf<String>()
        chain.add(primary)

        // Tier 2: Gemini 3.8 Flash (ultra-fast standard)
        if (primary != GeminiModelOption.GEMINI_3_8_FLASH.modelId) {
            chain.add(GeminiModelOption.GEMINI_3_8_FLASH.modelId)
        }

        // Tier 3: Gemini 3.8 Flash-Lite (low latency & high throughput)
        if (primary != GeminiModelOption.GEMINI_3_8_FLASH_LITE.modelId) {
            chain.add(GeminiModelOption.GEMINI_3_8_FLASH_LITE.modelId)
        }

        return chain.distinct()
    }

    /**
     * Tests the Gemini API connection with both the primary model and the fallback model health.
     * Used by the "Bağlantıyı Test Et" button in SettingsScreen.
     */
    suspend fun testConnection(
        apiKeyInput: String?,
        modelId: String = GeminiModelOption.GEMINI_3_8_FLASH.modelId,
        fallbackModelId: String? = null
    ): GeminiConnectionTestResult = withContext(Dispatchers.IO) {
        val apiKey = resolveApiKey(apiKeyInput)
        val primaryModel = modelId.ifBlank { GeminiModelOption.GEMINI_3_8_FLASH.modelId }
        val effectiveFallback = fallbackModelId ?: if (primaryModel == GeminiModelOption.GEMINI_3_8_FLASH_LITE.modelId) {
            GeminiModelOption.GEMINI_3_8_FLASH.modelId
        } else {
            GeminiModelOption.GEMINI_3_8_FLASH_LITE.modelId
        }

        if (apiKey.isEmpty()) {
            return@withContext GeminiConnectionTestResult(
                success = false,
                statusTitleTr = "API Anahtarı Girilmedi",
                detailMessageTr = "Lütfen Google AI Studio'dan aldığınız Gemini API anahtarını girin veya .env dosyasına GEMINI_API_KEY ekleyin.",
                modelTested = primaryModel,
                fallbackSuccess = false,
                fallbackModelTested = effectiveFallback
            )
        }

        val systemPrompt = "Sen Football Life futbol kariyer oyununun Türkçe yapay zeka motorusun. Sadece JSON formatında yanıt ver."
        val testUserPrompt = """
            Bağlantı ve yedekleme testi. Aşağıdaki JSON şemasıyla tek cümlelik Türkçe selam döndür:
            {"replyText": "Tesislerde bağlantı ve yedekleme hazır!", "emotionTag": "Kararlı"}
        """.trimIndent()

        // 1. Primary Model Health Test
        val start1 = System.currentTimeMillis()
        val result1 = executeCallWithTimeout(
            apiKey = apiKey,
            modelId = primaryModel,
            systemInstruction = systemPrompt,
            history = emptyList(),
            userPrompt = testUserPrompt,
            temperature = 0.5
        )
        val elapsed1 = (System.currentTimeMillis() - start1).coerceAtLeast(1L)

        // 2. Fallback Model Health Test
        val start2 = System.currentTimeMillis()
        val result2 = executeCallWithTimeout(
            apiKey = apiKey,
            modelId = effectiveFallback,
            systemInstruction = systemPrompt,
            history = emptyList(),
            userPrompt = testUserPrompt,
            temperature = 0.5
        )
        val elapsed2 = (System.currentTimeMillis() - start2).coerceAtLeast(1L)

        val primaryOk = result1.isSuccess
        val fallbackOk = result2.isSuccess

        when {
            primaryOk && fallbackOk -> {
                GeminiConnectionTestResult(
                    success = true,
                    statusTitleTr = "Bağlantı & Akıllı Devir Hattı Hazır (${elapsed1} ms)",
                    detailMessageTr = "Birincil: $primaryModel (${elapsed1} ms) • Yedek: $effectiveFallback (${elapsed2} ms) • Kesintisiz Geçiş Aktif",
                    latencyMs = elapsed1,
                    modelTested = primaryModel,
                    fallbackSuccess = true,
                    fallbackModelTested = effectiveFallback,
                    fallbackLatencyMs = elapsed2,
                    fallbackDetailTr = "Yedek model hazır ve çalışır durumda"
                )
            }
            primaryOk && !fallbackOk -> {
                val fbErr = sanitizeErrorMessage(result2.exceptionOrNull()?.message ?: "Yedek modele ulaşılamadı")
                GeminiConnectionTestResult(
                    success = true,
                    statusTitleTr = "Birincil Model Aktif (${elapsed1} ms)",
                    detailMessageTr = "Birincil model ($primaryModel) aktif. Yedek model uyarısı: $fbErr",
                    latencyMs = elapsed1,
                    modelTested = primaryModel,
                    fallbackSuccess = false,
                    fallbackModelTested = effectiveFallback,
                    fallbackLatencyMs = elapsed2,
                    fallbackDetailTr = fbErr
                )
            }
            !primaryOk && fallbackOk -> {
                GeminiConnectionTestResult(
                    success = true,
                    statusTitleTr = "Akıllı Yedek Model Devrede (${elapsed2} ms) ⚡",
                    detailMessageTr = "Birincil ($primaryModel) yanıt vermedi, fakat Yedek ($effectiveFallback) hazır! Auto-failover devrede.",
                    latencyMs = elapsed1,
                    modelTested = primaryModel,
                    fallbackSuccess = true,
                    fallbackModelTested = effectiveFallback,
                    fallbackLatencyMs = elapsed2,
                    fallbackDetailTr = "Yedek model kesintisiz devralmaya hazır"
                )
            }
            else -> {
                val err = sanitizeErrorMessage(result1.exceptionOrNull()?.message ?: "Sunucuya ulaşılamadı.")
                GeminiConnectionTestResult(
                    success = false,
                    statusTitleTr = "Bağlantı Hatası",
                    detailMessageTr = err,
                    latencyMs = elapsed1,
                    modelTested = primaryModel,
                    fallbackSuccess = false,
                    fallbackModelTested = effectiveFallback,
                    fallbackLatencyMs = elapsed2
                )
            }
        }
    }

    /**
     * Generates an in-character Turkish dialogue reply with structured JSON side-channel outcomes.
     * Features Automatic Multi-Agent Failover:
     * - If primary model hits 429, 503, or >5s latency timeout, automatically and invisibly falls back to
     *   the next fastest model in the chain (Primary -> Flash -> Flash-Lite -> Offline Engine).
     */
    suspend fun generateCharacterDialogue(
        apiKeyInput: String?,
        preferredModelId: String,
        systemInstruction: String,
        conversationHistory: List<ConversationTurn>,
        userMessageWithDirective: String,
        temperature: Double = 0.78,
        autoFailoverEnabled: Boolean = true
    ): Result<GeminiDialogueStructuredResponse> = withContext(Dispatchers.IO) {
        val apiKey = resolveApiKey(apiKeyInput)
        if (apiKey.isEmpty()) {
            return@withContext Result.failure(IllegalStateException("NO_API_KEY"))
        }

        val primaryModel = preferredModelId.trim().ifEmpty { GeminiModelOption.GEMINI_3_8_FLASH.modelId }
        val candidateModels = buildFailoverChain(primaryModel, autoFailoverEnabled)

        var lastError: Throwable? = null
        for (model in candidateModels) {
            val callResult = executeCallWithTimeout(
                apiKey = apiKey,
                modelId = model,
                systemInstruction = systemInstruction,
                history = conversationHistory,
                userPrompt = userMessageWithDirective,
                temperature = temperature
            )

            if (callResult.isSuccess) {
                val rawText = callResult.getOrNull().orEmpty()
                val structured = parseStructuredDialogueJson(rawText, model)
                if (structured != null && structured.replyText.isNotBlank()) {
                    val isFallback = !model.equals(primaryModel, ignoreCase = true)
                    val fallbackNote = if (isFallback) {
                        val friendlyName = GeminiModelOption.fromModelId(model).titleTr
                        "Model otomatik olarak $friendlyName'a devredildi"
                    } else null

                    if (isFallback) {
                        recordFailover(
                            fromModel = primaryModel,
                            toModel = model,
                            reason = lastError?.message ?: "Kota/yoğunluk failover"
                        )
                    }

                    return@withContext Result.success(
                        structured.copy(
                            modelUsed = model,
                            modelRequested = primaryModel,
                            fallbackOccurred = isFallback,
                            fallbackNoteTr = fallbackNote
                        )
                    )
                }
            } else {
                lastError = callResult.exceptionOrNull()
                val msg = lastError?.message.orEmpty()

                // If unauthorized / invalid API key, do not waste time retrying other models
                if (msg.contains("HTTP 401") || msg.contains("HTTP 403") || msg.contains("Geçersiz API anahtarı")) {
                    break
                }
                // If 429 (quota), 503 (busy/overloaded), timeout (>5s), or server error, continue loop to fallback model
            }
        }

        Result.failure(lastError ?: IllegalStateException("Gemini yanıtı alınamadı."))
    }

    private suspend fun executeCallWithTimeout(
        apiKey: String,
        modelId: String,
        systemInstruction: String,
        history: List<ConversationTurn>,
        userPrompt: String,
        temperature: Double
    ): Result<String> {
        // Deterministic test hook for unit tests
        testCallInterceptor?.let { interceptor ->
            return interceptor(modelId, userPrompt)
        }

        return withTimeoutOrNull(CALL_TIMEOUT_MS) {
            executeGenerateContentCall(
                apiKey = apiKey,
                modelId = modelId,
                systemInstruction = systemInstruction,
                history = history,
                userPrompt = userPrompt,
                temperature = temperature
            )
        } ?: Result.failure(TimeoutException("Zaman aşımı (>5s): $modelId yanıt vermedi."))
    }

    private fun executeGenerateContentCall(
        apiKey: String,
        modelId: String,
        systemInstruction: String,
        history: List<ConversationTurn>,
        userPrompt: String,
        temperature: Double
    ): Result<String> {
        var connection: HttpURLConnection? = null
        return try {
            val endpoint = "$BASE_URL/$modelId:generateContent?key=$apiKey"
            val url = URL(endpoint)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = CALL_TIMEOUT_MS.toInt()
                readTimeout = CALL_TIMEOUT_MS.toInt()
                doOutput = true
                doInput = true
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("Accept", "application/json")
            }

            val payload = buildRequestPayload(
                systemInstruction = systemInstruction,
                history = history,
                userPrompt = userPrompt,
                temperature = temperature
            )

            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(payload.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val responseBody = if (stream != null) {
                BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { it.readText() }
            } else {
                ""
            }

            if (responseCode !in 200..299) {
                val apiErrorMsg = extractApiErrorDetail(responseBody, responseCode)
                return Result.failure(IllegalStateException("HTTP $responseCode: $apiErrorMsg"))
            }

            val extractedText = extractCandidateText(responseBody)
            if (extractedText.isBlank()) {
                Result.failure(IllegalStateException("Boş yanıt döndü."))
            } else {
                Result.success(extractedText)
            }
        } catch (t: SocketTimeoutException) {
            Result.failure(TimeoutException("Zaman aşımı (>5s): Model yanıt vermedi."))
        } catch (t: Throwable) {
            Result.failure(IllegalStateException(sanitizeErrorMessage(t.message ?: "Ağ bağlantı hatası")))
        } finally {
            connection?.disconnect()
        }
    }

    private fun buildRequestPayload(
        systemInstruction: String,
        history: List<ConversationTurn>,
        userPrompt: String,
        temperature: Double
    ): JSONObject {
        val root = JSONObject()

        // System instruction
        val sysParts = JSONArray().put(JSONObject().put("text", systemInstruction))
        root.put("systemInstruction", JSONObject().put("parts", sysParts))

        // Multi-turn contents
        val contentsArray = JSONArray()
        val recentTurns = history.takeLast(10)
        var lastRole: String? = null

        for (turn in recentTurns) {
            val role = if (turn.isUser) "user" else "model"
            val formattedText = turn.text
            if (role == lastRole && contentsArray.length() > 0) {
                val prevObj = contentsArray.getJSONObject(contentsArray.length() - 1)
                val prevParts = prevObj.getJSONArray("parts")
                prevParts.put(JSONObject().put("text", formattedText))
            } else {
                val partObj = JSONObject().put("text", formattedText)
                val contentObj = JSONObject()
                    .put("role", role)
                    .put("parts", JSONArray().put(partObj))
                contentsArray.put(contentObj)
                lastRole = role
            }
        }

        // Append current user message
        if (lastRole == "user" && contentsArray.length() > 0) {
            val prevObj = contentsArray.getJSONObject(contentsArray.length() - 1)
            prevObj.getJSONArray("parts").put(JSONObject().put("text", userPrompt))
        } else {
            val userContent = JSONObject()
                .put("role", "user")
                .put("parts", JSONArray().put(JSONObject().put("text", userPrompt)))
            contentsArray.put(userContent)
        }

        root.put("contents", contentsArray)

        // Generation config requesting JSON output
        val genConfig = JSONObject()
            .put("temperature", temperature)
            .put("maxOutputTokens", 2048)
            .put("responseMimeType", "application/json")
        root.put("generationConfig", genConfig)

        return root
    }

    private fun extractCandidateText(rawResponseJson: String): String {
        return try {
            val root = JSONObject(rawResponseJson)
            val candidates = root.optJSONArray("candidates") ?: return ""
            if (candidates.length() == 0) return ""
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content") ?: return ""
            val parts = content.optJSONArray("parts") ?: return ""
            val sb = StringBuilder()
            for (i in 0 until parts.length()) {
                val part = parts.getJSONObject(i)
                if (part.optBoolean("thought", false)) continue
                val text = part.optString("text", "")
                if (text.isNotBlank()) {
                    sb.append(text)
                }
            }
            sb.toString().trim()
        } catch (_: Throwable) {
            ""
        }
    }

    internal fun parseStructuredDialogueJson(
        rawText: String,
        modelUsed: String
    ): GeminiDialogueStructuredResponse? {
        val cleaned = rawText
            .trim()
            .removePrefix("```json")
            .removePrefix("```JSON")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        return try {
            val jsonStart = cleaned.indexOf('{')
            val jsonEnd = cleaned.lastIndexOf('}')
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                val jsonSubstring = cleaned.substring(jsonStart, jsonEnd + 1)
                val obj = JSONObject(jsonSubstring)
                val reply = obj.optString("replyText", "")
                    .ifBlank { obj.optString("reply", "") }
                    .ifBlank { obj.optString("response", "") }
                    .ifBlank { obj.optString("text", "") }
                    .trim()

                if (reply.isBlank()) return null

                val quickRepliesArr = obj.optJSONArray("suggestedQuickReplies")
                    ?: obj.optJSONArray("quickReplies")
                val quickReplies = mutableListOf<String>()
                if (quickRepliesArr != null) {
                    for (i in 0 until minOf(quickRepliesArr.length(), 3)) {
                        val item = quickRepliesArr.optString(i, "").trim()
                        if (item.isNotBlank()) quickReplies.add(item)
                    }
                }

                val rawStatusHint = obj.optString("startingStatusHint", "").trim().uppercase()
                val validStatusHint = if (rawStatusHint == "STARTER" || rawStatusHint == "BENCH") rawStatusHint else null

                val rawMemory = obj.optString("newMemoryNote", "").trim().ifBlank { null }
                val rawHeadline = obj.optString("triggerPressHeadline", "").trim().ifBlank { null }

                GeminiDialogueStructuredResponse(
                    replyText = reply,
                    emotionTag = obj.optString("emotionTag", "Doğal").trim().ifBlank { "Doğal" },
                    affinityDelta = obj.optInt("affinityDelta", 0).coerceIn(-15, 15),
                    trustDelta = obj.optInt("trustDelta", 0).coerceIn(-15, 15),
                    moraleDelta = obj.optInt("moraleDelta", 0).coerceIn(-12, 12),
                    teamChemistryDelta = obj.optInt("teamChemistryDelta", 0).coerceIn(-10, 10),
                    fanSupportDelta = obj.optInt("fanSupportDelta", 0).coerceIn(-10, 10),
                    managerTrustDelta = obj.optInt("managerTrustDelta", 0).coerceIn(-15, 15),
                    startingStatusHint = validStatusHint,
                    newMemoryNote = rawMemory,
                    suggestedQuickReplies = quickReplies,
                    triggerTransferOffer = obj.optBoolean("triggerTransferOffer", false),
                    triggerWageRaise = obj.optBoolean("triggerWageRaise", false),
                    triggerPressHeadline = rawHeadline,
                    isFromAi = true,
                    modelUsed = modelUsed
                )
            } else if (cleaned.isNotBlank()) {
                // Fallback if model returned plain text despite responseMimeType
                GeminiDialogueStructuredResponse(
                    replyText = cleaned.take(500),
                    emotionTag = "Doğal",
                    affinityDelta = 2,
                    trustDelta = 1,
                    isFromAi = true,
                    modelUsed = modelUsed
                )
            } else {
                null
            }
        } catch (_: Throwable) {
            if (cleaned.isNotBlank() && !cleaned.startsWith("{")) {
                GeminiDialogueStructuredResponse(
                    replyText = cleaned.take(500),
                    emotionTag = "Doğal",
                    affinityDelta = 1,
                    trustDelta = 1,
                    isFromAi = true,
                    modelUsed = modelUsed
                )
            } else {
                null
            }
        }
    }

    private fun extractApiErrorDetail(errorBody: String, statusCode: Int): String {
        val parsedMsg = try {
            val obj = JSONObject(errorBody)
            val errObj = obj.optJSONObject("error")
            errObj?.optString("message").orEmpty()
        } catch (_: Throwable) {
            ""
        }
        val clean = sanitizeErrorMessage(parsedMsg)
        return when {
            statusCode == 400 && clean.contains("API key", ignoreCase = true) ->
                "Geçersiz API anahtarı. Lütfen anahtarınızı kontrol edin."
            statusCode == 401 || statusCode == 403 ->
                "API anahtarı yetkisiz veya erişim reddedildi."
            statusCode == 404 ->
                "Seçilen Gemini modeli bulunamadı veya bu anahtar için aktif değil."
            statusCode == 429 ->
                "API kota sınırına ulaşıldı (429). Otomatik yedekleme devrede."
            statusCode == 503 ->
                "Gemini sunucusu şu an aşırı yoğun (503). Otomatik yedek modele devrediliyor."
            clean.isNotBlank() -> clean
            else -> "Sunucu hatası (Kod: $statusCode)"
        }
    }

    private fun sanitizeErrorMessage(msg: String): String {
        // Ensure no query parameter `key=...` or `AIza...` token ever leaks into error strings
        return msg
            .replace(Regex("key=[^&\\s]+", RegexOption.IGNORE_CASE), "key=***")
            .replace(Regex("AIza[0-9A-Za-z_\\-]{20,}"), "AIza***")
            .take(220)
    }
}
