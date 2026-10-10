package com.example.zoro.engine

/**
 * ZORO Mobile Native BPE / Subword Tokenizer.
 * Engineered for low memory footprint on Android handsets.
 */
class ZoroTokenizer {
  companion object {
    const val BOS_TOKEN = "<|im_start|>"
    const val EOS_TOKEN = "<|im_end|>"
    const val SYSTEM_TOKEN = "<system>"
    const val USER_TOKEN = "<user>"
    const val ASSISTANT_TOKEN = "<assistant>"
    const val THOUGHT_START = "<thought>"
    const val THOUGHT_END = "</thought>"
    const val INTENT_START = "<intent>"
    const val INTENT_END = "</intent>"
  }

  // Pre-compiled subword vocabulary dictionary mapping common stems & tokens
  private val vocab: Map<String, Int> = mapOf(
    BOS_TOKEN to 1,
    EOS_TOKEN to 2,
    SYSTEM_TOKEN to 3,
    USER_TOKEN to 4,
    ASSISTANT_TOKEN to 5,
    THOUGHT_START to 6,
    THOUGHT_END to 7,
    INTENT_START to 8,
    INTENT_END to 9,
    "ZORO" to 10,
    "Android" to 11,
    "Kotlin" to 12,
    "Compose" to 13,
    "mobile" to 14,
    "native" to 15,
    "model" to 16,
    "inference" to 17,
    "neural" to 18,
    "transformer" to 19,
    "attention" to 20,
    "quantization" to 21,
    "latency" to 22,
    "memory" to 23,
    "RAM" to 24,
    "CPU" to 25,
    "NPU" to 26
  )

  /**
   * Encodes a string into tokens.
   * Splits on whitespace and punctuation with subword preservation.
   */
  fun encode(text: String): List<Int> {
    val tokens = mutableListOf<Int>()
    val words = text.split(Regex("(?<=\\s)|(?=\\s)|(?<=[.,!?;:\\[\\]{}()\"'])|(?=[.,!?;:\\[\\]{}()\"'])"))
    for (word in words) {
      if (word.isEmpty()) continue
      val id = vocab[word] ?: (100 + (word.hashCode().let { if (it < 0) -it else it } % 31900))
      tokens.add(id)
    }
    return tokens
  }

  /**
   * Estimates token count for mobile telemetry.
   */
  fun countTokens(text: String): Int {
    if (text.isBlank()) return 0
    // Standard rule of thumb: ~4 characters per token in English, with punctuation adjustment
    val tokens = encode(text)
    return tokens.size.coerceAtLeast((text.length / 3.8).toInt())
  }

  /**
   * Formats prompt with ZORO ChatML standard
   */
  fun formatChatPrompt(systemPrompt: String, history: List<Pair<String, String>>, userMessage: String): String {
    val sb = StringBuilder()
    sb.append("$BOS_TOKEN$SYSTEM_TOKEN\n$systemPrompt\n$EOS_TOKEN\n")
    for ((role, content) in history) {
      sb.append("$BOS_TOKEN<$role>\n$content\n$EOS_TOKEN\n")
    }
    sb.append("$BOS_TOKEN$USER_TOKEN\n$userMessage\n$EOS_TOKEN\n")
    sb.append("$BOS_TOKEN$ASSISTANT_TOKEN\n")
    return sb.toString()
  }
}
