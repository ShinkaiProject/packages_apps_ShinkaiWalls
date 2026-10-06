package com.shinkai.wallpapers.ui.screens.preview

import android.app.WallpaperManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shinkai.wallpapers.R
import com.shinkai.wallpapers.ui.components.AuriyaDragHandle
import com.shinkai.wallpapers.ui.components.BottomSheetHeader
import com.shinkai.wallpapers.ui.components.RichSelectionCard


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplyWallpaperBottomSheet(
    selectedTargetFlag: Int,
    onSelectTarget: (flag: Int) -> Unit,
    onDismissRequest: () -> Unit,
    sheetState: SheetState,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { AuriyaDragHandle() },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = modifier,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 48.dp),
        ) {
            item {
                BottomSheetHeader(
                    title = stringResource(R.string.preview_apply_question),
                    subtitle = "Select destination display",
                )
            }

            // 1. Both Displays (Recommended)
            item {
                val flag = WallpaperManager.FLAG_LOCK or WallpaperManager.FLAG_SYSTEM
                RichSelectionCard(
                    title = "Home & Lock Screen",
                    subtitle = "Complete System Synchronization",
                    description =
                        "Seamlessly synchronizes and sets this wallpaper across both lock and home screens.",
                    icon = Icons.Rounded.CheckCircle,
                    selected = selectedTargetFlag == flag,
                    badgeText = "RECOMMENDED",
                    onClick = { onSelectTarget(flag) },
                )
            }

            // 2. Lock Screen
            item {
                val flag = WallpaperManager.FLAG_LOCK
                RichSelectionCard(
                    title = stringResource(R.string.preview_lock_screen),
                    subtitle = "Lock & Ambient Display",
                    description =
                        "Applies only to your lock screen, leaving your home screen launcher untouched.",
                    icon = Icons.Rounded.Lock,
                    selected = selectedTargetFlag == flag,
                    badgeText = "ACTIVE",
                    onClick = { onSelectTarget(flag) },
                )
            }

            // 3. Home Screen
            item {
                val flag = WallpaperManager.FLAG_SYSTEM
                RichSelectionCard(
                    title = stringResource(R.string.preview_home_screen),
                    subtitle = "Launcher & Application Grid",
                    description =
                        "Displays wallpaper exclusively on your desktop launcher behind apps and widgets.",
                    icon = Icons.Rounded.Home,
                    selected = selectedTargetFlag == flag,
                    badgeText = "ACTIVE",
                    onClick = { onSelectTarget(flag) },
                )
            }
        }
    }
}
