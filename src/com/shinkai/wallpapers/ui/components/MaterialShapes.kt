package com.shinkai.wallpapers.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Profile representing a smooth, organic Material 3 Expressive shape. */
data class ExpressiveShapeProfile(
    val lobes: Int,
    val rBase: Float,
    val rDelta: Float,
    val power: Float = 1.5f,
    val angleOffset: Float = 0f,
)

private val M3_EXPRESSIVE_PROFILES = listOf(
    // 1. 4-lobed Puffy Clover (Signature M3 Expressive shape matching Google standard)
    ExpressiveShapeProfile(lobes = 4, rBase = 0.65f, rDelta = 0.35f, power = 1.4f),
    // 2. 6-lobed Blossom (Organic soft 6-petal bloom)
    ExpressiveShapeProfile(lobes = 6, rBase = 0.74f, rDelta = 0.26f, power = 1.5f),
    // 3. 8-lobed Flower (Soft rounded daisy bloom)
    ExpressiveShapeProfile(lobes = 8, rBase = 0.80f, rDelta = 0.20f, power = 1.5f),
    // 4. Soft 4-pointed Sparkle (Rotated organic sparkle)
    ExpressiveShapeProfile(lobes = 4, rBase = 0.58f, rDelta = 0.42f, power = 2.0f, angleOffset = (PI / 4f).toFloat()),
    // 5. 5-lobed Starbloom (Playful 5-leaf blossom)
    ExpressiveShapeProfile(lobes = 5, rBase = 0.70f, rDelta = 0.30f, power = 1.5f),
    // 6. Soft Squircle (Transitional puffy cushion)
    ExpressiveShapeProfile(lobes = 4, rBase = 0.86f, rDelta = 0.14f, power = 1.0f),
)

/** 4-lobed smooth puffy clover shape matching Material 3 Expressive standards. */
class ExpressivePuffyShape(
    private val profile: ExpressiveShapeProfile = M3_EXPRESSIVE_PROFILES[0],
) : Shape {
  override fun createOutline(
      size: Size,
      layoutDirection: LayoutDirection,
      density: Density,
  ): Outline {
    val path = Path()
    val cx = size.width / 2f
    val cy = size.height / 2f
    val radius = minOf(cx, cy)
    val steps = 144

    for (i in 0 until steps) {
      val theta = (i.toFloat() / steps) * (2f * PI.toFloat())
      val normCos = (cos(profile.lobes * (theta - profile.angleOffset)) + 1f) / 2f
      val p = Math.pow(normCos.toDouble(), profile.power.toDouble()).toFloat()
      val r = radius * (profile.rBase + profile.rDelta * p)
      val x = cx + r * cos(theta)
      val y = cy + r * sin(theta)

      if (i == 0) {
        path.moveTo(x, y)
      } else {
        path.lineTo(x, y)
      }
    }
    path.close()
    return Outline.Generic(path)
  }
}

val ExpressiveScallopShape: Shape = ExpressivePuffyShape()

/**
 * Material 3 Expressive morphing shape that continuously and smoothly interpolates
 * between signature M3 shapes: 4-lobed Clover, 6-lobed Blossom, 8-lobed Flower,
 * 4-pointed Sparkle, 5-lobed Starbloom, and Soft Squircle.
 */
class MorphingScallopShape(
    private val progress: Float,
    private val depthFactor: Float = 1f,
) : Shape {
  override fun createOutline(
      size: Size,
      layoutDirection: LayoutDirection,
      density: Density,
  ): Outline {
    val path = Path()
    val cx = size.width / 2f
    val cy = size.height / 2f
    val radius = minOf(cx, cy)
    val steps = 144

    val count = M3_EXPRESSIVE_PROFILES.size
    val normalized = (progress % count + count) % count
    val idxA = normalized.toInt()
    val idxB = (idxA + 1) % count
    val t = normalized - idxA

    val profA = M3_EXPRESSIVE_PROFILES[idxA]
    val profB = M3_EXPRESSIVE_PROFILES[idxB]

    for (i in 0 until steps) {
      val theta = (i.toFloat() / steps) * (2f * PI.toFloat())

      val normCosA = (cos(profA.lobes * (theta - profA.angleOffset)) + 1f) / 2f
      val pA = Math.pow(normCosA.toDouble(), profA.power.toDouble()).toFloat()
      val rA = radius * (profA.rBase + profA.rDelta * pA * depthFactor)

      val normCosB = (cos(profB.lobes * (theta - profB.angleOffset)) + 1f) / 2f
      val pB = Math.pow(normCosB.toDouble(), profB.power.toDouble()).toFloat()
      val rB = radius * (profB.rBase + profB.rDelta * pB * depthFactor)

      val r = (1f - t) * rA + t * rB
      val x = cx + r * cos(theta)
      val y = cy + r * sin(theta)

      if (i == 0) {
        path.moveTo(x, y)
      } else {
        path.lineTo(x, y)
      }
    }
    path.close()
    return Outline.Generic(path)
  }
}


/** 12-lobed smooth scallop / flower shape matching Material 3 Expressive standards. */
class Scallop12Shape(
    private val lobes: Int = 12,
    private val depth: Float = 0.08f,
) : Shape {
  override fun createOutline(
      size: Size,
      layoutDirection: LayoutDirection,
      density: Density,
  ): Outline {
    val path = Path()
    val cx = size.width / 2f
    val cy = size.height / 2f
    val radius = minOf(cx, cy)
    val steps = 96

    for (i in 0 until steps) {
      val theta = (i.toFloat() / steps) * (2f * PI.toFloat())
      val r = radius * (1f - depth + depth * cos(lobes * theta))
      val x = cx + r * cos(theta)
      val y = cy + r * sin(theta)

      if (i == 0) {
        path.moveTo(x, y)
      } else {
        path.lineTo(x, y)
      }
    }
    path.close()
    return Outline.Generic(path)
  }
}

val ScallopShape: Shape = ExpressiveScallopShape

/** Asymmetrical organic rounded card shape (M3 Expressive). */
fun asymmetricCardShape(
    large: Dp = 28.dp,
    small: Dp = 12.dp,
    flipped: Boolean = false,
): RoundedCornerShape {
  return if (!flipped) {
    RoundedCornerShape(
        topStart = large,
        topEnd = small,
        bottomStart = small,
        bottomEnd = large,
    )
  } else {
    RoundedCornerShape(
        topStart = small,
        topEnd = large,
        bottomStart = large,
        bottomEnd = small,
    )
  }
}

/** Tactile spring press scale animation modifier. */
@Composable
fun Modifier.tactilePress(
    targetScale: Float = 0.96f,
    onClick: () -> Unit,
): Modifier {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale = remember { Animatable(1f) }

  LaunchedEffect(isPressed) {
    scale.animateTo(
        targetValue = if (isPressed) targetScale else 1f,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
    )
  }

  return this.graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
      }
      .clickable(
          interactionSource = interactionSource,
          indication = androidx.compose.material3.ripple(bounded = true),
          onClick = onClick,
      )
}
