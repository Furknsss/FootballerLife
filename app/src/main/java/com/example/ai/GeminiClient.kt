package com.example.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Supported Gemini models selectable from the in-app Settings screen.
 */
enum class GeminiModelOption(
    val modelId: String,
    val titleTr: String,
    val descriptionTr: String,
    val badgeTr: String
) {
    GEMINI_2_5_FLASH(
        modelId = "gemini-2.5-flash",
        titleTr = "Gemini 2.5 Flash (Önerilen)",
        descriptionTr = "Anlık WhatsApp/DM mesajlaşmaları, hoca görüşmeleri ve telefon aramaları için hızlı & dengeli model.",
        badgeTr = "VARSAYILAN"
    ),
    GEMINI_2_5_PRO(
        modelId = "gemini-2.5-pro",
        titleTr = "Gemini 2.5 Pro (Derin Karakter Analizi)",
        descriptionTr = "Daha uzun kulüp toplantıları, sözleşme pazarlıkları ve derin psikolojik nüanslar için.",
        badgeTr = "PRO"
    ),
    GEMINI_2_5_FLASH_LITE(
        modelId = "gemini-2.5-flash-lite",
        titleTr = "Gemini 2.5 Flash-Lite (Ultra Hızlı / Ekonomik)",
        descriptionTr = "Minimum kota kullanımı ve en yüksek yanıt hızı.",
        badgeTr = "HAFİF"
    ),
    GEMINI_2_0_FLASH(
        modelId = "gemini-2.0-flash",
        titleTr = "Gemini 2.0 Flash (Kararlı & Geniş Uyumluluk)",
        descriptionTr = "Düşük gecikmeli standart üretim modeli.",
        badgeTr = "KARARLI"
    );

    companion object {
        fun fromModelId(id: String): GeminiModelOption {
            return entries.find { it.modelId.equals(id.trim(), ignoreCase = true) } ?: GEMINI_2_5_FLASH
        }
    }
}

/**
 * Structured response from Gemini AI dialogue generation, including the character's Turkish reply
 * and side-channel game state impacts.
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
    val modelUsed: String = ""
)

/**
 * Result of testing the Gemini API connection from the Settings screen.
 */
data class GeminiConnectionTestResult(
    val success: Boolean,
    val statusTitleTr: String,
    val detailMessageTr: String,
    val latencyMs: Long = 0L,
    val modelTested: String = ""
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
 * Google Gemini REST `generateContent` API.
 *
 * Security & Reliability Guarantees:
 * - Never logs the API key in logcat, exceptions, or UI messages.
 * - Resolves API key from user-entered Settings first, falling back to `BuildConfig.GEMINI_API_KEY`
 *   (from `.env` or `local.properties`) if present and valid.
 * - Automatically retries with fallback model (`gemini-2.5-flash` <-> `gemini-2.0-flash`) if a specific
 *   model endpoint is unavailable on the user's key tier.
 */
object GeminiClient {

    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
    private const val CONNECT_TIMEOUT_MS = 10_000
    private const val READ_TIMEOUT_MS = 18_000

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
     * Tests the Gemini API connection with the given API key and model.
     * Used by the "Bağlantıyı Test Et" button in SettingsScreen.
     */
    suspend fun testConnection(
        apiKeyInput: String?,
        modelId: String = GeminiModelOption.GEMINI_2_5_FLASH.modelId
    ): GeminiConnectionTestResult = withContext(Dispatchers.IO) {
        val apiKey = resolveApiKey(apiKeyInput)
        if (apiKey.isEmpty()) {
            return@withContext GeminiConnectionTestResult(
                success = false,
                statusTitleTr = "API Anahtarı Girilmedi",
                detailMessageTr = "Lütfen Google AI Studio'dan aldığınız Gemini API anahtarını girin veya .env dosyasına GEMINI_API_KEY ekleyin.",
                modelTested = modelId
            )
        }

        val startMs = System.currentTimeMillis()
        val systemPrompt = "Sen Football Life futbol kariyer oyununun Türkçe yapay zeka motorusun. Sadece JSON formatında yanıt ver."
        val testUserPrompt = """
            Bağlantı testi yapılıyor. Aşağıdaki JSON şemasıyla tek cümlelik Türkçe bir futbol teknik direktörü selamı döndür:
            {"replyText": "Tesislerde bağlantı hazır evlat, maça odaklan!", "emotionTag": "Kararlı"}
        """.trimIndent()

        val result = executeGenerateContentCall(
            apiKey = apiKey,
            modelId = modelId.ifBlank { GeminiModelOption.GEMINI_2_5_FLASH.modelId },
            systemInstruction = systemPrompt,
            history = emptyList(),
            userPrompt = testUserPrompt,
            temperature = 0.5
        )

        val elapsed = (System.currentTimeMillis() - startMs).coerceAtLeast(1L)
        result.fold(
            onSuccess = { rawJson ->
                val parsed = parseStructuredDialogueJson(rawJson, modelId)
                val preview = parsed?.replyText?.take(90) ?: rawJson.take(90)
                GeminiConnectionTestResult(
                    success = true,
                    statusTitleTr = "Bağlantı Başarılı (${elapsed} ms)",
                    detailMessageTr = "Model: $modelId • Yanıt: \"$preview\"",
                    latencyMs = elapsed,
                    modelTested = modelId
                )
            },
            onFailure = { err ->
                GeminiConnectionTestResult(
                    success = false,
                    statusTitleTr = "Bağlantı Hatası",
                    detailMessageTr = sanitizeErrorMessage(err.message ?: "Sunucuya ulaşılamadı."),
                    latencyMs = elapsed,
                    modelTested = modelId
                )
            }
        )
    }

    /**
     * Generates an in-character Turkish dialogue reply with structured JSON side-channel outcomes.
     */
    suspend fun generateCharacterDialogue(
        apiKeyInput: String?,
        preferredModelId: String,
        systemInstruction: String,
        conversationHistory: List<ConversationTurn>,
        userMessageWithDirective: String,
        temperature: Double = 0.78
    ): Result<GeminiDialogueStructuredResponse> = withContext(Dispatchers.IO) {
        val apiKey = resolveApiKey(apiKeyInput)
        if (apiKey.isEmpty()) {
            return@withContext Result.failure(IllegalStateException("NO_API_KEY"))
        }

        val primaryModel = preferredModelId.trim().ifEmpty { GeminiModelOption.GEMINI_2_5_FLASH.modelId }
        val candidateModels = listOf(
            primaryModel,
            GeminiModelOption.GEMINI_2_5_FLASH.modelId,
            "gemini-2.0-flash"
        ).distinct()

        var lastError: Throwable? = null
        for (model in candidateModels) {
            val callResult = executeGenerateContentCall(
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
                    return@withContext Result.success(structured)
                }
            } else {
                lastError = callResult.exceptionOrNull()
                val msg = lastError?.message.orEmpty()
                // If unauthorized / invalid API key, don't bother retrying other models
                if (msg.contains("HTTP 400") || msg.contains("HTTP 401") || msg.contains("HTTP 403")) {
                    break
                }
            }
        }

        Result.failure(lastError ?: IllegalStateException("Gemini yanıtı alınamadı."))
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
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
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
            val formattedText = if (turn.isUser) {
                turn.text
            } else {
                // Keep model history clean in natural text so the model maintains conversational flow
                turn.text
            }
            if (role == lastRole && contentsArray.length() > 0) {
                // Merge consecutive turns of the same role to satisfy Gemini strict alternating role rules
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

        // Append current user message + JSON directive
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

        // Generation config requesting JSON output (2048 tokens to accommodate Gemini 2.5 thinking + JSON output)
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
                "API kota sınırına ulaşıldı (429). Lütfen biraz bekleyin veya Flash-Lite modelini seçin."
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
