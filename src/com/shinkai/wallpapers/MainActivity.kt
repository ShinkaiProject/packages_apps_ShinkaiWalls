package com.shinkai.wallpapers

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONArray

class MainActivity : AppCompatActivity() {
    
    private var allWallpapers: List<Wallpaper> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {

        DynamicColors.applyToActivityIfAvailable(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val json = assets.open("wallpapers/manifest.json").bufferedReader().use { it.readText() }
        val arr = JSONArray(json)
        allWallpapers = (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Wallpaper(o.getString("name"), "wallpapers/${o.getString("file")}")
        }

        val grid = findViewById<RecyclerView>(R.id.wallpaper_grid)
        grid.layoutManager = GridLayoutManager(this, 2)
        
        setupAdapter(grid, allWallpapers)

        findViewById<ImageButton>(R.id.btn_about).setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Tentang App")
                .setIcon(R.drawable.ic_launcher) 
                .setMessage("Shinkai Walls\n\nDeveloper: Mnskkyy\nDesainer: SheMyWifee\n\nVersion 1.0")
                .setPositiveButton("Gasss", null)
                .show()
        }

        val searchBar = findViewById<TextInputEditText>(R.id.search_bar)
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                val keyword = s.toString().trim().lowercase()
                
                val filteredList = if (keyword.isEmpty()) {
                    allWallpapers
                } else {
                    allWallpapers.filter { it.name.lowercase().contains(keyword) }
                }
                
                setupAdapter(grid, filteredList)
            }
        })
    }

    private fun setupAdapter(grid: RecyclerView, list: List<Wallpaper>) {
        grid.adapter = WallpaperAdapter(list) { wp ->
            startActivity(Intent(this, PreviewActivity::class.java)
                .putExtra("asset_path", wp.assetPath)
                .putExtra("wallpaper_name", wp.name))
        }
    }
}
