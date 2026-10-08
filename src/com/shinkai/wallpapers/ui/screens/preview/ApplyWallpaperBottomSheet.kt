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
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
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
                    subtitle = stringResource(R.string.preview_apply_subtitle),
                )
            }

            // 1. Both Displays (Recommended)
            item {
                val flag = WallpaperManager.FLAG_LOCK or WallpaperManager.FLAG_SYSTEM
                RichSelectionCard(
                    title = stringResource(R.string.preview_both_screens),
                    subtitle = stringResource(R.string.preview_both_screens_sub),
                    description = stringResource(R.string.preview_both_screens_desc),
                    icon = Icons.Rounded.CheckCircle,
                    selected = selectedTargetFlag == flag,
                    badgeText = stringResource(R.string.preview_badge_recommended),
                    onClick = { onSelectTarget(flag) },
                )
            }

            // 2. Lock Screen
            item {
                val flag = WallpaperManager.FLAG_LOCK
                RichSelectionCard(
                    title = stringResource(R.string.preview_lock_screen),
                    subtitle = stringResource(R.string.preview_lock_screen_sub),
                    description = stringResource(R.string.preview_lock_screen_desc),
                    icon = Icons.Rounded.Lock,
                    selected = selectedTargetFlag == flag,
                    onClick = { onSelectTarget(flag) },
                )
            }

            // 3. Home Screen
            item {
                val flag = WallpaperManager.FLAG_SYSTEM
                RichSelectionCard(
                    title = stringResource(R.string.preview_home_screen),
                    subtitle = stringResource(R.string.preview_home_screen_sub),
                    description = stringResource(R.string.preview_home_screen_desc),
                    icon = Icons.Rounded.Home,
                    selected = selectedTargetFlag == flag,
                    onClick = { onSelectTarget(flag) },
                )
            }
        }
    }
}
