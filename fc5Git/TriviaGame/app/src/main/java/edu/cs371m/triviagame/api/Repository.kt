package edu.cs371m.triviagame.api

import retrofit2.http.Query
class Repository(private val api: TriviaApi) {
    suspend fun fetchTriviaQuestions(@Query("difficulty") level: String) : List<TriviaQuestion>{
        return api.getThree(level).results
    }
}