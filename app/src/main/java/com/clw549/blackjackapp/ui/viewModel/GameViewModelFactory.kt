package com.clw549.blackjackapp.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.clw549.blackjackapp.data.database.model.BlackjackGame
import com.clw549.blackjackapp.data.repository.BlackjackRepository

class GameViewModelFactory(private val repository: BlackjackRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            return GameViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}