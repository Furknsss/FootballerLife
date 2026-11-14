package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.engine.GameClock
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun WorldAgenda(state: CareerSaveState, onOpenTask: (String) -> Unit, onReadNotification: (String) -> Unit) {
    var history by remember { mutableStateOf(false) }
    val active = state.livingWorld.tasks.filter { it.isActive && it.dayIndex <= state.currentDayIndex }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("GÜNLÜK PROGRAM • ${active.size} AKTİF", color = AccentBright)
        if (active.isEmpty()) Text("Bugünkü programın tamamlandı. Takvimi ilerleterek yeni gelişmeleri takip edebilirsin.", color = TextSecondary)
        active.forEach { task ->
            OutlinedButton(onClick = { onOpenTask(task.id) }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("task_${task.action.lowercase()}")) {
                Column(Modifier.fillMaxWidth()) {
                    Text(task.title, color = TextPrimary)
                    Text(if (task.status == TaskStatus.ACTIVE) "Görüşmeye devam et" else "${GameClock.short(task.dayIndex, state.currentSeasonYear)} • ${task.sceneType?.title ?: "Antrenman"}", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        val unread = state.livingWorld.notifications.filter { it.status == NotificationStatus.UNREAD }
        if (unread.isNotEmpty()) Text("BİLDİRİMLER • ${unread.size} OKUNMAMIŞ", color = GoldPrimary)
        unread.takeLast(5).reversed().forEach { notice ->
            TextButton(onClick = { onReadNotification(notice.id) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("notice_${notice.id}")) {
                Text(notice.title, color = TextSecondary)
            }
        }
        TextButton(onClick = { history = !history }, modifier = Modifier.testTag("task_history_toggle")) { Text(if (history) "Geçmişi kapat" else "Görev ve görüşme geçmişi", color = AccentBright) }
        if (history) {
            state.livingWorld.tasks.filter { !it.isActive && it.status != TaskStatus.LOCKED }.takeLast(30).reversed().forEach { task ->
                Text("${GameClock.short(task.completedDayIndex ?: task.dayIndex, state.currentSeasonYear)} • ${task.title}\n${task.status.name}: ${task.outcome}\n${task.rewardSummary}", color = TextSecondary, modifier = Modifier.padding(vertical = 6.dp))
            }
        }
    }
}
