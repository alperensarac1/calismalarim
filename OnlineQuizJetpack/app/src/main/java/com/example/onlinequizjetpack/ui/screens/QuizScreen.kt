package com.example.onlinequizjetpack.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.onlinequizjetpack.model.QuestionData
import com.example.onlinequizjetpack.ui.components.ScreenContainer
import com.example.onlinequizjetpack.viewmodel.indexToLetter

@Composable
fun QuizScreen(
    questionData: QuestionData?,
    remainingTime: Int,
    selectedAnswerIndex: Int,
    currentCorrectIndex: Int,
    answeredCurrentQuestion: Boolean,
    answerResultText: String,
    scoreboardText: String,
    onSubmitAnswer: (Int) -> Unit
) {
    ScreenContainer {
        Text(
            text = "Quiz",
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (questionData == null) {
            Text(
                text = "Soru bekleniyor...",
                fontSize = 18.sp,
                color = Color(0xFF374151)
            )
        } else {
            Text(
                text = "Soru ${questionData.questionNumber} / ${questionData.totalQuestions}",
                fontSize = 15.sp,
                color = Color(0xFF6B7280)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = if (remainingTime > 0) {
                    "Süre: $remainingTime"
                } else {
                    "Süre bitti"
                },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFDC2626)
            )

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = questionData.questionText,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(22.dp))

            questionData.options.forEachIndexed { index, option ->
                val visualState = getOptionVisualState(
                    index = index,
                    selectedAnswerIndex = selectedAnswerIndex,
                    currentCorrectIndex = currentCorrectIndex,
                    answered = answeredCurrentQuestion
                )

                QuizOptionButton(
                    text = "${indexToLetter(index)}) $option",
                    state = visualState,
                    enabled = !answeredCurrentQuestion && remainingTime > 0,
                    onClick = {
                        onSubmitAnswer(index)
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = answerResultText,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF374151)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = scoreboardText,
                fontSize = 15.sp,
                color = Color(0xFF111827)
            )
        }
    }
}

enum class OptionVisualState {
    Normal,
    Waiting,
    Correct,
    Wrong
}

fun getOptionVisualState(
    index: Int,
    selectedAnswerIndex: Int,
    currentCorrectIndex: Int,
    answered: Boolean
): OptionVisualState {
    /*
        Doğru cevap server tarafından time_up mesajında gelir.
        O gelene kadar sadece seçilen cevap sarı bekleme renginde durur.
    */

    if (currentCorrectIndex >= 0 && index == currentCorrectIndex) {
        return OptionVisualState.Correct
    }

    if (
        currentCorrectIndex >= 0 &&
        selectedAnswerIndex >= 0 &&
        index == selectedAnswerIndex &&
        selectedAnswerIndex != currentCorrectIndex
    ) {
        return OptionVisualState.Wrong
    }

    if (
        answered &&
        selectedAnswerIndex == index &&
        currentCorrectIndex == -1
    ) {
        return OptionVisualState.Waiting
    }

    return OptionVisualState.Normal
}

@Composable
fun QuizOptionButton(
    text: String,
    state: OptionVisualState,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor: Color
    val borderColor: Color
    val textColor: Color

    when (state) {
        OptionVisualState.Normal -> {
            backgroundColor = Color.White
            borderColor = Color(0xFFD1D5DB)
            textColor = Color(0xFF111827)
        }

        OptionVisualState.Waiting -> {
            backgroundColor = Color(0xFFFEF3C7)
            borderColor = Color(0xFFF59E0B)
            textColor = Color(0xFF92400E)
        }

        OptionVisualState.Correct -> {
            backgroundColor = Color(0xFFDCFCE7)
            borderColor = Color(0xFF16A34A)
            textColor = Color(0xFF166534)
        }

        OptionVisualState.Wrong -> {
            backgroundColor = Color(0xFFFEE2E2)
            borderColor = Color(0xFFDC2626)
            textColor = Color(0xFF991B1B)
        }
    }

    Button(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = textColor,
            disabledContainerColor = backgroundColor,
            disabledContentColor = textColor
        ),
        enabled = enabled,
        onClick = onClick
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold
        )
    }
}