package com.example.zoro.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
  @PrimaryKey
  val id: String,
  val title: String,
  val personaId: String,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
  tableName = "messages",
  foreignKeys = [
    ForeignKey(
      entity = ConversationEntity::class,
      parentColumns = ["id"],
      childColumns = ["conversationId"],
      onDelete = ForeignKey.CASCADE
    )
  ],
  indices = [Index(value = ["conversationId"])]
)
data class MessageEntity(
  @PrimaryKey
  val id: String,
  val conversationId: String,
  val role: String, // "user", "assistant", "system"
  val content: String,
  val thinkingContent: String? = null,
  val tokensGenerated: Int = 0,
  val tokensPerSecond: Float = 0f,
  val timeToFirstTokenMs: Long = 0L,
  val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "benchmark_records")
data class BenchmarkRecordEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val timestamp: Long = System.currentTimeMillis(),
  val prefillTokensPerSec: Float,
  val decodeTokensPerSec: Float,
  val memoryUsedMb: Long,
  val quantizationMode: String,
  val scoreGrade: String
)
