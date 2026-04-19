package com.example.resimarkaplankaldirmajetpack.ui


import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import kotlin.math.min

@Composable
fun ZoomableImageEditor(
    bitmap: Bitmap?,
    modifier: Modifier = Modifier,
    onImageTap: (Int, Int) -> Unit
) {
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    var baseScale by remember { mutableFloatStateOf(1f) }

    var magnifierVisible by remember { mutableStateOf(false) }
    var magnifierTouch by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .background(Color(0xFFDDDDDD))
            .onSizeChanged { size ->
                containerSize = size

                bitmap?.let { bmp ->
                    val fitScale = min(
                        size.width.toFloat() / bmp.width.toFloat(),
                        size.height.toFloat() / bmp.height.toFloat()
                    )
                    baseScale = fitScale
                    if (scale == 1f) {
                        scale = fitScale
                        val drawnWidth = bmp.width * fitScale
                        val drawnHeight = bmp.height * fitScale
                        offset = Offset(
                            (size.width - drawnWidth) / 2f,
                            (size.height - drawnHeight) / 2f
                        )
                    }
                }
            }
            .pointerInput(bitmap) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    val newScale = (scale * zoom).coerceIn(baseScale, baseScale * 5f)
                    scale = newScale
                    offset += pan
                    magnifierVisible = false
                }
            }
            .pointerInput(bitmap, scale, offset) {
                detectTapGestures(
                    onTap = { touch ->
                        bitmap?.let { bmp ->
                            val mapped = mapTouchToBitmap(
                                touch = touch,
                                bitmap = bmp,
                                scale = scale,
                                offset = offset
                            )
                            if (mapped != null) {
                                onImageTap(mapped.first, mapped.second)
                            }
                        }
                    },
                    onLongPress = { touch ->
                        magnifierTouch = touch
                        magnifierVisible = true
                    },
                    onPress = { touch ->
                        magnifierTouch = touch
                        tryAwaitRelease()
                        magnifierVisible = false
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val bmp = bitmap ?: return@Canvas
            val imageBitmap = bmp.asImageBitmap()

            withTransform({
                translate(offset.x, offset.y)
                scale(scale, scale)
            }) {
                drawImage(imageBitmap)
            }

            if (magnifierVisible) {
                drawMagnifier(
                    bitmap = bmp,
                    touch = magnifierTouch,
                    radius = 160f,
                    zoom = 2.5f
                )
            }
        }
    }
}

private fun mapTouchToBitmap(
    touch: Offset,
    bitmap: Bitmap,
    scale: Float,
    offset: Offset
): Pair<Int, Int>? {
    val bx = ((touch.x - offset.x) / scale).toInt()
    val by = ((touch.y - offset.y) / scale).toInt()

    if (bx !in 0 until bitmap.width || by !in 0 until bitmap.height) return null
    return bx to by
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMagnifier(
    bitmap: Bitmap,
    touch: Offset,
    radius: Float,
    zoom: Float
) {
    val mapped = mapTouchToBitmap(
        touch = touch,
        bitmap = bitmap,
        scale = 1f,
        offset = Offset.Zero
    ) ?: return

    val bitmapX = mapped.first.toFloat()
    val bitmapY = mapped.second.toFloat()

    val cx = size.width - radius - 24f
    val cy = radius + 24f

    val clipPath = Path().apply {
        addOval(
            androidx.compose.ui.geometry.Rect(
                left = cx - radius,
                top = cy - radius,
                right = cx + radius,
                bottom = cy + radius
            )
        )
    }

    clipPath(clipPath) {
        withTransform({
            translate(cx - bitmapX * zoom, cy - bitmapY * zoom)
            scale(zoom, zoom)
        }) {
            drawImage(bitmap.asImageBitmap())
        }
    }

    drawCircle(
        color = Color.Black.copy(alpha = 0.3f),
        radius = radius,
        center = Offset(cx, cy),
        style = Stroke(width = 12f)
    )

    drawCircle(
        color = Color.White,
        radius = radius,
        center = Offset(cx, cy),
        style = Stroke(width = 5f)
    )

    drawLine(
        color = Color.Red,
        start = Offset(cx - 18f, cy),
        end = Offset(cx + 18f, cy),
        strokeWidth = 3f
    )

    drawLine(
        color = Color.Red,
        start = Offset(cx, cy - 18f),
        end = Offset(cx, cy + 18f),
        strokeWidth = 3f
    )
}