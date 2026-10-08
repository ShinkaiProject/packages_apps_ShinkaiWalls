package com.shinkai.wallpapers.ui.screens.settings

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shinkai.wallpapers.R
import com.shinkai.wallpapers.ui.screens.settings.components.SettingsGroupItem
import com.shinkai.wallpapers.ui.screens.settings.components.SettingsSubsection
import com.shinkai.wallpapers.ui.screens.settings.components.itemShapeFor
import com.shinkai.wallpapers.util.LocaleHelper

import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

enum class SettingsSubScreen {
    MAIN,
    APPEARANCE,
    LANGUAGE,
}

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(),
) {
    var currentSubScreen by rememberSaveable { mutableStateOf(SettingsSubScreen.MAIN) }

    BackHandler(enabled = currentSubScreen != SettingsSubScreen.MAIN) {
        currentSubScreen = SettingsSubScreen.MAIN
    }

    AnimatedContent(
        targetState = currentSubScreen,
        transitionSpec = {
            if (targetState != SettingsSubScreen.MAIN) {
                (slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec =
                        spring(
                            stiffness = Spring.StiffnessMediumLow,
                            dampingRatio = Spring.DampingRatioNoBouncy,
                        ),
                ) + fadeIn(animationSpec = tween(220)))
                    .togetherWith(
                        scaleOut(targetScale = 0.94f, animationSpec = tween(180, easing = EaseInCubic)) +
                                fadeOut(animationSpec = tween(180))
                    )
            } else {
                // Backward transition
                (scaleIn(initialScale = 0.94f, animationSpec = tween(220, easing = EaseOutCubic)) +
                        fadeIn(animationSpec = tween(200)))
                    .togetherWith(
                        slideOutHorizontally(
                            targetOffsetX = { it },
                            animationSpec =
                                spring(
                                    stiffness = Spring.StiffnessMediumLow,
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                ),
                        ) + fadeOut(animationSpec = tween(180, easing = EaseInCubic))
                    )
            }
        },
        label = "SettingsScreenTransition",
        modifier = modifier.fillMaxSize(),
    ) { subScreen ->
        when (subScreen) {
            SettingsSubScreen.MAIN -> {
                SettingsMainMenu(
                    onBack = onBack,
                    onNavigateToAppearance = { currentSubScreen = SettingsSubScreen.APPEARANCE },
                    onNavigateToLanguage = { currentSubScreen = SettingsSubScreen.LANGUAGE },
                    viewModel = viewModel,
                )
            }

            SettingsSubScreen.APPEARANCE -> {
                AppearanceScreen(
                    onBack = { currentSubScreen = SettingsSubScreen.MAIN },
                    viewModel = viewModel,
                )
            }

            SettingsSubScreen.LANGUAGE -> {
                LanguageScreen(
                    onBack = { currentSubScreen = SettingsSubScreen.MAIN },
                    viewModel = viewModel,
                )
            }
        }
    }
}

@Composable
private fun SettingsMainMenu(
    onBack: () -> Unit,
    onNavigateToAppearance: () -> Unit,
    onNavigateToLanguage: () -> Unit,
    viewModel: SettingsViewModel,
) {
    val context = LocalContext.current
    val currentLangTag by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val supportedLanguages = remember(context, currentLangTag) { LocaleHelper.getSupportedLanguages(context) }
    val currentLanguageItem =
        remember(currentLangTag, supportedLanguages) {
            supportedLanguages.firstOrNull { it.tag == currentLangTag }
                ?: supportedLanguages.first()
        }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        // --- 1. TOP APP BAR ---
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 16.dp, end = 20.dp, top = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
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

            Column {
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.settings_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // --- 2. FOREGROUND CONTENT SHEET ---
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
                    SettingsSubsection(
                        title = stringResource(R.string.appearance_title),
                    ) {
                        SettingsGroupItem(
                            icon = Icons.Outlined.Palette,
                            title = stringResource(R.string.appearance_title),
                            subtitle = stringResource(R.string.appearance_subtitle),
                            onClick = onNavigateToAppearance,
                            shape = itemShapeFor(0, 1),
                        )
                    }
                }

                item {
                    SettingsSubsection(
                        title = stringResource(R.string.language_title),
                    ) {
                        SettingsGroupItem(
                            icon = Icons.Outlined.Language,
                            title = stringResource(R.string.language_title),
                            subtitle = currentLanguageItem.nativeName,
                            onClick = onNavigateToLanguage,
                            shape = itemShapeFor(0, 1),
                        )
                    }
                }
            }
        }
    }
}
