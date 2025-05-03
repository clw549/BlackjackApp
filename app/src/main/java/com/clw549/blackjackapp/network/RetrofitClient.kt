package com.clw549.blackjackapp.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://deckofcardsapi.com/api/"

    // Create the Retrofit instance
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Expose the ApiService instance
    val apiService: CardApiService by lazy {
        retrofit.create(CardApiService::class.java)
    }
}