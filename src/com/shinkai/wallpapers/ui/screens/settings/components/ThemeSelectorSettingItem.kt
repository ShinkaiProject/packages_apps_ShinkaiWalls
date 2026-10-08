package com.shinkai.wallpapers.ui.screens.settings.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shinkai.wallpapers.R
import com.shinkai.wallpapers.data.theme.DarkThemeMode

@Composable
fun ThemeSelectorSettingItem(
    selectedMode: DarkThemeMode,
    onModeSelected: (DarkThemeMode) -> Unit,
    shape: Shape,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.settings_theme_mode),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier =
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                val modes =
                    listOf(
                        Triple(
                            DarkThemeMode.FOLLOW_SYSTEM,
                            Icons.Rounded.BrightnessAuto,
                            stringResource(R.string.settings_theme_system),
                        ),
                        Triple(
                            DarkThemeMode.LIGHT,
                            Icons.Rounded.LightMode,
                            stringResource(R.string.settings_theme_light),
                        ),
                        Triple(
                            DarkThemeMode.DARK,
                            Icons.Rounded.DarkMode,
                            stringResource(R.string.settings_theme_dark),
                        ),
                    )

                modes.forEach { (mode, icon, label) ->
                    val isSelected = selectedMode == mode
                    val containerColor by
                    animateColorAsState(
                        targetValue =
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else Color.Transparent,
                        label = "theme_sel_bg",
                    )
                    val contentColor by
                    animateColorAsState(
                        targetValue =
                            if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        label = "theme_sel_color",
                    )

                    Row(
                        modifier =
                            Modifier.weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(containerColor)
                                .clickable { onModeSelected(mode) }
                                .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = contentColor,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = contentColor,
                        )
                    }
                }
            }
        }
    }
}
