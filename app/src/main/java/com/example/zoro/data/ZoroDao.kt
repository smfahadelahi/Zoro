package com.example.zoro.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ZoroDao {
  @Query("SELECT * FROM conversations ORDER BY updatedAt DESC")
  fun getAllConversations(): Flow<List<ConversationEntity>>

  @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
  suspend fun getConversationById(id: String): ConversationEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertConversation(conversation: ConversationEntity)

  @Update
  suspend fun updateConversation(conversation: ConversationEntity)

  @Query("DELETE FROM conversations WHERE id = :id")
  suspend fun deleteConversation(id: String)

  @Query("DELETE FROM conversations")
  suspend fun deleteAllConversations()

  @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
  fun getMessagesForConversation(conversationId: String): Flow<List<MessageEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMessage(message: MessageEntity)

  @Update
  suspend fun updateMessage(message: MessageEntity)

  @Query("DELETE FROM messages WHERE conversationId = :conversationId")
  suspend fun deleteMessagesForConversation(conversationId: String)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBenchmarkRecord(record: BenchmarkRecordEntity)

  @Query("SELECT * FROM benchmark_records ORDER BY timestamp DESC LIMIT 20")
  fun getBenchmarkRecords(): Flow<List<BenchmarkRecordEntity>>
}
