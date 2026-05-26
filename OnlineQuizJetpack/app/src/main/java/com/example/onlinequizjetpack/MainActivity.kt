package com.example.onlinequizjetpack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.onlinequizjetpack.ui.LiveQuizApp
import com.example.onlinequizjetpack.viewmodel.QuizViewModel

class MainActivity : ComponentActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val quizViewModel: QuizViewModel = viewModel()
            LiveQuizApp(
                viewModel = quizViewModel
            )
        }
    }
}