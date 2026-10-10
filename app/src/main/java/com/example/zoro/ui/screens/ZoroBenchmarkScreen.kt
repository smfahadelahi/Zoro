package com.example.zoro.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zoro.engine.DeviceHardwareProfile
import com.example.zoro.model.QuantizationMode
import com.example.zoro.ui.ZoroUiState
import com.example.zoro.ui.ZoroViewModel
import com.example.ui.theme.ZoroAmber
import com.example.ui.theme.ZoroCyan
import com.example.ui.theme.ZoroEmerald
import com.example.ui.theme.ZoroRed
import com.example.ui.theme.ZoroViolet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZoroBenchmarkScreen(
  viewModel: ZoroViewModel,
  uiState: ZoroUiState,
  modifier: Modifier = Modifier
) {
  val hw = uiState.hardwareProfile

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.background,
          titleContentColor = MaterialTheme.colorScheme.onBackground
        ),
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Speed,
              contentDescription = null,
              tint = ZoroEmerald,
              modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column {
              Text(
                "Hardware & Benchmark",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                "Android Neural Profiling Suite",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      )
    }
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(horizontal = 14.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // 1. Real Device Hardware Profile Card
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
          ),
          border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Memory,
                  contentDescription = null,
                  tint = ZoroCyan,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                  text = "Host Device Telemetry",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
              }
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = ZoroCyan.copy(alpha = 0.15f)
              ) {
                Text(
                  text = hw?.androidVersion ?: "Android",
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                  style = MaterialTheme.typography.labelSmall,
                  fontFamily = FontFamily.Monospace,
                  color = ZoroCyan
                )
              }
            }

            Spacer(Modifier.height(14.dp))

            if (hw != null) {
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                HardwareRow("Device Model", "${hw.manufacturer} ${hw.model}")
                HardwareRow("CPU Cores / ABI", "${hw.cpuCores} Cores • ${hw.architecture}")
                HardwareRow("API Level", "SDK ${hw.apiLevel}")

                Spacer(Modifier.height(6.dp))

                // RAM Bar
                val ramFraction = if (hw.totalRamMb > 0) ((hw.totalRamMb - hw.availableRamMb).toFloat() / hw.totalRamMb.toFloat()).coerceIn(0f, 1f) else 0.5f
                Column {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text("Physical RAM Usage", style = MaterialTheme.typography.labelSmall)
                    Text(
                      "${hw.totalRamMb - hw.availableRamMb} / ${hw.totalRamMb} MB (${(ramFraction * 100).toInt()}%)",
                      style = MaterialTheme.typography.labelSmall,
                      fontFamily = FontFamily.Monospace,
                      color = ZoroEmerald
                    )
                  }
                  Spacer(Modifier.height(4.dp))
                  LinearProgressIndicator(
                    progress = { ramFraction },
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(6.dp)
                      .clip(RoundedCornerShape(3.dp)),
                    color = ZoroEmerald,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                  )
                }

                // JVM Heap Bar
                val jvmFraction = if (hw.jvmHeapMaxMb > 0) (hw.jvmHeapUsedMb.toFloat() / hw.jvmHeapMaxMb.toFloat()).coerceIn(0f, 1f) else 0.2f
                Column {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text("JVM Heap Allocated", style = MaterialTheme.typography.labelSmall)
                    Text(
                      "${hw.jvmHeapUsedMb} / ${hw.jvmHeapMaxMb} MB",
                      style = MaterialTheme.typography.labelSmall,
                      fontFamily = FontFamily.Monospace,
                      color = ZoroViolet
                    )
                  }
                  Spacer(Modifier.height(4.dp))
                  LinearProgressIndicator(
                    progress = { jvmFraction },
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(6.dp)
                      .clip(RoundedCornerShape(3.dp)),
                    color = ZoroViolet,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                  )
                }
              }
            } else {
              CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
          }
        }
      }

      // 2. Benchmark Runner Card
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
          ),
          border = androidx.compose.foundation.BorderStroke(1.dp, ZoroEmerald.copy(alpha = 0.5f))
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "ZORO On-Device Benchmark",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
              text = "Stress-tests mobile CPU GEMM FLOPS, prefill latency, and KV-cache autoregressive decode throughput.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(14.dp))

            Text("Select Quantization Target:", style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              for (mode in QuantizationMode.values()) {
                val isSelected = uiState.activeQuantization == mode
                FilterChip(
                  selected = isSelected,
                  onClick = { viewModel.setQuantization(mode) },
                  label = { Text(mode.displayName.split(" ").first()) },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ZoroEmerald.copy(alpha = 0.2f),
                    selectedLabelColor = ZoroEmerald
                  ),
                  border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = if (isSelected) ZoroEmerald else MaterialTheme.colorScheme.outline
                  ),
                  modifier = Modifier.testTag("bench_mode_${mode.key}")
                )
              }
            }

            Spacer(Modifier.height(16.dp))

            Button(
              onClick = { viewModel.runDeviceBenchmark() },
              enabled = !uiState.isBenchmarking,
              modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("run_benchmark_button"),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = ZoroEmerald,
                contentColor = Color.Black
              )
            ) {
              if (uiState.isBenchmarking) {
                CircularProgressIndicator(
                  modifier = Modifier.size(20.dp),
                  color = Color.Black,
                  strokeWidth = 2.dp
                )
                Spacer(Modifier.width(10.dp))
                Text("Executing Matrix Math & Decode Pass...", fontWeight = FontWeight.Bold)
              } else {
                Icon(
                  imageVector = Icons.Default.PlayArrow,
                  contentDescription = null,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Run Mobile Benchmark", fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }

      // 3. Latest Benchmark Result Card (if available)
      if (uiState.latestBenchmarkResult != null) {
        val result = uiState.latestBenchmarkResult
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.8f)
            ),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, ZoroEmerald)
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = "Official Performance Rating",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = "Tested format: ${result.quantizationTested.displayName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
                Box(
                  modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(
                      Brush.radialGradient(listOf(ZoroEmerald, ZoroCyan))
                    ),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = result.scoreGrade,
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp
                  )
                }
              }

              Spacer(Modifier.height(14.dp))

              // Metric dials grid
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                ResultMetricPill(
                  title = "Decode Speed",
                  value = "${result.decodeTokensPerSec}",
                  unit = "tok/s",
                  color = ZoroEmerald,
                  modifier = Modifier.weight(1f)
                )
                ResultMetricPill(
                  title = "Prefill Speed",
                  value = "${result.prefillTokensPerSec}",
                  unit = "tok/s",
                  color = ZoroCyan,
                  modifier = Modifier.weight(1f)
                )
                ResultMetricPill(
                  title = "Peak RAM",
                  value = "${result.peakRamMb}",
                  unit = "MB",
                  color = ZoroViolet,
                  modifier = Modifier.weight(1f)
                )
              }

              Spacer(Modifier.height(12.dp))

              Surface(
                shape = RoundedCornerShape(8.dp),
                color = ZoroEmerald.copy(alpha = 0.1f),
                border = androidx.compose.foundation.BorderStroke(1.dp, ZoroEmerald.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Text(
                  text = result.summary,
                  modifier = Modifier.padding(10.dp),
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }
        }
      }

      // 4. Benchmark History
      if (uiState.benchmarkHistory.isNotEmpty()) {
        item {
          Text(
            text = "Benchmark History",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
          )
        }

        items(uiState.benchmarkHistory, key = { it.id }) { record ->
          val dateStr = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(record.timestamp))
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = ZoroEmerald.copy(alpha = 0.2f),
                  border = androidx.compose.foundation.BorderStroke(1.dp, ZoroEmerald)
                ) {
                  Text(
                    text = record.scoreGrade,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = ZoroEmerald
                  )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                  Text(
                    text = "${record.decodeTokensPerSec} tok/s decode • ${record.quantizationMode}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                  )
                  Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
              Text(
                text = "${record.memoryUsedMb} MB RAM",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }

      item {
        Spacer(Modifier.height(16.dp))
      }
    }
  }
}

@Composable
private fun HardwareRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodySmall,
      fontWeight = FontWeight.SemiBold,
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}

@Composable
private fun ResultMetricPill(
  title: String,
  value: String,
  unit: String,
  color: Color,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(10.dp),
    color = MaterialTheme.colorScheme.surfaceVariant,
    border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f))
  ) {
    Column(
      modifier = Modifier.padding(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        fontSize = 10.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(Modifier.height(4.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Black,
        fontFamily = FontFamily.Monospace,
        color = color
      )
      Text(
        text = unit,
        style = MaterialTheme.typography.labelSmall,
        fontSize = 10.sp,
        color = color
      )
    }
  }
}
