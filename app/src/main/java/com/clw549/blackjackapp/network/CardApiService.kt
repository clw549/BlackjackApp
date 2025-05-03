package com.clw549.blackjackapp.network

import com.clw549.blackjackapp.network.model.CardResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CardApiService {
    @GET("deck/new/draw/?count=1")
    suspend fun getRandomCard(): CardResponse
    @GET("deck/new/draw")
    suspend fun getNumRandomCard(@Query("count", encoded = true) num: Int): CardResponse
}