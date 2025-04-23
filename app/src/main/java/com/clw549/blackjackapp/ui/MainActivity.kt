package com.clw549.blackjackapp.ui

import android.os.Bundle
import android.view.View
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

class MainActivity : AppCompatActivity() {

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
//        gameViewModel.saveGame(21)
//        gameViewModel.saveGame(2)

        val statsUi : TextView = findViewById(R.id.stats);

        gameViewModel.average.observe(this) { averageObserver ->
            statsUi.text = averageObserver.toString()

        }

    }

}

