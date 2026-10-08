package com.shinkai.wallpapers.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.LruCache
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
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
          val byteCount =
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                bitmap.allocationByteCount
              } else {
                bitmap.byteCount
              }
          return (byteCount / 1024).coerceAtLeast(1)
        }
      }

  fun get(key: String): Bitmap? = cache.get(key)

  fun put(key: String, bitmap: Bitmap) {
    cache.put(key, bitmap)
  }
}

private fun decodeSampledBitmap(path: String, targetMaxDim: Int): Bitmap? {
  val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
  BitmapFactory.decodeFile(path, options)

  if (options.outWidth <= 0 || options.outHeight <= 0) return null

  val largestSide = maxOf(options.outWidth, options.outHeight)
  var inSampleSize = 1
  while ((largestSide / (inSampleSize * 2)) >= targetMaxDim) {
    inSampleSize *= 2
  }

  options.inSampleSize = inSampleSize
  options.inJustDecodeBounds = false
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    options.inPreferredConfig = Bitmap.Config.HARDWARE
  } else {
    options.inPreferredConfig = Bitmap.Config.RGB_565
  }
  return BitmapFactory.decodeFile(path, options)
}

@Composable
fun AsyncImage(
    model: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    fallbackModel: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
    targetMaxDim: Int = 720,
    indicatorSize: Dp = 44.dp,
) {
  val context = LocalContext.current
  val cacheKey = remember(model, targetMaxDim) {
    if (model.isNullOrBlank()) "" else "$model@$targetMaxDim"
  }

  var bitmap by remember(cacheKey) {
    mutableStateOf(if (cacheKey.isNotEmpty()) ImageMemoryCache.get(cacheKey) else null)
  }
  var isLoading by remember(cacheKey) {
    mutableStateOf(bitmap == null && !model.isNullOrBlank())
  }

  LaunchedEffect(cacheKey, fallbackModel) {
    if (bitmap == null && !model.isNullOrBlank()) {
      isLoading = true
      val decoded = withContext(Dispatchers.IO) {
        val firstAttempt = try {
          val localPath = NativeLib.downloadImage(model, context.cacheDir.absolutePath)
          val localFile = File(localPath)
          if (localFile.exists()) decodeSampledBitmap(localFile.absolutePath, targetMaxDim) else null
        } catch (_: Exception) {
          null
        }

        if (firstAttempt != null) {
          firstAttempt
        } else if (!fallbackModel.isNullOrBlank() && fallbackModel != model) {
          try {
            val fbPath = NativeLib.downloadImage(fallbackModel, context.cacheDir.absolutePath)
            val fbFile = File(fbPath)
            if (fbFile.exists()) decodeSampledBitmap(fbFile.absolutePath, targetMaxDim) else null
          } catch (_: Exception) {
            null
          }
        } else {
          null
        }
      }

      if (decoded != null) {
        ImageMemoryCache.put(cacheKey, decoded)
        bitmap = decoded
      }
      isLoading = false
    }
  }

  val alpha by animateFloatAsState(
      targetValue = if (bitmap != null) 1f else 0f,
      animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
      label = "asyncImageAlpha",
  )

  Box(modifier = modifier, contentAlignment = Alignment.Center) {
    if (bitmap != null) {
      Image(
          bitmap = bitmap!!.asImageBitmap(),
          contentDescription = contentDescription,
          modifier = Modifier.fillMaxSize().graphicsLayer { this.alpha = alpha },
          contentScale = contentScale,
      )
    } else {
      Box(
          modifier =
              Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainerHigh),
          contentAlignment = Alignment.Center,
      ) {
        if (isLoading) {
          if (indicatorSize <= 24.dp) {
            ShinkaiLoadingIndicator(
                size = indicatorSize,
                contained = false,
            )
          } else {
            ShinkaiCircularWavyProgressIndicator(
                size = indicatorSize,
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
            )
          }
        }
      }
    }
  }
}
