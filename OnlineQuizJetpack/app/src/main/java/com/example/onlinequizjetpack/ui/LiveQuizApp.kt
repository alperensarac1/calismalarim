package com.example.onlinequizjetpack.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.example.onlinequizjetpack.model.AppScreen
import com.example.onlinequizjetpack.ui.screens.CreateRoomScreen
import com.example.onlinequizjetpack.ui.screens.HomeScreen
import com.example.onlinequizjetpack.ui.screens.JoinRoomScreen
import com.example.onlinequizjetpack.ui.screens.OwnerRoomScreen
import com.example.onlinequizjetpack.ui.screens.QuizScreen
import com.example.onlinequizjetpack.ui.screens.WaitingRoomScreen
import com.example.onlinequizjetpack.ui.screens.WinnerScreen
import com.example.onlinequizjetpack.viewmodel.QuizViewModel


@Composable
fun LiveQuizApp(
    viewModel: QuizViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    MaterialTheme {
        Surface(
            color = Color(0xFFF8FAFC)
        ) {
            when (uiState.currentScreen) {

                is AppScreen.Home -> {
                    HomeScreen(
                        onCreateRoomClick = {
                            viewModel.openCreateRoom()
                        },
                        onJoinRoomClick = {
                            viewModel.openJoinRoom()
                        }
                    )
                }

                is AppScreen.CreateRoom -> {
                    CreateRoomScreen(
                        statusText = uiState.statusText,
                        onCreateRoom = { username, questionTime ->
                            viewModel.createRoom(
                                username = username,
                                questionTime = questionTime
                            )
                        },
                        onBack = {
                            viewModel.openHome()
                        }
                    )
                }

                is AppScreen.JoinRoom -> {
                    JoinRoomScreen(
                        statusText = uiState.statusText,
                        onJoinRoom = { roomCode, username ->
                            viewModel.joinRoom(
                                roomCode = roomCode,
                                username = username
                            )
                        },
                        onBack = {
                            viewModel.openHome()
                        }
                    )
                }

                is AppScreen.OwnerRoom -> {
                    OwnerRoomScreen(
                        roomCode = uiState.roomCode,
                        username = uiState.username,
                        questionTime = uiState.questionTime,
                        playersText = uiState.playersText,
                        questionCount = uiState.questionCount,
                        statusText = uiState.statusText,
                        onAddQuestion = { questionText, options, correctIndex ->
                            viewModel.addQuestion(
                                questionText = questionText,
                                optionTexts = options,
                                selectedCorrectIndex = correctIndex
                            )
                        },
                        onStartQuiz = {
                            viewModel.startQuiz()
                        }
                    )
                }

                is AppScreen.WaitingRoom -> {
                    WaitingRoomScreen(
                        roomCode = uiState.roomCode,
                        username = uiState.username,
                        questionTime = uiState.questionTime,
                        playersText = uiState.playersText,
                        statusText = uiState.statusText
                    )
                }

                is AppScreen.Quiz -> {
                    QuizScreen(
                        questionData = uiState.currentQuestion,
                        remainingTime = uiState.remainingTime,
                        selectedAnswerIndex = uiState.selectedAnswerIndex,
                        currentCorrectIndex = uiState.currentCorrectIndex,
                        answeredCurrentQuestion = uiState.answeredCurrentQuestion,
                        answerResultText = uiState.answerResultText,
                        scoreboardText = uiState.scoreboardText,
                        onSubmitAnswer = { answerIndex ->
                            viewModel.submitAnswer(answerIndex)
                        }
                    )
                }

                is AppScreen.Winner -> {
                    WinnerScreen(
                        winnersJson = uiState.winnersJson,
                        scoreboardJson = uiState.scoreboardJson,
                        onBackHome = {
                            viewModel.disconnectAndGoHome()
                        }
                    )
                }
            }
        }
    }
}