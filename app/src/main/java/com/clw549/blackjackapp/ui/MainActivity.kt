package com.clw549.blackjackapp.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.clw549.blackjackapp.R
import com.clw549.blackjackapp.data.database.BlackjackDatabase
import com.clw549.blackjackapp.data.repository.BlackjackRepository
import com.clw549.blackjackapp.ui.viewModel.GameViewModel
import com.clw549.blackjackapp.ui.viewModel.GameViewModelFactory
import java.text.DecimalFormat
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private val format = DecimalFormat("#,##0.00")

    //var for querying the game database
    private lateinit var gameViewModel : GameViewModel



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.game_layout)
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

        val statsUi : TextView = findViewById(R.id.stats);

        val hitButton : Button = findViewById(R.id.hit);
        hitButton.setOnClickListener{hitClick()}

        val standButton : Button = findViewById(R.id.stand)
        standButton.setOnClickListener{standClick()}

        gameViewModel.average.observe(this) { averageObserver ->
            statsUi.text = "Game average: ${format.format(averageObserver)}"


        }

        gameViewModel.winRate.observe(this) {
        }

    }

    fun hitClick() {

    }

    fun standClick() {
        //TODO send game data to database to save the game
        gameViewModel.getAverage()
    }
}

