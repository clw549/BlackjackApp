package com.clw549.blackjackapp.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
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
    var score: Int = 0

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
        val DealtCard1: ImageView = binding.DealtCard1
        val DealtCard2: ImageView = binding.DealtCard2
        val PlayerCard1: ImageView = binding.PlayerCard1
        val PlayerCard2: ImageView = binding.PlayerCard2
        val PlayerCard3: ImageView = binding.PlayerCard3
        val PlayerCard4: ImageView = binding.PlayerCard4
        val PlayerCard5: ImageView = binding.PlayerCard5
        val scoreText: TextView = binding.scoreTextView

        //add the imageviews to different lists so I can iterate through them
        val playerCards = listOf(PlayerCard1, PlayerCard2, PlayerCard3, PlayerCard4, PlayerCard5)
        val dealtCards = listOf(DealtCard1, DealtCard2)

        //variable to keep track of how many cards have been dealt
        var cardCount: Int = 0

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

        //this is for the hit button on click listener. when you hit, it will call the api
        //and put the card into a image view depending on where it is in the list
        hitButton.setOnClickListener {
            if(cardCount < 2) {
                val hitThread = hitClick(dealtCards[cardCount])
                hitThread.start()
                ++cardCount
            }
            else{
                val hitThread = hitClick(playerCards[cardCount-2])
                hitThread.start()
                ++cardCount
            }
        }

        standButton.setOnClickListener { standClick() }

        gameViewModel.average.observe(this) { averageObserver ->
            statsUi.text = "Game average: ${format.format(averageObserver)}"
        }

        gameViewModel.winRate.observe(this) {
        }
    }

    fun hitClick(DealtCard: ImageView): Thread {
        return Thread {
            val url = URL("https://deckofcardsapi.com/api/deck/new/draw/?count=1")
            val connection = url.openConnection() as HttpURLConnection

            if (connection.responseCode == 200) {
                val inputSystem = connection.inputStream
                val inputStreamReader = InputStreamReader(inputSystem, "UTF-8")
                val request = Gson().fromJson(inputStreamReader, CardResponse::class.java)

                updateUI(request, DealtCard)

                inputStreamReader.close()
                inputSystem.close()
            } else {
                runOnUiThread {
                    Toast.makeText(this, "The call didn't work :(", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateUI(request: CardResponse?, DealtCard: ImageView) {
        runOnUiThread {
            if (request != null) {
                DealtCard.load(request.cards[0].image)

                //this updates the score and deals with the face cards and aces
                when (request.cards[0].value) {
                    "ACE" -> score += 11
                    "KING" -> score += 10
                    "QUEEN" -> score += 10
                    "JACK" -> score += 10
                    else -> score += request.cards[0].value.toInt()
                }

                binding.scoreTextView.text = "Score: $score"
            }
        }
    }

    fun standClick() {
        // TODO send game data to database to save the game
        gameViewModel.getAverage()
    }
}
