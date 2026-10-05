package com.shinkai.wallpapers.ui.screens.preview

import android.app.WallpaperManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shinkai.wallpapers.R
import com.shinkai.wallpapers.data.model.Wallpaper
import com.shinkai.wallpapers.ui.components.AsyncImage
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewScreen(
    wallpaper: Wallpaper,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  var showSheet by remember { mutableStateOf(false) }
  var isApplying by remember { mutableStateOf(false) }
  var applyLock by remember { mutableStateOf(true) }
  var applyHome by remember { mutableStateOf(true) }
  val sheetState = rememberModalBottomSheetState()

  BackHandler(onBack = onBack)

  Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
    AsyncImage(
        model = wallpaper.fullUrl.ifEmpty { wallpaper.assetPath },
        contentDescription = wallpaper.name,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize(),
    )

    IconButton(
        onClick = onBack,
        modifier =
            Modifier.statusBarsPadding()
                .padding(16.dp)
                .size(48.dp)
                .background(Color.Black.copy(alpha = 0.45f), CircleShape),
    ) {
      Icon(
          imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
          contentDescription = "Back",
          tint = Color.White,
      )
    }

    Button(
        onClick = { showSheet = true },
        modifier =
            Modifier.align(Alignment.BottomCenter)
                .fillMaxWidth(0.85f)
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
                .height(56.dp),
    ) {
      Text(stringResource(R.string.preview_apply))
    }

    if (isApplying) {
      Box(
          modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.65f)),
          contentAlignment = Alignment.Center,
      ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
      }
    }
  }

  if (showSheet) {
    ModalBottomSheet(
        onDismissRequest = { showSheet = false },
        sheetState = sheetState,
    ) {
      Column(
          modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(24.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        Text(
            text = stringResource(R.string.preview_apply_question),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
          Checkbox(checked = applyLock, onCheckedChange = { applyLock = it })
          Text(
              text = stringResource(R.string.preview_lock_screen),
              modifier = Modifier.padding(start = 8.dp),
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Checkbox(checked = applyHome, onCheckedChange = { applyHome = it })
          Text(
              text = stringResource(R.string.preview_home_screen),
              modifier = Modifier.padding(start = 8.dp),
          )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
          TextButton(onClick = { showSheet = false }) {
            Text(stringResource(R.string.btn_cancel))
          }
          Button(
              onClick = {
                var flag = 0
                if (applyLock) flag = flag or WallpaperManager.FLAG_LOCK
                if (applyHome) flag = flag or WallpaperManager.FLAG_SYSTEM

                if (flag == 0) {
                  Toast.makeText(context, R.string.preview_select_screen, Toast.LENGTH_SHORT).show()
                  return@Button
                }
                showSheet = false
                isApplying = true
                scope.launch {
                  applyWallpaperToDevice(context, wallpaper, flag)
                  isApplying = false
                  onBack()
                }
              }
          ) {
            Text(stringResource(R.string.preview_apply))
          }
        }
        Spacer(modifier = Modifier.height(8.dp))
      }
    }
  }
}

private suspend fun applyWallpaperToDevice(context: Context, wallpaper: Wallpaper, flag: Int) =
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
