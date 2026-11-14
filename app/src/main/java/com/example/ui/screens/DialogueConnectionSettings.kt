package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

@Composable
fun DialogueConnectionSettings(onConnect: (String, String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var endpoint by remember { mutableStateOf("") }
    var session by remember { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(onClick = { expanded = !expanded }) { Text("Çevrimiçi karakter konuşmaları", color = GoldPrimary) }
        if (expanded) {
            Text("Yerel konuşmalar bağlantısız çalışır. İsteğe bağlı çevrimiçi hizmet için kendi diyalog sunucunu ve geçici oturum kodunu kullan. Oturum kodu cihazda kaydedilmez.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            OutlinedTextField(endpoint, { endpoint = it }, label = { Text("Diyalog sunucusu (HTTPS)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(session, { session = it }, label = { Text("Geçici oturum kodu") }, modifier = Modifier.fillMaxWidth(), singleLine = true, visualTransformation = PasswordVisualTransformation())
            Button(onClick = { onConnect(endpoint, session); session = "" }, enabled = endpoint.startsWith("https://") && session.isNotBlank()) { Text("Bu oturumda kullan") }
            TextButton(onClick = { onConnect("", ""); endpoint = ""; session = "" }) { Text("Yerel konuşmalara dön") }
        }
    }
}
