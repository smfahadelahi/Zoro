package com.example.zoro.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.zoro.data.BenchmarkRecordEntity
import com.example.zoro.data.ConversationEntity
import com.example.zoro.data.MessageEntity
import com.example.zoro.data.ZoroRepository
import com.example.zoro.engine.BenchmarkResult
import com.example.zoro.engine.DeviceHardwareProfile
import com.example.zoro.model.GenerationMetrics
import com.example.zoro.model.QuantizationMode
import com.example.zoro.model.ZoroPersona
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ZoroUiState(
  val currentConversationId: String? = null,
  val conversations: List<ConversationEntity> = emptyList(),
  val messages: List<MessageEntity> = emptyList(),
  val isGenerating: Boolean = false,
  val currentStreamingText: String = "",
  val currentThinkingText: String? = null,
  val currentMetrics: GenerationMetrics = GenerationMetrics(),
  val activePersona: ZoroPersona = ZoroPersona.CORE,
  val activeQuantization: QuantizationMode = QuantizationMode.INT4,
  val temperature: Float = 0.7f,
  val topP: Float = 0.9f,
  val maxTokens: Int = 512,
  val threadCount: Int = 4,
  val hardwareProfile: DeviceHardwareProfile? = null,
  val isBenchmarking: Boolean = false,
  val latestBenchmarkResult: BenchmarkResult? = null,
  val benchmarkHistory: List<BenchmarkRecordEntity> = emptyList(),
  val errorMessage: String? = null
)

class ZoroViewModel(application: Application) : AndroidViewModel(application) {

  private val repository = ZoroRepository(application)
  private val _uiState = MutableStateFlow(ZoroUiState())
  val uiState: StateFlow<ZoroUiState> = _uiState.asStateFlow()

  private var generationJob: Job? = null
  private var messagesObservationJob: Job? = null

  init {
    loadHardwareProfile()
    observeConversations()
    observeBenchmarks()
  }

  private fun loadHardwareProfile() {
    viewModelScope.launch {
      val profile = repository.getHardwareProfile()
      _uiState.update { it.copy(hardwareProfile = profile) }
    }
  }

  private fun observeConversations() {
    viewModelScope.launch {
      repository.getAllConversations().collectLatest { list ->
        _uiState.update { state ->
          val activeId = state.currentConversationId ?: list.firstOrNull()?.id
          state.copy(conversations = list, currentConversationId = activeId)
        }
        if (_uiState.value.currentConversationId != null && messagesObservationJob == null) {
          observeMessages(_uiState.value.currentConversationId!!)
        }
      }
    }
  }

  private fun observeBenchmarks() {
    viewModelScope.launch {
      repository.getBenchmarkRecords().collectLatest { history ->
        _uiState.update { it.copy(benchmarkHistory = history) }
      }
    }
  }

  fun selectConversation(conversationId: String) {
    if (_uiState.value.currentConversationId == conversationId) return
    _uiState.update { it.copy(currentConversationId = conversationId, currentStreamingText = "", currentThinkingText = null) }
    observeMessages(conversationId)
  }

  fun createNewChat(persona: ZoroPersona = _uiState.value.activePersona) {
    viewModelScope.launch {
      val newId = repository.createNewConversation(persona)
      _uiState.update { it.copy(currentConversationId = newId, activePersona = persona, currentStreamingText = "", currentThinkingText = null) }
      observeMessages(newId)
    }
  }

  fun deleteCurrentChat() {
    val activeId = _uiState.value.currentConversationId ?: return
    viewModelScope.launch {
      repository.deleteConversation(activeId)
      val remaining = _uiState.value.conversations.filter { it.id != activeId }
      val nextId = remaining.firstOrNull()?.id
      _uiState.update { it.copy(currentConversationId = nextId) }
      if (nextId != null) {
        observeMessages(nextId)
      } else {
        createNewChat()
      }
    }
  }

  fun clearAllChats() {
    viewModelScope.launch {
      repository.deleteAllConversations()
      _uiState.update { it.copy(currentConversationId = null, messages = emptyList()) }
      createNewChat()
    }
  }

  private fun observeMessages(conversationId: String) {
    messagesObservationJob?.cancel()
    messagesObservationJob = viewModelScope.launch {
      repository.getMessagesForConversation(conversationId).collectLatest { msgs ->
        _uiState.update { it.copy(messages = msgs) }
      }
    }
  }

  fun setPersona(persona: ZoroPersona) {
    _uiState.update { it.copy(activePersona = persona) }
  }

  fun setQuantization(mode: QuantizationMode) {
    _uiState.update { it.copy(activeQuantization = mode) }
  }

  fun setTemperature(temp: Float) {
    _uiState.update { it.copy(temperature = temp.coerceIn(0.1f, 1.5f)) }
  }

  fun setTopP(p: Float) {
    _uiState.update { it.copy(topP = p.coerceIn(0.1f, 1.0f)) }
  }

  fun setMaxTokens(tokens: Int) {
    _uiState.update { it.copy(maxTokens = tokens) }
  }

  fun setThreadCount(threads: Int) {
    _uiState.update { it.copy(threadCount = threads) }
  }

  fun sendMessage(promptText: String) {
    val trimmed = promptText.trim()
    if (trimmed.isEmpty() || _uiState.value.isGenerating) return

    viewModelScope.launch {
      var convId = _uiState.value.currentConversationId
      if (convId == null) {
        convId = repository.createNewConversation(_uiState.value.activePersona)
        _uiState.update { it.copy(currentConversationId = convId) }
        observeMessages(convId)
      }

      // Save user message to database
      repository.saveUserMessage(convId, trimmed)

      // Start inference generation
      startInference(convId, trimmed)
    }
  }

  private fun startInference(convId: String, userPrompt: String) {
    val state = _uiState.value
    _uiState.update {
      it.copy(
        isGenerating = true,
        currentStreamingText = "",
        currentThinkingText = null,
        currentMetrics = GenerationMetrics(quantizationMode = state.activeQuantization)
      )
    }

    val history = state.messages.map { Pair(it.role, it.content) }

    generationJob?.cancel()
    generationJob = viewModelScope.launch {
      try {
        var finalAccumulated = ""
        var finalMetrics = GenerationMetrics()

        repository.streamInference(
          prompt = userPrompt,
          persona = state.activePersona,
          quantization = state.activeQuantization,
          temperature = state.temperature,
          topP = state.topP,
          maxTokens = state.maxTokens,
          history = history
        ).collect { step ->
          finalAccumulated = step.accumulatedText
          finalMetrics = step.metrics

          // Parse thinking content if wrapped in <thought>...</thought>
          var textToDisplay = step.accumulatedText
          var thinkingPart: String? = null
          if (step.accumulatedText.contains("<thought>")) {
            val thoughtStart = step.accumulatedText.indexOf("<thought>") + 9
            val thoughtEnd = step.accumulatedText.indexOf("</thought>")
            if (thoughtEnd != -1) {
              thinkingPart = step.accumulatedText.substring(thoughtStart, thoughtEnd).trim()
              textToDisplay = step.accumulatedText.substring(thoughtEnd + 10).trim()
            } else {
              thinkingPart = step.accumulatedText.substring(thoughtStart).trim()
              textToDisplay = ""
            }
          }

          _uiState.update {
            it.copy(
              currentStreamingText = textToDisplay,
              currentThinkingText = thinkingPart,
              currentMetrics = step.metrics
            )
          }

          if (step.isCompleted) {
            // Save to database
            repository.saveAssistantMessage(
              conversationId = convId,
              content = textToDisplay.ifEmpty { finalAccumulated },
              thinkingContent = thinkingPart,
              tokensGenerated = finalMetrics.totalTokens,
              tokensPerSecond = finalMetrics.tokensPerSecond,
              ttftMs = finalMetrics.timeToFirstTokenMs
            )
            _uiState.update {
              it.copy(
                isGenerating = false,
                currentStreamingText = "",
                currentThinkingText = null
              )
            }
          }
        }
      } catch (e: Exception) {
        _uiState.update {
          it.copy(
            isGenerating = false,
            errorMessage = e.message ?: "Inference interrupted"
          )
        }
      }
    }
  }

  fun stopGeneration() {
    repository.cancelGeneration()
    generationJob?.cancel()
    _uiState.update { it.copy(isGenerating = false) }
  }

  fun runDeviceBenchmark() {
    if (_uiState.value.isBenchmarking) return
    viewModelScope.launch {
      _uiState.update { it.copy(isBenchmarking = true) }
      try {
        val result = repository.runBenchmark(_uiState.value.activeQuantization)
        _uiState.update {
          it.copy(
            isBenchmarking = false,
            latestBenchmarkResult = result
          )
        }
      } catch (e: Exception) {
        _uiState.update {
          it.copy(
            isBenchmarking = false,
            errorMessage = "Benchmark failed: ${e.message}"
          )
        }
      }
    }
  }

  fun dismissError() {
    _uiState.update { it.copy(errorMessage = null) }
  }
}
