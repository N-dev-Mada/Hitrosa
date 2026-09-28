package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint as AndroidPaint
import android.graphics.Path as AndroidPath
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PaidGreen
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@Composable
fun TouchSignaturePad(
    modifier: Modifier = Modifier,
    onSignatureCaptured: (String?) -> Unit,
    hasSignature: Boolean = false
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val paths = remember { mutableStateListOf<List<Offset>>() }
    var currentPath by remember { mutableStateOf<List<Offset>>(emptyList()) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Draw,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Signature manuscrite du client",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Scellée dans le registre cryptographique",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (paths.isNotEmpty() || currentPath.isNotEmpty()) {
                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            paths.clear()
                            currentPath = emptyList()
                            onSignatureCaptured(null)
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Effacer",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Effacer", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Signature drawing area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(
                        width = 1.dp,
                        color = if (paths.isNotEmpty()) PaidGreen else MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                // Background visual baseline guideline
                Canvas(modifier = Modifier.matchParentSize()) {
                    val yLine = size.height * 0.75f
                    drawLine(
                        color = Color(0xFFE2E8F0),
                        start = Offset(24.dp.toPx(), yLine),
                        end = Offset(size.width - 24.dp.toPx(), yLine),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                    )
                }

                // Placeholder prompt when empty
                if (paths.isEmpty() && currentPath.isEmpty()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "✍️ Signer au doigt au-dessus de la ligne",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = "Engagement légal et reconnaissance de dette",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFCBD5E1),
                            fontSize = 10.sp
                        )
                    }
                }

                Canvas(
                    modifier = Modifier
                        .matchParentSize()
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    currentPath = listOf(offset)
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    currentPath = currentPath + change.position
                                },
                                onDragEnd = {
                                    if (currentPath.isNotEmpty()) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        paths.add(currentPath)
                                        currentPath = emptyList()

                                        val filePath = savePathsToBitmap(context, paths)
                                        onSignatureCaptured(filePath)
                                    }
                                }
                            )
                        }
                ) {
                    // Draw completed paths
                    for (p in paths) {
                        if (p.size > 1) {
                            val composePath = Path().apply {
                                moveTo(p.first().x, p.first().y)
                                for (i in 1 until p.size) {
                                    lineTo(p[i].x, p[i].y)
                                }
                            }
                            drawPath(
                                path = composePath,
                                color = Color(0xFF0F172A),
                                style = Stroke(
                                    width = 3.5.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }

                    // Draw active dragging stroke
                    if (currentPath.size > 1) {
                        val inProgressPath = Path().apply {
                            moveTo(currentPath.first().x, currentPath.first().y)
                            for (i in 1 until currentPath.size) {
                                lineTo(currentPath[i].x, currentPath[i].y)
                            }
                        }
                        drawPath(
                            path = inProgressPath,
                            color = Color(0xFF0F172A),
                            style = Stroke(
                                width = 3.5.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }
            }
        }
    }
}

private fun savePathsToBitmap(context: Context, paths: List<List<Offset>>): String? {
    if (paths.isEmpty()) return null
    return try {
        val width = 600
        val height = 300
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = AndroidCanvas(bitmap)
        canvas.drawColor(AndroidColor.WHITE)

        val paint = AndroidPaint().apply {
            color = AndroidColor.rgb(15, 23, 42)
            strokeWidth = 6f
            style = AndroidPaint.Style.STROKE
            strokeCap = AndroidPaint.Cap.ROUND
            strokeJoin = AndroidPaint.Join.ROUND
            isAntiAlias = true
        }

        val minX = paths.flatMap { it }.minOfOrNull { it.x } ?: 0f
        val maxX = paths.flatMap { it }.maxOfOrNull { it.x } ?: 1f
        val minY = paths.flatMap { it }.minOfOrNull { it.y } ?: 0f
        val maxY = paths.flatMap { it }.maxOfOrNull { it.y } ?: 1f

        val pathW = maxOf(1f, maxX - minX)
        val pathH = maxOf(1f, maxY - minY)

        val scale = minOf((width - 40) / pathW, (height - 40) / pathH)
        val offsetX = (width - pathW * scale) / 2f - minX * scale
        val offsetY = (height - pathH * scale) / 2f - minY * scale

        for (p in paths) {
            if (p.size > 1) {
                val androidPath = AndroidPath()
                androidPath.moveTo(p[0].x * scale + offsetX, p[0].y * scale + offsetY)
                for (i in 1 until p.size) {
                    androidPath.lineTo(p[i].x * scale + offsetX, p[i].y * scale + offsetY)
                }
                canvas.drawPath(androidPath, paint)
            }
        }

        val dir = File(context.filesDir, "signatures")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "sig_${UUID.randomUUID()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
