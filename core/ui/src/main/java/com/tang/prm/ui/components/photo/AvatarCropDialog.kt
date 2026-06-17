package com.tang.prm.ui.components.photo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tang.prm.ui.theme.Primary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun AvatarCropDialog(
    imagePath: String,
    onCropComplete: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 降采样加载预览图（避免大图卡顿）
    val previewBitmap = remember(imagePath) {
        loadSampledBitmap(imagePath, 800)
    }
    val imageBitmap = remember(previewBitmap) { previewBitmap?.asImageBitmap() }

    // Fix 1: Dialog 退出时回收 previewBitmap，避免内存泄漏
    DisposableEffect(previewBitmap) {
        onDispose {
            previewBitmap?.recycle()
        }
    }

    // 变换状态
    // Fix 5: 使用 rememberSaveable 保存手势状态，旋转配置变更后不丢失
    var userScale by rememberSaveable { mutableFloatStateOf(1f) }
    var userOffsetX by rememberSaveable { mutableFloatStateOf(0f) }
    var userOffsetY by rememberSaveable { mutableFloatStateOf(0f) }
    var canvasPixelSize by remember { mutableFloatStateOf(0f) }

    // Fix 4: 用 LaunchedEffect 包装 onCropComplete，避免在组合阶段触发状态变更导致递归重组
    if (imageBitmap == null) {
        LaunchedEffect(imagePath) {
            onCropComplete(imagePath)
        }
        return
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "裁剪头像",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "拖动调整位置，双指缩放大小",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF111111))
                ) {
                    val bitmapWidth = imageBitmap.width
                    val bitmapHeight = imageBitmap.height

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .onGloballyPositioned { coordinates ->
                                // Fix 3: 在布局阶段获取尺寸，避免在 draw-phase 写入状态导致重组→重绘循环
                                canvasPixelSize = coordinates.size.width.toFloat()
                            }
                            .pointerInput(Unit) {
                                detectDragGestures { _, dragAmount ->
                                    userOffsetX += dragAmount.x
                                    userOffsetY += dragAmount.y
                                }
                            }
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    userScale = (userScale * zoom).coerceIn(0.3f, 5f)
                                    userOffsetX += pan.x
                                    userOffsetY += pan.y
                                }
                            }
                    ) {
                        val canvasSize = size

                        // 裁剪圆占满整个 Canvas（直径 = Canvas 宽度）
                        val circleRadius = canvasSize.width / 2f
                        val circleCenter = Offset(canvasSize.width / 2f, canvasSize.height / 2f)

                        // 基础缩放：让图片短边 = Canvas 宽度（= 圆直径）
                        val baseScale = canvasSize.width / minOf(bitmapWidth, bitmapHeight)
                        val totalScale = baseScale * userScale
                        val drawWidth = bitmapWidth * totalScale
                        val drawHeight = bitmapHeight * totalScale
                        val drawLeft = circleCenter.x - drawWidth / 2f + userOffsetX
                        val drawTop = circleCenter.y - drawHeight / 2f + userOffsetY

                        drawImage(
                            image = imageBitmap,
                            dstSize = IntSize(drawWidth.toInt(), drawHeight.toInt()),
                            dstOffset = IntOffset(drawLeft.toInt(), drawTop.toInt())
                        )

                        // 半透明遮罩（4个矩形）
                        val maskColor = Color.Black.copy(alpha = 0.65f)
                        drawRect(maskColor, topLeft = Offset.Zero, size = Size(canvasSize.width, circleCenter.y - circleRadius))
                        drawRect(maskColor, topLeft = Offset(0f, circleCenter.y + circleRadius), size = Size(canvasSize.width, canvasSize.height - circleCenter.y - circleRadius))
                        drawRect(maskColor, topLeft = Offset(0f, circleCenter.y - circleRadius), size = Size(circleCenter.x - circleRadius, circleRadius * 2f))
                        drawRect(maskColor, topLeft = Offset(circleCenter.x + circleRadius, circleCenter.y - circleRadius), size = Size(canvasSize.width - circleCenter.x - circleRadius, circleRadius * 2f))

                        drawCircle(
                            color = Color.White.copy(alpha = 0.6f),
                            radius = circleRadius,
                            center = circleCenter,
                            style = Stroke(width = 2.5f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            userScale = 1f
                            userOffsetX = 0f
                            userOffsetY = 0f
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("重置")
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("取消")
                        }
                        Button(
                            onClick = {
                                scope.launch {
                                    val croppedPath = cropAvatar(
                                        context = context,
                                        imagePath = imagePath,
                                        userScale = userScale,
                                        userOffsetX = userOffsetX,
                                        userOffsetY = userOffsetY,
                                        canvasPixelSize = canvasPixelSize
                                    )
                                    if (croppedPath != null) {
                                        onCropComplete(croppedPath)
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Text("确认", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

/** 降采样加载图片，避免大图卡顿 */
private fun loadSampledBitmap(path: String, reqSize: Int): Bitmap? {
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, options)
    val sampleSize = calculateInSampleSize(options, reqSize, reqSize)
    val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    return BitmapFactory.decodeFile(path, decodeOptions)
}

private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val (height, width) = options.outHeight to options.outWidth
    var inSampleSize = 1
    if (height > reqHeight || width > reqWidth) {
        val halfHeight = height / 2
        val halfWidth = width / 2
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}

private suspend fun cropAvatar(
    context: android.content.Context,
    imagePath: String,
    userScale: Float,
    userOffsetX: Float,
    userOffsetY: Float,
    canvasPixelSize: Float
): String? = withContext(Dispatchers.IO) {
    // Fix 2: 将 bitmap 声明移到 try 外部，以便 catch 中也能回收
    var sourceBitmap: Bitmap? = null
    var outputBitmap: Bitmap? = null
    try {
        sourceBitmap = BitmapFactory.decodeFile(imagePath) ?: return@withContext null
        val sourceWidth = sourceBitmap.width
        val sourceHeight = sourceBitmap.height

        // 输出 512x512 正方形，裁剪圆占满整个输出区域
        val outputSize = 512
        outputBitmap = Bitmap.createBitmap(outputSize, outputSize, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(outputBitmap)

        // 裁剪圆直径 = 输出尺寸（圆占满整个输出图）
        val circleDiameter = outputSize.toFloat()

        // 基础缩放：让图片短边 = 圆直径 = 输出尺寸
        val baseScale = circleDiameter / minOf(sourceWidth, sourceHeight)
        val totalScale = baseScale * userScale

        // 将 Canvas 像素偏移量按比例缩放到输出图尺寸
        val scaleFactor = if (canvasPixelSize > 0f) outputSize / canvasPixelSize else 1f
        val scaledOffsetX = userOffsetX * scaleFactor
        val scaledOffsetY = userOffsetY * scaleFactor

        val matrix = Matrix()
        matrix.postScale(totalScale, totalScale)
        val drawWidth = sourceWidth * totalScale
        val drawHeight = sourceHeight * totalScale
        // 圆心在输出图中心
        val centerX = outputSize / 2f
        val centerY = outputSize / 2f
        matrix.postTranslate(
            centerX - drawWidth / 2f + scaledOffsetX,
            centerY - drawHeight / 2f + scaledOffsetY
        )

        canvas.drawBitmap(sourceBitmap, matrix, null)

        val imagesDir = File(context.filesDir, "app_images")
        if (!imagesDir.exists()) imagesDir.mkdirs()
        val outputFile = File(imagesDir, "avatar_cropped_${System.currentTimeMillis()}.jpg")
        outputFile.outputStream().use { fos ->
            outputBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
        }

        sourceBitmap.recycle()
        outputBitmap.recycle()

        outputFile.absolutePath
    } catch (e: Exception) {
        // Fix 2: catch 中也回收 bitmap，避免异常时内存泄漏
        sourceBitmap?.recycle()
        outputBitmap?.recycle()
        null
    }
}
