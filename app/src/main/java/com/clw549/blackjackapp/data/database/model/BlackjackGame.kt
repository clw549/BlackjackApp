package com.clw549.blackjackapp.data.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.clw549.blackjackapp.network.model.CardResponse

@Entity(tableName = "BlackjackGames")
data class BlackjackGame (
    val playerPoints : Int,
    val playerWin : Boolean,
    val hostPoints: Int,
    val numCards : Int
) {
    @PrimaryKey(autoGenerate = true)
    var gameId : Int = 0
}
