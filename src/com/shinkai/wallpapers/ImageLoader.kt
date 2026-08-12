package com.shinkai.wallpapers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.LruCache
import android.widget.ImageView
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.lang.ref.WeakReference
import java.net.URL
import java.security.MessageDigest
import java.util.concurrent.Executors

object ImageLoader {

    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8

    // Memory Cache (RAM)
    private val memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
    }

    private val executor = Executors.newFixedThreadPool(4)
    private val handler = Handler(Looper.getMainLooper())

    fun load(context: Context, imageUrl: String, imageView: ImageView, targetWidth: Int = 0, targetHeight: Int = 0) {
        imageView.tag = imageUrl

        // 1. Cek Memory Cache (Instan dari RAM)
        val cachedBitmap = memoryCache.get(imageUrl)
        if (cachedBitmap != null) {
            imageView.setImageBitmap(cachedBitmap)
            return
        }

        imageView.setImageDrawable(null)
        val viewRef = WeakReference(imageView)

        executor.execute {
            try {
                // Konversi URL menjadi nama file unik menggunakan MD5
                val fileName = hashKeyForDisk(imageUrl)
                val cacheDir = File(context.cacheDir, "image_cache")
                if (!cacheDir.exists()) cacheDir.mkdirs()
                
                val localFile = File(cacheDir, fileName)

                // 2. Cek Disk Cache (Penyimpanan HP)
                if (!localFile.exists()) {
                    downloadToFile(imageUrl, localFile)
                }

                // 3. Decode Gambar dengan Downsampling
                val bitmap = decodeSampledBitmapFromFile(localFile.absolutePath, targetWidth, targetHeight)

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

    private fun downloadToFile(urlString: String, file: File) {
        val url = URL(urlString)
        val connection = url.openConnection()
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        
        val input: InputStream = connection.getInputStream()
        val tempFile = File(file.parent, "${file.name}.tmp")
        val output = FileOutputStream(tempFile)

        val buffer = ByteArray(4096)
        var bytesRead: Int
        while (input.read(buffer).also { bytesRead = it } != -1) {
            output.write(buffer, 0, bytesRead)
        }

        output.flush()
        output.close()
        input.close()

        // Rename file sementara ke file asli jika download selesai
        tempFile.renameTo(file)
    }

    private fun decodeSampledBitmapFromFile(path: String, reqWidth: Int, reqHeight: Int): Bitmap? {
        if (reqWidth <= 0 || reqHeight <= 0) {
            return BitmapFactory.decodeFile(path)
        }

        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(path, options)

        // Hitung faktor downsampling
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false

        return BitmapFactory.decodeFile(path, options)
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
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

    private fun hashKeyForDisk(key: String): String {
        val digest = MessageDigest.getInstance("MD5")
        digest.update(key.toByteArray())
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}

