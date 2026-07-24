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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import com.tang.prm.ui.theme.Dimens

/** AvatarCropDialog 专用常量 */
private const val MIN_SCALE = 0.3f
private const val MAX_SCALE = 5f
private const val OUTPUT_SIZE = 512
private const val JPEG_QUALITY = 90
private const val MASK_ALPHA = 0.65f
private const val RING_ALPHA = 0.6f
private const val RING_STROKE_WIDTH = 2.5f
private val CROP_BG_COLOR = Color(0xFF111111)

@Composable
fun AvatarCropDialog(
    imagePath: String,
    onCropComplete: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val previewBitmap = remember(imagePath) {
        loadSampledBitmap(imagePath, 800)
    }
    val imageBitmap = remember(previewBitmap) { previewBitmap?.asImageBitmap() }

    DisposableEffect(previewBitmap) {
        onDispose {
            previewBitmap?.recycle()
        }
    }

    var userScale by rememberSaveable { mutableFloatStateOf(1f) }
    var userOffsetX by rememberSaveable { mutableFloatStateOf(0f) }
    var userOffsetY by rememberSaveable { mutableFloatStateOf(0f) }
    var canvasPixelSize by remember { mutableFloatStateOf(0f) }

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
            shape = RoundedCornerShape(Dimens.cornerXxl),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CropDialogHeader()
                Spacer(modifier = Modifier.height(16.dp))

                CropCanvas(
                    imageBitmap = imageBitmap,
                    userScale = userScale,
                    userOffsetX = userOffsetX,
                    userOffsetY = userOffsetY,
                    onOffsetChange = { dx, dy ->
                        userOffsetX += dx
                        userOffsetY += dy
                    },
                    onScaleChange = { newScale, panX, panY ->
                        userScale = newScale.coerceIn(MIN_SCALE, MAX_SCALE)
                        userOffsetX += panX
                        userOffsetY += panY
                    },
                    onCanvasSizeChanged = { canvasPixelSize = it }
                )

                Spacer(modifier = Modifier.height(20.dp))

                CropDialogButtons(
                    onReset = {
                        userScale = 1f
                        userOffsetX = 0f
                        userOffsetY = 0f
                    },
                    onCancel = onDismiss,
                    onConfirm = {
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
                    }
                )
            }
        }
    }
}

/** 裁剪对话框标题区 */
@Composable
private fun CropDialogHeader() {
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
}

/** 裁剪画布：绘制图片 + 遮罩 + 圆环描边 + 手势处理 */
@Composable
private fun CropCanvas(
    imageBitmap: ImageBitmap,
    userScale: Float,
    userOffsetX: Float,
    userOffsetY: Float,
    onOffsetChange: (Float, Float) -> Unit,
    onScaleChange: (Float, Float, Float) -> Unit,
    onCanvasSizeChanged: (Float) -> Unit
) {
    val bitmapWidth = imageBitmap.width
    val bitmapHeight = imageBitmap.height

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(Dimens.cornerLarge))
            .background(CROP_BG_COLOR)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    onCanvasSizeChanged(coordinates.size.width.toFloat())
                }
                .pointerInput(Unit) {
                    detectDragGestures { _, dragAmount ->
                        onOffsetChange(dragAmount.x, dragAmount.y)
                    }
                }
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        onScaleChange(userScale * zoom, pan.x, pan.y)
                    }
                }
        ) {
            val canvasSize = size
            val circleRadius = canvasSize.width / 2f
            val circleCenter = Offset(canvasSize.width / 2f, canvasSize.height / 2f)

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

            val maskColor = Color.Black.copy(alpha = MASK_ALPHA)
            drawRect(maskColor, topLeft = Offset.Zero, size = Size(canvasSize.width, circleCenter.y - circleRadius))
            drawRect(maskColor, topLeft = Offset(0f, circleCenter.y + circleRadius), size = Size(canvasSize.width, canvasSize.height - circleCenter.y - circleRadius))
            drawRect(maskColor, topLeft = Offset(0f, circleCenter.y - circleRadius), size = Size(circleCenter.x - circleRadius, circleRadius * 2f))
            drawRect(maskColor, topLeft = Offset(circleCenter.x + circleRadius, circleCenter.y - circleRadius), size = Size(canvasSize.width - circleCenter.x - circleRadius, circleRadius * 2f))

            drawCircle(
                color = Color.White.copy(alpha = RING_ALPHA),
                radius = circleRadius,
                center = circleCenter,
                style = Stroke(width = RING_STROKE_WIDTH)
            )
        }
    }
}

/** 裁剪对话框按钮区 */
@Composable
private fun CropDialogButtons(
    onReset: () -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onReset,
            shape = RoundedCornerShape(Dimens.cornerMedium)
        ) {
            Text("重置")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = onCancel,
                shape = RoundedCornerShape(Dimens.cornerMedium)
            ) {
                Text("取消")
            }
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(Dimens.cornerMedium),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("确认", fontWeight = FontWeight.SemiBold)
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

/**
 * 计算裁剪变换矩阵（纯函数，可单独测试）。
 *
 * @param sourceWidth 源图宽度
 * @param sourceHeight 源图高度
 * @param userScale 用户缩放系数
 * @param userOffsetX 用户 X 偏移（Canvas 像素）
 * @param userOffsetY 用户 Y 偏移（Canvas 像素）
 * @param canvasPixelSize Canvas 像素尺寸
 * @param outputSize 输出图尺寸（正方形边长）
 * @return 变换矩阵
 */
internal fun computeCropMatrix(
    sourceWidth: Int,
    sourceHeight: Int,
    userScale: Float,
    userOffsetX: Float,
    userOffsetY: Float,
    canvasPixelSize: Float,
    outputSize: Int = OUTPUT_SIZE
): Matrix {
    val circleDiameter = outputSize.toFloat()
    val baseScale = circleDiameter / minOf(sourceWidth, sourceHeight)
    val totalScale = baseScale * userScale

    val scaleFactor = if (canvasPixelSize > 0f) outputSize / canvasPixelSize else 1f
    val scaledOffsetX = userOffsetX * scaleFactor
    val scaledOffsetY = userOffsetY * scaleFactor

    val matrix = Matrix()
    matrix.postScale(totalScale, totalScale)
    val drawWidth = sourceWidth * totalScale
    val drawHeight = sourceHeight * totalScale
    val centerX = outputSize / 2f
    val centerY = outputSize / 2f
    matrix.postTranslate(
        centerX - drawWidth / 2f + scaledOffsetX,
        centerY - drawHeight / 2f + scaledOffsetY
    )
    return matrix
}

private suspend fun cropAvatar(
    context: android.content.Context,
    imagePath: String,
    userScale: Float,
    userOffsetX: Float,
    userOffsetY: Float,
    canvasPixelSize: Float
): String? = withContext(Dispatchers.IO) {
    var sourceBitmap: Bitmap? = null
    var outputBitmap: Bitmap? = null
    try {
        sourceBitmap = BitmapFactory.decodeFile(imagePath) ?: return@withContext null
        val sourceWidth = sourceBitmap.width
        val sourceHeight = sourceBitmap.height

        outputBitmap = Bitmap.createBitmap(OUTPUT_SIZE, OUTPUT_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(outputBitmap)

        val matrix = computeCropMatrix(
            sourceWidth = sourceWidth,
            sourceHeight = sourceHeight,
            userScale = userScale,
            userOffsetX = userOffsetX,
            userOffsetY = userOffsetY,
            canvasPixelSize = canvasPixelSize,
            outputSize = OUTPUT_SIZE
        )

        canvas.drawBitmap(sourceBitmap, matrix, null)

        val imagesDir = File(context.filesDir, "app_images")
        if (!imagesDir.exists()) imagesDir.mkdirs()
        val outputFile = File(imagesDir, "avatar_cropped_${System.currentTimeMillis()}.jpg")
        outputFile.outputStream().use { fos ->
            outputBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, fos)
        }

        sourceBitmap.recycle()
        outputBitmap.recycle()

        outputFile.absolutePath
    } catch (e: Exception) {
        sourceBitmap?.recycle()
        outputBitmap?.recycle()
        null
    }
}
