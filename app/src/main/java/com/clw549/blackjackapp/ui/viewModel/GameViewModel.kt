package com.clw549.blackjackapp.ui.viewModel

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
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
import kotlinx.coroutines.awaitAll
import retrofit2.Retrofit
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class GameViewModel(private val repository: BlackjackRepository,
                    private val retrofit: RetrofitClient) : ViewModel() {

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

    private val _playerPoints = MutableLiveData<Int>()
    val playerPoints : LiveData<Int> get() = _playerPoints

    private val _playerCards = MutableLiveData<Card>()
    val playerCards : LiveData<Card> get() = _playerCards

    private val _playerCardNum = MutableLiveData<Int>()
    val playerCardNum : LiveData<Int> get() = _playerCardNum

    fun initHouseHand() {
        _housePoints.value = 0
        _playerPoints.value = 0
        _playerCardNum.value = 0

        var request : CardResponse? = null
        var totalPoints : Int = 0
        viewModelScope.launch {
            request = retrofit.apiService.getNumRandomCard(2)
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

    fun saveGame() {
        val pPoints = _playerPoints.value!!
        val hPoints = _housePoints.value!!
        val numCards = _playerCardNum.value!!
        val playerWin:Boolean = (pPoints < 21)&&(pPoints > hPoints)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.addGame(pPoints, hPoints, numCards, playerWin)
                getAverage()
            }
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

    fun getCard() : CardResponse?{
        var cardRes: CardResponse? = null
        var card : Card
        var points = 0
        var cardNum : Int = _playerCardNum.value!!
        viewModelScope.launch {
            cardRes = retrofit.apiService.getRandomCard()
            if (cardRes != null && cardRes?.cards?.get(0) != null) {
                card = cardRes!!.cards[0]
                _playerCards.value = card
                points = _playerPoints.value!!
                points += getCardValue(card.value)
                _playerPoints.value = points
            }


        }
        cardNum += 1
        _playerCardNum.value = cardNum
        return cardRes;
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

    fun housePullCards() {
        var newCard : Card
        var cardRes : CardResponse?
        var points = 0
        viewModelScope.launch {
            while (housePoints.value != null && housePoints.value!! < 17) {
                cardRes = retrofit.apiService.getRandomCard()
                if (cardRes != null) {
                    newCard = cardRes!!.cards[0]
                    points = _housePoints.value!!
                    points += getCardValue(newCard.value)
                    _housePoints.value = points
                }
            }

        }
    }
}
