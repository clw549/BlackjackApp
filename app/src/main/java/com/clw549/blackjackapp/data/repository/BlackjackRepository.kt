package com.clw549.blackjackapp.data.repository


import android.app.Application
import androidx.annotation.WorkerThread
import com.clw549.blackjackapp.data.database.BlackjackDatabase
import com.clw549.blackjackapp.data.database.GameDao
import com.clw549.blackjackapp.data.database.model.BlackjackGame
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

class BlackjackRepository(private val gameDao: GameDao) {

    @WorkerThread
    suspend fun addGame(playerPoints : Int)
    {
        val game = BlackjackGame(playerPoints = playerPoints, hostPoints = 0, playerWin = true, numCards = 2)
        gameDao.insertGame(game)
        //TODO insert game stats into game object
    }

    @WorkerThread
    fun getGames() : Flow<List<BlackjackGame>> {
        return gameDao.getGames()
    }

    //TODO finish or remove function
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