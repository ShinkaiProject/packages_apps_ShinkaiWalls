package com.shinkai.wallpapers.ui.screens.walls

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import com.shinkai.wallpapers.ui.components.ShinkaiCircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shinkai.wallpapers.R
import com.shinkai.wallpapers.data.model.WallCategory
import com.shinkai.wallpapers.ui.components.WallCategoryCard

// Portrait aspect ratios for taller, authentic Pinterest masonry previews
private val CategoryMasonryAspectRatios =
    listOf(
        0.68f, // Tall portrait
        0.80f, // Balanced portrait
        0.72f, // Medium portrait
        0.85f, // Compact portrait
        0.65f, // Extra tall portrait
        0.76f, // Standard portrait
    )

@Composable
fun WallsScreen(
    onCategoryClick: (WallCategory) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WallsViewModel = viewModel(),
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.loadCategories(context)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    WallsScreenContent(
        uiState = uiState,
        onCategoryClick = onCategoryClick,
        modifier = modifier,
    )
}

@Composable
fun WallsScreenContent(
    uiState: WallsUiState,
    onCategoryClick: (WallCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
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
                    text = stringResource(R.string.walls_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.walls_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // --- 2. FOREGROUND STACKED CARD SHEET ---
        Surface(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            shadowElevation = 8.dp,
        ) {
            when (uiState) {
                is WallsUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        ShinkaiCircularProgressIndicator(size = 48.dp)
                    }
                }

                is WallsUiState.Success -> {
                    if (uiState.categories.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = stringResource(R.string.walls_empty),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    } else {
                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Fixed(2),
                            contentPadding =
                                PaddingValues(
                                    start = 12.dp,
                                    end = 12.dp,
                                    top = 16.dp,
                                    bottom = 120.dp,
                                ),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalItemSpacing = 10.dp,
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            itemsIndexed(
                                items = uiState.categories,
                                key = { _, category -> category.id },
                                contentType = { _, _ -> "category_card" },
                            ) { index, category ->
                                val ratio = CategoryMasonryAspectRatios[index % CategoryMasonryAspectRatios.size]
                                WallCategoryCard(
                                    category = category,
                                    aspectRatio = ratio,
                                    onClick = { onCategoryClick(category) },
                                )
                            }
                        }
                    }
                }

                is WallsUiState.Error -> {
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
