package musicplayer.cs371m.musicplayer

import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    private fun currentSongIs(name: String) {
        onView(withId(R.id.playerCurrentSongText))
            .check { view, _ ->
                val tv = view as TextView
                assertEquals(tv.text, name)
            }
    }
    private fun nextSongIs(name: String) {
        onView(withId(R.id.playerNextSongText))
            .check { view, _ ->
                val tv = view as TextView
                assertEquals(tv.text, name)
            }
    }
    private fun playerTimeRemainingIs(time: String) {
        onView(withId(R.id.playerTimeRemainingText))
            .check { view, _ ->
                val tv = view as TextView
                assertEquals(tv.text, time)
            }
    }

}


