package com.shinkai.wallpapers.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Material 3 Expressive Selectable Card component.
 *
 * Designed with borderless clean aesthetic:
 * - Selected state is clearly conveyed through container color contrast and dynamic shape.
 * - Leading icon container animates smoothly if provided.
 * - Active state badge with spring scale & fade animation.
 * - Uniform height and size across all grouped items for neat alignment.
 */
@Composable
fun ExpressiveSelectableCard(
    selected: Boolean,
    onClick: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: ImageVector? = null,
    shape: Shape = RoundedCornerShape(20.dp),
    selectedContainerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    unselectedContainerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    selectedContentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    unselectedContentColor: Color = MaterialTheme.colorScheme.onSurface,
    selectedLeadingContainerColor: Color = MaterialTheme.colorScheme.primary,
    selectedLeadingIconColor: Color = MaterialTheme.colorScheme.onPrimary,
    unselectedLeadingContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    unselectedLeadingIconColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    trailingContent: @Composable (() -> Unit)? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    // 1. Smooth container background color transition
    val animatedContainerColor by
        animateColorAsState(
            targetValue = if (selected) selectedContainerColor else unselectedContainerColor,
            animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
            label = "CardContainerColor",
        )

    // 2. Smooth leading icon container background color transition
    val animatedLeadingBg by
        animateColorAsState(
            targetValue =
                if (selected) selectedLeadingContainerColor
                else unselectedLeadingContainerColor,
            animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
            label = "LeadingIconBgColor",
        )

    val animatedLeadingIconColor by
        animateColorAsState(
            targetValue =
                if (selected) selectedLeadingIconColor
                else unselectedLeadingIconColor,
            animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
            label = "LeadingIconColor",
        )

    Surface(
        shape = shape,
        color = animatedContainerColor,
        border = null, // Completely borderless
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = 74.dp)
                .clip(shape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(),
                    role = Role.RadioButton,
                    onClick = onClick,
                )
                .semantics {
                    this.selected = selected
                    this.role = Role.RadioButton
                },
    ) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // --- OPTIONAL LEADING ICON WIDGET ---
            if (leadingIcon != null) {
                Box(
                    modifier =
                        Modifier.size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(animatedLeadingBg),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = animatedLeadingIconColor,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
            }

            // --- TITLE & SUBTITLE ---
            Crossfade(
                targetState = title to subtitle,
                animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                modifier = Modifier.weight(1f),
                label = "CardTextCrossfade",
            ) { (currentTitle, currentSubtitle) ->
                Column(
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = currentTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (selected) selectedContentColor else unselectedContentColor,
                    )
                    if (!currentSubtitle.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currentSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color =
                                if (selected) selectedContentColor.copy(alpha = 0.8f)
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            if (trailingContent != null) {
                trailingContent()
            }
        }
    }
}
