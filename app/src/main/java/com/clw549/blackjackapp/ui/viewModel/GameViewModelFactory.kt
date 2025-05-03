package com.clw549.blackjackapp.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.clw549.blackjackapp.data.database.model.BlackjackGame
import com.clw549.blackjackapp.data.repository.BlackjackRepository
import com.clw549.blackjackapp.network.RetrofitClient

class GameViewModelFactory(private val repository: BlackjackRepository,
                           private val retrofit: RetrofitClient) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            return GameViewModel(repository, retrofit) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}