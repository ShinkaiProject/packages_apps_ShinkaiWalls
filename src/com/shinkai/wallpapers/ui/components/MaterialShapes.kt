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

val ScallopShape: Shape = Scallop12Shape()

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
