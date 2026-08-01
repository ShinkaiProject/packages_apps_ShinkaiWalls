package com.shinkai.wallpapers

import android.app.WallpaperManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.DynamicColors

class PreviewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        DynamicColors.applyToActivityIfAvailable(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        val path = intent.getStringExtra("asset_path") ?: return finish()

        findViewById<ImageView>(R.id.preview_image).setImageBitmap(BitmapFactory.decodeStream(assets.open(path)))

        findViewById<android.view.View>(R.id.btn_back).setOnClickListener {
            finish()
        }

        findViewById<MaterialButton>(R.id.btn_berikutnya).setOnClickListener {
            showApplyBottomSheet(path)
        }
    }

    private fun showApplyBottomSheet(path: String) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_apply, null)
        dialog.setContentView(view)

        val cbLock = view.findViewById<CheckBox>(R.id.cb_lockscreen)
        val cbHome = view.findViewById<CheckBox>(R.id.cb_homescreen)
        val btnTerapkan = view.findViewById<MaterialButton>(R.id.btn_terapkan)
        val btnBatal = view.findViewById<MaterialButton>(R.id.btn_batal)

        btnBatal.setOnClickListener {
            dialog.dismiss()
        }

        btnTerapkan.setOnClickListener {
            var flag = 0
            if (cbLock.isChecked) flag = flag or WallpaperManager.FLAG_LOCK
            if (cbHome.isChecked) flag = flag or WallpaperManager.FLAG_SYSTEM

            if (flag == 0) {
                Toast.makeText(this, "Pilih minimal satu layar king!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            dialog.dismiss()
            setWallpaper(path, flag)
        }

        dialog.show()
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
