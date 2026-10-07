package com.shinkai.wallpapers.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Material 3 Expressive Loading Indicator (https://m3.material.io/components/loading-indicator/overview).
 * 
 * Features smooth continuous rotation, tactile breathing scale, and organic shape morphing
 * across multiple signature M3 Expressive shapes (4-lobed Clover, 6-lobed Blossom,
 * 8-lobed Flower, and 10-lobed Scallop).
 */
@Composable
fun ShinkaiLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    contained: Boolean = true,
    morphShapes: Boolean = true,
    color: Color = MaterialTheme.colorScheme.primary,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "m3_loading_indicator")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "scallop_rotation",
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 950, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "scallop_scale",
    )

    val shapeMorphProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shape_morph",
    )

    val isContained = contained && containerColor != Color.Transparent

    val activeShape = if (morphShapes) {
        MorphingScallopShape(progress = shapeMorphProgress)
    } else {
        ExpressiveScallopShape
    }

    Box(
        modifier = modifier
            .size(size)
            .then(
                if (isContained) {
                    Modifier.background(containerColor, CircleShape)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        val innerSize = if (isContained) size * 0.65f else size
        Box(
            modifier = Modifier
                .size(innerSize)
                .graphicsLayer {
                    rotationZ = rotation
                    scaleX = scale
                    scaleY = scale
                }
                .clip(activeShape)
                .background(color)
        )
    }
}

/**
 * Material 3 Expressive Wavy Progress Indicator (https://m3.material.io/components/progress-indicators/overview).
 * 
 * Features a sleek horizontal track with a terminal stop dot, traversed by an animated,
 * flowing active sinusoidal wave with rounded stroke caps.
 */
@Composable
fun ShinkaiWavyProgressIndicator(
    modifier: Modifier = Modifier,
    width: Dp = 68.dp,
    height: Dp = 18.dp,
    strokeWidth: Dp = 3.dp,
    amplitude: Dp = 3.dp,
    wavelength: Dp = 15.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "m3_wavy_progress")

    val travelProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "wave_travel",
    )

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "wave_phase",
    )

    Canvas(modifier = modifier.size(width = width, height = height)) {
        val centerY = size.height / 2f
        val strokePx = strokeWidth.toPx()
        val ampPx = amplitude.toPx()
        val waveLenPx = wavelength.toPx()
        val totalWidth = size.width
        val padX = strokePx

        val trackStartX = padX
        val trackEndX = totalWidth - padX - strokePx * 2f
        val trackLength = (trackEndX - trackStartX).coerceAtLeast(1f)

        // 1. Draw inactive straight track
        drawLine(
            color = trackColor,
            start = Offset(trackStartX, centerY),
            end = Offset(trackEndX, centerY),
            strokeWidth = strokePx,
            cap = StrokeCap.Round,
        )

        // 2. Draw terminal stop dot
        drawCircle(
            color = trackColor,
            radius = strokePx * 0.95f,
            center = Offset(totalWidth - padX, centerY),
        )

        // 3. Draw active sinusoidal wavy segment
        val activeLength = trackLength * 0.48f
        val activeStartX = trackStartX + (trackLength - activeLength) * travelProgress
        val activeEndX = activeStartX + activeLength

        val wavePath = Path()
        val steps = 36
        for (s in 0..steps) {
            val x = activeStartX + (s.toFloat() / steps) * activeLength
            val relX = x - trackStartX
            val y = centerY + ampPx * sin((relX / waveLenPx) * (2f * PI.toFloat()) - phase)
            if (s == 0) {
                wavePath.moveTo(x, y)
            } else {
                wavePath.lineTo(x, y)
            }
        }

        drawPath(
            path = wavePath,
            color = color,
            style = Stroke(
                width = strokePx,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )
    }
}

/**
 * Material 3 Expressive Circular Wavy Progress Indicator (https://m3.material.io/components/progress-indicators/overview).
 *
 * Features an undulating active sinusoidal wave with rounded caps traveling along
 * a smooth circular track with M3-compliant gap spacing.
 */
@Composable
fun ShinkaiCircularWavyProgressIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    strokeWidth: Dp = if (size <= 32.dp) 3.dp else 4.dp,
    amplitude: Dp = if (size <= 32.dp) 1.8.dp else 2.6.dp,
    waveCount: Int = if (size <= 32.dp) 8 else 12,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
) {
    val infiniteTransition = rememberInfiniteTransition(label = "m3_circular_wavy_progress")

    val baseRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "circular_wavy_rotation",
    )

    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 75f,
        targetValue = 210f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "circular_wavy_sweep",
    )

    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "circular_wavy_phase",
    )

    Canvas(modifier = modifier.size(size)) {
        val strokePx = strokeWidth.toPx()
        val ampPx = amplitude.toPx()
        val cx = this.size.width / 2f
        val cy = this.size.height / 2f
        val r0 = ((this.size.minDimension - strokePx) / 2f - ampPx).coerceAtLeast(1f)

        val startAngle = baseRotation
        val gapAngle = 10f

        // 1. Draw smooth circular track with rounded caps
        val trackStartAngle = startAngle + sweepAngle + gapAngle
        val trackSweepAngle = (360f - sweepAngle - (2f * gapAngle)).coerceAtLeast(0f)
        if (trackSweepAngle > 1f && trackColor != Color.Transparent) {
            drawArc(
                color = trackColor,
                startAngle = trackStartAngle,
                sweepAngle = trackSweepAngle,
                useCenter = false,
                topLeft = Offset(cx - r0, cy - r0),
                size = androidx.compose.ui.geometry.Size(r0 * 2f, r0 * 2f),
                style = Stroke(
                    width = strokePx,
                    cap = StrokeCap.Round,
                ),
            )
        }

        // 2. Draw active undulating sinusoidal wavy arc
        val steps = (sweepAngle * 1.5f).toInt().coerceIn(36, 120)
        val wavePath = Path()
        for (i in 0..steps) {
            val fraction = i.toFloat() / steps
            val angleDeg = startAngle + fraction * sweepAngle
            val angleRad = (angleDeg * PI / 180.0).toFloat()
            val currentR = r0 + ampPx * sin(waveCount * angleRad - wavePhase)
            val x = cx + currentR * cos(angleRad)
            val y = cy + currentR * sin(angleRad)
            if (i == 0) {
                wavePath.moveTo(x, y)
            } else {
                wavePath.lineTo(x, y)
            }
        }

        drawPath(
            path = wavePath,
            color = color,
            style = Stroke(
                width = strokePx,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )
    }
}

/**
 * Material 3 Expressive Circular Progress Indicator (https://m3.material.io/components/progress-indicators/overview).
 * Features rounded stroke caps, surface container track, and smooth rotation.
 */
@Composable
fun ShinkaiCircularProgressIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    strokeWidth: Dp = if (size <= 28.dp) 2.5.dp else 3.5.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(size),
            color = color,
            strokeWidth = strokeWidth,
            trackColor = trackColor,
            strokeCap = StrokeCap.Round,
        )
    }
}
