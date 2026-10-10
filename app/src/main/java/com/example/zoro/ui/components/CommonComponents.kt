package com.example.zoro.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zoro.model.GenerationMetrics
import com.example.ui.theme.ZoroCyan
import com.example.ui.theme.ZoroEmerald
import com.example.ui.theme.ZoroViolet

@Composable
fun TelemetryPill(
  metrics: GenerationMetrics,
  isLive: Boolean,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier
      .clip(RoundedCornerShape(20.dp))
      .border(
        width = 1.dp,
        brush = Brush.horizontalGradient(
          colors = if (isLive) listOf(ZoroCyan, ZoroEmerald) else listOf(MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.outline)
        ),
        shape = RoundedCornerShape(20.dp)
      ),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      if (isLive) {
        Box(
          modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(ZoroEmerald)
        )
      }

      // Tokens per second
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Speed,
          contentDescription = null,
          tint = ZoroCyan,
          modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(3.dp))
        Text(
          text = "${metrics.tokensPerSecond} tok/s",
          style = MaterialTheme.typography.labelSmall,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          color = ZoroCyan
        )
      }

      // Time to First Token
      if (metrics.timeToFirstTokenMs > 0) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Timer,
            contentDescription = null,
            tint = ZoroViolet,
            modifier = Modifier.size(14.dp)
          )
          Spacer(Modifier.width(3.dp))
          Text(
            text = "${metrics.timeToFirstTokenMs}ms TTFT",
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      // RAM
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Memory,
          contentDescription = null,
          tint = ZoroEmerald,
          modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(3.dp))
        Text(
          text = "${metrics.ramUsageMb}MB",
          style = MaterialTheme.typography.labelSmall,
          fontFamily = FontFamily.Monospace,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      // Quantization tag
      Text(
        text = metrics.quantizationMode.displayName.split(" ").first(),
        style = MaterialTheme.typography.labelSmall,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
      )
    }
  }
}

@Composable
fun ThoughtSection(
  thoughtText: String,
  modifier: Modifier = Modifier
) {
  var isExpanded by remember { mutableStateOf(false) }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(bottom = 8.dp),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
    ),
    border = androidx.compose.foundation.BorderStroke(1.dp, ZoroViolet.copy(alpha = 0.4f))
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(10.dp)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { isExpanded = !isExpanded },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Psychology,
            contentDescription = null,
            tint = ZoroViolet,
            modifier = Modifier.size(18.dp)
          )
          Spacer(Modifier.width(8.dp))
          Text(
            text = "ZORO Reasoning Stream",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = ZoroViolet
          )
        }
        Icon(
          imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
          contentDescription = if (isExpanded) "Collapse" else "Expand",
          tint = ZoroViolet,
          modifier = Modifier.size(18.dp)
        )
      }

      AnimatedVisibility(
        visible = isExpanded,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
      ) {
        Column(modifier = Modifier.padding(top = 8.dp)) {
          Text(
            text = thoughtText,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 18.sp
          )
        }
      }
    }
  }
}

@Composable
fun CopyButton(
  textToCopy: String,
  modifier: Modifier = Modifier
) {
  val clipboardManager = LocalClipboardManager.current
  var isCopied by remember { mutableStateOf(false) }

  IconButton(
    onClick = {
      clipboardManager.setText(AnnotatedString(textToCopy))
      isCopied = true
    },
    modifier = modifier.size(32.dp).testTag("copy_button")
  ) {
    Icon(
      imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
      contentDescription = "Copy message",
      tint = if (isCopied) ZoroEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.size(16.dp)
    )
  }
}
