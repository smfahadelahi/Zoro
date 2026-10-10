package com.example.zoro.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zoro.data.ConversationEntity
import com.example.zoro.data.MessageEntity
import com.example.zoro.model.GenerationMetrics
import com.example.zoro.model.ZoroPersona
import com.example.zoro.ui.ZoroUiState
import com.example.zoro.ui.ZoroViewModel
import com.example.zoro.ui.components.CopyButton
import com.example.zoro.ui.components.TelemetryPill
import com.example.zoro.ui.components.ThoughtSection
import com.example.ui.theme.ZoroCyan
import com.example.ui.theme.ZoroEmerald
import com.example.ui.theme.ZoroViolet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ZoroChatScreen(
  viewModel: ZoroViewModel,
  uiState: ZoroUiState,
  modifier: Modifier = Modifier
) {
  var inputText by remember { mutableStateOf("") }
  var showHistorySheet by remember { mutableStateOf(false) }
  val listState = rememberLazyListState()
  val coroutineScope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }

  // Auto-scroll to bottom on new messages
  LaunchedEffect(uiState.messages.size, uiState.currentStreamingText.length) {
    if (uiState.messages.isNotEmpty() || uiState.isGenerating) {
      val targetIndex = (uiState.messages.size + (if (uiState.isGenerating) 1 else 0)).coerceAtLeast(1) - 1
      listState.animateScrollToItem(targetIndex)
    }
  }

  // Show error in snackbar if any
  LaunchedEffect(uiState.errorMessage) {
    uiState.errorMessage?.let {
      snackbarHostState.showSnackbar(it)
      viewModel.dismissError()
    }
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.background,
          titleContentColor = MaterialTheme.colorScheme.onBackground
        ),
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(
                  Brush.linearGradient(listOf(ZoroCyan, ZoroEmerald))
                ),
              contentAlignment = Alignment.Center
            ) {
              Text("Z", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 18.sp)
            }
            Spacer(Modifier.width(10.dp))
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  "ZORO",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.sp
                )
                Spacer(Modifier.width(6.dp))
                Surface(
                  shape = RoundedCornerShape(4.dp),
                  color = ZoroCyan.copy(alpha = 0.15f),
                  border = androidx.compose.foundation.BorderStroke(1.dp, ZoroCyan.copy(alpha = 0.5f))
                ) {
                  Text(
                    "MOBILE SLM",
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = ZoroCyan
                  )
                }
              }
              Text(
                "100% On-Device Neural • ${uiState.activeQuantization.displayName.split(" ").first()}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        },
        actions = {
          IconButton(
            onClick = { showHistorySheet = true },
            modifier = Modifier.testTag("chat_history_button")
          ) {
            Icon(Icons.Default.History, contentDescription = "Chat History")
          }
          IconButton(
            onClick = { viewModel.createNewChat() },
            modifier = Modifier.testTag("new_chat_button")
          ) {
            Icon(Icons.Default.Add, contentDescription = "New Chat")
          }
        }
      )
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .imePadding()
    ) {
      // Persona Selector Row
      LazyRow(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(ZoroPersona.values()) { persona ->
          val isSelected = uiState.activePersona == persona
          FilterChip(
            selected = isSelected,
            onClick = { viewModel.setPersona(persona) },
            label = {
              Text("${persona.iconEmoji} ${persona.displayName}")
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = ZoroCyan.copy(alpha = 0.2f),
              selectedLabelColor = ZoroCyan
            ),
            border = FilterChipDefaults.filterChipBorder(
              enabled = true,
              selected = isSelected,
              borderColor = if (isSelected) ZoroCyan else MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier.testTag("persona_chip_${persona.id}")
          )
        }
      }

      // Messages or Empty State
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
      ) {
        if (uiState.messages.isEmpty() && !uiState.isGenerating) {
          EmptyChatHero(
            activePersona = uiState.activePersona,
            onPromptSelected = { prompt ->
              inputText = prompt
              viewModel.sendMessage(prompt)
            }
          )
        } else {
          LazyColumn(
            state = listState,
            modifier = Modifier
              .fillMaxSize()
              .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            items(uiState.messages, key = { it.id }) { message ->
              MessageBubble(message = message)
            }

            // Live Generating Bubble
            if (uiState.isGenerating) {
              item(key = "generating_bubble") {
                LiveGeneratingBubble(
                  streamingText = uiState.currentStreamingText,
                  thinkingText = uiState.currentThinkingText,
                  metrics = uiState.currentMetrics
                )
              }
            }
          }
        }
      }

      // Bottom Input Bar & Live Telemetry
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.background)
          .padding(horizontal = 12.dp, vertical = 8.dp)
      ) {
        // Telemetry Pill HUD above input
        if (uiState.isGenerating) {
          TelemetryPill(
            metrics = uiState.currentMetrics,
            isLive = true,
            modifier = Modifier
              .align(Alignment.CenterHorizontally)
              .padding(bottom = 6.dp)
          )
        }

        // Input Box
        Surface(
          shape = RoundedCornerShape(24.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (uiState.isGenerating) ZoroEmerald.copy(alpha = 0.7f) else MaterialTheme.colorScheme.outline
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = inputText,
              onValueChange = { inputText = it },
              placeholder = {
                Text(
                  "Message ZORO (On-Device)...",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
              },
              modifier = Modifier
                .weight(1f)
                .testTag("chat_input_field"),
              maxLines = 4,
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                disabledBorderColor = Color.Transparent
              ),
              keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
              keyboardActions = KeyboardActions(
                onSend = {
                  if (inputText.isNotBlank() && !uiState.isGenerating) {
                    val prompt = inputText
                    inputText = ""
                    viewModel.sendMessage(prompt)
                  }
                }
              )
            )

            // Send / Stop button
            if (uiState.isGenerating) {
              IconButton(
                onClick = { viewModel.stopGeneration() },
                modifier = Modifier
                  .size(42.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.errorContainer)
                  .testTag("stop_button")
              ) {
                Icon(
                  imageVector = Icons.Default.Stop,
                  contentDescription = "Stop Generation",
                  tint = MaterialTheme.colorScheme.error,
                  modifier = Modifier.size(20.dp)
                )
              }
            } else {
              IconButton(
                onClick = {
                  if (inputText.isNotBlank()) {
                    val prompt = inputText
                    inputText = ""
                    viewModel.sendMessage(prompt)
                  }
                },
                enabled = inputText.isNotBlank(),
                modifier = Modifier
                  .size(42.dp)
                  .clip(CircleShape)
                  .background(
                    if (inputText.isNotBlank()) ZoroCyan else MaterialTheme.colorScheme.surface
                  )
                  .testTag("send_button")
              ) {
                Icon(
                  imageVector = Icons.Default.Send,
                  contentDescription = "Send Message",
                  tint = if (inputText.isNotBlank()) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        }
      }
    }
  }

  // Conversation History Modal Bottom Sheet
  if (showHistorySheet) {
    ModalBottomSheet(
      onDismissRequest = { showHistorySheet = false },
      sheetState = rememberModalBottomSheetState()
    ) {
      ConversationHistorySheet(
        conversations = uiState.conversations,
        activeId = uiState.currentConversationId,
        onSelect = { id ->
          viewModel.selectConversation(id)
          showHistorySheet = false
        },
        onDelete = {
          viewModel.deleteCurrentChat()
          showHistorySheet = false
        },
        onClearAll = {
          viewModel.clearAllChats()
          showHistorySheet = false
        }
      )
    }
  }
}

@Composable
fun MessageBubble(message: MessageEntity) {
  val isUser = message.role == "user"

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
  ) {
    if (!isUser) {
      Box(
        modifier = Modifier
          .size(32.dp)
          .clip(CircleShape)
          .background(Brush.linearGradient(listOf(ZoroCyan, ZoroEmerald))),
        contentAlignment = Alignment.Center
      ) {
        Text("Z", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 15.sp)
      }
      Spacer(Modifier.width(8.dp))
    }

    Column(
      modifier = Modifier.weight(1f, fill = false),
      horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
      Surface(
        shape = RoundedCornerShape(
          topStart = 16.dp,
          topEnd = 16.dp,
          bottomStart = if (isUser) 16.dp else 4.dp,
          bottomEnd = if (isUser) 4.dp else 16.dp
        ),
        color = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        border = if (isUser) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          // If message has thinking content
          if (!message.thinkingContent.isNullOrBlank()) {
            ThoughtSection(thoughtText = message.thinkingContent)
          }

          // Message Body
          Text(
            text = message.content,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 22.sp
          )

          // Footer for assistant messages
          if (!isUser) {
            Spacer(Modifier.height(8.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              if (message.tokensPerSecond > 0f) {
                Text(
                  text = "⚡ ${message.tokensPerSecond} tok/s  |  ⏱ ${message.timeToFirstTokenMs}ms",
                  style = MaterialTheme.typography.labelSmall,
                  fontFamily = FontFamily.Monospace,
                  color = ZoroCyan,
                  fontSize = 11.sp
                )
              } else {
                Spacer(Modifier.width(1.dp))
              }
              CopyButton(textToCopy = message.content)
            }
          }
        }
      }
    }
  }
}

@Composable
fun LiveGeneratingBubble(
  streamingText: String,
  thinkingText: String?,
  metrics: GenerationMetrics
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.Start
  ) {
    Box(
      modifier = Modifier
        .size(32.dp)
        .clip(CircleShape)
        .background(Brush.linearGradient(listOf(ZoroCyan, ZoroEmerald))),
      contentAlignment = Alignment.Center
    ) {
      Text("Z", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 15.sp)
    }
    Spacer(Modifier.width(8.dp))

    Surface(
      shape = RoundedCornerShape(16.dp),
      color = MaterialTheme.colorScheme.surfaceVariant,
      border = androidx.compose.foundation.BorderStroke(1.dp, ZoroCyan.copy(alpha = 0.7f)),
      modifier = Modifier.weight(1f, fill = false)
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        if (!thinkingText.isNullOrBlank()) {
          ThoughtSection(thoughtText = thinkingText)
        }

        if (streamingText.isNotEmpty()) {
          Text(
            text = "$streamingText ▌", // Animated cursor block
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 22.sp
          )
        } else {
          Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(
              modifier = Modifier.size(16.dp),
              strokeWidth = 2.dp,
              color = ZoroCyan
            )
            Spacer(Modifier.width(8.dp))
            Text(
              text = "Evaluating prompt on-device...",
              style = MaterialTheme.typography.bodySmall,
              fontFamily = FontFamily.Monospace,
              color = ZoroCyan
            )
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EmptyChatHero(
  activePersona: ZoroPersona,
  onPromptSelected: (String) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Box(
      modifier = Modifier
        .size(68.dp)
        .clip(CircleShape)
        .background(
          Brush.radialGradient(
            listOf(ZoroCyan.copy(alpha = 0.4f), Color.Transparent)
          )
        )
        .border(1.5.dp, ZoroCyan, CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Default.Bolt,
        contentDescription = null,
        tint = ZoroCyan,
        modifier = Modifier.size(36.dp)
      )
    }

    Spacer(Modifier.height(16.dp))

    Text(
      text = "ZORO Native Mobile LLM",
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onBackground
    )

    Spacer(Modifier.height(6.dp))

    Text(
      text = activePersona.subtitle,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(Modifier.height(24.dp))

    // Privacy & Spec Badge
    Surface(
      shape = RoundedCornerShape(12.dp),
      color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
      border = androidx.compose.foundation.BorderStroke(1.dp, ZoroEmerald.copy(alpha = 0.3f))
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.Check,
          contentDescription = null,
          tint = ZoroEmerald,
          modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
          text = "100% Offline • 0 Server Calls • Zero Battery Drain",
          style = MaterialTheme.typography.labelSmall,
          fontFamily = FontFamily.Monospace,
          color = ZoroEmerald
        )
      }
    }

    Spacer(Modifier.height(28.dp))

    Text(
      text = "Recommended Starters",
      style = MaterialTheme.typography.labelMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(Modifier.height(12.dp))

    val samplePrompts = when (activePersona) {
      ZoroPersona.CODER -> listOf(
        "Build a Compose button with haptic feedback",
        "Write a Room DAO with Coroutine Flow",
        "Explain ViewModel state hoisting",
        "How to optimize memory in RecyclerView/LazyColumn"
      )
      ZoroPersona.DEEP_THINK -> listOf(
        "Why is GQA 4x more memory efficient than MHA?",
        "Explain RoPE positional rotation mathematics",
        "How does INT4 block quantization work on ARM?",
        "What causes thermal throttling in mobile neural chips?"
      )
      ZoroPersona.INTENT_AGENT -> listOf(
        "Set an alarm for 7:00 AM tomorrow",
        "Parse timer intent for 15 minutes pasta",
        "Query battery health and engage power saving",
        "Create a voice note intent about shopping list"
      )
      ZoroPersona.MICRO -> listOf(
        "What is the difference between FP16 and INT4?",
        "Best practice for Android background tasks?",
        "Define SwiGLU in one sentence",
        "Why avoid Dispatchers.Main for neural decode?"
      )
      else -> listOf(
        "What makes ZORO unique as an Android SLM?",
        "Write a Jetpack Compose card component",
        "How does on-device inference preserve my privacy?",
        "Explain mobile KV-cache memory limits"
      )
    }

    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      for (prompt in samplePrompts) {
        SuggestionChip(
          onClick = { onPromptSelected(prompt) },
          label = {
            Text(
              text = prompt,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              style = MaterialTheme.typography.bodySmall
            )
          },
          colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
          )
        )
      }
    }
  }
}

@Composable
fun ConversationHistorySheet(
  conversations: List<ConversationEntity>,
  activeId: String?,
  onSelect: (String) -> Unit,
  onDelete: () -> Unit,
  onClearAll: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Conversation History",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
      if (conversations.isNotEmpty()) {
        IconButton(onClick = onClearAll) {
          Icon(
            imageVector = Icons.Default.DeleteOutline,
            contentDescription = "Clear All Chats",
            tint = MaterialTheme.colorScheme.error
          )
        }
      }
    }

    Spacer(Modifier.height(12.dp))

    if (conversations.isEmpty()) {
      Text(
        text = "No saved conversations yet.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 16.dp)
      )
    } else {
      LazyColumn(
        modifier = Modifier.heightIn(max = 300.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(conversations) { conv ->
          val isSelected = conv.id == activeId
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onSelect(conv.id) },
            colors = CardDefaults.cardColors(
              containerColor = if (isSelected) ZoroCyan.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
            ),
            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, ZoroCyan) else null
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                  imageVector = Icons.Default.Forum,
                  contentDescription = null,
                  tint = if (isSelected) ZoroCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                  text = conv.title,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
              if (isSelected) {
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                  Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
            }
          }
        }
      }
    }
    Spacer(Modifier.height(16.dp))
  }
}
