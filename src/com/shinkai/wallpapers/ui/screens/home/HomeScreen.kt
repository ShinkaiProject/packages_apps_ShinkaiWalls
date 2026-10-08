package com.shinkai.wallpapers.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shinkai.wallpapers.data.model.WallCategory
import com.shinkai.wallpapers.data.model.Wallpaper
import com.shinkai.wallpapers.ui.components.ShinkaiLoadingIndicator
import com.shinkai.wallpapers.ui.screens.home.components.HomeExploreHeader
import com.shinkai.wallpapers.ui.screens.home.components.HomeHeader
import com.shinkai.wallpapers.ui.screens.home.components.HomeSpotlightCard
import com.shinkai.wallpapers.ui.screens.home.components.PinterestWallpaperCard
import com.shinkai.wallpapers.ui.screens.home.components.StackedCardSheet

import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

// Varied portrait Pinterest aspect ratios for taller, authentic wallpaper previews
private val PINTEREST_ASPECT_RATIOS =
    listOf(
        0.60f, // Tall portrait
        0.72f, // Balanced portrait
        0.65f, // Medium tall portrait
        0.78f, // Compact portrait
        0.58f, // Extra tall portrait
        0.70f, // Standard portrait
    )

@Composable
fun HomeScreen(
    onWallpaperClick: (Wallpaper) -> Unit,
    onCategoryClick: (WallCategory) -> Unit,
    onMoreWallpapersClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.ensureLoaded(context)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreenContent(
        uiState = uiState,
        onRefresh = { viewModel.refresh(context) },
        onWallpaperClick = onWallpaperClick,
        onCategoryClick = onCategoryClick,
        onMoreWallpapersClick = onMoreWallpapersClick,
        onSettingsClick = onSettingsClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    onRefresh: () -> Unit,
    onWallpaperClick: (Wallpaper) -> Unit,
    onCategoryClick: (WallCategory) -> Unit,
    onMoreWallpapersClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val isRefreshing =
        (uiState as? HomeUiState.Success)?.isRefreshing ?: (uiState is HomeUiState.Loading)
    val pullRefreshState = rememberPullToRefreshState()

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        // --- 1. HEADER ---
        HomeHeader(onSettingsClick = onSettingsClick)

        val gridState = rememberLazyStaggeredGridState()
        val coroutineScope = rememberCoroutineScope()

        // --- 2. SINGLE FOREGROUND STACKED CARD SHEET ---
        StackedCardSheet(
            modifier = Modifier.fillMaxSize().weight(1f),
        ) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    coroutineScope.launch {
                        gridState.animateScrollToItem(0)
                    }
                    onRefresh()
                },
                state = pullRefreshState,
                indicator = {
                    androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.IndicatorBox(
                        state = pullRefreshState,
                        isRefreshing = isRefreshing,
                        elevation = 0.dp,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.align(Alignment.TopCenter),
                    ) {
                        ShinkaiLoadingIndicator(
                            size = 32.dp,
                            contained = false,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                },
                modifier = Modifier.fillMaxSize(),
            ) {
                when (uiState) {
                    is HomeUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            ShinkaiLoadingIndicator(size = 48.dp)
                        }
                    }

                    is HomeUiState.Success -> {
                        val wallpapers = uiState.wallpapers
                        val categories = uiState.categories

                        LazyVerticalStaggeredGrid(
                            state = gridState,
                            columns = StaggeredGridCells.Fixed(2),
                            contentPadding =
                                PaddingValues(
                                    start = 14.dp,
                                    end = 14.dp,
                                    top = 16.dp,
                                    bottom = 120.dp,
                                ),
                            verticalItemSpacing = 12.dp,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            if (wallpapers.isNotEmpty()) {
                                // --- SPOTLIGHT CAROUSEL ---
                                item(span = StaggeredGridItemSpan.FullLine) {
                                    HomeSpotlightCard(
                                        categories = categories,
                                        wallpapers = wallpapers,
                                        onCategoryClick = onCategoryClick,
                                        onWallpaperClick = onWallpaperClick,
                                    )
                                }

                                // --- EXPLORE SECTION TITLE & SUBTITLE ---
                                item(span = StaggeredGridItemSpan.FullLine) {
                                    HomeExploreHeader()
                                }

                                // --- PINTEREST MASONRY ITEMS ---
                                itemsIndexed(
                                    items = wallpapers,
                                    key = { _, item -> item.assetPath },
                                    contentType = { _, _ -> "wallpaper_card" },
                                ) { index, wallpaper ->
                                    val ratio = PINTEREST_ASPECT_RATIOS[index % PINTEREST_ASPECT_RATIOS.size]

                                    PinterestWallpaperCard(
                                        wallpaper = wallpaper,
                                        aspectRatio = ratio,
                                        onClick = { onWallpaperClick(wallpaper) },
                                    )
                                }
                            }
                        }
                    }

                    is HomeUiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = uiState.message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }
            }
        }
    }
}
