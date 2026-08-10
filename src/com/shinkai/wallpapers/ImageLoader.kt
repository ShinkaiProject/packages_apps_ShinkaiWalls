package com.shinkai.wallpapers

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.LruCache
import android.widget.ImageView
import java.lang.ref.WeakReference
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
        val viewRef = WeakReference(imageView)

        executor.execute {
            try {
                // Url Image Downloader
                val connection = URL(imageUrl).openConnection()
                connection.connectTimeout = 10_000
                connection.readTimeout = 10_000
                val stream = connection.getInputStream()
                val bitmap = BitmapFactory.decodeStream(stream)
                stream.close()

                if (bitmap != null) {
                    memoryCache.put(imageUrl, bitmap)

                    handler.post {
                        val view = viewRef.get()
                        if (view != null && view.tag == imageUrl) {
                            view.setImageBitmap(bitmap)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
