package com.shinkai.wallpapers

import android.app.Activity
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes

/**
 * Navigation helpers built on top of the existing Intent based navigation of Shinkai Walls.
 *
 * Home -> Walls -> Category detail -> Wallpaper preview is the resulting flow; deeper screens
 * keep the plain Android back behaviour of the project.
 */

/**
 * The top level destinations of the app, and the single source of truth for them.
 *
 * Each destination owns its own floating toolbar item, so the bar, its labels and its icons can
 * never drift apart, and the active pill is always rendered from a destination instead of from
 * a click count. The current destination is whatever [android.app.Activity.onResume] reports,
 * which keeps the pill correct after creation, back navigation, `CLEAR_TOP` deliveries, returning
 * from the background and configuration changes without any duplicated navigation state.
 */
enum class TopLevelDestination(
    @param:IdRes val navItemId: Int,
    @param:DrawableRes val iconRes: Int,
    @param:StringRes val titleRes: Int
) {
    HOME(R.id.nav_item_home, R.drawable.ic_home, R.string.nav_home),
    WALLS(R.id.nav_item_walls, R.drawable.ic_walls, R.string.nav_walls)
}

/** Opens the screen of [destination]; callers skip the entry that is already visible. */
fun Activity.openDestination(destination: TopLevelDestination) {
    when (destination) {
        TopLevelDestination.HOME -> openHome()
        TopLevelDestination.WALLS -> openWallCategories()
    }
}

fun Activity.navigateTo(intent: Intent) {
    startActivity(intent)
    @Suppress("DEPRECATION")
    overridePendingTransition(R.anim.nav_fade_in, R.anim.nav_fade_out)
}

fun Activity.openWallCategories() {
    // SINGLE_TOP keeps a repeated tap on the same destination from stacking a second Walls
    // instance on top of the running one; the task stack stays the source of truth for it.
    navigateTo(
        Intent(this, WallsActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
    )
}

fun Activity.openWallCategory(category: WallCategory) {
    navigateTo(
        Intent(this, CategoryActivity::class.java)
            .putExtra(CategoryActivity.EXTRA_CATEGORY_ID, category.id)
            .putExtra(CategoryActivity.EXTRA_CATEGORY_TITLE, category.title)
    )
}

fun Activity.openWallpaperPreview(wallpaper: Wallpaper) {
    navigateTo(
        Intent(this, PreviewActivity::class.java)
            .putExtra(PreviewActivity.EXTRA_ASSET_PATH, wallpaper.fullUrl)
            .putExtra(PreviewActivity.EXTRA_WALLPAPER_NAME, wallpaper.name)
    )
}

/**
 * Returns to the already running Home instance instead of stacking a second one on top of it,
 * so back from Home keeps the default Android behaviour of leaving the app.
 */
fun Activity.openHome() {
    startActivity(
        Intent(this, MainActivity::class.java).addFlags(
            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        )
    )
}
