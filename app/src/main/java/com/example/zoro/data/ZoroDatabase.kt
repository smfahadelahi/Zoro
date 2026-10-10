package com.example.zoro.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [
    ConversationEntity::class,
    MessageEntity::class,
    BenchmarkRecordEntity::class
  ],
  version = 1,
  exportSchema = false
)
abstract class ZoroDatabase : RoomDatabase() {
  abstract fun zoroDao(): ZoroDao

  companion object {
    @Volatile
    private var INSTANCE: ZoroDatabase? = null

    fun getDatabase(context: Context): ZoroDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          ZoroDatabase::class.java,
          "zoro_llm_database.db"
        )
        .fallbackToDestructiveMigration()
        .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
