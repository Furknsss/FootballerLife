package com.example.model

import com.squareup.moshi.JsonClass

enum class TaskStatus { LOCKED, AVAILABLE, ACTIVE, COMPLETED, FAILED, EXPIRED, CANCELLED, ARCHIVED }
enum class NotificationStatus { UNREAD, READ, DISMISSED, ARCHIVED }
enum class SceneStatus { PENDING, READY, ENTERING, PLAYING, AWAITING_CHOICE, RESOLVING, EXITING, COMPLETED, CANCELLED, EXPIRED, INTERRUPTED, SUSPENDED, FAILED }
enum class SceneType(val title: String, val environment: String) {
    COACH("Teknik direktörün odası", "COACH_OFFICE"),
    MEDICAL("Sağlık kontrolü", "MEDICAL_ROOM"),
    TRANSFER("Menajer görüşmesi", "TRANSFER_LOUNGE"),
    PRE_MATCH("Maç öncesi", "LOCKER_ROOM"),
    POST_MATCH("Maç değerlendirmesi", "LOCKER_ROOM"),
    PRESS("Basın toplantısı", "PRESS_ROOM"),
    SOCIAL("Sosyal buluşma", "RESTAURANT"),
    SIGNING("Sözleşme imzası", "TRANSFER_LOUNGE"),
    WELCOME("Yeni kulübün", "LOCKER_ROOM")
}
enum class TransferStage { SCOUTING, INTEREST, INITIAL_CONTACT, FORMAL_OFFER, NEGOTIATION, AGREEMENT, MEDICAL, CONTRACT_SIGNING, REGISTRATION, COMPLETED, REJECTED, WITHDRAWN, EXPIRED, NEGOTIATION_FAILED, MEDICAL_FAILED, REGISTRATION_BLOCKED }
enum class RecoveryPlan { REST, PHYSIOTHERAPY, CONSERVATIVE, CONTROLLED_RETURN }

@JsonClass(generateAdapter = true)
data class WorldTask(
    val id: String,
    val sourceEventId: String,
    val title: String,
    val dayIndex: Int,
    val action: String,
    val status: TaskStatus = TaskStatus.AVAILABLE,
    val sceneType: SceneType? = null,
    val npcId: String? = null,
    val entityId: String? = null,
    val expiresDayIndex: Int? = null,
    val completedDayIndex: Int? = null,
    val outcome: String = "",
    val rewardSummary: String = ""
) {
    val isActive: Boolean get() = status == TaskStatus.AVAILABLE || status == TaskStatus.ACTIVE
}

@JsonClass(generateAdapter = true)
data class WorldNotification(
    val id: String,
    val sourceEventId: String,
    val dayIndex: Int,
    val title: String,
    val taskId: String? = null,
    val status: NotificationStatus = NotificationStatus.UNREAD
)

@JsonClass(generateAdapter = true)
data class NpcMemory(
    val id: String,
    val npcId: String,
    val dayIndex: Int,
    val subject: String,
    val text: String,
    val importance: Int = 1,
    val sourceEventId: String,
    val promiseDueDay: Int? = null,
    val promiseResolved: Boolean = false
)

@JsonClass(generateAdapter = true)
data class CharacterIdentity(
    val id: String,
    val npcId: String,
    val name: String,
    val clubId: String?,
    val role: String,
    val portraitId: String,
    val outfit: String,
    val respect: Int = 50,
    val knownEventIds: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class SceneCheckpoint(
    val sceneId: String,
    val taskId: String,
    val sceneType: SceneType,
    val triggerEventId: String,
    val environmentId: String,
    val characterId: String,
    val status: SceneStatus = SceneStatus.ENTERING,
    val dialogueIndex: Int = 0,
    val selectedChoiceId: String? = null,
    val transcript: List<ChatMessage> = emptyList(),
    val outcome: String = "",
    val presentationSkipped: Boolean = false
)

@JsonClass(generateAdapter = true)
data class MedicalState(
    val injuryId: String? = null,
    val diagnosis: String = "Sağlıklı",
    val fatigue: Int = 0,
    val trainingLoad: Int = 0,
    val matchLoad: Int = 0,
    val plan: RecoveryPlan = RecoveryPlan.REST,
    val diagnosed: Boolean = false,
    val recoveryStartedDay: Int = 0,
    val lastRecoveryDay: Int = 0,
    val initialRecoveryDays: Int = 0,
    val rehabilitationProgress: Int = 100,
    val clearedForFullTraining: Boolean = true
)

@JsonClass(generateAdapter = true)
data class ContractTerms(
    val weeklyWageEur: Long,
    val years: Int = 3,
    val signingBonusEur: Long = 0,
    val appearanceBonusEur: Long = 0,
    val goalBonusEur: Long = 0,
    val role: SquadRole = SquadRole.ROTATION,
    val preferredNumber: Int? = null,
    val releaseClauseEur: Long? = null,
    val loan: Boolean = false
)

@JsonClass(generateAdapter = true)
data class NegotiationRound(
    val dayIndex: Int,
    val requested: ContractTerms,
    val counter: ContractTerms?,
    val decision: String
)

@JsonClass(generateAdapter = true)
data class TransferProcess(
    val offerId: String,
    val stage: TransferStage = TransferStage.FORMAL_OFFER,
    val terms: ContractTerms,
    val feeEur: Long,
    val medicalPassed: Boolean = false,
    val rounds: List<NegotiationRound> = emptyList(),
    val outcome: String = "",
    val initialTerms: ContractTerms? = null
)

/** Dates below are simulation configuration, never claims about official registration dates. */
@JsonClass(generateAdapter = true)
data class RegistrationWindow(val start: String, val end: String)

@JsonClass(generateAdapter = true)
data class LeagueRegistrationRules(
    val leagueId: String,
    val seasonYear: Int,
    val windows: List<RegistrationWindow>,
    val freeAgentsOutsideWindow: Boolean = false,
    val preContractMonths: Int = 6,
    val source: String = "Football Life oyun yapılandırması; resmî takvim değildir",
    val sourceDate: String? = null
)

@JsonClass(generateAdapter = true)
data class ExternalEntityMapping(val internalId: String, val provider: String, val externalId: String, val sourceDate: String)

@JsonClass(generateAdapter = true)
data class LivingWorldState(
    val schemaVersion: Int = 1,
    val tasks: List<WorldTask> = emptyList(),
    val notifications: List<WorldNotification> = emptyList(),
    val memories: List<NpcMemory> = emptyList(),
    val characters: List<CharacterIdentity> = emptyList(),
    val activeScene: SceneCheckpoint? = null,
    val suspendedScenes: List<SceneCheckpoint> = emptyList(),
    val sceneHistory: List<SceneCheckpoint> = emptyList(),
    val appliedEventIds: Set<String> = emptySet(),
    val medical: MedicalState = MedicalState(),
    val transfers: List<TransferProcess> = emptyList(),
    val registrationRules: List<LeagueRegistrationRules> = emptyList(),
    val externalMappings: List<ExternalEntityMapping> = emptyList(),
    val dataPackSourceDate: String? = null,
    val archivedClubs: List<Club> = emptyList(),
    val archivedNpcs: List<NpcCharacter> = emptyList()
)
