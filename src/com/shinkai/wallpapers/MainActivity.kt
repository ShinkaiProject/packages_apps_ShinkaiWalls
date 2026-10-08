package com.shinkai.wallpapers

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shinkai.wallpapers.data.theme.ThemeRepository
import com.shinkai.wallpapers.ui.components.FloatingNavBar
import com.shinkai.wallpapers.ui.navigation.Screen
import com.shinkai.wallpapers.ui.navigation.TopLevelDestination
import com.shinkai.wallpapers.ui.screens.about.AboutScreen
import com.shinkai.wallpapers.ui.screens.category.CategoryDetailScreen
import com.shinkai.wallpapers.ui.screens.home.HomeScreen
import com.shinkai.wallpapers.ui.screens.preview.PreviewScreen
import com.shinkai.wallpapers.ui.screens.settings.SettingsScreen
import com.shinkai.wallpapers.ui.screens.walls.WallsScreen
import com.shinkai.wallpapers.ui.theme.ShinkaiTheme
import com.shinkai.wallpapers.util.LocaleHelper

val ScreenSaver: Saver<Screen, String> =
    Saver(
        save = { screen ->
          when (screen) {
            is Screen.Home -> "home"
            is Screen.Walls -> "walls"
            is Screen.About -> "about"
            is Screen.Settings -> "settings"
            else -> "home"
          }
        },
        restore = { value ->
          when (value) {
            "home" -> Screen.Home
            "walls" -> Screen.Walls
            "about" -> Screen.About
            "settings" -> Screen.Settings
            else -> Screen.Home
          }
        },
    )

class MainActivity : ComponentActivity() {

  override fun attachBaseContext(newBase: Context) {
    super.attachBaseContext(LocaleHelper.wrapContext(newBase))
  }

  override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
    super.onConfigurationChanged(newConfig)
    LocaleHelper.applySavedLocale(this)
  }

  override fun onStop() {
    super.onStop()
    LocaleHelper.syncSystemLocale(this)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)

    LocaleHelper.applySavedLocale(this)

    val themeRepository = ThemeRepository.getInstance(applicationContext)

    setContent {
      val themePrefs by themeRepository.themePrefs.collectAsStateWithLifecycle()
      val currentLanguage by LocaleHelper.currentLanguage.collectAsStateWithLifecycle()
      val baseContext = LocalContext.current
      val localizedContext =
          remember(baseContext, currentLanguage) {
            LocaleHelper.wrapContext(baseContext, currentLanguage)
          }

      CompositionLocalProvider(
          LocalContext provides localizedContext,
          LocalConfiguration provides localizedContext.resources.configuration,
      ) {
        ShinkaiTheme(prefs = themePrefs) {
          Surface(
              modifier = Modifier.fillMaxSize(),
              color = MaterialTheme.colorScheme.surface,
          ) {
            ShinkaiApp()
          }
        }
      }
    }
  }
}

@Composable
fun ShinkaiApp() {
  var currentScreen by rememberSaveable(stateSaver = ScreenSaver) { mutableStateOf<Screen>(Screen.Home) }
  var currentTab by rememberSaveable { mutableStateOf(TopLevelDestination.HOME) }
  var categoryDetailBackTarget by remember { mutableStateOf<Screen>(Screen.Home) }
  var previewBackTarget by remember { mutableStateOf<Screen>(Screen.Home) }

  BackHandler(enabled = currentScreen !is Screen.Home) {
    when (currentScreen) {
      is Screen.Preview -> currentScreen = previewBackTarget
      is Screen.CategoryDetail -> currentScreen = categoryDetailBackTarget
      is Screen.Settings ->
          currentScreen =
              when (currentTab) {
                TopLevelDestination.HOME -> Screen.Home
                TopLevelDestination.WALLS -> Screen.Walls
                TopLevelDestination.ABOUT -> Screen.About
              }
      is Screen.Walls, is Screen.About -> {
        currentTab = TopLevelDestination.HOME
        currentScreen = Screen.Home
      }
      Screen.Home -> Unit
    }
  }

  Box(modifier = Modifier.fillMaxSize()) {
    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
          val initialTabIndex =
              when (initialState) {
                is Screen.Home -> 0
                is Screen.Walls -> 1
                is Screen.About -> 2
                else -> -1
              }
          val targetTabIndex =
              when (targetState) {
                is Screen.Home -> 0
                is Screen.Walls -> 1
                is Screen.About -> 2
                else -> -1
              }

          if (initialTabIndex != -1 && targetTabIndex != -1) {
            val isForward = targetTabIndex > initialTabIndex
            if (isForward) {
              (slideInHorizontally(
                      initialOffsetX = { (it * 0.35f).toInt() },
                      animationSpec =
                          spring(
                              dampingRatio = 0.88f,
                              stiffness = Spring.StiffnessMediumLow,
                          ),
                  ) + fadeIn(animationSpec = tween(280, easing = EaseOutCubic)))
                  .togetherWith(
                      slideOutHorizontally(
                          targetOffsetX = { (-it * 0.35f).toInt() },
                          animationSpec =
                              spring(
                                  dampingRatio = 0.88f,
                                  stiffness = Spring.StiffnessMediumLow,
                              ),
                      ) + fadeOut(animationSpec = tween(200, easing = EaseInCubic))
                  )
            } else {
              (slideInHorizontally(
                      initialOffsetX = { (-it * 0.35f).toInt() },
                      animationSpec =
                          spring(
                              dampingRatio = 0.88f,
                              stiffness = Spring.StiffnessMediumLow,
                          ),
                  ) + fadeIn(animationSpec = tween(280, easing = EaseOutCubic)))
                  .togetherWith(
                      slideOutHorizontally(
                          targetOffsetX = { (it * 0.35f).toInt() },
                          animationSpec =
                              spring(
                                  dampingRatio = 0.88f,
                                  stiffness = Spring.StiffnessMediumLow,
                              ),
                      ) + fadeOut(animationSpec = tween(200, easing = EaseInCubic))
                  )
            }
          } else if (targetState is Screen.CategoryDetail || targetState is Screen.Preview || targetState is Screen.Settings) {
            (slideInHorizontally(
                    initialOffsetX = { (it * 0.32f).toInt() },
                    animationSpec =
                        spring(
                            dampingRatio = 0.88f,
                            stiffness = Spring.StiffnessMediumLow,
                        ),
                ) + fadeIn(animationSpec = tween(280, easing = EaseOutCubic)) +
                    scaleIn(initialScale = 0.94f, animationSpec = tween(280, easing = EaseOutCubic)))
                .togetherWith(
                    slideOutHorizontally(
                        targetOffsetX = { (-it * 0.12f).toInt() },
                        animationSpec = tween(220, easing = EaseInCubic),
                    ) + fadeOut(animationSpec = tween(180, easing = EaseInCubic)) +
                        scaleOut(targetScale = 0.96f, animationSpec = tween(220, easing = EaseInCubic))
                )
          } else {
            (slideInHorizontally(
                    initialOffsetX = { (-it * 0.12f).toInt() },
                    animationSpec = tween(240, easing = EaseOutCubic),
                ) + fadeIn(animationSpec = tween(220, easing = EaseOutCubic)) +
                    scaleIn(initialScale = 0.96f, animationSpec = tween(240, easing = EaseOutCubic)))
                .togetherWith(
                    slideOutHorizontally(
                        targetOffsetX = { (it * 0.32f).toInt() },
                        animationSpec =
                            spring(
                                dampingRatio = 0.88f,
                                stiffness = Spring.StiffnessMediumLow,
                            ),
                    ) + fadeOut(animationSpec = tween(200, easing = EaseInCubic)) +
                        scaleOut(targetScale = 0.94f, animationSpec = tween(220, easing = EaseInCubic))
                )
          }
        },
        label = "MainScreenTransition",
        modifier = Modifier.fillMaxSize(),
    ) { screen ->
      when (screen) {
        is Screen.Home -> {
          HomeScreen(
              onWallpaperClick = { wp ->
                previewBackTarget = Screen.Home
                currentScreen = Screen.Preview(wp)
              },
              onCategoryClick = { cat ->
                categoryDetailBackTarget = Screen.Home
                currentScreen = Screen.CategoryDetail(cat)
              },
              onMoreWallpapersClick = {
                currentTab = TopLevelDestination.WALLS
                currentScreen = Screen.Walls
              },
              onSettingsClick = { currentScreen = Screen.Settings },
          )
        }
        is Screen.Walls -> {
          WallsScreen(
              onCategoryClick = { cat ->
                categoryDetailBackTarget = Screen.Walls
                currentScreen = Screen.CategoryDetail(cat)
              }
          )
        }
        is Screen.CategoryDetail -> {
          CategoryDetailScreen(
              category = screen.category,
              onBack = { currentScreen = categoryDetailBackTarget },
              onWallpaperClick = { wp ->
                previewBackTarget = screen
                currentScreen = Screen.Preview(wp)
              },
          )
        }
        is Screen.Preview -> {
          PreviewScreen(
              wallpaper = screen.wallpaper,
              onBack = { currentScreen = previewBackTarget },
          )
        }
        is Screen.About -> {
          AboutScreen(onBack = {
            currentTab = TopLevelDestination.HOME
            currentScreen = Screen.Home
          })
        }
        is Screen.Settings -> {
          SettingsScreen(onBack = { currentScreen = Screen.Home })
        }
      }
    }

    AnimatedVisibility(
        visible = currentScreen is Screen.Home || currentScreen is Screen.Walls || currentScreen is Screen.About,
        enter =
            fadeIn(animationSpec = tween(200)) +
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec =
                        spring(
                            stiffness = Spring.StiffnessMediumLow,
                            dampingRatio = Spring.DampingRatioNoBouncy,
                        ),
                ),
        exit =
            fadeOut(animationSpec = tween(160)) +
                slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(160, easing = EaseInCubic),
                ),
        modifier = Modifier.align(Alignment.BottomCenter),
    ) {
      FloatingNavBar(
          currentDestination = currentTab,
          onSelect = { destination ->
            currentTab = destination
            currentScreen =
                when (destination) {
                  TopLevelDestination.HOME -> Screen.Home
                  TopLevelDestination.WALLS -> Screen.Walls
                  TopLevelDestination.ABOUT -> Screen.About
                }
          },
      )
    }
  }
}
