package com.clw549.blackjackapp.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.clw549.blackjackapp.data.database.model.BlackjackGame
import kotlinx.coroutines.flow.Flow


@Dao
interface GameDao {
    @Insert
    fun insertGame(game: BlackjackGame)

    @Query ("SELECT * FROM BlackjackGames")
    fun getGames() : Flow<List<BlackjackGame>>
}