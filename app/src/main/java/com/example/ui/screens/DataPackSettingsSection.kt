package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.datapack.DataPackImportReport
import com.example.datapack.InstalledDataPackEntity
import com.example.ui.theme.ChampionshipGold
import com.example.ui.theme.CrispWhiteBorder
import com.example.ui.theme.DeepStadiumGreen
import com.example.ui.theme.PrimaryWhite
import com.example.ui.theme.SecondaryMintGray
import com.example.ui.theme.VibrantFootballGreen

@Composable
fun DataPackSettingsSection(
    installed: List<InstalledDataPackEntity>,
    selectedPackKey: String?,
    latestReport: DataPackImportReport?,
    onImport: () -> Unit,
    onSelect: (String?) -> Unit
) {
    Surface(color = DeepStadiumGreen, shape = MaterialTheme.shapes.large, border = androidx.compose.foundation.BorderStroke(1.dp, CrispWhiteBorder), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("FOOTBALL WORLD DATA PACK", style = MaterialTheme.typography.titleMedium, color = ChampionshipGold)
            Text("Paketler kariyer kayıtlarından ayrı yerel Room veritabanına kurulur. Seçim yalnızca bundan sonra oluşturulacak kariyere bağlanır.", style = MaterialTheme.typography.bodySmall, color = SecondaryMintGray)
            Button(onClick = onImport) { Text("JSON / CSV / ZIP paketi içe aktar") }
            if (installed.isEmpty()) Text("Kurulu paket yok. Örnek paket veya kendi doğrulanmış DataPack dosyanı içe aktar.", color = PrimaryWhite, style = MaterialTheme.typography.bodySmall)
            installed.forEach { pack ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    RadioButton(selected = selectedPackKey == pack.packKey, onClick = { onSelect(pack.packKey) })
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${pack.packId}@${pack.version}  •  ${pack.season}", color = PrimaryWhite, style = MaterialTheme.typography.bodyMedium)
                        Text("${pack.recordCount} kayıt • ${pack.snapshotDate} • eksik veri ${(pack.missingValueRate * 100).toInt()}%", color = SecondaryMintGray, style = MaterialTheme.typography.bodySmall)
                    }
                    TextButton(onClick = { onSelect(if (selectedPackKey == pack.packKey) null else pack.packKey) }) {
                        Text(if (selectedPackKey == pack.packKey) "Yeni kariyerlerde kullanılıyor" else "Yeni kariyerlerde kullan", color = VibrantFootballGreen)
                    }
                }
            }
            if (selectedPackKey != null && installed.none { it.packKey == selectedPackKey }) {
                Text("Seçili paket bulunamadı; yeni kariyer varsayılan oyun dünyasıyla başlayacak.", color = SecondaryMintGray)
            }
            latestReport?.let { DataPackReportView(it) }
            Text("Oyuncu nitelikleri, sözleşmeler, teknik ekip, transferler ve medya yalnızca kaynak bilgisi bulunan satırlar geldiyse eklenir. Eksik görseller için oyunun tutarlı varsayılan portresi kullanılır.", color = SecondaryMintGray, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun DataPackReportView(report: DataPackImportReport) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
        Text(if (report.accepted) "PAKET DOĞRULANDI / KURULDU" else "PAKET KURULMADI", color = if (report.accepted) VibrantFootballGreen else ChampionshipGold, style = MaterialTheme.typography.titleSmall)
        Text("${report.packId ?: "Bilinmeyen paket"} • ${report.errorCount} hata • ${report.warningCount} uyarı", color = PrimaryWhite, style = MaterialTheme.typography.bodySmall)
        report.moduleReports.forEach { module ->
            Text("${module.module}: ${module.rows} satır • ${if (module.rows == 0) "veri yok" else "eksik ${(module.missingValueRate * 100).toInt()}%"} • eşleşmeyen ID ${module.unmatchedIds.size} • hata ${module.errors}", color = SecondaryMintGray, style = MaterialTheme.typography.labelSmall)
        }
        report.issues.take(8).forEach { issue ->
            Text("${issue.severity}: ${issue.path} — ${issue.message}", color = PrimaryWhite, style = MaterialTheme.typography.labelSmall)
        }
    }
}

