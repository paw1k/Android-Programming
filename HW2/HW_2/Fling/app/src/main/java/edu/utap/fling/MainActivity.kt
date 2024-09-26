package edu.utap.fling

import android.os.Bundle
import android.util.Log
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.appcompat.app.AppCompatActivity
import edu.utap.fling.databinding.ActivityMainBinding
import edu.utap.fling.databinding.ContentMainBinding
import kotlinx.coroutines.*


class MainActivity :
    AppCompatActivity(),
    CoroutineScope by MainScope() {
    // A round lasts this many milliseconds
    private val durationMillis = 9000L // Increase for testing if you want
    private var score = 0
    private lateinit var contentMainBinding: ContentMainBinding
    private lateinit var timer: Timer
    private lateinit var border: Border
    private lateinit var fling: Fling
    private lateinit var jump: Jump
    private var timerActive = false
    // This determines which borders are chosen as goal boarders.
    // It is to your advantage to change these values to more thoroughly
    // test your code.
    private var goalOrder = intArrayOf(0, 1, 2, 3)
    // NB: We use this variable for our testing
    var testing = false
    enum class GameType {
        JUMP, FLING
    }
    // Start on Jump
    private var gameType = GameType.JUMP

    override fun onDestroy() {
        super.onDestroy()
        cancel() // destroy all coroutines
    }

    private fun layoutThenFinishInit() {
        // The problem is that the layout has 0dp elements whose true dimension is only
        // known after layout has happened.  Unfortunately (in this case), layout
        // hasn't happened in onCreate.
        // So set a callback to initialize ourselves the content has been laid out
        contentMainBinding.allContent.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                //Remove it here unless you want to get this callback for EVERY
                //layout pass, which can get you into infinite loops if you ever
                //modify the layout from within this method.
                contentMainBinding.allContent.viewTreeObserver.removeOnGlobalLayoutListener(this)

                // frame* from XML layout
                border = Border(listOf(contentMainBinding.frameT,
                    contentMainBinding.frameB,
                    contentMainBinding.frameS,
                    contentMainBinding.frameE),
                    goalOrder)
                fling = Fling(contentMainBinding.puck, border, testing)
                jump = Jump(contentMainBinding.puck, border)
                jump.start()
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        contentMainBinding = activityMainBinding.contentMain
        setContentView(activityMainBinding.root)
        setSupportActionBar(activityMainBinding.toolbar)
        // XML button
        contentMainBinding.flingBut.setOnClickListener {
            if(gameType == GameType.FLING) {
                // If user gets impatient and reclicks button, cancel running coroutines
                coroutineContext.cancelChildren()
                doRound()
            }
        }

        contentMainBinding.gameBut.setOnClickListener {
            // Don't switch modes during a game of fling
            Log.d("XXX ${timer.millisLeft()}", "")
            if( !timerActive ) {
                // Toggle game type
                if (gameType == GameType.JUMP) {
                    jump.finish()
                    gameType = GameType.FLING
                    contentMainBinding.gameBut.text = "Fling"
                } else {
                    gameType = GameType.JUMP
                    contentMainBinding.gameBut.text = "Jump"
                    jump.start()
                }
            }
        }
        // Create timer
        timer = Timer(contentMainBinding.flingBut)
        // XML TextView
        contentMainBinding.scoreTV.text = "0"
        score = 0

        layoutThenFinishInit()
    }

    private fun doScore(millisLeft: Long) {
        // User won Get one point plus tenths of a sec left
        if( millisLeft > 0 ) {
            if (testing) {
                score++
                contentMainBinding.scoreTV.text = score.toString()
                Log.d(localClassName, "User wins ${score}")
            } else {
                score += (millisLeft / 100).toInt()
                score++
                contentMainBinding.scoreTV.text = score.toString()
                Log.d(localClassName, "User wins ${score}")
            }
        }
    }

    private fun doRound() {
        launch {
            val timerJob = async {
                timerActive = true
                timer.timerCo(durationMillis)
                fling.deactivatePuck()
                timerActive = false
            }
            fling.playRound() {
                doScore(timer.millisLeft())
                launch{timerJob.cancelAndJoin()}
                fling.deactivatePuck()
                timerActive = false
            }
        }
    }
    // Don't inflate a menu
}
