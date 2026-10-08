package com.shinkai.wallpapers.ui.screens.home.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shinkai.wallpapers.data.model.Wallpaper
import com.shinkai.wallpapers.ui.components.AsyncImage

/**
 * Pinterest-style masonry pin card with tactile bouncy spring press feedback.
 */
@Composable
fun PinterestWallpaperCard(
    wallpaper: Wallpaper,
    aspectRatio: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale =
        animateFloatAsState(
            targetValue = if (isPressed) 0.95f else 1f,
            animationSpec =
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow,
                ),
            label = "pinScale",
        )

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .graphicsLayer {
                    val s = scale.value
                    scaleX = s
                    scaleY = s
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        },
                        onTap = { onClick() },
                    )
                },
        shape = RoundedCornerShape(24.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column {
            Box(
                modifier =
                    Modifier.fillMaxWidth()
                        .aspectRatio(aspectRatio)
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
            ) {
                AsyncImage(
                    model = wallpaper.assetPath,
                    contentDescription = wallpaper.name,
                    contentScale = ContentScale.Crop,
                    targetMaxDim = 720,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Text(
                text = wallpaper.name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
            )
        }
    }
}
