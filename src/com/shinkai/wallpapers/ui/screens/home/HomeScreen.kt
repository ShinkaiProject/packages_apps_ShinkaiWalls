package com.shinkai.wallpapers.ui.screens.home

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shinkai.wallpapers.R
import com.shinkai.wallpapers.data.model.Wallpaper
import com.shinkai.wallpapers.ui.components.AsyncImage
import com.shinkai.wallpapers.ui.components.ShinkaiLoadingIndicator
import com.shinkai.wallpapers.ui.components.WallpaperExpressiveCarousel

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
    onMoreWallpapersClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreenContent(
        uiState = uiState,
        onRefresh = viewModel::refresh,
        onWallpaperClick = onWallpaperClick,
        onMoreWallpapersClick = onMoreWallpapersClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    onRefresh: () -> Unit,
    onWallpaperClick: (Wallpaper) -> Unit,
    onMoreWallpapersClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val isRefreshing =
        (uiState as? HomeUiState.Success)?.isRefreshing ?: (uiState is HomeUiState.Loading)
    var showAboutDialog by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullToRefreshState()

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.about_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.about_message),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text(stringResource(R.string.about_dismiss))
                }
            },
        )
    }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainerLow)) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 22.dp, end = 20.dp, top = 8.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Shinkai",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.home_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            FilledIconButton(
                onClick = { showAboutDialog = true },
                shape = CircleShape,
                colors =
                    IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                modifier = Modifier.size(42.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Settings",
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        // --- 2. FOREGROUND STACKED CARD SHEET (Overlapping on top of backdrop) ---
        Surface(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                state = pullRefreshState,
                indicator = {
                    val rawProgress = pullRefreshState.distanceFraction
                    val isVisible = isRefreshing || rawProgress >= 0.35f
                    if (isVisible) {
                        Box(modifier = Modifier.align(Alignment.TopCenter).padding(top = 12.dp)) {
                            val scale =
                                if (isRefreshing) 1f else ((rawProgress - 0.35f) / 0.65f).coerceIn(0f, 1f)
                            ShinkaiLoadingIndicator(
                                size = (50.dp * scale).coerceAtLeast(26.dp),
                                modifier = Modifier.align(Alignment.Center),
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxSize(),
            ) {
                when (uiState) {
                    is HomeUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            ShinkaiLoadingIndicator(size = 64.dp)
                        }
                    }

                    is HomeUiState.Success -> {
                        val wallpapers = uiState.wallpapers

                        LazyVerticalStaggeredGrid(
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
                                // --- 1. SPOTLIGHT SECTION TITLE & SUBTITLE ---
                                item(span = StaggeredGridItemSpan.FullLine) {
                                    Column(
                                        modifier =
                                            Modifier.fillMaxWidth()
                                                .padding(start = 4.dp, end = 4.dp, top = 2.dp, bottom = 10.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.home_spotlight_title),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = stringResource(R.string.home_spotlight_subtitle),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }

                                // --- 2. SPOTLIGHT MATERIAL 3 EXPRESSIVE CAROUSEL ---
                                item(span = StaggeredGridItemSpan.FullLine) {
                                    WallpaperExpressiveCarousel(
                                        wallpapers = wallpapers,
                                        onWallpaperClick = onWallpaperClick,
                                        preferredItemWidth = 186.dp,
                                        itemHeight = 154.dp,
                                        itemSpacing = 10.dp,
                                        contentPadding = PaddingValues(horizontal = 2.dp),
                                        shape = RoundedCornerShape(26.dp),
                                        autoScroll = true,
                                        autoScrollIntervalMs = 3500L,
                                    )
                                }

                                // --- 3. EXPLORE SECTION TITLE & SUBTITLE ---
                                item(span = StaggeredGridItemSpan.FullLine) {
                                    Column(
                                        modifier =
                                            Modifier.fillMaxWidth()
                                                .padding(start = 4.dp, end = 4.dp, top = 18.dp, bottom = 4.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.home_explore_title),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = stringResource(R.string.home_explore_subtitle),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }

                            // --- 4. MAIN SECTION: Pinterest Staggered Masonry Items ---
                            itemsIndexed(
                                items = wallpapers,
                                key = { _, item -> item.assetPath },
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

/** Pinterest Masonry Pin Card with tactile press feedback. */
@Composable
private fun PinterestWallpaperCard(
    wallpaper: Wallpaper,
    aspectRatio: Float,
    onClick: () -> Unit,
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by
    animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        label = "pinScale",
    )

    Column(
        modifier =
            Modifier.fillMaxWidth().graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Card(
            modifier =
                Modifier.fillMaxWidth().aspectRatio(aspectRatio).pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        },
                        onTap = { onClick() },
                    )
                },
            shape = RoundedCornerShape(20.dp),
            colors =
                CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            AsyncImage(
                model = wallpaper.assetPath,
                contentDescription = wallpaper.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Text(
            text = wallpaper.name,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
        )
    }
}
