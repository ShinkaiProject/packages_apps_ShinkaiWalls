package com.shinkai.wallpapers.ui.screens.preview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shinkai.wallpapers.data.model.Wallpaper
import com.shinkai.wallpapers.ui.components.AsyncImage

/** Centered floating phone preview canvas with rounded bezel and drop shadow. */
@Composable
fun WallpaperPreviewCanvas(
    wallpaper: Wallpaper,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 32.dp,
    aspectRatio: Float = 9f / 19.5f,
) {
  Box(
      modifier = modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 12.dp),
      contentAlignment = Alignment.Center,
  ) {
    Box(
        modifier =
            Modifier.fillMaxHeight()
                .aspectRatio(aspectRatio)
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
      AsyncImage(
          model = wallpaper.fullUrl.ifEmpty { wallpaper.assetPath },
          contentDescription = wallpaper.name,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize(),
          indicatorSize = 40.dp,
      )
    }
  }
}
