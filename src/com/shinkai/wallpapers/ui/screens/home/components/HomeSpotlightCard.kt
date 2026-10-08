package com.shinkai.wallpapers.ui.screens.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shinkai.wallpapers.R
import com.shinkai.wallpapers.data.model.WallCategory
import com.shinkai.wallpapers.data.model.Wallpaper
import com.shinkai.wallpapers.data.repository.WallCategories
import com.shinkai.wallpapers.ui.components.CategoryExpressiveCarousel

/**
 * Editorial spotlight carousel card showcasing curated wallpaper categories.
 * Features a layered background card, refined M3 Expressive typography,
 * and a smooth, infinite looping category carousel.
 */
@Composable
fun HomeSpotlightCard(
    categories: List<WallCategory>,
    wallpapers: List<Wallpaper>,
    onCategoryClick: (WallCategory) -> Unit,
    onWallpaperClick: (Wallpaper) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val displayCategories =
        remember(categories, wallpapers) {
            if (categories.isNotEmpty()) {
                categories
            } else if (wallpapers.isNotEmpty()) {
                WallCategories.resolve(context, wallpapers)
            } else {
                emptyList()
            }
        }

    if (displayCategories.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth().padding(bottom = 12.dp)
    ) {
        // Section Title & Subtitle with neat typography matching Explore
        Column(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(start = 4.dp, end = 4.dp, bottom = 12.dp)
        ) {
            Text(
                text = stringResource(R.string.home_spotlight_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                letterSpacing = (-0.3).sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.home_spotlight_subtitle),
                style = MaterialTheme.typography.bodySmall,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Spotlight Expressive Carousel strictly filled with Categories
        CategoryExpressiveCarousel(
            categories = displayCategories,
            onCategoryClick = onCategoryClick,
            preferredItemWidth = 176.dp,
            itemHeight = 216.dp,
            itemSpacing = 10.dp,
            contentPadding = PaddingValues(horizontal = 2.dp),
            shape = RoundedCornerShape(28.dp),
            autoScroll = true,
            autoScrollIntervalMs = 3500L,
        )
    }
}
