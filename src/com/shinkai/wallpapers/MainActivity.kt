package com.shinkai.wallpapers

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val wallpapers = (assets.list("wallpapers") ?: emptyArray())
            .map { Wallpaper(it.substringBeforeLast("."), "wallpapers/$it") }

        val grid = findViewById<RecyclerView>(R.id.wallpaper_grid)
        grid.layoutManager = GridLayoutManager(this, 2)
        grid.adapter = WallpaperAdapter(wallpapers) { wp ->
            startActivity(Intent(this, PreviewActivity::class.java)
                .putExtra("asset_path", wp.assetPath))
        }
    }
}
