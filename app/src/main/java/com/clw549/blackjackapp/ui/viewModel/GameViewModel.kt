package com.clw549.blackjackapp.ui.viewModel

import androidx.annotation.WorkerThread
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.clw549.blackjackapp.network.RetrofitClient
import com.clw549.blackjackapp.network.model.Card

class GameViewModel(private val repository: BlackjackRepository) : ViewModel() {

    //average value for the UI
    private val _average = MutableLiveData<Double>();
    val average : LiveData<Double> get() = _average;

    private val _winRate = MutableLiveData<Double>();
    val winRate : LiveData<Double> get() = _winRate;

    //list of games to operate on
    val games : Flow<List<BlackjackGame>> = repository.getGames()
    //val games : LiveData<List<BlackjackGame>> get() = _games;

    private val _randomCard = MutableLiveData<Card?>()
    val randomCard: LiveData<Card?> get() = _randomCard

    fun getStatistics() {
        //TODO
    }

    fun saveGame(points : Int) {
        println(points)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
            repository.addGame(points)
            getAverage() }
        }
    }

    fun getAverage() {
        var totalPlayerPoints : Double = 0.0
        var counter : Int = 0
        var winCounter : Int = 0
        viewModelScope.launch {

            games.collect { flowGames ->
                for (flowGame in flowGames) {
                    counter += 1;
                    totalPlayerPoints += flowGame.playerPoints.toDouble()
                    if (flowGame.playerWin) winCounter ++;
                }
                _average.value = (totalPlayerPoints/counter)
                _winRate.value = (counter.toDouble()/winCounter.toDouble())
            }
        }
    }



}
