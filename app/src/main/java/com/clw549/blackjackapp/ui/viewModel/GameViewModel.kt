package com.clw549.blackjackapp.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.clw549.blackjackapp.data.database.model.BlackjackGame
import com.clw549.blackjackapp.data.repository.BlackjackRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class GameViewModel(private val repository: BlackjackRepository) : ViewModel() {

    //average value for the UI
    private val _average = MutableLiveData<Double>();
    val average : LiveData<Double> get() = _average;

    //list of games to operate on
    val games : LiveData<List<BlackjackGame>> = repository.getGames().asLiveData()
    //val games : LiveData<List<BlackjackGame>> get() = _games;

    fun getStatistics() {
        //TODO
    }

    suspend fun saveGame(points : Int) {
        println(points)
        viewModelScope.launch {
            repository.addGame(points)
            getAverage()
        }
    }

    fun getAverage() {
        val currentGames : List<BlackjackGame>? = games.value
        var totalPlayerPoints : Double = 0.0
        var counter : Double = 0.0
        if (currentGames is List<BlackjackGame>) {
            for (game in currentGames) {
                totalPlayerPoints += game.playerPoints
                counter++;
                println("game printed");
            }
            _average.value = (totalPlayerPoints/counter)
        }
        println("printing getGames")
        println(repository.getGames().asLiveData().value)
    }

}