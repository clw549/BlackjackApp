package com.clw549.blackjackapp.data.repository

import android.app.Application
import com.clw549.blackjackapp.data.database.BlackjackDatabase
import com.clw549.blackjackapp.data.database.GameDao
import com.clw549.blackjackapp.data.database.model.BlackjackGame
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

class BlackjackRepository(private val gameDao: GameDao) {

    suspend fun addGame(playerPoints : Int)
    {
        //TODO insert game stats into game object
    }

    suspend fun getGames() : Flow<List<BlackjackGame>> {
        return gameDao.getGames()
    }

    suspend fun calculateStats() {
        val games : Flow<List<BlackjackGame>> = getGames()
        var totalPlayerPoints : Int = 0
        var totalHostPoints : Int = 0
        var counter : Int = 0

        games.collect{ blackjackGames ->
            for (blackjackGame in blackjackGames) {
                totalPlayerPoints += blackjackGame.playerPoints
                totalHostPoints += blackjackGame.hostPoints
                counter ++
            }
        }
    }
}