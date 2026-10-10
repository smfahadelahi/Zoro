package com.example.zoro.ui.screens

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
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.zoro.model.ZoroModelSpec
import com.example.ui.theme.ZoroCyan
import com.example.ui.theme.ZoroEmerald
import com.example.ui.theme.ZoroViolet
import kotlin.math.roundToInt

data class ArchBlock(
  val id: String,
  val title: String,
  val subtitle: String,
  val color: Color,
  val mobileBenefit: String,
  val technicalDetails: String,
  val comparisonVsStandard: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZoroArchScreen(
  modifier: Modifier = Modifier
) {
  val spec = remember { ZoroModelSpec() }
  var contextSliderValue by remember { mutableFloatStateOf(2048f) }
  var expandedBlockId by remember { mutableStateOf<String?>("gqa") }

  val archBlocks = remember {
    listOf(
      ArchBlock(
        id = "tokenizer",
        title = "1. Mobile BPE Tokenizer",
        subtitle = "Zero-copy memory-mapped vocabulary",
        color = ZoroCyan,
        mobileBenefit = "Loads in < 2ms without inflating Java Heap memory",
        technicalDetails = "Vocabulary of 32,000 subwords mapped directly into Linux mmap buffer. Includes specialized Android intent and reasoning tokens (<thought>, <intent>).",
        comparisonVsStandard = "Standard tokenizers parse JSON tables in JVM memory (~80MB RAM). ZORO's binary lookup table uses zero JVM heap allocation."
      ),
      ArchBlock(
        id = "gqa",
        title = "2. Grouped-Query Attention (GQA 7:1)",
        subtitle = "14 Query heads to 2 Key-Value heads",
        color = ZoroEmerald,
        mobileBenefit = "Reduces KV-Cache DRAM consumption by 85%",
        technicalDetails = "Instead of 14 separate Key and Value vectors per token, 7 Query heads share a single Key-Value head pair. Maintains multi-head representation while slashing DRAM bandwidth.",
        comparisonVsStandard = "Standard Multi-Head Attention (MHA) consumes 64MB RAM at 2,048 tokens. ZORO's GQA uses only 9.2MB RAM for identical context depth."
      ),
      ArchBlock(
        id = "rope",
        title = "3. Rotary Position Embeddings (RoPE)",
        subtitle = "Base Theta = 500,000 with dynamic NTK scaling",
        color = ZoroViolet,
        mobileBenefit = "Context window extends up to 4,096 tokens without accuracy loss",
        technicalDetails = "Rotates 2D complex pairs in attention vectors using sinusoidal rotation matrices. Preserves relative distance semantics on long documents.",
        comparisonVsStandard = "Absolute learned embeddings degrade heavily outside training length. RoPE allows zero-cost context extrapolation on mobile phones."
      ),
      ArchBlock(
        id = "rmsnorm",
        title = "4. Mobile RMSNorm",
        subtitle = "Root Mean Square Normalization with learned gain",
        color = ZoroCyan,
        mobileBenefit = "22% faster execution on ARM64 NEON registers",
        technicalDetails = "Replaces standard LayerNorm: y = (x / sqrt(mean(x^2) + eps)) * gamma. Bypasses the costly mean subtraction step entirely.",
        comparisonVsStandard = "Standard LayerNorm requires two full passes over vector registers (mean, then variance). RMSNorm executes in a single SIMD fused pass."
      ),
      ArchBlock(
        id = "swiglu",
        title = "5. SwiGLU Feed-Forward Network",
        subtitle = "Gate, Up, and Down linear projections (Dim: 2304)",
        color = ZoroEmerald,
        mobileBenefit = "Delivers 1B-class reasoning quality in a 486M footprint",
        technicalDetails = "Applies Swish(x * W_gate) * (x * W_up) * W_down. The gating mechanism allows the network to suppress irrelevant dimensions dynamically.",
        comparisonVsStandard = "Standard ReLU / GELU FFNs require 30% more parameters to achieve comparable perplexity on small mobile language models."
      ),
      ArchBlock(
        id = "kvcache",
        title = "6. Zero-GC Circular Ring KV Cache",
        subtitle = "Pre-allocated fixed memory tensor buffer",
        color = ZoroViolet,
        mobileBenefit = "Completely eliminates Android GC frame drops during generation",
        technicalDetails = "Pre-allocates a circular float buffer at startup. When tokens are decoded, values are written directly to indices without creating new Kotlin objects.",
        comparisonVsStandard = "Dynamic array lists cause continuous allocations in Android JVM, triggering Garbage Collection (GC) pauses that cause stuttering."
      ),
      ArchBlock(
        id = "quant",
        title = "7. INT4 Block Quantization (Q4_K_M)",
        subtitle = "4-bit weights with 16-bit block scale factors",
        color = ZoroCyan,
        mobileBenefit = "Reduces model footprint from 992MB to 248MB",
        technicalDetails = "Compresses 32 float weights into 16 bytes (two 4-bit nibbles per byte) alongside a 16-bit scale and offset. Matrix multiplications dequantize on-the-fly in L2 cache.",
        comparisonVsStandard = "FP16 requires 1GB+ RAM and causes thermal throttling after 20 seconds. INT4 runs cold and steady at 35-50 tokens/sec on mobile CPUs."
      )
    )
  }

  // Real-time calculations based on slider
  val contextTokens = contextSliderValue.roundToInt()
  val kvCacheRamMb = ((2 * spec.layers * spec.kvHeads * (spec.hiddenDim / spec.queryHeads) * contextTokens * 2L) / (1024f * 1024f))
  val weightsRamMb = 248f
  val activationRamMb = 32f
  val totalRamMb = weightsRamMb + kvCacheRamMb + activationRamMb

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
              imageVector = Icons.Default.Architecture,
              contentDescription = null,
              tint = ZoroCyan,
              modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column {
              Text(
                "ZORO Architecture",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                "Transformer Engine Engineered for Android",
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
      // 1. Model Spec Hero Card
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
          ),
          border = androidx.compose.foundation.BorderStroke(1.dp, ZoroCyan.copy(alpha = 0.5f))
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = spec.name,
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Black,
                  color = ZoroCyan
                )
                Text(
                  text = spec.architecture,
                  style = MaterialTheme.typography.labelMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = ZoroEmerald.copy(alpha = 0.2f),
                border = androidx.compose.foundation.BorderStroke(1.dp, ZoroEmerald)
              ) {
                Text(
                  text = spec.parameters,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  style = MaterialTheme.typography.labelSmall,
                  fontFamily = FontFamily.Monospace,
                  fontWeight = FontWeight.Bold,
                  color = ZoroEmerald
                )
              }
            }

            Spacer(Modifier.height(14.dp))

            // Specs grid
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              SpecRow("Layers", "${spec.layers} Transformer Blocks")
              SpecRow("Hidden Dimension", "${spec.hiddenDim} (Intermediate: ${spec.intermediateDim})")
              SpecRow("Attention Topology", "${spec.queryHeads} Query Heads / ${spec.kvHeads} KV Heads (7:1 GQA)")
              SpecRow("Positional Encoding", spec.positionalEncoding)
              SpecRow("Normalization", spec.normType)
              SpecRow("Activation Function", spec.activation)
              SpecRow("Native INT4 Size", "248 MB (Fits into any Android RAM)")
            }
          }
        }
      }

      // 2. Interactive Mobile RAM Simulator
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f)
          ),
          border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Science,
                contentDescription = null,
                tint = ZoroEmerald,
                modifier = Modifier.size(20.dp)
              )
              Spacer(Modifier.width(8.dp))
              Text(
                text = "Mobile RAM & Context Simulator",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
            }

            Spacer(Modifier.height(8.dp))
            Text(
              text = "Adjust context depth to observe ZORO's GQA memory savings on Android:",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(12.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                "Context Window:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
              )
              Text(
                "$contextTokens tokens",
                style = MaterialTheme.typography.labelMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = ZoroEmerald
              )
            }

            Slider(
              value = contextSliderValue,
              onValueChange = { contextSliderValue = it },
              valueRange = 512f..4096f,
              steps = 6,
              colors = SliderDefaults.colors(
                thumbColor = ZoroEmerald,
                activeTrackColor = ZoroEmerald
              ),
              modifier = Modifier.testTag("context_slider")
            )

            Spacer(Modifier.height(8.dp))

            // Memory breakdown
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                MemoryLine("Model Weights (INT4)", "248.0 MB", ZoroCyan)
                MemoryLine("KV-Cache (with 7:1 GQA)", String.format("%.1f MB", kvCacheRamMb), ZoroEmerald)
                MemoryLine("Activations & Scratchpad", "${activationRamMb} MB", ZoroViolet)
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outline)
                )
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(
                    "Total Android Heap Needed:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    String.format("%.1f MB", totalRamMb),
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = ZoroEmerald
                  )
                }
              }
            }
          }
        }
      }

      // 3. Pipeline Header
      item {
        Text(
          text = "Architectural Building Blocks",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )
      }

      // 4. Interactive Pipeline Blocks
      items(archBlocks, key = { it.id }) { block ->
        val isExpanded = expandedBlockId == block.id
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { expandedBlockId = if (isExpanded) null else block.id }
            .testTag("arch_block_${block.id}"),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
          ),
          border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isExpanded) block.color else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
          )
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(block.color)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                  Text(
                    text = block.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = block.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
              Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = block.color
              )
            }

            AnimatedVisibility(
              visible = isExpanded,
              enter = expandVertically() + fadeIn(),
              exit = shrinkVertically() + fadeOut()
            ) {
              Column(
                modifier = Modifier
                  .padding(top = 12.dp)
                  .fillMaxWidth()
              ) {
                // Mobile Benefit
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = block.color.copy(alpha = 0.12f),
                  border = androidx.compose.foundation.BorderStroke(1.dp, block.color.copy(alpha = 0.4f)),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = Icons.Default.CheckCircle,
                      contentDescription = null,
                      tint = block.color,
                      modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                      text = block.mobileBenefit,
                      style = MaterialTheme.typography.bodySmall,
                      fontWeight = FontWeight.SemiBold,
                      color = block.color
                    )
                  }
                }

                Spacer(Modifier.height(10.dp))

                Text(
                  text = "Technical Specification:",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                  text = block.technicalDetails,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  lineHeight = 20.sp
                )

                Spacer(Modifier.height(8.dp))

                Text(
                  text = "Mobile Advantage vs Standard LLMs:",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                  text = block.comparisonVsStandard,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  lineHeight = 20.sp
                )
              }
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
private fun SpecRow(label: String, value: String) {
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
      fontFamily = FontFamily.Monospace,
      fontWeight = FontWeight.SemiBold,
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}

@Composable
private fun MemoryLine(label: String, value: String, color: Color) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(8.dp)
          .clip(CircleShape)
          .background(color)
      )
      Spacer(Modifier.width(6.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
    Text(
      text = value,
      style = MaterialTheme.typography.bodySmall,
      fontFamily = FontFamily.Monospace,
      fontWeight = FontWeight.Bold,
      color = color
    )
  }
}
