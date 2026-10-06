package com.shinkai.wallpapers.ui.screens.preview

import android.app.WallpaperManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.shinkai.wallpapers.R
import com.shinkai.wallpapers.data.model.Wallpaper
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Preview Screen: Composes dedicated reusable components:
 * - [PreviewTopBar] for top navigation & info
 * - [WallpaperPreviewCanvas] for the center floating phone preview
 * - [PreviewApplyBar] for bottom action button
 * - [ApplyWallpaperBottomSheet] for destination selection popup
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewScreen(
    wallpaper: Wallpaper,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    @Suppress("UNUSED_PARAMETER") wallpaperList: List<Wallpaper> = emptyList(),
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  var isApplying by remember { mutableStateOf(false) }
  var showApplySheet by remember { mutableStateOf(false) }
  val applySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  var selectedTargetFlag by remember {
    mutableIntStateOf(WallpaperManager.FLAG_LOCK or WallpaperManager.FLAG_SYSTEM)
  }

  BackHandler(onBack = onBack)

  Column(
      modifier =
          modifier
              .fillMaxSize()
              .background(MaterialTheme.colorScheme.surfaceContainer)
              .statusBarsPadding()
              .navigationBarsPadding(),
      horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    // 1. Top Bar
    PreviewTopBar(
        title = wallpaper.name,
        onBack = onBack,
    )

    // 2. Floating Phone Canvas (Center)
    WallpaperPreviewCanvas(
        wallpaper = wallpaper,
        modifier = Modifier.weight(1f),
    )

    // 3. Bottom Apply Bar
    PreviewApplyBar(
        isApplying = isApplying,
        onApplyClick = { showApplySheet = true },
    )
  }

  // 4. Auriya-Style Apply BottomSheet
  if (showApplySheet) {
    ApplyWallpaperBottomSheet(
        selectedTargetFlag = selectedTargetFlag,
        sheetState = applySheetState,
        onDismissRequest = { showApplySheet = false },
        onSelectTarget = { flag ->
          selectedTargetFlag = flag
          showApplySheet = false
          isApplying = true
          scope.launch {
            applyWallpaperToDevice(context, wallpaper, flag)
            isApplying = false
            onBack()
          }
        },
    )
  }
}

private suspend fun applyWallpaperToDevice(
    context: Context,
    wallpaper: Wallpaper,
    flag: Int,
) =
    withContext(Dispatchers.IO) {
      try {
        val safeFileName = "${wallpaper.name.replace(Regex("[^A-Za-z0-9]"), "_")}.jpg"
        val wallsDir = File(context.filesDir, "saved_wallpapers").apply { mkdirs() }
        val localFile = File(wallsDir, safeFileName)

        if (!localFile.exists()) {
          val url = wallpaper.fullUrl.ifEmpty { wallpaper.assetPath }
          val conn =
              URL(url).openConnection().apply {
                connectTimeout = 10_000
                readTimeout = 10_000
              }
          conn.getInputStream().use { input ->
            FileOutputStream(localFile).use { output -> input.copyTo(output) }
          }
        }

        val wm = WallpaperManager.getInstance(context)
        localFile.inputStream().use { stream ->
          wm.setStream(stream, null, true, flag)
        }
        withContext(Dispatchers.Main) {
          Toast.makeText(context, R.string.preview_wallpaper_set, Toast.LENGTH_SHORT).show()
        }
      } catch (e: Exception) {
        withContext(Dispatchers.Main) {
          Toast.makeText(
                  context,
                  context.getString(R.string.preview_wallpaper_failed, e.message),
                  Toast.LENGTH_LONG,
              )
              .show()
        }
      }
    }
