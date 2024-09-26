package edu.cs371m.peck

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.Rect
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.setPadding
import kotlin.random.Random

class Words(private val sentenceTV: TextView,
            private val frame: FrameLayout,
            private val random: Random) {
    private val neutralBgColor = Color.rgb(0xCD, 0xCD, 0xCD)
    private val outOfOrderColor = Color.rgb(200, 0, 0)
    private val textViewHeight by lazy {
        val textView = createTextView("Doesn't matter", 0)
        textView.measure(0, 0)
        textView.measuredHeight
    }


    private var indexForCurrrentWord = 0
    private val textViewForWord = mutableListOf<TextView>()

    private fun findTVWidth(textView: TextView): Int {
        textView.measure(0, 0)
        return textView.measuredWidth
    }
    private fun createTextView(text: String, index: Int): TextView {
        val lparams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val textView = TextView(frame.context)
        textView.layoutParams = lparams
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        textView.setPadding(8)
        textView.text = text
        textView.tag = index.toString()
        textView.setBackgroundColor(neutralBgColor)
        return textView

    }
    private fun outOfOrderPick(view: View) {
        val colorToWarn : Animator = ValueAnimator
            .ofObject(ArgbEvaluator(), neutralBgColor, outOfOrderColor)
            .apply{duration = 200} // milliseconds
            .apply{addUpdateListener { animator -> view.setBackgroundColor(animator.animatedValue as Int) }}
        val colorFromWarn = ValueAnimator
            .ofObject(ArgbEvaluator(), outOfOrderColor, neutralBgColor)
            .apply{duration = 350}
            .apply{addUpdateListener { animator -> view.setBackgroundColor(animator.animatedValue as Int) }}
        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(
            colorToWarn,
            colorFromWarn
        )
        animatorSet.start()
    }



    fun playRound(numWords: Int, wordsDone: () -> Unit) {
        frame.removeAllViews()
        textViewForWord.clear()
        indexForCurrrentWord = 0

        val words = PickWords.pick(PrideAndPrejudice, 100, numWords)
        sentenceTV.text = words.joinToString(" ")

        words.forEachIndexed { index, word ->
            val wordTextView = createTextView(word, index)

            var placedSuccessfully = false
            while (!placedSuccessfully) {
                wordTextView.x = random.nextInt(frame.width - findTVWidth(wordTextView)).toFloat()
                wordTextView.y = random.nextInt(frame.height - textViewHeight).toFloat()

                placedSuccessfully = textViewForWord.none { existingTextView ->
                    Rect.intersects(
                        Rect(existingTextView.x.toInt(), existingTextView.y.toInt(), existingTextView.x.toInt() + findTVWidth(existingTextView), existingTextView.y.toInt() + textViewHeight),
                        Rect(wordTextView.x.toInt(), wordTextView.y.toInt(), wordTextView.x.toInt() + findTVWidth(wordTextView), wordTextView.y.toInt() + textViewHeight)
                    )
                }
            }

            frame.addView(wordTextView)
            textViewForWord.add(wordTextView)

            wordTextView.setOnClickListener { view ->
                if (view.tag.toString().toInt() == indexForCurrrentWord) {
                    indexForCurrrentWord++
                    view.visibility = View.GONE
                    if (indexForCurrrentWord == numWords) {
                        wordsDone()
                    }
                } else {
                    outOfOrderPick(view)
                }
            }
        }
    }
}
