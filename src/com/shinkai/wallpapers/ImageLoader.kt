package com.shinkai.wallpapers

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.LruCache
import android.widget.ImageView
import java.util.concurrent.Executors

object ImageLoader {

    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8

    private val memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
    }

    private val executor = Executors.newFixedThreadPool(4)
    private val handler = Handler(Looper.getMainLooper())

    fun load(assetPath: String, imageView: ImageView) {

        imageView.tag = assetPath

        val cachedBitmap = memoryCache.get(assetPath)
        if (cachedBitmap != null) {
            imageView.setImageBitmap(cachedBitmap)
            return
        }

        imageView.setImageDrawable(null)

        executor.execute {
            try {
                val stream = imageView.context.assets.open(assetPath)
                val opts = BitmapFactory.Options().apply { inSampleSize = 4 }
                val bitmap = BitmapFactory.decodeStream(stream, null, opts)
                stream.close()

                if (bitmap != null) {
                    memoryCache.put(assetPath, bitmap)
                    
                    handler.post {
                        if (imageView.tag == assetPath) {
                            imageView.setImageBitmap(bitmap)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
