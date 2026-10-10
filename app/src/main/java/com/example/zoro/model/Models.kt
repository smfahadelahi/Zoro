package com.example.zoro.model

enum class ZoroPersona(
  val id: String,
  val displayName: String,
  val subtitle: String,
  val systemPrompt: String,
  val iconEmoji: String
) {
  CORE(
    id = "core",
    displayName = "ZORO Core",
    subtitle = "Fast, balanced on-device mobile assistant",
    systemPrompt = "You are ZORO, a custom-engineered native mobile Large Language Model running locally on Android. You are ultra-responsive, privacy-preserving, helpful, and concise.",
    iconEmoji = "⚡"
  ),
  CODER(
    id = "coder",
    displayName = "ZORO Coder",
    subtitle = "Kotlin, Jetpack Compose & Android engineer",
    systemPrompt = "You are ZORO Coder, specialized in Kotlin, Jetpack Compose, Coroutines, and Android software architecture. Provide pristine code examples with architectural rationale.",
    iconEmoji = "💻"
  ),
  DEEP_THINK(
    id = "deep_think",
    displayName = "ZORO DeepThink",
    subtitle = "Step-by-step chain-of-thought reasoning",
    systemPrompt = "You are ZORO DeepThink. Always output your step-by-step chain of thought inside <thought>...</thought> tags before presenting your final structured answer.",
    iconEmoji = "🧠"
  ),
  INTENT_AGENT(
    id = "intent_agent",
    displayName = "ZORO Device Agent",
    subtitle = "Parses and plans Android system actions",
    systemPrompt = "You are ZORO Device Agent. Parse user requests into mobile intents (alarm, calendar, notes, battery, connectivity, settings) with JSON intent specs.",
    iconEmoji = "📱"
  ),
  MICRO(
    id = "micro",
    displayName = "ZORO Micro",
    subtitle = "Ultra-fast single-sentence edge answers",
    systemPrompt = "You are ZORO Micro, an ultra-compact SLM. Give ultra-dense, direct 1-to-2 sentence answers without fluff.",
    iconEmoji = "🚀"
  )
}

enum class QuantizationMode(
  val key: String,
  val displayName: String,
  val bits: Int,
  val modelSizeMb: Int,
  val speedMultiplier: String,
  val memorySavings: String,
  val description: String
) {
  INT4(
    key = "q4_k_m",
    displayName = "INT4 (Q4_K_M)",
    bits = 4,
    modelSizeMb = 248,
    speedMultiplier = "4.2x",
    memorySavings = "75%",
    description = "Recommended for mobile. 4-bit block-quantized weights. Ultra-low RAM usage, runs cold without battery drain."
  ),
  INT8(
    key = "q8_0",
    displayName = "INT8 (Q8_0)",
    bits = 8,
    modelSizeMb = 496,
    speedMultiplier = "2.3x",
    memorySavings = "50%",
    description = "Near lossless 8-bit integer quantization. Great balance of precision and memory efficiency for modern phones."
  ),
  FP16(
    key = "fp16",
    displayName = "FP16 (Half Precision)",
    bits = 16,
    modelSizeMb = 992,
    speedMultiplier = "1.0x",
    memorySavings = "0%",
    description = "Full 16-bit float representation. Maximum precision, requires 1GB+ available memory."
  )
}

data class GenerationMetrics(
  val tokensPerSecond: Float = 0f,
  val timeToFirstTokenMs: Long = 0L,
  val totalTokens: Int = 0,
  val totalDurationMs: Long = 0L,
  val kvCacheUsedPercent: Int = 0,
  val ramUsageMb: Long = 0L,
  val quantizationMode: QuantizationMode = QuantizationMode.INT4
)

data class ZoroModelSpec(
  val name: String = "ZORO-Mobile-0.5B",
  val version: String = "1.0.0-MobileNative",
  val parameters: String = "486 Million",
  val architecture: String = "Decoder-Only Dense Transformer",
  val hiddenDim: Int = 896,
  val layers: Int = 18,
  val queryHeads: Int = 14,
  val kvHeads: Int = 2, // 7:1 GQA
  val intermediateDim: Int = 2304, // SwiGLU
  val vocabSize: Int = 32000,
  val contextWindow: Int = 4096,
  val positionalEncoding: String = "RoPE (Rotary Position Embeddings, Base 500,000)",
  val normType: String = "RMSNorm with learnable gain",
  val activation: String = "SwiGLU (Swish-Gated Linear Unit)",
  val targetPlatform: String = "Android ARM64 & x86_64"
)
