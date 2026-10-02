package com.shinkai.wallpapers

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.DynamicColors
import kotlinx.coroutines.launch

/**
 * Walls, the top level destination that browses wallpapers per category.
 */
class WallsActivity : AppCompatActivity() {

    private lateinit var grid: RecyclerView
    private lateinit var empty: TextView
    private lateinit var navBar: FloatingNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {

        DynamicColors.applyToActivityIfAvailable(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_walls)

        empty = findViewById(R.id.walls_empty)

        grid = findViewById(R.id.category_grid)
        grid.layoutManager = GridLayoutManager(this, spanCount())
        grid.setHasFixedSize(true)
        grid.setItemViewCacheSize(20)

        val adapter = WallCategoryAdapter { category -> openWallCategory(category) }
        grid.adapter = adapter

        navBar = findViewById(R.id.nav_floating)
        navBar.setItems(TopLevelDestination.entries)
        navBar.setOnItemSelectedListener { destination ->
            if (destination != TopLevelDestination.WALLS) openDestination(destination)
        }

        loadCategories(adapter)
    }

    /**
     * Being resumed is what makes this the current destination, so the active pill is derived
     * here instead of from a click. That keeps Walls highlighted after returning from a category,
     * from the launcher, or after Home was reopened with `CLEAR_TOP`.
     */
    override fun onResume() {
        super.onResume()
        navBar.showDestination(TopLevelDestination.WALLS)
    }

    private fun spanCount(): Int =
        if (resources.configuration.smallestScreenWidthDp >= WIDE_SCREEN_SW_DP) 3 else 2

    private fun loadCategories(adapter: WallCategoryAdapter) {
        lifecycleScope.launch {
            try {
                val wallpapers = WallpaperRepository.load()
                val categories = WallCategories.resolve(this@WallsActivity, wallpapers)
                if (isFinishing || isDestroyed) return@launch
                adapter.submit(categories)
                empty.visibility = if (categories.isEmpty()) View.VISIBLE else View.GONE
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@WallsActivity, R.string.error_network, Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val WIDE_SCREEN_SW_DP = 600
    }
}
