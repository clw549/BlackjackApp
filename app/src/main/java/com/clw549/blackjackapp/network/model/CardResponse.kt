package com.clw549.blackjackapp.network.model

// Data class to represent the API response
data class CardResponse (
    val success: Boolean,   //API response status (e.g., "true")
    val deck_id: String, // ID of the deck
    val cards: List<Card>, // List of drawn cards
    val remaining: Int // Number of cards remaining in the deck
)