package com.example.zoro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zoro.model.QuantizationMode
import com.example.zoro.ui.ZoroUiState
import com.example.zoro.ui.ZoroViewModel
import com.example.ui.theme.ZoroCyan
import com.example.ui.theme.ZoroEmerald
import com.example.ui.theme.ZoroViolet
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZoroSettingsScreen(
  viewModel: ZoroViewModel,
  uiState: ZoroUiState,
  modifier: Modifier = Modifier
) {
  var showClearConfirm by remember { mutableStateOf(false) }

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
              imageVector = Icons.Default.Settings,
              contentDescription = null,
              tint = ZoroCyan,
              modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column {
              Text(
                "Model Engine Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                "Configure Quantization & Sampler",
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
      // 1. Quantization Selection Section
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
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Memory,
                contentDescription = null,
                tint = ZoroCyan,
                modifier = Modifier.size(20.dp)
              )
              Spacer(Modifier.width(8.dp))
              Text(
                text = "Model Quantization Profile",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
            }

            Spacer(Modifier.height(6.dp))
            Text(
              text = "Adjust the precision and memory footprint of ZORO's transformer weight tensors:",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(14.dp))

            for (mode in QuantizationMode.values()) {
              val isSelected = uiState.activeQuantization == mode
              Surface(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp)
                  .clickable { viewModel.setQuantization(mode) }
                  .testTag("quant_option_${mode.key}"),
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) ZoroCyan.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                  1.dp,
                  if (isSelected) ZoroCyan else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  RadioButton(
                    selected = isSelected,
                    onClick = { viewModel.setQuantization(mode) },
                    colors = RadioButtonDefaults.colors(selectedColor = ZoroCyan)
                  )
                  Spacer(Modifier.width(8.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                      Text(
                        text = mode.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                      )
                      Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = ZoroEmerald.copy(alpha = 0.2f)
                      ) {
                        Text(
                          text = "${mode.modelSizeMb} MB",
                          modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                          style = MaterialTheme.typography.labelSmall,
                          fontFamily = FontFamily.Monospace,
                          fontWeight = FontWeight.Bold,
                          color = ZoroEmerald
                        )
                      }
                      Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = ZoroCyan.copy(alpha = 0.2f)
                      ) {
                        Text(
                          text = mode.speedMultiplier,
                          modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                          style = MaterialTheme.typography.labelSmall,
                          fontFamily = FontFamily.Monospace,
                          fontWeight = FontWeight.Bold,
                          color = ZoroCyan
                        )
                      }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                      text = mode.description,
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }
              }
            }
          }
        }
      }

      // 2. Inference Sampler Hyperparameters
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
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = null,
                tint = ZoroEmerald,
                modifier = Modifier.size(20.dp)
              )
              Spacer(Modifier.width(8.dp))
              Text(
                text = "Sampling Parameters",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
            }

            Spacer(Modifier.height(14.dp))

            // Temperature Slider
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text("Temperature", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Text(
                  if (uiState.temperature < 0.5f) "Deterministic & Factual" else if (uiState.temperature > 0.9f) "Creative & Exploratory" else "Balanced",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              Text(
                String.format("%.2f", uiState.temperature),
                style = MaterialTheme.typography.labelMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = ZoroEmerald
              )
            }
            Slider(
              value = uiState.temperature,
              onValueChange = { viewModel.setTemperature(it) },
              valueRange = 0.1f..1.5f,
              colors = SliderDefaults.colors(
                thumbColor = ZoroEmerald,
                activeTrackColor = ZoroEmerald
              ),
              modifier = Modifier.testTag("temp_slider")
            )

            Spacer(Modifier.height(10.dp))

            // Top-P Nucleus Sampling Slider
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text("Top-P (Nucleus)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Text(
                  "Cumulative probability mass threshold",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              Text(
                String.format("%.2f", uiState.topP),
                style = MaterialTheme.typography.labelMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = ZoroCyan
              )
            }
            Slider(
              value = uiState.topP,
              onValueChange = { viewModel.setTopP(it) },
              valueRange = 0.1f..1.0f,
              colors = SliderDefaults.colors(
                thumbColor = ZoroCyan,
                activeTrackColor = ZoroCyan
              ),
              modifier = Modifier.testTag("topp_slider")
            )

            Spacer(Modifier.height(10.dp))

            // Max Tokens Slider
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text("Max Output Tokens", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Text(
                  "Upper limit per response",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              Text(
                "${uiState.maxTokens} tok",
                style = MaterialTheme.typography.labelMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = ZoroViolet
              )
            }
            Slider(
              value = uiState.maxTokens.toFloat(),
              onValueChange = { viewModel.setMaxTokens(it.roundToInt()) },
              valueRange = 64f..1024f,
              steps = 14,
              colors = SliderDefaults.colors(
                thumbColor = ZoroViolet,
                activeTrackColor = ZoroViolet
              ),
              modifier = Modifier.testTag("max_tokens_slider")
            )
          }
        }
      }

      // 3. Data & Storage Controls
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
            Text(
              text = "Data & Conversation Storage",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
              text = "All chats and tokens are stored in local Room database on device.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(14.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              OutlinedButton(
                onClick = {
                  viewModel.setTemperature(0.7f)
                  viewModel.setTopP(0.9f)
                  viewModel.setMaxTokens(512)
                  viewModel.setQuantization(QuantizationMode.INT4)
                },
                modifier = Modifier
                  .weight(1f)
                  .testTag("reset_defaults_button"),
                shape = RoundedCornerShape(10.dp)
              ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Reset Defaults")
              }

              Button(
                onClick = { showClearConfirm = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                  .weight(1f)
                  .testTag("clear_all_chats_button"),
                shape = RoundedCornerShape(10.dp)
              ) {
                Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Clear All Chats")
              }
            }
          }
        }
      }

      // 4. About ZORO Mobile LLM
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
          )
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = ZoroCyan,
                modifier = Modifier.size(20.dp)
              )
              Spacer(Modifier.width(8.dp))
              Text(
                text = "About ZORO Mobile LLM",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
            }
            Spacer(Modifier.height(8.dp))
            Text(
              text = "ZORO is an on-device Small/Large Language Model engineered specifically to solve the physical constraints of mobile phones: memory bandwidth limits, thermal throttling, and battery conservation. With 7:1 Grouped-Query Attention (GQA) and INT4 block quantization, ZORO delivers instant offline intelligence without sharing a single byte of user data with cloud servers.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              lineHeight = 20.sp
            )
            Spacer(Modifier.height(10.dp))
            Text(
              text = "Version: 1.0.0-NativeAndroid • Target: Android 7.0+",
              style = MaterialTheme.typography.labelSmall,
              fontFamily = FontFamily.Monospace,
              color = ZoroCyan
            )
          }
        }
      }

      item {
        Spacer(Modifier.height(16.dp))
      }
    }
  }

  // Clear Confirmation Dialog
  if (showClearConfirm) {
    AlertDialog(
      onDismissRequest = { showClearConfirm = false },
      title = { Text("Clear All Conversations?") },
      text = { Text("This will permanently delete all local chat history and saved messages from device storage.") },
      confirmButton = {
        TextButton(
          onClick = {
            viewModel.clearAllChats()
            showClearConfirm = false
          }
        ) {
          Text("Delete All", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showClearConfirm = false }) {
          Text("Cancel")
        }
      }
    )
  }
}
