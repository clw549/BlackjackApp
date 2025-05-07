package com.clw549.blackjackapp.ui

import android.os.Bundle
import android.os.SystemClock
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

        // Remove the manual findViewById calls; we're using binding instead
        val statsUi: TextView = binding.stats
        val hitButton: Button = binding.hit
        val standButton: Button = binding.stand
        val resetButton: Button = binding.reset
        val cardsImageView: ScrollView = binding.scrollView2
        val DealtCard1: ImageView = binding.DealtCard1
        val DealtCard2: ImageView = binding.DealtCard2
        val HousePoints:TextView = binding.HousePoints

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
        val gameDatabase = BlackjackDatabase.getInstance(application)
        // get repository for the database
        val repository = BlackjackRepository(gameDatabase.gameDao())
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

        //This initializes the house hand, and the game as a whole
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

        //get a card from the API
        val request: CardResponse? = gameViewModel.getCard()

    }

    fun resetGame() {
        //reset the game
        gameViewModel.saveGame()
        //game cleanup
        binding.cardLinLayout.removeAllViewsInLayout();

        //deal a new house hand and reset the score
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

        SystemClock.sleep(3000);

        resetGame()
    }

    fun clearData() {
        gameViewModel.clearData()
    }
}
