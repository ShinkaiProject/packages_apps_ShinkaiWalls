package com.shinkai.wallpapers

import android.app.WallpaperManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.color.DynamicColors

class PreviewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        DynamicColors.applyToActivityIfAvailable(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        val path = intent.getStringExtra("asset_path") ?: return finish()
        val name = intent.getStringExtra("wallpaper_name") ?: ""

        findViewById<ImageView>(R.id.preview_image).setImageBitmap(BitmapFactory.decodeStream(assets.open(path)))
        findViewById<TextView>(R.id.preview_title).text = name

        findViewById<Button>(R.id.btn_set_home).setOnClickListener { setWallpaper(path, WallpaperManager.FLAG_SYSTEM) }
        findViewById<Button>(R.id.btn_set_lock).setOnClickListener { setWallpaper(path, WallpaperManager.FLAG_LOCK) }
        findViewById<Button>(R.id.btn_set_both).setOnClickListener {
            setWallpaper(path, WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
        }
    }

    private fun setWallpaper(path: String, flag: Int) {
        Thread {
            try {
                val bmp = assets.open(path).use { BitmapFactory.decodeStream(it) }
                WallpaperManager.getInstance(this).setBitmap(bmp, null, true, flag)
                runOnUiThread { Toast.makeText(this, "Set berhasil", Toast.LENGTH_SHORT).show(); finish() }
            } catch (e: Exception) {
                runOnUiThread { Toast.makeText(this, "Gagal: ${e.message}", Toast.LENGTH_SHORT).show() }
            }
        }.start()
    }
}
