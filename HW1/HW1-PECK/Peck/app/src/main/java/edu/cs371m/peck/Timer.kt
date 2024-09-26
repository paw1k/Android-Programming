package edu.cs371m.peck

import android.graphics.Color
import android.widget.Button
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext

class Timer(private var button: Button) {
    private var endMillis = 0L // Only read, not Atomic

    fun millisLeft(): Long {return endMillis - System.currentTimeMillis()}

    suspend fun timerCo(durationMillis: Long) {
        endMillis = System.currentTimeMillis() + durationMillis
        var currentMillis = System.currentTimeMillis()
        // End color of replayButton is  red
        val delayMillis = 100L // Time step for updates
        // XML button
        button.setBackgroundColor(Color.WHITE)

        while (coroutineContext.isActive
            && (endMillis > currentMillis)) {
            // XML TextView
            button.text = String.format(
                "%1.1f",
                (endMillis - currentMillis) / 1000.0f
            )
            val scaleFactor = (endMillis - currentMillis).toFloat() / durationMillis.toFloat()
            button.setBackgroundColor(
                Color.rgb(
                    255,
                    (255 * scaleFactor).toInt(),
                    (255 * scaleFactor).toInt()
                )
            )
            delay(delayMillis)
            currentMillis = System.currentTimeMillis()
        }
        button.text = "0"
    }
}