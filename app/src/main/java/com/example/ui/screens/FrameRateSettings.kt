package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ui.FrameRateController
import com.example.ui.theme.*

@Composable
fun FrameRateSettings(selected: Int, onSelect: (Int) -> Unit) {
    val context = LocalContext.current
    val rates = remember(context) { FrameRateController.activity(context)?.let(FrameRateController::supportedRates).orEmpty() }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("KARE HIZI", color = GoldPrimary)
        Text("Cihazın desteklediği hedefi seç. Grafik kalitesi ayrı ayarlanır; gerçek akıcılık cihaz yüküne ve sıcaklığına bağlıdır.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        (listOf(0) + rates).forEach { rate ->
            OutlinedButton(onClick = { onSelect(rate) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Text("${if (selected == rate) "✓ " else ""}${if (rate == 0) "Otomatik • en yüksek uygun yenileme" else "$rate Hz hedef"}", color = TextPrimary)
            }
        }
        Text("${rates.joinToString(" / ")} Hz ekran modları. Bu değerler ölçülmüş FPS değildir.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
    }
}
