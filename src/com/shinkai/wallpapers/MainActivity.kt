package com.shinkai.wallpapers

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.MaterialColors
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    /** Wallpapers listed by Home, which defaults to a single category of the library. */
    private var homeWallpapers: List<Wallpaper> = listOf()
    private lateinit var grid: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var navBar: FloatingNavigationView

    /**
     * One shot marker that this Home instance is about to be left for Walls, so the wallpaper
     * cards replay their rise once it is resumed again. Home is reused through `CLEAR_TOP` and
     * keeps its adapter, which is why the layout animation has to be requested by hand here
     * instead of running on its own.
     */
    private var replayCardsOnResume = false

    override fun onCreate(savedInstanceState: Bundle?) {

        DynamicColors.applyToActivityIfAvailable(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        grid = findViewById(R.id.wallpaper_grid)
        grid.layoutManager = GridLayoutManager(this, 2)
        
        // Optimasi RecyclerView untuk scroll super halus
        grid.setHasFixedSize(true)
        grid.setItemViewCacheSize(20)

        swipeRefresh = findViewById(R.id.swipe_refresh)
        swipeRefresh.setColorSchemeColors(
            MaterialColors.getColor(swipeRefresh, android.R.attr.colorPrimary)
        )
        swipeRefresh.setProgressBackgroundColorSchemeColor(
            MaterialColors.getColor(swipeRefresh, com.google.android.material.R.attr.colorSurface)
        )
        swipeRefresh.setOnRefreshListener {
            fetchWallpapersOnline()
        }

        navBar = findViewById(R.id.nav_floating)
        navBar.setItems(TopLevelDestination.entries)
        navBar.setOnItemSelectedListener { destination ->
            if (destination != TopLevelDestination.HOME) {
                replayCardsOnResume = true
                openDestination(destination)
            }
        }

        fetchWallpapersOnline()
    }

    /**
     * Being resumed is what makes this the current destination, so the active pill is derived
     * here instead of from a click. That covers creation, back navigation, `CLEAR_TOP` reuse of
     * this instance, background returns and configuration changes.
     */
    override fun onResume() {
        super.onResume()
        navBar.showDestination(TopLevelDestination.HOME)
        if (replayCardsOnResume) {
            replayCardsOnResume = false
            if (homeWallpapers.isNotEmpty()) grid.scheduleLayoutAnimation()
        }
    }

    private fun fetchWallpapersOnline() {
        lifecycleScope.launch {
            try {
                val library = WallpaperRepository.load(forceRefresh = true)
                homeWallpapers = WallCategories.wallpapersIn(
                    library,
                    WallCategories.DEFAULT_HOME_CATEGORY_ID
                )
                setupAdapter(grid, homeWallpapers)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MainActivity, R.string.error_network, Toast.LENGTH_SHORT).show()
            } finally {
                swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun setupAdapter(grid: RecyclerView, list: List<Wallpaper>) {
        grid.adapter = WallpaperAdapter(list) { wp ->
            openWallpaperPreview(wp)
        }
    }
}
