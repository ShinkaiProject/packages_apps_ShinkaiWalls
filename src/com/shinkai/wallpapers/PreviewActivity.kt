package com.shinkai.wallpapers

import android.app.WallpaperManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.net.URL

class PreviewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        DynamicColors.applyToActivityIfAvailable(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        // Menerima URL full_url atau asset_path dari Intent
        val imageUrl = intent.getStringExtra("asset_path") ?: return finish()
        val imageView = findViewById<ImageView>(R.id.preview_image)

        // Load gambar preview menggunakan ImageLoader online
        ImageLoader.load(imageUrl, imageView)

        findViewById<View>(R.id.btn_back).setOnClickListener {
            finish()
        }

        findViewById<MaterialButton>(R.id.btn_next).setOnClickListener {
            showApplyBottomSheet(imageUrl)
        }
    }

    private fun showApplyBottomSheet(url: String) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_apply, null)
        dialog.setContentView(view)

        val cbLock = view.findViewById<CheckBox>(R.id.cb_lockscreen)
        val cbHome = view.findViewById<CheckBox>(R.id.cb_homescreen)
        val btnApply = view.findViewById<MaterialButton>(R.id.btn_apply)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btn_cancel)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnApply.setOnClickListener {
            var flag = 0
            if (cbLock.isChecked) flag = flag or WallpaperManager.FLAG_LOCK
            if (cbHome.isChecked) flag = flag or WallpaperManager.FLAG_SYSTEM

            if (flag == 0) {
                Toast.makeText(this, "select at least 1 screen!!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            dialog.dismiss()
            setWallpaperWithLoading(url, flag)
        }

        dialog.show()
    }

    private fun setWallpaperWithLoading(urlString: String, flag: Int) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_loading, null)
        
        val loadingDialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        loadingDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        loadingDialog.show()

        Thread {
            try {
                // Download dan decode bitmap langsung dari URL internet
                val connection = URL(urlString).openConnection()
                connection.connectTimeout = 10_000
                connection.readTimeout = 10_000
                val stream = connection.getInputStream()
                val bitmap = BitmapFactory.decodeStream(stream)
                stream.close()

                if (bitmap != null) {
                    WallpaperManager.getInstance(this).setBitmap(bitmap, null, true, flag)
                    bitmap.recycle()

                    runOnUiThread {
                        if (!isFinishing && !isDestroyed) {
                            loadingDialog.dismiss()
                            Toast.makeText(this, "Wallpaper successfully installed.", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                } else {
                    throw Exception("Gagal mendecode gambar dari server.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    if (!isFinishing && !isDestroyed) {
                        loadingDialog.dismiss()
                        Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }.start()
    }
}
