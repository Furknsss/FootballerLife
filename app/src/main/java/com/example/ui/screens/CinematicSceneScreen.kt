package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.engine.*
import com.example.model.*
import com.example.ui.components.parseHexColor
import com.example.ui.theme.BarlowCondensedFontFamily
import com.example.ui.theme.ManropeFontFamily

private val CinemaInk = Color(0xFF101813)
private val CinemaPaper = Color(0xFFF3EFE4)
private val CinemaGold = Color(0xFFD7BD83)

@Composable
fun CinematicSceneScreen(
    state: CareerSaveState,
    scene: SceneCheckpoint,
    onAdvance: () -> Unit,
    onSkip: () -> Unit,
    onSuspend: () -> Unit,
    onMessage: (String) -> Unit,
    onChoose: (String) -> Unit,
    onContinue: () -> Unit,
    onNegotiate: (ContractTerms) -> Unit
) {
    val definition = remember(state, scene) { CinematicEngine.definition(state, scene) }
    val character = state.livingWorld.characters.find { it.id == scene.characterId }
    val task = state.livingWorld.tasks.find { it.id == scene.taskId }
    var text by remember(scene.sceneId) { mutableStateOf("") }
    val finished = scene.status == SceneStatus.COMPLETED
    Column(Modifier.fillMaxSize().background(CinemaInk).windowInsetsPadding(WindowInsets.safeDrawing).imePadding().testTag("cinematic_screen")) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("FOOTBALL LIFE • ${state.formattedClockTime}", fontFamily = BarlowCondensedFontFamily, fontSize = 13.sp, color = CinemaGold)
                Text(scene.sceneType.title, fontFamily = BarlowCondensedFontFamily, fontWeight = FontWeight.Bold, fontSize = 25.sp, color = CinemaPaper)
            }
            if (!finished) {
                TextButton(onClick = onSkip, modifier = Modifier.testTag("scene_skip")) { Text("Özete geç", color = CinemaPaper) }
                TextButton(onClick = onSuspend, modifier = Modifier.testTag("scene_suspend")) { Text("Sonra", color = CinemaGold) }
            }
        }
        if (!scene.presentationSkipped) CinematicStage(state, scene, definition, Modifier.fillMaxWidth().heightIn(min = 150.dp, max = 280.dp).aspectRatio(1.6f))
        LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 18.dp).testTag("scene_dialogue"), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(vertical = 18.dp)) {
            item {
                Text(character?.name ?: "Görüşme", fontFamily = BarlowCondensedFontFamily, color = CinemaGold, fontSize = 19.sp)
                Text(if (finished) scene.outcome else scene.transcript.lastOrNull { !it.isFromUser }?.text
                    ?: definition.dialogueNodes[scene.dialogueIndex.coerceAtMost(definition.dialogueNodes.lastIndex)],
                    fontFamily = ManropeFontFamily, color = CinemaPaper, fontSize = 17.sp, lineHeight = 26.sp)
            }
            scene.transcript.lastOrNull { it.isFromUser }?.let { message -> item {
                Surface(color = Color(0xFF27352B), shape = RoundedCornerShape(12.dp)) {
                    Text("${state.player.firstName}: ${message.text}", Modifier.padding(14.dp), color = CinemaPaper, fontSize = 15.sp)
                }
            } }
            if (!finished && scene.status != SceneStatus.AWAITING_CHOICE) item {
                Button(onClick = onAdvance, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("scene_advance")) { Text("Diyaloğu ilerlet") }
            }
            if (!finished && scene.status == SceneStatus.AWAITING_CHOICE) {
                definition.availableChoices.forEach { choice -> item(key = choice.id) {
                    OutlinedButton(onClick = { onChoose(choice.id) }, enabled = choice.enabled,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("scene_choice_${choice.id}"),
                        shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, CinemaGold.copy(alpha = .5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CinemaPaper, disabledContentColor = CinemaPaper.copy(alpha = .4f))) {
                        Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                            Text(choice.label, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            Text(choice.consequence, fontSize = 12.sp, lineHeight = 18.sp, color = CinemaGold)
                        }
                    }
                } }
                if (scene.sceneType == SceneType.TRANSFER) state.transferOffers.find { it.id == task?.entityId }?.let { offer ->
                    item { NegotiationEditor(TransferEngine.process(state, offer), onNegotiate) }
                }
            }
            if (finished) item {
                Button(onClick = onContinue, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("scene_continue")) {
                    Text(if (scene.sceneType == SceneType.PRE_MATCH) "Maça geç" else "Hayatına dön")
                }
            }
            item { Text("Kurgusal oyun diyaloğu • ${if (scene.sceneType == SceneType.MEDICAL) "Sağlık simülasyonu" else if (scene.transcript.lastOrNull()?.dialogueSource == "GEMINI") "Çevrimiçi karakter diyaloğu" else "Yerel bağlamsal diyalog"}", color = CinemaGold.copy(alpha = .75f), fontSize = 11.sp) }
        }
        if (!finished) Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(value = text, onValueChange = { text = it.take(2000) }, label = { Text("Kendi cümleni yaz") },
                modifier = Modifier.weight(1f).testTag("scene_message_input"), maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = CinemaPaper, unfocusedTextColor = CinemaPaper, focusedLabelColor = CinemaGold, unfocusedLabelColor = CinemaGold))
            TextButton(onClick = { onMessage(text); text = "" }, enabled = text.isNotBlank(), modifier = Modifier.heightIn(min = 48.dp).testTag("scene_send")) { Text("Söyle", color = CinemaGold) }
        }
    }
}

@Composable
private fun CinematicStage(state: CareerSaveState, scene: SceneCheckpoint, definition: SceneDefinition, modifier: Modifier) {
    val cue = definition.cameraSequence[scene.dialogueIndex.coerceAtMost(definition.cameraSequence.lastIndex)]
    val zoom by animateFloatAsState(cue.zoom, tween(cue.durationMs, easing = FastOutSlowInEasing), label = "Camera zoom")
    val pan by animateFloatAsState(cue.panX, tween(cue.durationMs), label = "Camera pan")
    val idle = rememberInfiniteTransition(label = "Character breathing")
    val breath by idle.animateFloat(0f, 3f, infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Reverse), label = "Breathing")
    val speaking by idle.animateFloat(-.5f, .5f, infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "Speaking gesture")
    val entry = remember(scene.sceneId) { Animatable(0f) }
    LaunchedEffect(scene.sceneId) { entry.animateTo(1f, tween(900, easing = FastOutSlowInEasing)) }
    val background = when (scene.sceneType) {
        SceneType.PRE_MATCH, SceneType.POST_MATCH, SceneType.WELCOME -> R.drawable.img_locker_room
        SceneType.TRANSFER, SceneType.SIGNING -> R.drawable.img_transfer_lounge
        SceneType.MEDICAL -> R.drawable.scene_medical_room
        SceneType.PRESS -> R.drawable.scene_press_room
        SceneType.SOCIAL -> R.drawable.scene_restaurant
        else -> R.drawable.scene_coach_office
    }
    val identity = state.livingWorld.characters.find { it.id == scene.characterId }
    val npcArt = when (identity?.portraitId) {
        "CAST_DOCTOR" -> R.drawable.cast_doctor
        "CAST_AGENT" -> R.drawable.cast_agent
        "CAST_FAMILY" -> R.drawable.cast_family
        "CAST_PLAYER_DARK" -> R.drawable.cast_player_dark
        "CAST_COACH" -> R.drawable.cast_coach
        else -> when (identity?.outfit) { "MEDICAL" -> R.drawable.cast_doctor; "AGENT" -> R.drawable.cast_agent; "COACH" -> R.drawable.cast_coach; else -> R.drawable.cast_family }
    }
    val skin = parseHexColor(state.player.appearance.skinToneHex)
    val playerArt = if (skin.red * .299f + skin.green * .587f + skin.blue * .114f < .53f) R.drawable.cast_player_dark else R.drawable.cast_player_light
    val npcFacing = if (npcArt in setOf(R.drawable.cast_coach, R.drawable.cast_family, R.drawable.cast_player_dark)) -1f else 1f
    Box(modifier.clipToBounds()) {
        Image(painterResource(background), contentDescription = "${scene.sceneType.title} mekânı", contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().graphicsLayer { scaleX = zoom; scaleY = zoom; translationX = size.width * pan })
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = .05f), CinemaInk.copy(alpha = .65f)))))
        Row(Modifier.fillMaxSize().graphicsLayer { translationX = size.width * pan * 1.6f }, verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween) {
            Image(painterResource(playerArt), "${state.player.fullName} • kurgusal karakter temsili", contentScale = ContentScale.Fit,
                modifier = Modifier.weight(1f).fillMaxHeight(.98f).graphicsLayer {
                    alpha = entry.value; translationY = breath; translationX = (1 - entry.value) * -24f
                    scaleX = 1f + (zoom - 1f) * .5f; scaleY = scaleX
                })
            Image(painterResource(npcArt), "${identity?.name ?: "Görüşme karakteri"} • kurgusal karakter temsili", contentScale = ContentScale.Fit,
                modifier = Modifier.weight(1f).fillMaxHeight().graphicsLayer {
                    alpha = entry.value; translationY = -breath; rotationZ = speaking * .5f; translationX = (1 - entry.value) * 24f
                    scaleY = 1f + (zoom - 1f) * .5f; scaleX = scaleY * npcFacing
                })
        }
        Canvas(Modifier.fillMaxSize()) {
            if (scene.sceneType in setOf(SceneType.COACH, SceneType.TRANSFER, SceneType.SIGNING, SceneType.PRESS, SceneType.SOCIAL)) {
                drawRect(Color(0xFF463B2F), Offset(0f, size.height * .92f), Size(size.width, size.height * .08f))
                drawLine(Color(0xFFAB916B), Offset(0f, size.height * .92f), Offset(size.width, size.height * .92f), 3f)
            }
        }
    }
}

@Composable
private fun NegotiationEditor(process: TransferProcess, onSubmit: (ContractTerms) -> Unit) {
    var wage by remember(process.terms) { mutableStateOf(process.terms.weeklyWageEur.toString()) }
    var years by remember(process.terms) { mutableStateOf(process.terms.years.toString()) }
    var signing by remember(process.terms) { mutableStateOf(process.terms.signingBonusEur.toString()) }
    var appearance by remember(process.terms) { mutableStateOf(process.terms.appearanceBonusEur.toString()) }
    var goal by remember(process.terms) { mutableStateOf(process.terms.goalBonusEur.toString()) }
    var number by remember(process.terms) { mutableStateOf(process.terms.preferredNumber?.toString().orEmpty()) }
    var release by remember(process.terms) { mutableStateOf(process.terms.releaseClauseEur?.toString().orEmpty()) }
    var role by remember(process.terms) { mutableStateOf(process.terms.role) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("SÖZLEŞME MASASI • TUR ${process.rounds.size}/3", color = CinemaGold, fontFamily = BarlowCondensedFontFamily, fontSize = 21.sp)
        listOf(Triple("Haftalık maaş (€)", wage, { v: String -> wage = v }), Triple("Süre (yıl)", years, { v: String -> years = v }),
            Triple("İmza parası (€)", signing, { v: String -> signing = v }), Triple("Maç primi (€)", appearance, { v: String -> appearance = v }),
            Triple("Gol primi (€)", goal, { v: String -> goal = v }), Triple("Forma tercihi (isteğe bağlı)", number, { v: String -> number = v }),
            Triple("Serbest kalma (€, isteğe bağlı)", release, { v: String -> release = v })).forEach { (label, value, change) ->
            OutlinedTextField(value, { change(it.filter(Char::isDigit).take(10)) }, label = { Text(label, color = CinemaGold) },
                modifier = Modifier.fillMaxWidth(), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = CinemaPaper, unfocusedTextColor = CinemaPaper))
        }
        TextButton(onClick = { role = SquadRole.entries[(role.ordinal + 1) % SquadRole.entries.size] }) { Text("Kadro rolü: ${role.labelTr}", color = CinemaGold) }
        Button(onClick = { onSubmit(process.terms.copy(weeklyWageEur = wage.toLongOrNull() ?: 0, years = years.toIntOrNull() ?: 0,
            signingBonusEur = signing.toLongOrNull() ?: 0, appearanceBonusEur = appearance.toLongOrNull() ?: 0,
            goalBonusEur = goal.toLongOrNull() ?: 0, preferredNumber = number.toIntOrNull(), releaseClauseEur = release.toLongOrNull(), role = role)) },
            enabled = process.rounds.size < 3 && process.stage in setOf(TransferStage.FORMAL_OFFER, TransferStage.NEGOTIATION), modifier = Modifier.fillMaxWidth().testTag("negotiation_submit")) { Text("Karşı teklif gönder") }
        process.rounds.takeLast(3).forEach { Text(it.decision, color = CinemaPaper, fontSize = 14.sp) }
    }
}
