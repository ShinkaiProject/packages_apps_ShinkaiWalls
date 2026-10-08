package com.shinkai.wallpapers.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shinkai.wallpapers.data.model.Wallpaper
import kotlinx.coroutines.delay

/**
 * Material 3 Expressive Horizontal Multi-Browse Carousel matching the official Android Wallpaper
 * Selector UI.
 *
 * Sizing & Morphing Strategy:
 * - Uses [preferredItemWidth] (186.dp) to compute large focal cards.
 * - Dynamically narrows edge items into vertical peek pills (40-56.dp).
 * - Applies [maskClip] from CarouselItemScope to seamlessly animate mask bounds.
 * - Supports automatic sliding loop ([autoScroll]) that pauses when the user interacts.
 * - Infinite virtual scrolling prevents jerky rewinds.
 * - Gracefully fades out text overlays as items narrow to avoid clipped words.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperExpressiveCarousel(
    wallpapers: List<Wallpaper>,
    onWallpaperClick: (Wallpaper) -> Unit,
    modifier: Modifier = Modifier,
    preferredItemWidth: Dp = 186.dp,
    itemHeight: Dp = 146.dp,
    itemSpacing: Dp = 10.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp),
    shape: Shape = RoundedCornerShape(26.dp),
    autoScroll: Boolean = true,
    autoScrollIntervalMs: Long = 3500L,
) {
  if (wallpapers.isEmpty()) return

  key(wallpapers.size) {
    val isLooping = wallpapers.size > 1
    val virtualItemCount = if (isLooping) 20_000 else 1
    val initialItem = if (isLooping) (10_000 / wallpapers.size) * wallpapers.size else 0

    val state = rememberCarouselState(initialItem = initialItem) { virtualItemCount }

    // Smooth continuous forward auto-scroll loop
    LaunchedEffect(state, wallpapers.size, autoScroll) {
      if (autoScroll && isLooping) {
        while (true) {
          delay(autoScrollIntervalMs)
          if (!state.isScrollInProgress) {
            state.animateScrollToItem(state.currentItem + 1)
          }
        }
      }
    }

    HorizontalMultiBrowseCarousel(
        state = state,
        preferredItemWidth = preferredItemWidth,
        itemSpacing = itemSpacing,
        contentPadding = contentPadding,
        modifier = modifier.fillMaxWidth().height(itemHeight),
    ) { index ->
      val wallpaper = wallpapers[index % wallpapers.size]
      val info = carouselItemDrawInfo
      val sizeFraction =
          if (info.maxSize > info.minSize) {
            ((info.size - info.minSize) / (info.maxSize - info.minSize)).coerceIn(0f, 1f)
          } else {
            1f
          }
      val textAlpha = ((sizeFraction - 0.65f) / 0.35f).coerceIn(0f, 1f)

      WallpaperCarouselItem(
          wallpaper = wallpaper,
          shape = shape,
          textAlpha = textAlpha,
          onClick = { onWallpaperClick(wallpaper) },
          modifier = Modifier.height(itemHeight).maskClip(shape),
      )
    }
  }
}

/**
 * Material 3 Expressive Multi-Browse Carousel specifically showcasing WallCategory cards.
 * Uses an infinite virtual scroll loop and smooth textAlpha fade so edge peek pills
 * show clean artwork without clipped text or abrupt rewinds.
 * Clicking a category card navigates directly to its CategoryDetailScreen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryExpressiveCarousel(
    categories: List<com.shinkai.wallpapers.data.model.WallCategory>,
    onCategoryClick: (com.shinkai.wallpapers.data.model.WallCategory) -> Unit,
    modifier: Modifier = Modifier,
    preferredItemWidth: Dp = 176.dp,
    itemHeight: Dp = 216.dp,
    itemSpacing: Dp = 10.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 2.dp),
    shape: Shape = RoundedCornerShape(28.dp),
    autoScroll: Boolean = true,
    autoScrollIntervalMs: Long = 3500L,
) {
  if (categories.isEmpty()) return

  key(categories.size) {
    val isLooping = categories.size > 1
    val virtualItemCount = if (isLooping) 20_000 else 1
    val initialItem = if (isLooping) (10_000 / categories.size) * categories.size else 0

    val state = rememberCarouselState(initialItem = initialItem) { virtualItemCount }

    // Seamless forward infinite auto-scroll
    LaunchedEffect(state, categories.size, autoScroll) {
      if (autoScroll && isLooping) {
        while (true) {
          delay(autoScrollIntervalMs)
          if (!state.isScrollInProgress) {
            state.animateScrollToItem(state.currentItem + 1)
          }
        }
      }
    }

    HorizontalMultiBrowseCarousel(
        state = state,
        preferredItemWidth = preferredItemWidth,
        itemSpacing = itemSpacing,
        contentPadding = contentPadding,
        modifier = modifier.fillMaxWidth().height(itemHeight),
    ) { index ->
      val category = categories[index % categories.size]
      val info = carouselItemDrawInfo
      val sizeFraction =
          if (info.maxSize > info.minSize) {
            ((info.size - info.minSize) / (info.maxSize - info.minSize)).coerceIn(0f, 1f)
          } else {
            1f
          }
      val textAlpha = ((sizeFraction - 0.65f) / 0.35f).coerceIn(0f, 1f)

      CategoryCarouselItem(
          category = category,
          shape = shape,
          textAlpha = textAlpha,
          onClick = { onCategoryClick(category) },
          modifier = Modifier.height(itemHeight).maskClip(shape),
      )
    }
  }
}

@Composable
private fun CategoryCarouselItem(
    category: com.shinkai.wallpapers.data.model.WallCategory,
    shape: Shape,
    textAlpha: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale =
      animateFloatAsState(
          targetValue = if (isPressed) 0.95f else 1f,
          animationSpec =
              spring(
                  dampingRatio = Spring.DampingRatioMediumBouncy,
                  stiffness = Spring.StiffnessLow,
              ),
          label = "categoryCarouselPressScale",
      )

  Box(
      modifier =
          modifier
              .fillMaxSize()
              .graphicsLayer {
                val s = scale.value
                scaleX = s
                scaleY = s
              }
              .clickable(
                  interactionSource = interactionSource,
                  indication = null,
                  onClick = onClick,
              )
              .background(MaterialTheme.colorScheme.surfaceContainerHigh)
  ) {
    AsyncImage(
        model = category.previewUrl,
        contentDescription = category.title,
        contentScale = ContentScale.Crop,
        targetMaxDim = 720,
        modifier = Modifier.fillMaxSize(),
    )

    if (textAlpha > 0.05f) {
      // Smooth vignette gradient overlay at bottom for high legibility
      Box(
          modifier =
              Modifier.fillMaxSize()
                  .graphicsLayer { alpha = textAlpha }
                  .background(CarouselScrimGradient)
      )

      // Bottom text overlay with neat title and sleek Monet pill badge
      Column(
          modifier =
              Modifier.align(Alignment.BottomStart)
                  .fillMaxWidth()
                  .graphicsLayer { alpha = textAlpha }
                  .padding(horizontal = 14.dp, vertical = 12.dp),
      ) {
        Text(
            text = category.title,
            style =
                MaterialTheme.typography.titleMedium.copy(
                    shadow =
                        Shadow(
                            color = Color.Black.copy(alpha = 0.55f),
                            offset = Offset(0f, 1.5f),
                            blurRadius = 6f,
                        ),
                ),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(5.dp))
        Box(
            modifier =
                Modifier.clip(RoundedCornerShape(100.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.88f))
                    .padding(horizontal = 10.dp, vertical = 3.5.dp),
            contentAlignment = Alignment.Center,
        ) {
          Text(
              text = "${category.count} Wall",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.SemiBold,
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSecondaryContainer,
              letterSpacing = 0.2.sp,
          )
        }
      }
    }
  }
}

private val CarouselScrimGradient =
    Brush.verticalGradient(
        0.0f to Color.Transparent,
        0.50f to Color.Transparent,
        0.75f to Color.Black.copy(alpha = 0.35f),
        1.0f to Color.Black.copy(alpha = 0.85f),
    )

@Composable
private fun WallpaperCarouselItem(
    wallpaper: Wallpaper,
    shape: Shape,
    textAlpha: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale =
      animateFloatAsState(
          targetValue = if (isPressed) 0.95f else 1f,
          animationSpec =
              spring(
                  dampingRatio = Spring.DampingRatioMediumBouncy,
                  stiffness = Spring.StiffnessLow,
              ),
          label = "carouselPressScale",
      )

  Box(
      modifier =
          modifier
              .fillMaxSize()
              .graphicsLayer {
                val s = scale.value
                scaleX = s
                scaleY = s
              }
              .clickable(
                  interactionSource = interactionSource,
                  indication = null,
                  onClick = onClick,
              )
              .background(MaterialTheme.colorScheme.surfaceContainerHigh)
  ) {
    AsyncImage(
        model = wallpaper.assetPath,
        contentDescription = wallpaper.name,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize(),
    )

    if (textAlpha > 0.05f) {
      // Subtle vignette gradient overlay at bottom
      Box(
          modifier =
              Modifier.fillMaxSize()
                  .graphicsLayer { alpha = textAlpha }
                  .background(CarouselScrimGradient)
      )

      // Bottom text overlay
      Text(
          text = wallpaper.name,
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          color = Color.White,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier =
              Modifier.align(Alignment.BottomStart)
                  .graphicsLayer { alpha = textAlpha }
                  .padding(horizontal = 12.dp, vertical = 10.dp),
      )
    }
  }
}
