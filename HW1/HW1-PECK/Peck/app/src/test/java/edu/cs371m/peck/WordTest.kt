package edu.cs371m.peck

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner


@RunWith(
    RobolectricTestRunner::class
)
class WordTest {

    private fun testWords(start: Int, numWords: Int, expectedList: List<String>) {
        val wordList = PickWords.pick(PrideAndPrejudice, start, numWords)
        // XXX write me assertEquals(expectedList, wordList)
    }

    @Test
    fun start0() {
        // XXX testWords(....
    }
}

