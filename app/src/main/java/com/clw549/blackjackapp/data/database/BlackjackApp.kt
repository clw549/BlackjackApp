package com.clw549.blackjackapp.data.database
//TODO finish this
class BlackjackApp : Application() {
    lateinit var repository: BlackjackRepository
    private set

            override fun onCreate() {
        super.onCreate()

        val database = RecipeDatabase.getInstance(this)
        repository = RecipeRepository(
            recipeDao = database.recipeDao()
        )
    }
}