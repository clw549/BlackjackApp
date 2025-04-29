package com.clw549.blackjackapp.data.database

import android.app.Application
import com.clw549.blackjackapp.data.repository.BlackjackRepository

class BlackjackApp : Application() {
    lateinit var repository: BlackjackRepository
        private set

    override fun onCreate() {
        super.onCreate()

        val database = BlackjackDatabase.getInstance(this)
        repository = BlackjackRepository(
            gameDao = database.gameDao()
        )
    }
}