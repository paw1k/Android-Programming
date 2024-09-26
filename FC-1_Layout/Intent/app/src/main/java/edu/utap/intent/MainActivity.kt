package edu.utap.intent

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import com.google.android.material.snackbar.Snackbar
import edu.utap.intent.databinding.ActivityMainBinding
import edu.utap.intent.databinding.ContentMainBinding

class MainActivity : AppCompatActivity() {
    companion object {
        const val userKey = "userKey"
        const val scoreIntKey = "scoreIntKey"
    }
    class Score(var name: String, var score: Int) {
        override fun toString(): String {
            return this.name + ": " + this.score
        }
    }

    private var highScores = mutableListOf<Score>()

    private lateinit var contentMainBinding: ContentMainBinding

    private var resultLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val userName = data?.getStringExtra(userKey)
                val score = data?.getIntExtra(scoreIntKey, 0)

                if (userName != null && score != null) {
                    addHighScore(Score(userName, score))
                    Snackbar.make(contentMainBinding.highScoreList, "Correct! Adding Score:  $userName - $score", Snackbar.LENGTH_LONG).setAction("Ok"){}.show()
                } else {
                    Snackbar.make(contentMainBinding.highScoreList, "Error in getting result", Snackbar.LENGTH_LONG).setAction("Ok"){}.show()
                }
            } else {
                Log.w(javaClass.simpleName, "Bad activity return code ${result.resultCode}")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)
        setSupportActionBar(activityMainBinding.toolbar)
        contentMainBinding = activityMainBinding.contentMain

        highScores.addAll(
            listOf(
                Score("Frank Zappa", 997),
                Score("A Student", 997),
                Score("Time to touch grass", 13)
            )
        )
        this.renderHighScores()
        contentMainBinding.playButton.setOnClickListener {
            play()
        }
        // Add menu items without overriding methods in the Activity
        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Inflate the menu; this adds items to the action bar if it is present.
                menuInflater.inflate(R.menu.menu_main, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle action bar item clicks here.
                // Other entities (e.g., fragments) can handle other menu items
                return when (menuItem.itemId) {
                    R.id.menu_exit -> {
                        finish(); true
                    }
                    else -> false
                }
            }
        })
    }
    private fun addHighScore(score: Score) {
        highScores.add(score)
        renderHighScores()
    }

    private fun renderHighScores() {
        // Sort list.  I love you Kotlin!
        highScores.sortWith(compareByDescending<Score>{it.score}.thenBy{it.name})
        // Convert Score objects into a list of strings
        val stringList = highScores.map { it.toString() }
        // A simple way to display lists
        val adapter = ArrayAdapter(this,
            android.R.layout.simple_list_item_1,
            stringList)
        contentMainBinding.highScoreList.adapter = adapter
    }

    private fun play() {
        val userName = contentMainBinding.nameField.text.toString().trim()

        when {
            //Pop up snack bar in the bottom if Name field is empty
            //Also added a Ok button to exit snack bar quickly
            //Shows snackbar if empty name..else launch the game
            userName.isBlank() -> {
                Snackbar.make(contentMainBinding.playButton, "Name is required to start the game!", Snackbar.LENGTH_LONG)
                    .setAction("OK") { }.show()
            }
            else -> {
                Intent(this, GuessingGame::class.java).also { intent ->
                    intent.putExtra(userKey, userName)
                    resultLauncher.launch(intent)
                }
            }
        }
    }
}
