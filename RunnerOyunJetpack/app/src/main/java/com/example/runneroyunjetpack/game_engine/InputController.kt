package com.example.runneroyunjetpack.game_engine

import kotlin.math.abs

class InputController(
    private val screenHProvider: () -> Int,
    private val onTap: () -> Unit,
    private val onSwipeDown: () -> Unit,
    private val onRestartTap: () -> Unit,
    private val isGameOver: () -> Boolean
) {
    private var downX = 0f
    private var downY = 0f
    private var downTime = 0L

    fun onDown(x: Float, y: Float) {
        downX = x
        downY = y
        downTime = System.currentTimeMillis()
    }

    fun onUp(x: Float, y: Float): Boolean {
        val dx = x - downX
        val dy = y - downY

        if (isGameOver()) {
            onRestartTap()
            return true
        }

        val swipeDownThreshold = screenHProvider() * 0.10f
        if (dy > swipeDownThreshold && abs(dy) > abs(dx)) {
            onSwipeDown()
            return true
        }

        val dtMs = System.currentTimeMillis() - downTime
        if (dtMs < 220) onTap()

        return true
    }
}
