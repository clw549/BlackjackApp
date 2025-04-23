package com.clw549.blackjackapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.clw549.blackjackapp.data.database.model.BlackjackGame

@Database(entities = [BlackjackGame::class], version = 1, exportSchema = false)
abstract class BlackjackDatabase: RoomDatabase() {

    // Abstract function to get the DAO
    abstract fun gameDao(): GameDao

    companion object {
        private var blackjackDatabase: BlackjackDatabase? = null

        // Singleton pattern to ensure only one instance of the database is created
        fun getInstance(context: Context): BlackjackDatabase {
            return blackjackDatabase ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BlackjackDatabase::class.java,
                    "BlackjackDatabase"
                ).build()
                blackjackDatabase = instance
                instance
            }
        }
    }
}