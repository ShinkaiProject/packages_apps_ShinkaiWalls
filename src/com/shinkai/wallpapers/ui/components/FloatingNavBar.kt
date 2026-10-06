package com.shinkai.wallpapers.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shinkai.wallpapers.ui.navigation.TopLevelDestination

@Composable
fun FloatingNavBar(
    currentDestination: TopLevelDestination,
    onSelect: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
  Box(
      modifier = modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = 20.dp),
      contentAlignment = Alignment.Center,
  ) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 6.dp,
        shadowElevation = 10.dp,
    ) {
      Row(
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
      ) {
        TopLevelDestination.entries.forEach { destination ->
          val selected = destination == currentDestination
          val bg by
              animateColorAsState(
                  targetValue =
                      if (selected) MaterialTheme.colorScheme.primaryContainer
                      else Color.Transparent,
                  label = "pill_bg",
              )
          val contentColor by
              animateColorAsState(
                  targetValue =
                      if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                      else MaterialTheme.colorScheme.onSurfaceVariant,
                  label = "pill_color",
              )

          Row(
              modifier =
                  Modifier.clip(CircleShape)
                      .background(bg)
                      .clickable { onSelect(destination) }
                      .padding(
                          horizontal = if (selected) 22.dp else 18.dp,
                          vertical = 14.dp,
                      ),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Icon(
                imageVector = destination.icon,
                contentDescription = stringResource(destination.titleRes),
                tint = contentColor,
                modifier = Modifier.size(24.dp),
            )
            if (selected) {
              Text(
                  text = stringResource(destination.titleRes),
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp,
                  color = contentColor,
              )
            }
          }
        }
      }
    }
  }
}
