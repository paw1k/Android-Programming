package edu.cs371m.triviagame

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cs371m.triviagame.api.Repository
import edu.cs371m.triviagame.api.TriviaApi
import edu.cs371m.triviagame.api.TriviaQuestion
import kotlinx.coroutines.launch
import java.util.*

class MainViewModel : ViewModel() {
    private var difficulty = "medium"
    val triviaQuestionsList: MutableLiveData<List<TriviaQuestion>> = MutableLiveData()
    val triviaApi = TriviaApi.create()
    val repository = Repository(triviaApi)

    var fetchDone : MutableLiveData<Boolean> = MutableLiveData(false)
    init {
        this.netRefresh()
    }

    fun setDifficulty(level: String) {
        difficulty = when(level.lowercase(Locale.getDefault())) {
            // Sanitize input
            "easy" -> "easy"
            "medium" -> "medium"
            "hard" -> "hard"
            else -> "medium"
        }
        Log.d(javaClass.simpleName, "level $level END difficulty $difficulty")
    }

    fun netRefresh() {
        viewModelScope.launch {
            fetchDone.postValue(false)
            try {
                val response = repository.fetchTriviaQuestions(difficulty)
                if (response.isNotEmpty()) {
                    triviaQuestionsList.postValue(response)
                    fetchDone.postValue(true)
                } else {
                    fetchDone.postValue(true)
                }
            } catch (e: Exception) {
                fetchDone.postValue(true)
            }
        }
    }

}
