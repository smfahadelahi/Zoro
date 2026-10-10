package com.example.zoro.data

import android.content.Context
import com.example.zoro.engine.BenchmarkResult
import com.example.zoro.engine.DeviceHardwareProfile
import com.example.zoro.engine.GenerationStep
import com.example.zoro.engine.ZoroBenchmarkEngine
import com.example.zoro.engine.ZoroInferenceEngine
import com.example.zoro.model.QuantizationMode
import com.example.zoro.model.ZoroPersona
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ZoroRepository(private val context: Context) {

  private val database = ZoroDatabase.getDatabase(context)
  private val dao = database.zoroDao()
  private val inferenceEngine = ZoroInferenceEngine(context)
  private val benchmarkEngine = ZoroBenchmarkEngine(context)

  fun getAllConversations(): Flow<List<ConversationEntity>> = dao.getAllConversations()

  fun getMessagesForConversation(conversationId: String): Flow<List<MessageEntity>> =
    dao.getMessagesForConversation(conversationId)

  suspend fun createNewConversation(persona: ZoroPersona): String {
    val id = UUID.randomUUID().toString()
    val conversation = ConversationEntity(
      id = id,
      title = "New Chat (${persona.displayName})",
      personaId = persona.id
    )
    dao.insertConversation(conversation)
    return id
  }

  suspend fun updateConversationTitle(id: String, title: String) {
    val existing = dao.getConversationById(id)
    if (existing != null) {
      dao.updateConversation(existing.copy(title = title, updatedAt = System.currentTimeMillis()))
    }
  }

  suspend fun deleteConversation(id: String) {
    dao.deleteConversation(id)
  }

  suspend fun deleteAllConversations() {
    dao.deleteAllConversations()
  }

  suspend fun saveUserMessage(conversationId: String, text: String): String {
    val msgId = UUID.randomUUID().toString()
    val msg = MessageEntity(
      id = msgId,
      conversationId = conversationId,
      role = "user",
      content = text,
      timestamp = System.currentTimeMillis()
    )
    dao.insertMessage(msg)
    return msgId
  }

  suspend fun saveAssistantMessage(
    conversationId: String,
    content: String,
    thinkingContent: String?,
    tokensGenerated: Int,
    tokensPerSecond: Float,
    ttftMs: Long
  ): String {
    val msgId = UUID.randomUUID().toString()
    val msg = MessageEntity(
      id = msgId,
      conversationId = conversationId,
      role = "assistant",
      content = content,
      thinkingContent = thinkingContent,
      tokensGenerated = tokensGenerated,
      tokensPerSecond = tokensPerSecond,
      timeToFirstTokenMs = ttftMs,
      timestamp = System.currentTimeMillis()
    )
    dao.insertMessage(msg)

    // Update conversation timestamp
    val conv = dao.getConversationById(conversationId)
    if (conv != null) {
      // If title is default, extract first 6 words as title
      val newTitle = if (conv.title.startsWith("New Chat")) {
        content.take(30).replace(Regex("[#*`\n]"), " ").trim() + "..."
      } else {
        conv.title
      }
      dao.updateConversation(conv.copy(title = newTitle, updatedAt = System.currentTimeMillis()))
    }
    return msgId
  }

  fun streamInference(
    prompt: String,
    persona: ZoroPersona,
    quantization: QuantizationMode,
    temperature: Float,
    topP: Float,
    maxTokens: Int,
    history: List<Pair<String, String>>
  ): Flow<GenerationStep> {
    return inferenceEngine.generateStream(
      prompt = prompt,
      persona = persona,
      quantization = quantization,
      temperature = temperature,
      topP = topP,
      maxTokens = maxTokens,
      chatHistory = history
    )
  }

  fun cancelGeneration() {
    inferenceEngine.cancelGeneration()
  }

  fun getHardwareProfile(): DeviceHardwareProfile {
    return benchmarkEngine.getDeviceHardwareProfile()
  }

  suspend fun runBenchmark(mode: QuantizationMode): BenchmarkResult {
    val result = benchmarkEngine.runBenchmark(mode)
    dao.insertBenchmarkRecord(
      BenchmarkRecordEntity(
        prefillTokensPerSec = result.prefillTokensPerSec,
        decodeTokensPerSec = result.decodeTokensPerSec,
        memoryUsedMb = result.peakRamMb,
        quantizationMode = mode.displayName,
        scoreGrade = result.scoreGrade
      )
    )
    return result
  }

  fun getBenchmarkRecords(): Flow<List<BenchmarkRecordEntity>> = dao.getBenchmarkRecords()
}
