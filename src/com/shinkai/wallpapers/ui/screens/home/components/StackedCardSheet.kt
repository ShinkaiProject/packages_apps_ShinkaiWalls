package com.shinkai.wallpapers.ui.screens.home.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Material 3 Expressive stacked card sheet with a secondary backing card peeking out from the top.
 */
@Composable
fun StackedCardSheet(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    shadowElevation: Dp = 2.dp,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        shape = shape,
        color = containerColor,
        shadowElevation = shadowElevation,
        content = content,
    )
}
