package com.shinkai.wallpapers.ui.screens.preview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shinkai.wallpapers.data.model.Wallpaper
import com.shinkai.wallpapers.ui.components.AsyncImage
import com.shinkai.wallpapers.ui.components.ShinkaiLoadingIndicator

/** Centered floating phone preview canvas with rounded bezel and state-driven animations. */
@Composable
fun WallpaperPreviewCanvas(
    wallpaper: Wallpaper,
    isApplying: Boolean = false,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 32.dp,
    aspectRatio: Float = 9f / 19.5f,
) {
  val scaleAnimate by animateFloatAsState(
      targetValue = if (isApplying) 0.90f else 1.0f,
      animationSpec =
          spring(
              dampingRatio = Spring.DampingRatioMediumBouncy,
              stiffness = Spring.StiffnessLow,
          ),
      label = "PreviewScale",
  )

  val alphaAnimate by animateFloatAsState(
      targetValue = if (isApplying) 0.72f else 1.0f,
      animationSpec = tween(durationMillis = 350),
      label = "PreviewAlpha",
  )

  Box(
      modifier = modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 12.dp),
      contentAlignment = Alignment.Center,
  ) {
    Box(
        modifier =
            Modifier.fillMaxHeight()
                .aspectRatio(aspectRatio)
                .graphicsLayer {
                  scaleX = scaleAnimate
                  scaleY = scaleAnimate
                }
                .shadow(
                    16.dp,
                    RoundedCornerShape(cornerRadius),
                    spotColor = Color.Black.copy(alpha = 0.5f),
                )
                .clip(RoundedCornerShape(cornerRadius))
                .border(
                    BorderStroke(
                        1.5.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                    ),
                    RoundedCornerShape(cornerRadius),
                )
                .background(Color.Black)
    ) {
      Box(
          modifier =
              Modifier.fillMaxSize().graphicsLayer {
                alpha = alphaAnimate
              }
      ) {
        AsyncImage(
            model = wallpaper.fullUrl.ifEmpty { wallpaper.assetPath },
            fallbackModel = wallpaper.assetPath,
            contentDescription = wallpaper.name,
            contentScale = ContentScale.Crop,
            targetMaxDim = 2560,
            modifier = Modifier.fillMaxSize(),
            indicatorSize = 40.dp,
        )
      }

      AnimatedVisibility(
          visible = isApplying,
          enter = fadeIn(animationSpec = tween(250)),
          exit = fadeOut(animationSpec = tween(200)),
          modifier = Modifier.align(Alignment.Center),
      ) {
        Box(
            modifier =
                Modifier.size(68.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.88f),
                        shape = CircleShape,
                    ),
            contentAlignment = Alignment.Center,
        ) {
          ShinkaiLoadingIndicator(
              size = 42.dp,
              contained = false,
              color = MaterialTheme.colorScheme.primary,
          )
        }
      }
    }
  }
}
