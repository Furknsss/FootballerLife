package com.example.data

import com.example.engine.NpcDialogueEngine
import com.example.model.CareerSaveState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** The Android client never receives a Gemini key. Only a short-lived user session token. */
class DialogueGateway {
    private val cache = object : LinkedHashMap<String, String>(32, .75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, String>?): Boolean = size > 64
    }

    suspend fun reply(endpoint: String, sessionToken: String, state: CareerSaveState, npcId: String, message: String): String? = withContext(Dispatchers.IO) {
        if (endpoint.isBlank() || sessionToken.isBlank()) return@withContext null
        val url = runCatching { URL(endpoint) }.getOrNull() ?: return@withContext null
        if (url.protocol != "https" || url.userInfo != null) return@withContext null
        val npc = state.npcs.find { it.id == npcId } ?: return@withContext null
        val context = JSONObject().put("npc", JSONObject().put("id", npc.id).put("name", npc.name).put("role", npc.roleTitle).put("personality", npc.personality).put("mood", npc.currentMood))
            .put("gameDay", state.currentDayIndex).put("club", state.currentClub.name)
            .put("scene", state.livingWorld.activeScene?.sceneType?.name ?: "PHONE")
            .put("message", message.take(2000)).put("energy", state.player.energy).put("injuryDays", state.player.injuryDaysRemaining)
            .put("managerTrust", state.managerTrust).put("squadRole", state.nextMatchStartingStatus.name)
            .put("lastMatch", state.lastMatchReport?.let { "${it.userRating} reyting, ${it.userGoals} gol, ${it.userAssists} asist" })
            .put("recentMessages", JSONArray(npc.messages.takeLast(8).map { JSONObject().put("fromUser", it.isFromUser).put("text", it.text.take(1000)) }))
            .put("memories", JSONArray(NpcDialogueEngine.relevantMemories(state, npcId, message).map { it.text.take(300) }))
            .put("allowedActions", JSONArray())
        val key = "$endpoint:${state.player.id}:$context"
        synchronized(cache) { cache[key] }?.let { return@withContext it }
        var connection: HttpURLConnection? = null
        try {
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.instanceFollowRedirects = false
            connection.connectTimeout = 4000
            connection.readTimeout = 7000
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.setRequestProperty("Authorization", "Bearer $sessionToken")
            connection.outputStream.use { it.write(context.toString().toByteArray(Charsets.UTF_8)) }
            if (connection.responseCode != 200) return@withContext null
            val raw = connection.inputStream.bufferedReader().use { reader ->
                val buffer = CharArray(16_385)
                var length = 0
                while (length < buffer.size) {
                    val read = reader.read(buffer, length, buffer.size - length)
                    if (read < 0) break
                    length += read
                }
                if (length > 16_384) return@withContext null
                String(buffer, 0, length)
            }
            val result = JSONObject(raw)
            if (!result.isNull("proposedAction") || !result.isNull("targetEntityId")) return@withContext null
            val text = result.optString("dialogue").trim()
            if (text.length !in 1..2000 || result.optString("conversationStatus") !in setOf("OPEN", "CLOSED")) return@withContext null
            synchronized(cache) { cache[key] = text }
            text
        } catch (_: java.io.IOException) {
            null
        } catch (_: org.json.JSONException) {
            null
        } finally {
            connection?.disconnect()
        }
    }
}
