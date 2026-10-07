package com.shinkai.wallpapers.ui.screens.settings.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Calculates adaptive Material 3 Expressive corner shapes for grouped vertical items.
 *
 * Supports dynamic selection state:
 * - Middle items: when selected, smoothly animate into fully rounded corners [outerCorner] (24.dp).
 *   When unselected, stay with normal grouped inner corners [innerCorner] (6.dp).
 * - First and last items: retain their distinct grouped boundary shapes (first has top rounded,
 *   last has bottom rounded).
 */
@Composable
fun animatedItemShapeFor(
    index: Int,
    total: Int,
    isSelected: Boolean = false,
    outerCorner: Dp = 24.dp,
    innerCorner: Dp = 6.dp,
): RoundedCornerShape {
    val animatedMiddleCorner by
        animateDpAsState(
            targetValue = if (isSelected) outerCorner else innerCorner,
            animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
            label = "MiddleCornerAnimation",
        )

    return when {
        total <= 1 -> RoundedCornerShape(outerCorner)
        index == 0 ->
            RoundedCornerShape(
                topStart = outerCorner,
                topEnd = outerCorner,
                bottomStart = innerCorner,
                bottomEnd = innerCorner,
            )
        index == total - 1 ->
            RoundedCornerShape(
                topStart = innerCorner,
                topEnd = innerCorner,
                bottomStart = outerCorner,
                bottomEnd = outerCorner,
            )
        else -> RoundedCornerShape(animatedMiddleCorner)
    }
}

/**
 * Static shape calculation for non-animated or non-composable usages.
 */
fun itemShapeFor(
    index: Int,
    total: Int,
    outerCorner: Dp = 24.dp,
    innerCorner: Dp = 6.dp,
): RoundedCornerShape =
    when {
        total <= 1 -> RoundedCornerShape(outerCorner)
        index == 0 ->
            RoundedCornerShape(
                topStart = outerCorner,
                topEnd = outerCorner,
                bottomStart = innerCorner,
                bottomEnd = innerCorner,
            )
        index == total - 1 ->
            RoundedCornerShape(
                topStart = innerCorner,
                topEnd = innerCorner,
                bottomStart = outerCorner,
                bottomEnd = outerCorner,
            )
        else -> RoundedCornerShape(innerCorner)
    }
