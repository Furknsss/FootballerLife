package com.example.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.datapack.DataPackRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Loads only the visible entity's small decoded image; every missing/bad asset gets the same stable icon. */
@Composable
fun DataPackMediaImage(
    repository: DataPackRepository,
    packId: String,
    module: String,
    internalId: String,
    kind: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    fallbackIcon: ImageVector = if (module.equals("PlayerPortraits", true)) Icons.Default.Person else Icons.Default.SportsSoccer
) {
    val context = LocalContext.current
    val bitmap = produceState<android.graphics.Bitmap?>(null, repository, packId, module, internalId, kind) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                repository.openMedia(packId, module, internalId, kind)?.use { BitmapFactory.decodeStream(it, null, bounds) }
                if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@runCatching null
                val targetPx = (96 * context.resources.displayMetrics.density).toInt().coerceAtLeast(1)
                var sample = 1
                while (bounds.outWidth / (sample * 2) >= targetPx && bounds.outHeight / (sample * 2) >= targetPx) sample *= 2
                val options = BitmapFactory.Options().apply { inSampleSize = sample; inPreferredConfig = android.graphics.Bitmap.Config.RGB_565 }
                repository.openMedia(packId, module, internalId, kind)?.use { BitmapFactory.decodeStream(it, null, options) }
            }.getOrNull()
        }
    }
    Box(modifier = modifier.semantics { this.contentDescription = contentDescription.orEmpty() }, contentAlignment = Alignment.Center) {
        val image = bitmap.value
        if (image != null) Image(image.asImageBitmap(), contentDescription, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        else Icon(fallbackIcon, contentDescription, Modifier.fillMaxSize(.58f), tint = androidx.compose.ui.graphics.Color.White)
    }
}

