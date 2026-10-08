package com.shinkai.wallpapers.ui.screens.settings.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * 4-Quadrant divided color swatch dot matching Auriya design system.
 * Displays primary, secondary, tertiary, and neutral tones in 90-degree arcs
 * with an outer pulsating ring and center check badge when selected.
 */
@Composable
fun SwatchDot(
    primary: Color,
    secondary: Color,
    tertiary: Color,
    neutral: Color,
    selected: Boolean,
    pulseScale: Float,
    pulseAlpha: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(54.dp)
                .clip(CircleShape)
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (selected && pulseAlpha > 0f) {
            Box(
                modifier =
                    Modifier.size(44.dp)
                        .scale(pulseScale)
                        .border(
                            BorderStroke(2.dp, primary.copy(alpha = pulseAlpha)),
                            CircleShape,
                        ),
            )
        }
        Canvas(
            modifier = Modifier.size(40.dp),
        ) {
            // Draw 4 arcs representing Monet palette tone quadrants
            drawArc(color = primary, startAngle = 180f, sweepAngle = 90f, useCenter = true)
            drawArc(color = secondary, startAngle = 270f, sweepAngle = 90f, useCenter = true)
            drawArc(color = tertiary, startAngle = 0f, sweepAngle = 90f, useCenter = true)
            drawArc(color = neutral, startAngle = 90f, sweepAngle = 90f, useCenter = true)
        }
        if (selected) {
            Box(
                modifier =
                    Modifier.size(18.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(BorderStroke(1.dp, primary), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = primary,
                    modifier = Modifier.size(12.dp),
                )
            }
        }
    }
}
