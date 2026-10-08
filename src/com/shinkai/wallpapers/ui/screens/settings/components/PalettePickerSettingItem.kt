package com.shinkai.wallpapers.ui.screens.settings.components

import android.os.Build
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shinkai.wallpapers.R
import com.shinkai.wallpapers.data.theme.PALETTE_PRESETS

@Composable
fun PalettePickerSettingItem(
    isDynamic: Boolean,
    seedColor: Int,
    onDynamicClick: () -> Unit,
    onSeedChange: (Int) -> Unit,
    shape: Shape,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val infiniteTransition = rememberInfiniteTransition(label = "themePulse")
    val pulseScale by
    infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.22f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "pulseScale",
    )
    val pulseAlpha by
    infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "pulseAlpha",
    )

    // Dynamic Monet colors from system wallpaper
    val dynamicColors =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val scheme = dynamicDarkColorScheme(context)
            Triple(scheme.primary, scheme.secondary, scheme.tertiary) to scheme.primaryContainer
        } else {
            Triple(Color(0xFF2E7D32), Color(0xFF81C784), Color(0xFFC8E6C9)) to Color(0xFFE8F5E9)
        }

    Surface(
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(42.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.ColorLens,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
                Column {
                    Text(
                        text = stringResource(R.string.settings_custom_palette),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.settings_dynamic_color_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 1. Dynamic Wallpaper (Monet) Quadrant Swatch
                item {
                    SwatchDot(
                        primary = dynamicColors.first.first,
                        secondary = dynamicColors.first.second,
                        tertiary = dynamicColors.first.third,
                        neutral = dynamicColors.second,
                        selected = isDynamic,
                        pulseScale = pulseScale,
                        pulseAlpha = pulseAlpha,
                        onClick = onDynamicClick,
                    )
                }

                // 2. Preset 4-Quadrant Swatches
                items(PALETTE_PRESETS) { palette ->
                    val isSelected = !isDynamic && palette.seed == seedColor
                    SwatchDot(
                        primary = palette.primary,
                        secondary = palette.secondary,
                        tertiary = palette.tertiary,
                        neutral = palette.neutral,
                        selected = isSelected,
                        pulseScale = pulseScale,
                        pulseAlpha = pulseAlpha,
                        onClick = { onSeedChange(palette.seed) },
                    )
                }
            }
        }
    }
}
