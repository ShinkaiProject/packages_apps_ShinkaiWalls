package com.shinkai.wallpapers

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.shinkai.wallpapers.ui.components.FloatingNavBar
import com.shinkai.wallpapers.ui.navigation.Screen
import com.shinkai.wallpapers.ui.navigation.TopLevelDestination
import com.shinkai.wallpapers.ui.screens.category.CategoryDetailScreen
import com.shinkai.wallpapers.ui.screens.home.HomeScreen
import com.shinkai.wallpapers.ui.screens.preview.PreviewScreen
import com.shinkai.wallpapers.ui.screens.walls.WallsScreen
import com.shinkai.wallpapers.ui.theme.ShinkaiTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            ShinkaiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    ShinkaiApp()
                }
            }
        }
    }
}

@Composable
fun ShinkaiApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var currentTab by remember { mutableStateOf(TopLevelDestination.HOME) }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val screen = currentScreen) {
            is Screen.Home -> {
                HomeScreen(
                    onWallpaperClick = { wp -> currentScreen = Screen.Preview(wp) },
                    onMoreWallpapersClick = {
                        currentTab = TopLevelDestination.WALLS
                        currentScreen = Screen.Walls
                    }
                )
            }
            is Screen.Walls -> {
                WallsScreen(
                    onCategoryClick = { cat -> currentScreen = Screen.CategoryDetail(cat) }
                )
            }
            is Screen.CategoryDetail -> {
                CategoryDetailScreen(
                    category = screen.category,
                    onBack = { currentScreen = Screen.Walls },
                    onWallpaperClick = { wp -> currentScreen = Screen.Preview(wp) }
                )
            }
            is Screen.Preview -> {
                PreviewScreen(
                    wallpaper = screen.wallpaper,
                    onBack = {
                        currentScreen = when (currentTab) {
                            TopLevelDestination.HOME -> Screen.Home
                            TopLevelDestination.WALLS -> Screen.Walls
                        }
                    }
                )
            }
        }

        if (currentScreen is Screen.Home || currentScreen is Screen.Walls) {
            FloatingNavBar(
                currentDestination = currentTab,
                onSelect = { destination ->
                    currentTab = destination
                    currentScreen = when (destination) {
                        TopLevelDestination.HOME -> Screen.Home
                        TopLevelDestination.WALLS -> Screen.Walls
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
