package com.clw549.blackjackapp.data.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.clw549.blackjackapp.network.model.CardResponse

@Entity(tableName = "BlackjackGames")
data class BlackjackGame (
    @PrimaryKey(autoGenerate = true)
    val gameId : Int,
    val playerPoints : Int,
    val playerWin : Boolean,
    val hostPoints: Int,
    val playerCards : List<CardResponse>,
    val numCards : Int
)
