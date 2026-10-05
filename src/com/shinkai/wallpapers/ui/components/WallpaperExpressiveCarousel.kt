package com.shinkai.wallpapers.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
 * Material 3 Expressive Horizontal Multi-Browse Carousel matching the
 * official Android Wallpaper Selector UI.
 *
 * Sizing & Morphing Strategy:
 * - Uses [preferredItemWidth] (186.dp) to compute large focal cards.
 * - Dynamically narrows edge items into vertical peek pills (40-56.dp).
 * - Applies [maskClip] from CarouselItemScope to seamlessly animate mask bounds.
 * - Supports automatic sliding loop ([autoScroll]) that pauses when the user interacts.
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
    autoScrollIntervalMs: Long = 3500L
) {
    if (wallpapers.isEmpty()) return

    val state = rememberCarouselState { wallpapers.size }

    // Smooth auto-scroll loop that pauses during user manual drag/interaction
    LaunchedEffect(state, wallpapers.size, autoScroll) {
        if (autoScroll && wallpapers.size > 1) {
            while (true) {
                delay(autoScrollIntervalMs)
                if (!state.isScrollInProgress) {
                    val nextItem = (state.currentItem + 1) % wallpapers.size
                    state.animateScrollToItem(nextItem)
                }
            }
        }
    }

    HorizontalMultiBrowseCarousel(
        state = state,
        preferredItemWidth = preferredItemWidth,
        itemSpacing = itemSpacing,
        contentPadding = contentPadding,
        modifier = modifier
            .fillMaxWidth()
            .height(itemHeight)
    ) { index ->
        val wallpaper = wallpapers[index]
        WallpaperCarouselItem(
            wallpaper = wallpaper,
            shape = shape,
            onClick = { onWallpaperClick(wallpaper) },
            modifier = Modifier
                .height(itemHeight)
                .maskClip(shape)
        )
    }
}

@Composable
private fun WallpaperCarouselItem(
    wallpaper: Wallpaper,
    shape: Shape,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "carouselPressScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        AsyncImage(
            model = wallpaper.assetPath,
            contentDescription = wallpaper.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Subtle vignette gradient overlay at bottom
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.20f),
                            Color.Black.copy(alpha = 0.85f)
                        ),
                        startY = 60f
                    )
                )
        )

        // Bottom text overlay, gracefully clipped as the item transitions to edge peek pill
        Text(
            text = wallpaper.name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        )
    }
}
