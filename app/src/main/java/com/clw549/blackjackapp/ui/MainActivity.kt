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
        // get the view model factory and give it the database repository
        val factory = GameViewModelFactory(repository)
        // set the view model database variable we had earlier
        gameViewModel = ViewModelProvider(this, factory).get(GameViewModel::class.java)
//TODO test the Room database with correct threading to not block the main UI thread

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
            lifecycleScope.launch {

                //get a card from the API
                val request: CardResponse? = getCard()

                //update the UI with the card
                updateUserUI(request)

            }
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
        //gameViewModel.saveGame(score, houseScore, cardCount)
        //gameViewModel.getAverage()
        //game cleanup
        cardCount = 0;
        score = 0
        houseScore = 0
        binding.cardLinLayout.removeAllViewsInLayout();
        gameViewModel.initHouseHand()
        binding.scoreTextView.text = "Score: 0"
    }

    fun standClick() {

        //save the current score in a variable
        var playerScore: Int = score;

        //set up a lifecycle scope
        lifecycleScope.launch {

            //reset the current score to the house points, and the card count to 0
            score = gameViewModel.housePoints.value ?: 0;
            cardCount = 0;

            //remove all the cards from the screen aside from the first two
            binding.cardLinLayout.removeAllViewsInLayout();

            //while the score (which will now be the score the house has) is less than 17
            //hit the house
            while (score < 17) {
                val cardResponse = getCard()
                if (cardResponse != null) {
                    // Update score and UI in the same coroutine
                    //score += getCardValue(cardResponse.cards[0].value)
                    updateUserUI(cardResponse)
                }
            }
            //end the lifecycle scope
        }

        //if the playerScore is greater than the house score, and the player score is less than 21
        //then the player wins
        if (playerScore > score && playerScore <= 21) {
            Toast.makeText(this, "You win!", Toast.LENGTH_SHORT).show()
        } else if (playerScore == score) {
            //if the player score is equal to the house score, its a draw
            Toast.makeText(this, "Draw!", Toast.LENGTH_SHORT).show()
        } else if ((playerScore > 21 && score > 21)) {
            //if both the player and the house go over 21, that's a draw
            Toast.makeText(this, "Draw!", Toast.LENGTH_SHORT).show()
        } else if (playerScore > 21 && score <= 21 ) {
            //if the player score is greater than 21, and the house score is less
            // than or equal to 21 then the player loses
            Toast.makeText(this, "You lose!", Toast.LENGTH_SHORT).show()
        } else if (score > playerScore && score <= 21) {
            //if the house score is greater than the player score, then the house wins
            Toast.makeText(this, "You lose!", Toast.LENGTH_SHORT).show()
        }

    }

    //function to get a fetch a card from the api
    private suspend fun getCard(): CardResponse? {
        return withContext(Dispatchers.IO) {
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
