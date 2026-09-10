package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.GridType
import com.example.sensor.LevelState
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CameraBlack
import com.example.ui.theme.LevelGreen
import com.example.ui.theme.ViewfinderGridActive
import com.example.ui.theme.ViewfinderGridColor
import kotlin.math.roundToInt

@Composable
fun ViewfinderOverlay(
    language: AppLanguage = AppLanguage.ARABIC,
    gridType: GridType,
    showSpiritLevel: Boolean,
    showHistogram: Boolean,
    levelState: LevelState,
    focusPoint: Pair<Float, Float>?,
    focusLocked: Boolean,
    evIndex: Int,
    evStep: Float,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // 1. Grid lines canvas
        if (gridType != GridType.NONE) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                when (gridType) {
                    GridType.THIRDS -> {
                        val stroke = Stroke(width = 1.dp.toPx())
                        // Vertical lines
                        drawLine(ViewfinderGridColor, Offset(w / 3f, 0f), Offset(w / 3f, h), strokeWidth = stroke.width)
                        drawLine(ViewfinderGridColor, Offset(w * 2f / 3f, 0f), Offset(w * 2f / 3f, h), strokeWidth = stroke.width)
                        // Horizontal lines
                        drawLine(ViewfinderGridColor, Offset(0f, h / 3f), Offset(w, h / 3f), strokeWidth = stroke.width)
                        drawLine(ViewfinderGridColor, Offset(0f, h * 2f / 3f), Offset(w, h * 2f / 3f), strokeWidth = stroke.width)

                        // Intersection accent dots
                        val intersectionPoints = listOf(
                            Offset(w / 3f, h / 3f),
                            Offset(w * 2f / 3f, h / 3f),
                            Offset(w / 3f, h * 2f / 3f),
                            Offset(w * 2f / 3f, h * 2f / 3f)
                        )
                        for (pt in intersectionPoints) {
                            drawCircle(AmberGold, radius = 3.dp.toPx(), center = pt)
                        }
                    }
                    GridType.GOLDEN_RATIO -> {
                        val phi1 = 0.382f
                        val phi2 = 0.618f
                        val stroke = Stroke(width = 1.dp.toPx())
                        // Vertical lines
                        drawLine(ViewfinderGridColor, Offset(w * phi1, 0f), Offset(w * phi1, h), strokeWidth = stroke.width)
                        drawLine(ViewfinderGridColor, Offset(w * phi2, 0f), Offset(w * phi2, h), strokeWidth = stroke.width)
                        // Horizontal lines
                        drawLine(ViewfinderGridColor, Offset(0f, h * phi1), Offset(w, h * phi1), strokeWidth = stroke.width)
                        drawLine(ViewfinderGridColor, Offset(0f, h * phi2), Offset(w, h * phi2), strokeWidth = stroke.width)
                    }
                    GridType.SQUARE -> {
                        val minSide = kotlin.math.min(w, h)
                        val left = (w - minSide) / 2f
                        val top = (h - minSide) / 2f

                        // Dark letterbox masks
                        if (h > w) {
                            drawRect(CameraBlack.copy(alpha = 0.55f), topLeft = Offset(0f, 0f), size = Size(w, top))
                            drawRect(CameraBlack.copy(alpha = 0.55f), topLeft = Offset(0f, top + minSide), size = Size(w, h - (top + minSide)))
                        } else {
                            drawRect(CameraBlack.copy(alpha = 0.55f), topLeft = Offset(0f, 0f), size = Size(left, h))
                            drawRect(CameraBlack.copy(alpha = 0.55f), topLeft = Offset(left + minSide, 0f), size = Size(w - (left + minSide), h))
                        }

                        // Square outline
                        drawRect(
                            color = ViewfinderGridActive,
                            topLeft = Offset(left, top),
                            size = Size(minSide, minSide),
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                    }
                    GridType.NONE -> {}
                }
            }
        }

        // 2. Real-time Spirit Level / Horizon Indicator
        if (showSpiritLevel) {
            val levelColor = if (levelState.isLevel) LevelGreen else AmberGold
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val horizonLineWidth = size.width * 0.38f

                rotate(degrees = -levelState.rollDegrees, pivot = Offset(cx, cy)) {
                    // Left horizon bar
                    drawLine(
                        color = levelColor,
                        start = Offset(cx - horizonLineWidth, cy),
                        end = Offset(cx - 30.dp.toPx(), cy),
                        strokeWidth = if (levelState.isLevel) 2.5.dp.toPx() else 1.5.dp.toPx()
                    )
                    // Right horizon bar
                    drawLine(
                        color = levelColor,
                        start = Offset(cx + 30.dp.toPx(), cy),
                        end = Offset(cx + horizonLineWidth, cy),
                        strokeWidth = if (levelState.isLevel) 2.5.dp.toPx() else 1.5.dp.toPx()
                    )
                    // Center alignment ring
                    drawCircle(
                        color = levelColor,
                        radius = 12.dp.toPx(),
                        center = Offset(cx, cy),
                        style = Stroke(width = if (levelState.isLevel) 2.dp.toPx() else 1.2.dp.toPx())
                    )
                    // Center dot
                    drawCircle(
                        color = levelColor,
                        radius = if (levelState.isLevel) 3.5.dp.toPx() else 2.dp.toPx(),
                        center = Offset(cx, cy)
                    )
                }

                // Reference stationary horizon ticks
                drawLine(
                    color = Color.White.copy(alpha = 0.4f),
                    start = Offset(cx - horizonLineWidth, cy - 8.dp.toPx()),
                    end = Offset(cx - horizonLineWidth, cy + 8.dp.toPx()),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.4f),
                    start = Offset(cx + horizonLineWidth, cy - 8.dp.toPx()),
                    end = Offset(cx + horizonLineWidth, cy + 8.dp.toPx()),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Roll angle digital readout pill
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 42.dp)
                    .background(CameraBlack.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                    .border(0.5.dp, levelColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                val formattedRoll = if (kotlin.math.abs(levelState.rollDegrees) < 0.1f) {
                    "0.0°"
                } else {
                    String.format(java.util.Locale.US, "%+.1f°", -levelState.rollDegrees)
                }
                val levelLabel = if (language == AppLanguage.ARABIC) "مستوى" else "LEVEL"
                Text(
                    text = if (levelState.isLevel) "$levelLabel  $formattedRoll" else formattedRoll,
                    color = levelColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // 3. Touch-to-Focus Reticle
        focusPoint?.let { (fx, fy) ->
            FocusReticle(
                fx = fx,
                fy = fy,
                isLocked = focusLocked
            )
        }

        // 4. Live Exposure Meter (EV Ladder)
        val calculatedEv = evIndex * evStep
        val evString = String.format(java.util.Locale.US, "%+.1f EV", calculatedEv)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
                .background(CameraBlack.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = evString,
                    color = if (evIndex == 0) Color.White else AmberGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                // Meter scale: -2  -1  0  +1  +2
                Canvas(
                    modifier = Modifier
                        .width(130.dp)
                        .height(8.dp)
                        .padding(top = 2.dp)
                ) {
                    val w = size.width
                    val cy = size.height / 2f
                    // Baseline
                    drawLine(Color.Gray.copy(alpha = 0.5f), Offset(0f, cy), Offset(w, cy), strokeWidth = 1.dp.toPx())
                    // Major ticks (-2, -1, 0, +1, +2)
                    val ticks = 5
                    for (i in 0 until ticks) {
                        val tx = i * (w / (ticks - 1))
                        val isCenter = i == 2
                        drawLine(
                            color = if (isCenter) Color.White else Color.Gray,
                            start = Offset(tx, cy - (if (isCenter) 4.dp.toPx() else 2.5.dp.toPx())),
                            end = Offset(tx, cy + (if (isCenter) 4.dp.toPx() else 2.5.dp.toPx())),
                            strokeWidth = if (isCenter) 1.5.dp.toPx() else 1.dp.toPx()
                        )
                    }

                    // Indicator arrow / cursor for current EV
                    val normalizedEv = (calculatedEv.coerceIn(-2f, 2f) + 2f) / 4f
                    val indicatorX = normalizedEv * w
                    drawCircle(AmberGold, radius = 3.dp.toPx(), center = Offset(indicatorX, cy))
                }
            }
        }

        // 5. Live Histogram HUD Overlay (Top Right)
        if (showHistogram) {
            LiveHistogramHud(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 56.dp, end = 12.dp)
            )
        }
    }
}

@Composable
fun FocusReticle(
    fx: Float,
    fy: Float,
    isLocked: Boolean
) {
    val density = LocalDensity.current
    val scaleAnim = remember { Animatable(1.4f) }

    LaunchedEffect(fx, fy) {
        scaleAnim.snapTo(1.5f)
        scaleAnim.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing)
        )
    }

    val reticleColor = if (isLocked) LevelGreen else AmberGold
    val sizeDp = 64.dp * scaleAnim.value

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Canvas(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = (fx - with(density) { (sizeDp / 2f).toPx() }).roundToInt(),
                        y = (fy - with(density) { (sizeDp / 2f).toPx() }).roundToInt()
                    )
                }
                .size(sizeDp)
        ) {
            val s = size.width
            val cornerLen = s * 0.25f
            val stroke = Stroke(width = 2.dp.toPx())

            // Top-left bracket
            drawLine(reticleColor, Offset(0f, 0f), Offset(cornerLen, 0f), strokeWidth = stroke.width)
            drawLine(reticleColor, Offset(0f, 0f), Offset(0f, cornerLen), strokeWidth = stroke.width)

            // Top-right bracket
            drawLine(reticleColor, Offset(s, 0f), Offset(s - cornerLen, 0f), strokeWidth = stroke.width)
            drawLine(reticleColor, Offset(s, 0f), Offset(s, cornerLen), strokeWidth = stroke.width)

            // Bottom-left bracket
            drawLine(reticleColor, Offset(0f, s), Offset(cornerLen, s), strokeWidth = stroke.width)
            drawLine(reticleColor, Offset(0f, s), Offset(0f, s - cornerLen), strokeWidth = stroke.width)

            // Bottom-right bracket
            drawLine(reticleColor, Offset(s, s), Offset(s - cornerLen, s), strokeWidth = stroke.width)
            drawLine(reticleColor, Offset(s, s), Offset(s, s - cornerLen), strokeWidth = stroke.width)

            // Center crosshair dot
            drawCircle(reticleColor, radius = 2.dp.toPx(), center = Offset(s / 2f, s / 2f))
        }
    }
}

@Composable
fun LiveHistogramHud(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "histogram")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "histogram_pulse"
    )

    Box(
        modifier = modifier
            .width(96.dp)
            .height(52.dp)
            .background(CameraBlack.copy(alpha = 0.75f), RoundedCornerShape(6.dp))
            .border(0.5.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
            .padding(4.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Simulated RGB curves
            val pathLuma = Path().apply {
                moveTo(0f, h)
                cubicTo(w * 0.2f, h * 0.8f * pulse, w * 0.45f, h * 0.2f / pulse, w * 0.7f, h * 0.6f)
                cubicTo(w * 0.85f, h * 0.75f, w * 0.95f, h * 0.3f, w, h)
                close()
            }
            drawPath(pathLuma, color = Color.White.copy(alpha = 0.35f))

            val pathAccent = Path().apply {
                moveTo(0f, h)
                cubicTo(w * 0.3f, h * 0.65f, w * 0.55f, h * 0.25f * pulse, w * 0.75f, h * 0.7f)
                cubicTo(w * 0.9f, h * 0.85f, w * 0.98f, h * 0.4f, w, h)
                close()
            }
            drawPath(pathAccent, color = AmberGold.copy(alpha = 0.45f))
        }
        Text(
            text = "RGB",
            color = Color.LightGray,
            fontSize = 8.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(2.dp)
        )
    }
}
