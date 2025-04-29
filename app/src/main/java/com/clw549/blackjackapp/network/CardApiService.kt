package com.clw549.blackjackapp.network

import com.clw549.blackjackapp.network.model.CardResponse
import retrofit2.http.GET

interface CardApiService {
    @GET("deck/new/draw/?count=1")
    suspend fun getRandomCard(): CardResponse
}