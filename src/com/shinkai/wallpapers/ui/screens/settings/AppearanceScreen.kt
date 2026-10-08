package com.shinkai.wallpapers.ui.screens.settings

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shinkai.wallpapers.R
import com.shinkai.wallpapers.data.theme.DarkThemeMode
import com.shinkai.wallpapers.ui.screens.settings.components.PalettePickerSettingItem
import com.shinkai.wallpapers.ui.screens.settings.components.SettingsSubsection
import com.shinkai.wallpapers.ui.screens.settings.components.SwitchSettingItem
import com.shinkai.wallpapers.ui.screens.settings.components.ThemeSelectorSettingItem
import com.shinkai.wallpapers.ui.screens.settings.components.itemShapeFor

@Composable
fun AppearanceScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBack)

    val themePrefs by viewModel.themePrefs.collectAsStateWithLifecycle()
    val isSystemDark = isSystemInDarkTheme()
    val isDarkActive =
        when (themePrefs.darkThemeMode) {
            DarkThemeMode.FOLLOW_SYSTEM -> isSystemDark
            DarkThemeMode.LIGHT -> false
            DarkThemeMode.DARK -> true
        }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        // --- 1. TOP PINNED HEADER AREA ---
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 16.dp, end = 20.dp, top = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilledIconButton(
                onClick = onBack,
                shape = CircleShape,
                colors =
                    IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                modifier = Modifier.size(42.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            AnimatedContent(
                targetState = themePrefs.darkThemeMode,
                transitionSpec = {
                    (fadeIn(tween(220, easing = EaseOutCubic)) +
                            slideInVertically(initialOffsetY = { -it / 3 }))
                        .togetherWith(
                            fadeOut(tween(160, easing = EaseInCubic)) +
                                slideOutVertically(targetOffsetY = { it / 3 })
                        )
                },
                label = "AppearanceHeaderTransition",
            ) { _ ->
                Column {
                    Text(
                        text = stringResource(R.string.appearance_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.appearance_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // --- 2. FOREGROUND STACKED CARD SHEET ---
        Surface(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            shadowElevation = 8.dp,
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding =
                    PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 20.dp,
                        bottom = 40.dp,
                    ),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                item {
                    val isAmoledVisible = isDarkActive
                    val itemCount = 2 + (if (isAmoledVisible) 1 else 0)
                    var index = 0

                    SettingsSubsection(
                        title = stringResource(R.string.appearance_global_theme),
                    ) {
                        // 1. Theme Mode
                        ThemeSelectorSettingItem(
                            selectedMode = themePrefs.darkThemeMode,
                            onModeSelected = viewModel::setDarkThemeMode,
                            shape = itemShapeFor(index++, itemCount),
                        )

                        // 2. Color Palette (Dynamic Monet + Presets)
                        PalettePickerSettingItem(
                            isDynamic = themePrefs.useDynamicColor,
                            seedColor = themePrefs.seedColor,
                            onDynamicClick = { viewModel.setUseDynamicColor(true) },
                            onSeedChange = viewModel::setSeedColor,
                            shape = itemShapeFor(index++, itemCount),
                        )

                        // 3. Pure Black (AMOLED)
                        if (isAmoledVisible) {
                            SwitchSettingItem(
                                title = stringResource(R.string.settings_amoled),
                                subtitle = stringResource(R.string.settings_amoled_desc),
                                checked = themePrefs.isAmoled,
                                onCheckedChange = viewModel::setAmoled,
                                icon = Icons.Rounded.DarkMode,
                                shape = itemShapeFor(index++, itemCount),
                            )
                        }
                    }
                }
            }
        }
    }
}
