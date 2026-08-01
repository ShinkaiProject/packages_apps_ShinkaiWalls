package com.shinkai.wallpapers

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val json = assets.open("wallpapers/manifest.json").bufferedReader().use { it.readText() }
        val arr = JSONArray(json)
        val wallpapers = (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Wallpaper(o.getString("name"), "wallpapers/${o.getString("file")}")
        }

        val grid = findViewById<RecyclerView>(R.id.wallpaper_grid)
        grid.layoutManager = GridLayoutManager(this, 2)
        grid.adapter = WallpaperAdapter(wallpapers) { wp ->
            startActivity(Intent(this, PreviewActivity::class.java)
                .putExtra("asset_path", wp.assetPath)
                .putExtra("wallpaper_name", wp.name))
        }
    }
}
