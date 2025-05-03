package com.clw549.blackjackapp.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import coil.load
import com.clw549.blackjackapp.R
import com.clw549.blackjackapp.data.database.BlackjackDatabase
import com.clw549.blackjackapp.data.repository.BlackjackRepository
import com.clw549.blackjackapp.databinding.GameLayoutBinding
import com.clw549.blackjackapp.network.RetrofitClient
import com.clw549.blackjackapp.network.model.CardResponse
import com.clw549.blackjackapp.ui.viewModel.GameViewModel
import com.clw549.blackjackapp.ui.viewModel.GameViewModelFactory
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.DecimalFormat
import kotlin.random.Random
import kotlinx.coroutines.withContext
import okhttp3.internal.wait

class MainActivity : AppCompatActivity() {
    private val format = DecimalFormat("#,##0.00")

    //var for querying the game database
    private lateinit var gameViewModel: GameViewModel

    private lateinit var binding: GameLayoutBinding

    //global variable to help us with stuff like keeping score
    //and keeping track of how many cards have been dealt
    var score: Int = 0
    var cardCount: Int = 0
    var houseScore: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize ViewBinding
        binding = GameLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Remove the manual findViewById calls; use binding instead
        val statsUi: TextView = binding.stats
        val hitButton: Button = binding.hit
        val standButton: Button = binding.stand
        val resetButton: Button = binding.reset
        val cardsImageView: ScrollView = binding.scrollView2
        val DealtCard1: ImageView = binding.DealtCard1
        val DealtCard2: ImageView = binding.DealtCard2
        val HousePoints:TextView = binding.HousePoints
//        val PlayerCard1: ImageView = binding.PlayerCard1
//        val PlayerCard2: ImageView = binding.PlayerCard2
//        val PlayerCard3: ImageView = binding.PlayerCard3
//        val PlayerCard4: ImageView = binding.PlayerCard4
//        val PlayerCard5: ImageView = binding.PlayerCard5
        val scoreText: TextView = binding.scoreTextView

        //add the imageviews to different lists so I can iterate through them
//        val playerCards = listOf(PlayerCard1, PlayerCard2, PlayerCard3, PlayerCard4, PlayerCard5)
        val dealtCards = listOf(DealtCard1, DealtCard2)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.gameLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // get a database instance
        val recipeDatabase = BlackjackDatabase.getInstance(application)
        // get repository for the database
        val repository = BlackjackRepository(recipeDatabase.gameDao())
        //getting retrofit
        val retrofit = RetrofitClient

        // get the view model factory and give it the database repository
        val factory = GameViewModelFactory(repository, retrofit)
        // set the view model database variable we had earlier
        gameViewModel = ViewModelProvider(this, factory).get(GameViewModel::class.java)

        standButton.setOnClickListener { standClick() }

        gameViewModel.average.observe(this) { averageObserver ->
            statsUi.text = "Game average: ${format.format(averageObserver)}"
        }

        gameViewModel.houseHand.observe(this) { houseHandObs ->
            if (houseHandObs.size > 1) {
                DealtCard1.load(houseHandObs[0]?.image)
                DealtCard2.load(houseHandObs[1]?.image)
            }
        }

        gameViewModel.housePoints.observe(this) { housePointsObs ->
            HousePoints.text = "House points: ${housePointsObs}"
            houseScore = housePointsObs
        }

        gameViewModel.playerCards.observe(this) { card ->
            val dealtCard = ImageView(this)
            dealtCard.load(card.image)
            binding.cardLinLayout.addView(dealtCard)
        }

        gameViewModel.playerPoints.observe(this) { playerPoints ->
            binding.scoreTextView.text = "Score: $playerPoints"

        }

        val clearDataButton: Button = binding.clearData
        clearDataButton.setOnClickListener{clearData()}
// OLD METHOD OF STARTING GAME
//        runHitClickStart()
        gameViewModel.initHouseHand()

        //this is for the hit button on click listener. when you hit, it will call the api
        //and put the card into a image view depending on where it is in the list
        hitButton.setOnClickListener {
            hitClick(cardCount)
            ++cardCount

        }

        //this is for the reset button on click listener
        resetButton.setOnClickListener {
            resetGame()
        }
    }

    fun hitClick(cardIndex: Int){
        // the calls should be done in a viewModel and observed into the view -ciaran
            //new lifecycle so we can use the suspend function

                //get a card from the API
        val request: CardResponse? = gameViewModel.getCard()

                //update the UI with the card
        // updateUserUI(request)


    }

    private fun updateUserUI(request: CardResponse?) {
        runOnUiThread {

            if (request != null) {
                val dealtCard = ImageView(this)
                dealtCard.load(request.cards[0].image)
                binding.cardLinLayout.addView(dealtCard)


                //this updates the score and deals with the face cards and aces
                score += getCardValue(request.cards[0].value)

                binding.scoreTextView.text = "Score: $score"
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

    fun resetGame() {
        //reset the game
        gameViewModel.saveGame()
        //game cleanup
        binding.cardLinLayout.removeAllViewsInLayout();
        binding.HousePoints.text = "House points: 0"
        gameViewModel.initHouseHand()
        binding.scoreTextView.text = "Score: 0"
    }

    fun standClick() {

        //save the current score in a variable
        var playerScore: Int = score;

        // house turn to pull cards
        gameViewModel.housePullCards()

        houseScore = gameViewModel.housePoints.value!!
        playerScore = gameViewModel.playerPoints.value!!

        //if the playerScore is greater than the house score, and the player score is less than 21
        //then the player wins

        //lose conditions
        if ((playerScore < houseScore && houseScore <= 21) || playerScore > 21) {
            Toast.makeText(this, "You lose!", Toast.LENGTH_SHORT).show()
        } else if (playerScore > houseScore || houseScore > 21) {
            Toast.makeText(this, "You win!", Toast.LENGTH_SHORT).show()
        } else if (houseScore == playerScore){
            Toast.makeText(this, "Draw!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Unkown winner", Toast.LENGTH_SHORT).show()
        }

        resetGame()
    }

    //function to get a fetch a card from the api
    private suspend fun getCard(): CardResponse? {
        //TODO put this functionality into GameViewModel, and call it from there
        return withContext(Dispatchers.IO) {
            //TODO this URL and connection opeing should be from the network module (RetrofitClient)
            val url = URL("https://deckofcardsapi.com/api/deck/new/draw/?count=1")
            val connection = url.openConnection() as HttpURLConnection

            if (connection.responseCode == 200) {
                connection.inputStream.use { inputStream ->
                    InputStreamReader(inputStream, "UTF-8").use { reader ->
                        Gson().fromJson(reader, CardResponse::class.java)
                    }
                }
            } else {
                null
            }
        }
    }

    fun clearData() {
        gameViewModel.clearData()
    }
}
