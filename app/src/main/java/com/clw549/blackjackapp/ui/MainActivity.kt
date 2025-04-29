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
import kotlinx.coroutines.launch
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.DecimalFormat
import kotlin.random.Random

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
        val cardsImageView: ScrollView = binding.scrollView2
        val DealtCard1: ImageView = binding.DealtCard1
        val DealtCard2: ImageView = binding.DealtCard2
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

        val clearDataButton: Button = binding.clearData
        clearDataButton.setOnClickListener{clearData()}

        runHitClickStart()

        //this is for the hit button on click listener. when you hit, it will call the api
        //and put the card into a image view depending on where it is in the list
        hitButton.setOnClickListener {
            val hitThread = hitClick(cardCount)
            hitThread.start()
            ++cardCount

        }
    }

    fun runHitClickStart() {
        //run hitClick twice to get the first two cards the dealer has in onCreate
        val initialHitThread = hitClick(0)
        initialHitThread.start()
        ++cardCount


        val initialHitThread2 = hitClick(1)
        initialHitThread2.start()
        ++cardCount

        binding.HousePoints.text = houseScore.toString()
    }

    fun hitClick(cardIndex: Int): Thread {
        // the calls should be done in a viewModel and observed into the view -ciaran
        return Thread {
            val url = URL("https://deckofcardsapi.com/api/deck/new/draw/?count=1")
            val connection = url.openConnection() as HttpURLConnection

            if (connection.responseCode == 200) {
                val inputSystem = connection.inputStream
                val inputStreamReader = InputStreamReader(inputSystem, "UTF-8")
                val request = Gson().fromJson(inputStreamReader, CardResponse::class.java)



                when (cardIndex) {
                    0 -> {
                        binding.DealtCard1.load(request.cards[0].image)
                        houseScore += getCardValue(request.cards[0].value)
                    }

                    1 -> {
                        binding.DealtCard2.load(request.cards[0].image)
                        houseScore += getCardValue(request.cards[0].value)

                    }

                    else -> {
                        updateUserUI(request)
                    }

                }

                inputStreamReader.close()
                inputSystem.close()


            } else {
                runOnUiThread {
                    Toast.makeText(this, "The call didn't work :(", Toast.LENGTH_SHORT).show()
                }
            }
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
                binding.HousePoints.text = houseScore.toString()
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

    fun standClick() {
        // subtract 2 from card count to get the number of cards the player had

        gameViewModel.saveGame(score, houseScore, cardCount-2)
        gameViewModel.getAverage()
        //game cleanup
        cardCount = 0;
        score = 0
        houseScore = 0
        binding.cardLinLayout.removeAllViewsInLayout();
        runHitClickStart()
        binding.HousePoints.text = houseScore.toString()

    }

    fun clearData() {
        gameViewModel.clearData()
    }
}
