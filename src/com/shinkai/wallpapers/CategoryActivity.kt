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
 * Detail screen of a single category. It reuses the existing wallpaper grid, wallpaper preview
 * and wallpaper model, the only difference being that the list is already filtered.
 */
class CategoryActivity : AppCompatActivity() {

    private lateinit var grid: RecyclerView
    private lateinit var title: TextView
    private lateinit var empty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {

        DynamicColors.applyToActivityIfAvailable(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        val categoryId = intent.getStringExtra(EXTRA_CATEGORY_ID)
        if (categoryId == null) {
            finish()
            return
        }
        val fallbackTitle = intent.getStringExtra(EXTRA_CATEGORY_TITLE)

        title = findViewById(R.id.category_title)
        empty = findViewById(R.id.category_empty)

        grid = findViewById(R.id.wallpaper_grid)
        grid.layoutManager = GridLayoutManager(this, 2)
        grid.setHasFixedSize(true)
        grid.setItemViewCacheSize(20)

        findViewById<View>(R.id.btn_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        loadCategory(categoryId, fallbackTitle)
    }

    private fun loadCategory(categoryId: String, fallbackTitle: String?) {
        lifecycleScope.launch {
            try {
                val wallpapers = WallpaperRepository.load()
                val category = WallCategories.resolve(this@CategoryActivity, wallpapers)
                    .firstOrNull { it.id == categoryId }

                if (isFinishing || isDestroyed) return@launch

                val wallpapersOfCategory = category?.wallpapers.orEmpty()
                val name = category?.title ?: fallbackTitle ?: categoryId

                setTitle(name)
                title.text = name
                grid.adapter = WallpaperAdapter(wallpapersOfCategory) { wallpaper ->
                    openWallpaperPreview(wallpaper)
                }
                empty.visibility = if (wallpapersOfCategory.isEmpty()) View.VISIBLE else View.GONE
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@CategoryActivity, R.string.error_network, Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        const val EXTRA_CATEGORY_ID = "category_id"
        const val EXTRA_CATEGORY_TITLE = "category_title"
    }
}
