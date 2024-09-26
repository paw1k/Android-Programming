package edu.utap.intent

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import edu.utap.intent.databinding.GameBinding
import kotlin.random.Random


// Recoded by witchel 8/21/2019
class GuessingGame : AppCompatActivity() {

    private var currentUser: String? = null
    private var theAnswer: Int = 0
    private var guesses: Int = 0
    private val deterministic = false // Make it true for deterministic testing
    private var random = Random(if (deterministic) 1000L else System.currentTimeMillis())
    private lateinit var gameBinding : GameBinding

    private fun messageUser(message: String) {
        Snackbar.make(gameBinding.guessLayout,
            message, Snackbar.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gameBinding = GameBinding.inflate(layoutInflater)
        setContentView(gameBinding.root)

        // XXX Write me Set our currentUser variable based on what MainActivity passed us
        currentUser = intent.getStringExtra(MainActivity.userKey)

        // From here to the end of the function is correct
        guesses = 0
        theAnswer = random.nextInt(101)

        currentUser?.apply {
            if (startsWith("Hint", ignoreCase = true)) {
                messageUser("The number is $theAnswer")
                gameBinding.maxScoreText.text = "Max score possible: 1000"
            }
        }
        gameBinding.guessButton.setOnClickListener {
            doGuess()
        }
    }

    private fun doGuess() {
        // No number, no guess
        if (gameBinding.theGuess.text.toString() == "") return
        val guessInt = Integer.parseInt(gameBinding.theGuess.text.toString())

        when {
            guessInt < theAnswer -> { messageUser("Too low")  }
            guessInt > theAnswer -> { messageUser("Too high") }
            else -> {finishGame()}
        }
        gameBinding.theGuess.setText("")
        guesses++
        gameBinding.currentGuessText.text = "Guesses: $guesses"
        gameBinding.maxScoreText.text = String.format("Max score possible: %3d",
            computeScore())
    }

    private fun computeScore(): Int {
        return when(guesses) {
            in 0..3 -> 1000 - 15*guesses
            in 4..7 -> 1000 - 30*guesses
            in 8..11 -> 1000 - 50*guesses
            else -> 0
        }
    }

    private fun finishGame() {
        // Intent to hold the result
        val returnIntent = Intent().apply {
            // Add the current players name and score in the Intent
            putExtra(MainActivity.userKey, currentUser)
            putExtra(MainActivity.scoreIntKey, computeScore())
        }
        // Set the result of the activity
        setResult(Activity.RESULT_OK, returnIntent)
        // finish and close
        finish()
    }
}

