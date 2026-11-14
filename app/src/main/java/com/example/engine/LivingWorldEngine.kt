package com.example.engine

import com.example.model.*

/** Pure reducers: all related changes are persisted together as one career snapshot. */
object LivingWorldEngine {
    fun migrate(state: CareerSaveState): CareerSaveState = reconcile(state.copy(saveVersion = maxOf(4, state.saveVersion),
        calendarEvents = state.calendarEvents.map { it.copy(dateLabel = GameClock.full(it.dayIndex, state.currentSeasonYear)) },
        fixtures = state.fixtures.map { it.copy(dateLabel = GameClock.full(it.dayIndex, state.currentSeasonYear)) }))

    fun identityId(state: CareerSaveState, npc: NpcCharacter): String = when (npc.id) {
        "NPC_MANAGER", "NPC_TEAM_GROUP" -> "${npc.id}:${state.contract.clubId}:${npc.name.hashCode()}"
        else -> npc.id
    }

    fun reconcile(input: CareerSaveState): CareerSaveState {
        var state = input
        var world = state.livingWorld
        val characters = world.characters.toMutableList()
        state.npcs.forEach { npc ->
            val id = identityId(state, npc)
            if (characters.none { it.id == id }) characters += CharacterIdentity(
                id, npc.id, npc.name,
                if (npc.id in listOf("NPC_MANAGER", "NPC_TEAM_GROUP")) state.contract.clubId else null,
                npc.roleTitle, when (npc.id) { "NPC_MANAGER" -> "CAST_COACH"; "NPC_AGENT", "NPC_PRESS" -> "CAST_AGENT"; "NPC_FAMILY" -> "CAST_FAMILY"; else -> "CAST_PLAYER_DARK" },
                when (npc.id) { "NPC_MANAGER" -> "COACH"; "NPC_AGENT" -> "AGENT"; else -> "CASUAL" }
            )
        }
        if (characters.none { it.id == "NPC_DOCTOR:${state.contract.clubId}" }) characters += CharacterIdentity(
            "NPC_DOCTOR:${state.contract.clubId}", "NPC_DOCTOR", "Dr. Deniz Aydın", state.contract.clubId,
            "Kulüp doktoru", "CAST_DOCTOR", "MEDICAL"
        )
        var medical = world.medical
        if (state.player.injuryDaysRemaining > 0 && (medical.injuryId == null || medical.clearedForFullTraining)) {
            val injury = state.injuryHistory.firstOrNull()
            medical = medical.copy(
                injuryId = "INJURY:${injury?.id ?: "${state.currentDayIndex}:${state.lastMatchReport?.fixtureId ?: "training"}"}",
                diagnosis = injury?.injuryType ?: "Sportif kas zorlanması",
                diagnosed = false, recoveryStartedDay = state.currentDayIndex, lastRecoveryDay = state.currentDayIndex,
                initialRecoveryDays = state.player.injuryDaysRemaining, rehabilitationProgress = 0, clearedForFullTraining = false
            )
        }
        world = world.copy(characters = characters, medical = medical)
        val tasks = world.tasks.map { task ->
            val transfer = world.transfers.find { it.offerId == task.entityId }
            when {
                task.status == TaskStatus.LOCKED && task.dayIndex <= state.currentDayIndex -> task.copy(status = TaskStatus.AVAILABLE)
                !task.isActive -> task
                task.action == "TRANSFER" && transfer?.stage in setOf(TransferStage.MEDICAL, TransferStage.CONTRACT_SIGNING, TransferStage.REGISTRATION, TransferStage.REGISTRATION_BLOCKED, TransferStage.COMPLETED) -> task.copy(
                    status = TaskStatus.COMPLETED, completedDayIndex = state.currentDayIndex, outcome = "Teklif şartlarında anlaşıldı; sonraki transfer aşamasına geçildi.")
                task.action == "TRANSFER_MEDICAL" && transfer?.medicalPassed == true -> task.copy(
                    status = TaskStatus.COMPLETED, completedDayIndex = state.currentDayIndex, outcome = "Sağlık kontrolü onaylandı.")
                task.action == "TRANSFER_SIGNING" && transfer?.stage == TransferStage.COMPLETED -> task.copy(
                    status = TaskStatus.COMPLETED, completedDayIndex = state.currentDayIndex, outcome = "Sözleşme ve tescil tamamlandı.")
                task.action == "TRAINING" && !task.id.startsWith("TASK:TRAINING:${state.contract.clubId}:") -> task.copy(status = TaskStatus.CANCELLED, outcome = "Kulüp değişikliği nedeniyle program yenilendi.")
                task.action == "DILEMMA" && state.activeDilemma?.id != task.entityId -> task.copy(status = TaskStatus.CANCELLED, outcome = "Görüşme artık geçerli değil.")
                task.action == "TRAINING" && task.dayIndex == state.currentDayIndex && state.trainedToday -> task.copy(
                    status = TaskStatus.COMPLETED, completedDayIndex = state.currentDayIndex,
                    outcome = "Antrenman sonucu ve oyuncu gelişimi kaydedildi.", rewardSummary = "Gelişim antrenman motorunda uygulandı."
                )
                task.action == "TRAINING" && task.dayIndex == state.currentDayIndex && !state.trainedToday -> task.copy(
                    title = if (state.player.injuryDaysRemaining > 0) "Rehabilitasyon seansına katıl" else "Bugünkü antrenmana katıl")
                task.action == "MATCH" && state.fixtures.any { it.id == task.entityId && it.played } -> task.copy(
                    status = TaskStatus.COMPLETED, completedDayIndex = state.currentDayIndex, outcome = "Maç sonucu kaydedildi."
                )
                task.action == "PRESS" && task.entityId != null && state.pressCompletedForLastMatch && task.entityId == state.lastMatchReport?.fixtureId -> task.copy(
                    status = TaskStatus.COMPLETED, completedDayIndex = state.currentDayIndex, outcome = "Basın açıklaması kaydedildi."
                )
                task.expiresDayIndex != null && state.currentDayIndex > task.expiresDayIndex -> task.copy(status = TaskStatus.EXPIRED, outcome = "Randevu süresi doldu.")
                task.action == "MEDICAL" && state.player.injuryDaysRemaining == 0 -> task.copy(status = TaskStatus.CANCELLED, outcome = "Sakatlık dönemi sona erdi.")
                task.action == "TRANSFER" && state.transferOffers.none { it.id == task.entityId } -> task.copy(status = TaskStatus.CANCELLED, outcome = "Teklif artık geçerli değil.")
                else -> task
            }
        }.toMutableList()
        val candidates = mutableListOf<WorldTask>()
        fun candidate(id: String, title: String, action: String, type: SceneType?, npc: String?, entity: String? = null, expiry: Int? = null) {
            candidates += WorldTask(id, "SOURCE:$id", title, state.currentDayIndex, action, sceneType = type, npcId = npc, entityId = entity, expiresDayIndex = expiry)
        }
        if (state.player.injuryDaysRemaining > 0 && !medical.diagnosed) candidate("TASK:${medical.injuryId}", "Kulüp doktoruna görün", "MEDICAL", SceneType.MEDICAL, "NPC_DOCTOR", medical.injuryId)
        state.todayUserFixture?.let { fixture ->
            candidate("TASK:MATCH:${fixture.id}", "${fixture.homeClubName} – ${fixture.awayClubName}", "MATCH", SceneType.PRE_MATCH, "NPC_MANAGER", fixture.id)
        }
        if (state.todayUserFixture == null) candidate(
            "TASK:TRAINING:${state.contract.clubId}:${state.currentDayIndex}",
            if (state.player.injuryDaysRemaining > 0) "Rehabilitasyon seansına katıl" else "Bugünkü antrenmana katıl",
            "TRAINING", null, "NPC_MANAGER", expiry = state.currentDayIndex
        )
        state.lastMatchReport?.let { report ->
            if (!state.pressCompletedForLastMatch && (report.userRating >= 7.0 || report.userGoals > 0 || report.userRating < 6.0)) candidate(
                "TASK:PRESS:${report.fixtureId}", "Maç sonrası basın toplantısı", "PRESS", SceneType.PRESS, "NPC_PRESS", report.fixtureId, state.currentDayIndex + 2
            )
            candidate("TASK:POST:${report.fixtureId}", "Soyunma odasında maç değerlendirmesi", "POST_MATCH", SceneType.POST_MATCH, "NPC_MANAGER", report.fixtureId, state.currentDayIndex + 1)
        }
        state.transferOffers.filter { it.expiresDayIndex >= state.currentDayIndex }.forEach { offer ->
            if (world.transfers.none { it.offerId == offer.id && it.stage in terminalTransferStages }) candidate(
                "TASK:TRANSFER:${offer.id}", "${offer.clubName} teklifini menajerinle görüş", "TRANSFER", SceneType.TRANSFER, "NPC_AGENT", offer.id, offer.expiresDayIndex
            )
        }
        if (state.trainedToday && (state.currentDayIndex == 1 || state.managerTrust < 40 || state.currentDayIndex % 7 == 0)) candidate(
            "TASK:COACH:${state.contract.clubId}:${state.currentDayIndex}", "Antrenman sonrası hocanın odasına uğra", "COACH", SceneType.COACH, "NPC_MANAGER", expiry = state.currentDayIndex + 2
        )
        if (state.currentDayIndex % 7 == 4 && state.player.morale < 85 && state.todayUserFixture == null) candidate(
            "TASK:SOCIAL:${state.currentDayIndex}", "Ailenle akşam yemeği", "SOCIAL", SceneType.SOCIAL, "NPC_FAMILY", expiry = state.currentDayIndex + 1
        )
        state.activeDilemma?.let { dilemma ->
            val sceneType = when (dilemma.sceneType) { "MEDICAL_CHECKUP" -> SceneType.MEDICAL; "TRANSFER_NEGOTIATION" -> SceneType.TRANSFER; "LOCKER_ROOM" -> SceneType.POST_MATCH; else -> SceneType.COACH }
            val npc = when (sceneType) { SceneType.MEDICAL -> "NPC_DOCTOR"; SceneType.TRANSFER -> "NPC_AGENT"; else -> "NPC_MANAGER" }
            candidate("TASK:DILEMMA:${dilemma.id}", dilemma.title, "DILEMMA", sceneType, npc, dilemma.id)
        }
        var newCinematics = tasks.count { it.dayIndex == state.currentDayIndex && it.sceneType != null && it.action != "MATCH" }
        val notifications = world.notifications.toMutableList()
        val calendar = state.calendarEvents.toMutableList()
        val npcs = state.npcs.toMutableList()
        val calls = state.callRecords.toMutableList()
        candidates.forEach { task ->
            if (tasks.any { it.id == task.id }) return@forEach
            if (task.sceneType != null && task.action != "MATCH") {
                if (newCinematics >= 3 || tasks.count { it.isActive && it.sceneType != null } >= 6) return@forEach
                newCinematics++
            }
            tasks += if (task.action == "TRAINING" && state.trainedToday) task.copy(status = TaskStatus.COMPLETED,
                completedDayIndex = state.currentDayIndex, outcome = "Antrenman sonucu kaydedildi.") else task
            if (task.action == "TRAINING" && state.trainedToday) return@forEach
            notifications += WorldNotification("NOTICE:${task.id}", task.sourceEventId, state.currentDayIndex, task.title, task.id)
            if (task.sceneType != null && task.action != "MATCH") {
                calendar += CalendarEvent("CAL:${task.id}", task.dayIndex, GameClock.full(task.dayIndex, state.currentSeasonYear), "${state.currentHour.toString().padStart(2, '0')}:00",
                    task.sceneType.environment, task.action, task.title, "Görüşme: ${task.sceneType.title}")
                val idx = npcs.indexOfFirst { it.id == task.npcId }
                if (idx >= 0) {
                    val npc = npcs[idx]
                    val message = ChatMessage("MSG:${task.id}", npc.name, false, "${state.player.firstName}, ${task.title.lowercase()}. Takvimine randevu ekledim.", state.currentDayIndex, state.formattedClockTime)
                    npcs[idx] = npc.copy(messages = npc.messages + message, unreadCount = npc.unreadCount + 1)
                    if (task.action in setOf("TRANSFER", "SOCIAL")) calls += PhoneCallRecord(
                        "CALL:${task.id}", npc.id, npc.name, npc.roleTitle, state.currentDayIndex,
                        GameClock.short(state.currentDayIndex, state.currentSeasonYear), task.title,
                        "${state.player.firstName}, ${task.title.lowercase()}. Uygun olduğunda görüşebiliriz.",
                        listOf("Görüşmeye katıl", "Sonra ara", "Reddet"), linkedTaskId = task.id)
                }
            }
        }
        val activeIds = tasks.filter { it.isActive }.map { it.id }.toSet()
        val activeScene = world.activeScene?.takeIf { it.taskId in activeIds || it.status == SceneStatus.COMPLETED }
        val syncedCalendar = calendar.distinctBy { it.id }.map { event ->
            val task = tasks.firstOrNull { "CAL:${it.id}" == event.id }
            if (task != null && !task.isActive) event.copy(completed = true, outcomeSummary = task.outcome) else event
        }
        val completedToLog = tasks.filter { it.status == TaskStatus.COMPLETED && "COMPLETE:${it.id}" !in world.appliedEventIds }
        state = state.copy(saveVersion = maxOf(4, state.saveVersion), calendarEvents = syncedCalendar, npcs = npcs,
            callRecords = calls.distinctBy { it.id }.map { call -> if (call.linkedTaskId != null && call.linkedTaskId !in activeIds) call.copy(completed = true) else call },
            eventHistory = completedToLog.map { GameEvent("COMPLETE:${it.id}", it.completedDayIndex ?: state.currentDayIndex, 0, "TASK_COMPLETED", it.title, it.outcome) } + state.eventHistory,
            livingWorld = world.copy(tasks = tasks, notifications = notifications.distinctBy { it.id }, activeScene = activeScene,
                appliedEventIds = world.appliedEventIds + completedToLog.map { "COMPLETE:${it.id}" }))
        return state
    }

    fun markNotification(state: CareerSaveState, id: String, status: NotificationStatus): CareerSaveState = state.copy(
        livingWorld = state.livingWorld.copy(notifications = state.livingWorld.notifications.map { if (it.id == id) it.copy(status = status) else it })
    )

    fun startScene(input: CareerSaveState, taskId: String): CareerSaveState {
        val state = reconcile(input)
        val task = state.livingWorld.tasks.find { it.id == taskId && it.isActive } ?: return state
        val type = task.sceneType ?: return state
        val active = state.livingWorld.activeScene
        if (active != null && active.status !in setOf(SceneStatus.COMPLETED, SceneStatus.SUSPENDED) && active.taskId != taskId) return state
        val character = state.livingWorld.characters.lastOrNull { it.npcId == task.npcId && (it.clubId == null || it.clubId == state.contract.clubId) } ?: return state
        val checkpoint = active?.takeIf { it.taskId == taskId }
            ?: state.livingWorld.suspendedScenes.lastOrNull { it.taskId == taskId }
            ?: state.livingWorld.sceneHistory.lastOrNull { it.taskId == taskId && it.status == SceneStatus.COMPLETED }
            ?: SceneCheckpoint(
            "SCENE:$taskId", taskId, type, task.sourceEventId, type.environment, character.id
        )
        return state.copy(livingWorld = state.livingWorld.copy(
            activeScene = checkpoint.copy(status = if (checkpoint.status == SceneStatus.COMPLETED) SceneStatus.COMPLETED
                else if (checkpoint.presentationSkipped) SceneStatus.AWAITING_CHOICE else SceneStatus.ENTERING),
            suspendedScenes = state.livingWorld.suspendedScenes.filterNot { it.taskId == taskId } +
                listOfNotNull(active?.takeIf { it.status == SceneStatus.SUSPENDED && it.taskId != taskId }),
            tasks = state.livingWorld.tasks.map { if (it.id == taskId) it.copy(status = TaskStatus.ACTIVE) else it },
            notifications = state.livingWorld.notifications.map { if (it.taskId == taskId) it.copy(status = NotificationStatus.READ) else it }
        ))
    }

    fun advanceScene(state: CareerSaveState, skip: Boolean = false): CareerSaveState {
        val scene = state.livingWorld.activeScene ?: return state
        if (scene.status in listOf(SceneStatus.COMPLETED, SceneStatus.RESOLVING, SceneStatus.EXITING, SceneStatus.AWAITING_CHOICE)) return state
        return state.copy(livingWorld = state.livingWorld.copy(activeScene = scene.copy(
            status = if (skip || scene.dialogueIndex >= 1) SceneStatus.AWAITING_CHOICE else SceneStatus.PLAYING,
            dialogueIndex = if (skip) 2 else scene.dialogueIndex + 1, presentationSkipped = skip || scene.presentationSkipped
        )))
    }

    fun suspendScene(state: CareerSaveState): CareerSaveState {
        val scene = state.livingWorld.activeScene ?: return state
        if (scene.status == SceneStatus.COMPLETED) return dismissCompletedScene(state)
        return state.copy(livingWorld = state.livingWorld.copy(activeScene = null,
            suspendedScenes = state.livingWorld.suspendedScenes.filterNot { it.sceneId == scene.sceneId } + scene.copy(status = SceneStatus.SUSPENDED)))
    }

    fun dismissCompletedScene(state: CareerSaveState): CareerSaveState {
        val scene = state.livingWorld.activeScene ?: return state
        if (scene.status != SceneStatus.COMPLETED) return state
        return state.copy(livingWorld = state.livingWorld.copy(activeScene = null))
    }

    fun finishTask(state: CareerSaveState, taskId: String, outcome: String, reward: String = ""): CareerSaveState {
        val task = state.livingWorld.tasks.find { it.id == taskId && it.isActive } ?: return state
        val eventId = "COMPLETE:$taskId"
        if (eventId in state.livingWorld.appliedEventIds) return state
        return state.copy(
            calendarEvents = state.calendarEvents.map { if (it.id == "CAL:$taskId") it.copy(completed = true, outcomeSummary = outcome) else it },
            eventHistory = listOf(GameEvent(eventId, state.currentDayIndex, 0, "TASK_COMPLETED", task.title, outcome)) + state.eventHistory,
            livingWorld = state.livingWorld.copy(
                tasks = state.livingWorld.tasks.map { if (it.id == taskId) it.copy(status = TaskStatus.COMPLETED, completedDayIndex = state.currentDayIndex, outcome = outcome, rewardSummary = reward) else it },
                appliedEventIds = state.livingWorld.appliedEventIds + eventId
            )
        )
    }

    fun remember(state: CareerSaveState, memory: NpcMemory): CareerSaveState {
        if (state.livingWorld.memories.any { it.id == memory.id }) return state
        return state.copy(livingWorld = state.livingWorld.copy(memories = state.livingWorld.memories + memory))
    }

    val terminalTransferStages = setOf(TransferStage.COMPLETED, TransferStage.REJECTED, TransferStage.WITHDRAWN, TransferStage.EXPIRED, TransferStage.NEGOTIATION_FAILED, TransferStage.MEDICAL_FAILED)
}
