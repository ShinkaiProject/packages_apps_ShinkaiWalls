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
import com.google.android.material.dialog.MaterialAlertDialogBuilder

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

        findViewById<MaterialButton>(R.id.btn_next).setOnClickListener {
            showApplyBottomSheet(path)
        }
    }

    private fun showApplyBottomSheet(path: String) {
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
            setWallpaperWithLoading(path, flag)
        }

        dialog.show()
    }

    private fun setWallpaperWithLoading(path: String, flag: Int) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_loading, null)
        
        val loadingDialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        loadingDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        loadingDialog.show()

        Thread {
            try {

                val displayMetrics = resources.displayMetrics
                val targetWidth = displayMetrics.widthPixels
                val targetHeight = displayMetrics.heightPixels

                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                assets.open(path).use { BitmapFactory.decodeStream(it, null, options) }

                options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight)
                options.inJustDecodeBounds = false
                options.inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888 

                val optimizedBmp = assets.open(path).use { BitmapFactory.decodeStream(it, null, options) }

                if (optimizedBmp != null) {
                    WallpaperManager.getInstance(this).setBitmap(optimizedBmp, null, true, flag)
                    optimizedBmp.recycle() 
                }
                
                runOnUiThread {
                    loadingDialog.dismiss()
                    Toast.makeText(this, "Wallpaper successfully installed.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    loadingDialog.dismiss()
                    Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
