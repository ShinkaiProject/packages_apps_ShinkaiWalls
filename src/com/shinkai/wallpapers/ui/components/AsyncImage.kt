package com.shinkai.wallpapers.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.shinkai.wallpapers.NativeLib
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ImageMemoryCache {
  private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
  private val cacheSize = (maxMemory / 8).coerceAtLeast(1024)

  private val cache =
      object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
          return bitmap.byteCount / 1024
        }
      }

  fun get(key: String): Bitmap? = cache.get(key)

  fun put(key: String, bitmap: Bitmap) {
    cache.put(key, bitmap)
  }
}

private fun decodeSampledBitmap(path: String, maxDim: Int = 1920): Bitmap? {
  val options =
      BitmapFactory.Options().apply {
        inJustDecodeBounds = true
      }
  BitmapFactory.decodeFile(path, options)

  var inSampleSize = 1
  if (options.outHeight > maxDim || options.outWidth > maxDim) {
    val halfHeight = options.outHeight / 2
    val halfWidth = options.outWidth / 2
    while ((halfHeight / inSampleSize) >= maxDim && (halfWidth / inSampleSize) >= maxDim) {
      inSampleSize *= 2
    }
  }
  options.inSampleSize = inSampleSize
  options.inJustDecodeBounds = false
  return BitmapFactory.decodeFile(path, options)
}

@Composable
fun AsyncImage(
    model: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
  val context = LocalContext.current
  var bitmap by remember(model) { mutableStateOf(model?.let { ImageMemoryCache.get(it) }) }
  var isLoading by remember(model) { mutableStateOf(bitmap == null && !model.isNullOrBlank()) }

  LaunchedEffect(model) {
    if (bitmap == null && !model.isNullOrBlank()) {
      isLoading = true
      withContext(Dispatchers.IO) {
        try {
          val localPath = NativeLib.downloadImage(model, context.cacheDir.absolutePath)
          val localFile = File(localPath)
          if (localFile.exists()) {
            val decoded = decodeSampledBitmap(localFile.absolutePath)
            if (decoded != null) {
              ImageMemoryCache.put(model, decoded)
              withContext(Dispatchers.Main) {
                bitmap = decoded
                isLoading = false
              }
            }
          }
        } catch (e: Exception) {
          e.printStackTrace()
          withContext(Dispatchers.Main) {
            isLoading = false
          }
        }
      }
    }
  }

  Box(modifier = modifier, contentAlignment = Alignment.Center) {
    if (bitmap != null) {
      Image(
          bitmap = bitmap!!.asImageBitmap(),
          contentDescription = contentDescription,
          modifier = Modifier.fillMaxSize(),
          contentScale = contentScale,
      )
    } else {
      Box(
          modifier =
              Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainerHigh),
          contentAlignment = Alignment.Center,
      ) {
        if (isLoading) {
          CircularProgressIndicator(
              modifier = Modifier.size(24.dp),
              strokeWidth = 2.dp,
              color = MaterialTheme.colorScheme.primary,
          )
        }
      }
    }
  }
}
