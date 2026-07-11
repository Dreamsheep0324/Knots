package com.tang.prm.ui.components.photo

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

data class PhotoPickerConfig(
    val maxCount: Int = 1,
    val prefix: String = "img"
)

data class PhotoPickerResult(
    val localPaths: List<String>,
    val failedCount: Int = 0
)

class ManagedPhotoPickerLauncher(private val onLaunch: () -> Unit) {
    fun launch() = onLaunch()
}

@Composable
fun rememberPhotoPickerLauncher(
    config: PhotoPickerConfig,
    onResult: (PhotoPickerResult) -> Unit
): ManagedPhotoPickerLauncher {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val multiPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri> ->
        scope.launch {
            val result = processUris(context, uris, config.prefix)
            onResult(result)
        }
    }

    val singlePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val result = processUris(context, listOf(uri), config.prefix)
                onResult(result)
            }
        }
    }

    return remember(config) {
        ManagedPhotoPickerLauncher(
            onLaunch = {
                if (config.maxCount == 1) {
                    singlePickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                } else {
                    multiPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            }
        )
    }
}

private const val IMAGES_DIR = "app_images"

private suspend fun processUris(
    context: Context,
    uris: List<Uri>,
    prefix: String
): PhotoPickerResult = withContext(Dispatchers.IO) {
    val dir = File(context.filesDir, IMAGES_DIR)
    if (!dir.exists()) dir.mkdirs()
    var failedCount = 0
    val localPaths = uris.mapNotNull { uri ->
        try {
            val fileName = "${prefix}_${UUID.randomUUID()}.jpg"
            val file = File(dir, fileName)
            context.contentResolver.openInputStream(uri)?.use { stream ->
                FileOutputStream(file).use { out -> stream.copyTo(out) }
            } ?: run { failedCount++; return@mapNotNull null }
            file.absolutePath
        } catch (_: Exception) {
            failedCount++
            null
        }
    }
    PhotoPickerResult(localPaths = localPaths, failedCount = failedCount)
}
