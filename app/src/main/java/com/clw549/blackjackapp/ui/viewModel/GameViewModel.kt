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
import com.clw549.blackjackapp.network.model.CardResponse
import com.google.gson.Gson
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

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

    private val _houseHand = MutableLiveData<List<Card?>>()
    val houseHand : LiveData<List<Card?>> get() = _houseHand

    private val _housePoints = MutableLiveData<Int>()
    val housePoints : LiveData<Int> get() = _housePoints

    fun initHouseHand() {
        var request : CardResponse? = null
        var totalPoints : Int = 0
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val url = URL("https://deckofcardsapi.com/api/deck/new/draw/?count=2")
                val connection = url.openConnection() as HttpURLConnection

                if (connection.responseCode == 200) {
                    val inputSystem = connection.inputStream
                    val inputStreamReader = InputStreamReader(inputSystem, "UTF-8")
                    request = Gson().fromJson(inputStreamReader, CardResponse::class.java)

                    inputStreamReader.close()
                    inputSystem.close()

                }
            }
           withContext(Dispatchers.Main) {
               if (request != null) {
                   _houseHand.value = request!!.cards
                   totalPoints = getCardValue(request!!.cards[0].value)
                   totalPoints += getCardValue(request!!.cards[1].value)
                   _housePoints.value = totalPoints
               }
           }
        }
    }

    fun saveGame(playerPoints : Int, hostPoints:Int, playerCards:Int) {
        val playerWin:Boolean = (playerPoints<21)&&(playerPoints>hostPoints)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
            repository.addGame(playerPoints, hostPoints, playerCards, playerWin)
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

    fun clearData() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.clearData()
                getAverage()
            }
        }
    }

    fun getCardValue(card : String) :Int {
        var cardValue = 0;
        when (card) {
            "ACE" -> cardValue = 11
            "KING" -> cardValue = 10
            "QUEEN" -> cardValue = 10
            "JACK" -> cardValue = 10
            else -> cardValue = card.toInt()
        }
        return cardValue;
    }



}
