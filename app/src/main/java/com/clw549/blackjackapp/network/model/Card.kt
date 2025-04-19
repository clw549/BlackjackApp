package com.clw549.blackjackapp.network.model

data class Images(
    val svg: String, // SVG format of the card image
    val png: String  // PNG format of the card image
)

data class Card(
    val code: String, //this is the card info, both suit and value (e.g., AS, 10H, etc.)
    val image: String, //this is the image of the card
    val images: Images, //this is the image of the card in both svg and png format. NEVER USE THIS
    val value: String, //this is the value of the card, yes it is a string. Probably change it if
    // you want to use it in calculations
    val suit: String //this is the suit of the card (e.g., HEARTS, DIAMONDS, etc.)
)
