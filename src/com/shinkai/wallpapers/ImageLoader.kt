package com.shinkai.wallpapers

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.LruCache
import android.widget.ImageView
import java.net.URL
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

    fun load(imageUrl: String, imageView: ImageView) {

        imageView.tag = imageUrl

        val cachedBitmap = memoryCache.get(imageUrl)
        if (cachedBitmap != null) {
            imageView.setImageBitmap(cachedBitmap)
            return
        }

        imageView.setImageDrawable(null)

        executor.execute {
            try {
                // Url Image Downloader
                val stream = URL(imageUrl).openStream()
                val bitmap = BitmapFactory.decodeStream(stream)
                stream.close()

                if (bitmap != null) {
                    memoryCache.put(imageUrl, bitmap)
                    
                    handler.post {
                        if (imageView.tag == imageUrl) {
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
