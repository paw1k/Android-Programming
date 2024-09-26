package edu.utap.fling

import android.view.View

class Jump(private val puck: View,
           private val border: Border
) {
    private var currentCorner = 0

    private fun placePuck() {
        when (currentCorner) {
            0 -> {
                puck.x = border.minX().toFloat()
                puck.y = border.minY().toFloat()
            }
            1 -> {
                puck.x = border.maxX().toFloat() - puck.width
                puck.y = border.minY().toFloat()
            }
            2 -> {
                puck.x = border.maxX().toFloat() - puck.width
                puck.y = border.maxY().toFloat() - puck.height
            }
            3 -> {
                puck.x = border.minX().toFloat()
                puck.y = border.maxY().toFloat() - puck.height
            }
        }
    }
    fun start() {
        puck.visibility = View.VISIBLE
        puck.isClickable = true
        border.resetBorderColors()
        placePuck()
        puck.setOnClickListener {
            currentCorner = (currentCorner + 1) % 4
            placePuck()
        }
    }
    fun finish() {
        puck.setOnClickListener(null)
        puck.visibility = View.GONE
    }
}