package com.example.runneroyunkotlin

import android.app.Activity
import android.os.Bundle

class GameActivity : Activity() {

    private lateinit var gameView: GameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        gameView = findViewById(R.id.gameView)
    }

    override fun onResume() {
        super.onResume()
        gameView.resume()
    }

    override fun onPause() {
        super.onPause()
        gameView.pause()
    }
}
