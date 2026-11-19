package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        LoadoutEntity::class,
        ScrimEntity::class,
        MatchLogEntity::class,
        SquadNoteEntity::class,
        PlayerProfileEntity::class,
        UserAccountEntity::class,
        VideoItemEntity::class,
        ChatMessageEntity::class,
        ReportedMessageEntity::class,
        BlockedUserEntity::class,
        HelpQuestionEntity::class,
        AiPromptLogEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class HarshgamerDatabase : RoomDatabase() {
    abstract fun harshgamerDao(): HarshgamerDao

    companion object {
        @Volatile
        private var INSTANCE: HarshgamerDatabase? = null

        fun getInstance(context: Context): HarshgamerDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    HarshgamerDatabase::class.java,
                    "harshgamer113.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
