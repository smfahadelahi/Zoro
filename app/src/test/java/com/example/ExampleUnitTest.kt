package com.example

import com.example.zoro.engine.ZoroTensorOps
import com.example.zoro.engine.ZoroTokenizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testZoroTokenizerEncoding() {
    val tokenizer = ZoroTokenizer()
    val text = "ZORO Android mobile neural inference"
    val tokens = tokenizer.encode(text)
    assertTrue("Tokenizer should produce tokens", tokens.isNotEmpty())
    val count = tokenizer.countTokens(text)
    assertTrue("Token count should be greater than 0", count > 0)
  }

  @Test
  fun testZoroTensorRmsNorm() {
    val x = floatArrayOf(1.0f, 2.0f, 3.0f, 4.0f)
    val gamma = floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f)
    val normed = ZoroTensorOps.rmsNorm(x, gamma)
    assertEquals(4, normed.size)
    // RMS of [1, 2, 3, 4] is sqrt((1+4+9+16)/4) = sqrt(7.5) ≈ 2.7386
    // Check first element normalized is ~ 1/2.7386 ≈ 0.365
    assertTrue(normed[0] > 0.3f && normed[0] < 0.4f)
  }

  @Test
  fun testZoroTensorSoftmax() {
    val logits = floatArrayOf(2.0f, 1.0f, 0.1f)
    val probs = ZoroTensorOps.softmax(logits, temperature = 1.0f)
    var sum = 0f
    for (p in probs) sum += p
    assertTrue("Softmax probabilities must sum to approximately 1.0", Math.abs(sum - 1.0f) < 0.01f)
    assertTrue("Higher logit must have higher probability", probs[0] > probs[1] && probs[1] > probs[2])
  }
}
