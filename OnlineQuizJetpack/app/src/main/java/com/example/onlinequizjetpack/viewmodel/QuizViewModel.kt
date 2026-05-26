package com.example.onlinequizjetpack.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onlinequizjetpack.model.AppScreen
import com.example.onlinequizjetpack.model.QuestionData
import com.example.onlinequizjetpack.network.SocketEventListener
import com.example.onlinequizjetpack.network.SocketMessageFactory
import com.example.onlinequizjetpack.network.WebSocketManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class QuizViewModel : ViewModel(), SocketEventListener {

    /*
        ViewModel görevleri:

        1. WebSocket olaylarını dinler.
        2. Server'dan gelen JSON mesajları işler.
        3. UI state'i günceller.
        4. Compose ekranları bu state'i dinler.
        5. Ekran geçişlerini yönetir.
    */

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private var pendingUsername: String = ""
    private var pendingRoomCode: String = ""
    private var pendingQuestionTime: Int = 20

    private var pendingAction: PendingAction = PendingAction.None

    private var timerJob: Job? = null

    init {
        WebSocketManager.setListener(this)
    }

    // ============================================================
    // SCREEN NAVIGATION
    // ============================================================

    fun openHome() {
        _uiState.value = QuizUiState(
            currentScreen = AppScreen.Home
        )
    }

    fun openCreateRoom() {
        _uiState.value = _uiState.value.copy(
            currentScreen = AppScreen.CreateRoom,
            statusText = ""
        )
    }

    fun openJoinRoom() {
        _uiState.value = _uiState.value.copy(
            currentScreen = AppScreen.JoinRoom,
            statusText = ""
        )
    }

    // ============================================================
    // ROOM OPERATIONS
    // ============================================================

    fun createRoom(
        username: String,
        questionTime: Int
    ) {
        val cleanUsername = username.trim()

        if (cleanUsername.isEmpty()) {
            updateStatus("Kullanıcı adı boş olamaz.")
            return
        }

        if (questionTime < 5) {
            updateStatus("Soru süresi en az 5 saniye olmalı.")
            return
        }

        pendingUsername = cleanUsername
        pendingQuestionTime = questionTime
        pendingAction = PendingAction.CreateRoom

        updateStatus("Sunucuya bağlanılıyor...")

        if (WebSocketManager.isConnected()) {
            sendCreateRoom()
        } else {
            WebSocketManager.connect()
        }
    }

    private fun sendCreateRoom() {
        pendingAction = PendingAction.None

        WebSocketManager.sendMessage(
            SocketMessageFactory.createRoom(
                username = pendingUsername,
                questionTime = pendingQuestionTime
            )
        )

        updateStatus("Oda oluşturma isteği gönderildi...")
    }

    fun joinRoom(
        roomCode: String,
        username: String
    ) {
        val cleanRoomCode = roomCode.trim()
        val cleanUsername = username.trim()

        if (cleanUsername.isEmpty()) {
            updateStatus("Kullanıcı adı boş olamaz.")
            return
        }

        if (cleanRoomCode.isEmpty()) {
            updateStatus("Oda kodu boş olamaz.")
            return
        }

        pendingRoomCode = cleanRoomCode
        pendingUsername = cleanUsername
        pendingAction = PendingAction.JoinRoom

        updateStatus("Sunucuya bağlanılıyor...")

        if (WebSocketManager.isConnected()) {
            sendJoinRoom()
        } else {
            WebSocketManager.connect()
        }
    }

    private fun sendJoinRoom() {
        pendingAction = PendingAction.None

        WebSocketManager.sendMessage(
            SocketMessageFactory.joinRoom(
                roomCode = pendingRoomCode,
                username = pendingUsername
            )
        )

        updateStatus("Odaya katılma isteği gönderildi...")
    }

    // ============================================================
    // QUESTION OPERATIONS
    // ============================================================

    fun addQuestion(
        questionText: String,
        optionTexts: List<String>,
        selectedCorrectIndex: Int
    ) {
        val state = _uiState.value
        val cleanQuestion = questionText.trim()

        if (cleanQuestion.isEmpty()) {
            updateStatus("Soru metni boş olamaz.")
            return
        }

        if (selectedCorrectIndex == -1) {
            updateStatus("Doğru cevabı seçmelisin.")
            return
        }

        val filledOptions = mutableListOf<String>()
        var correctIndexInFilledOptions = -1

        optionTexts.forEachIndexed { originalIndex, option ->
            val cleanOption = option.trim()

            if (cleanOption.isNotEmpty()) {
                if (originalIndex == selectedCorrectIndex) {
                    correctIndexInFilledOptions = filledOptions.size
                }

                filledOptions.add(cleanOption)
            }
        }

        if (filledOptions.size < 2) {
            updateStatus("En az 2 dolu şık girmelisin.")
            return
        }

        if (correctIndexInFilledOptions == -1) {
            updateStatus("Doğru cevap olarak seçtiğin şık boş olamaz.")
            return
        }

        WebSocketManager.sendMessage(
            SocketMessageFactory.addQuestion(
                roomCode = state.roomCode,
                questionText = cleanQuestion,
                options = filledOptions,
                correctIndex = correctIndexInFilledOptions
            )
        )

        updateStatus("Soru gönderildi...")
    }

    fun startQuiz() {
        val state = _uiState.value

        if (state.questionCount <= 0) {
            updateStatus("Quiz başlatmak için en az 1 soru eklemelisin.")
            return
        }

        WebSocketManager.sendMessage(
            SocketMessageFactory.startQuiz(state.roomCode)
        )

        updateStatus("Quiz başlatma isteği gönderildi...")
    }

    // ============================================================
    // ANSWER OPERATIONS
    // ============================================================

    fun submitAnswer(answerIndex: Int) {
        val state = _uiState.value

        if (state.answeredCurrentQuestion) {
            _uiState.value = state.copy(
                answerResultText = "Bu soruya zaten cevap verdin."
            )
            return
        }

        _uiState.value = state.copy(
            answeredCurrentQuestion = true,
            selectedAnswerIndex = answerIndex,
            answerResultText = "Cevabın gönderildi..."
        )

        WebSocketManager.sendMessage(
            SocketMessageFactory.submitAnswer(
                roomCode = state.roomCode,
                username = state.username,
                answerIndex = answerIndex
            )
        )
    }

    // ============================================================
    // SOCKET EVENTS
    // ============================================================

    override fun onSocketConnected() {
        updateStatus("Sunucuya bağlandı.")

        when (pendingAction) {
            PendingAction.CreateRoom -> sendCreateRoom()
            PendingAction.JoinRoom -> sendJoinRoom()
            PendingAction.None -> Unit
        }
    }

    override fun onSocketMessage(message: String) {
        val json = JSONObject(message)
        val type = json.optString("type")

        when (type) {
            "room_created" -> handleRoomCreated(json)

            "room_joined" -> handleRoomJoined(json)

            "player_list_updated" -> handlePlayerListUpdated(json)

            "question_added" -> handleQuestionAdded(json)

            "room_question_count_updated" -> handleQuestionCountUpdated(json)

            "quiz_started" -> handleQuizStarted()

            "new_question" -> handleNewQuestion(json)

            "answer_result" -> handleAnswerResult(json)

            "scoreboard_updated" -> handleScoreboardUpdated(json)

            "time_up" -> handleTimeUp(json)

            "quiz_finished" -> handleQuizFinished(json)

            "answer_rejected" -> {
                _uiState.value = _uiState.value.copy(
                    answerResultText = json.optString("message", "Cevap reddedildi.")
                )
            }

            "error" -> {
                updateStatus(json.optString("message", "Bilinmeyen hata oluştu."))
            }
        }
    }

    override fun onSocketDisconnected() {
        updateStatus("Sunucu bağlantısı kapandı.")
    }

    override fun onSocketError(error: String) {
        updateStatus("Bağlantı hatası: $error")
    }

    // ============================================================
    // SOCKET MESSAGE HANDLERS
    // ============================================================

    private fun handleRoomCreated(json: JSONObject) {
        val roomCode = json.optString("room_code")
        val username = json.optString("username")
        val questionTime = json.optInt("question_time", pendingQuestionTime)

        _uiState.value = _uiState.value.copy(
            currentScreen = AppScreen.OwnerRoom(
                roomCode = roomCode,
                username = username,
                questionTime = questionTime
            ),
            roomCode = roomCode,
            username = username,
            questionTime = questionTime,
            statusText = "Oda oluşturuldu: $roomCode",
            playersText = "Oyuncular bekleniyor...",
            questionCount = 0
        )
    }

    private fun handleRoomJoined(json: JSONObject) {
        val roomCode = json.optString("room_code")
        val username = json.optString("username")
        val questionTime = json.optInt("question_time", 20)

        _uiState.value = _uiState.value.copy(
            currentScreen = AppScreen.WaitingRoom(
                roomCode = roomCode,
                username = username,
                questionTime = questionTime
            ),
            roomCode = roomCode,
            username = username,
            questionTime = questionTime,
            statusText = "Odaya katıldın.",
            playersText = "Oyuncular yükleniyor..."
        )
    }

    private fun handlePlayerListUpdated(json: JSONObject) {
        _uiState.value = _uiState.value.copy(
            playersText = buildPlayersText(json.optJSONArray("players"))
        )
    }

    private fun handleQuestionAdded(json: JSONObject) {
        val count = json.optInt("question_count", _uiState.value.questionCount + 1)

        _uiState.value = _uiState.value.copy(
            questionCount = count,
            statusText = json.optString("message", "Soru eklendi.")
        )
    }

    private fun handleQuestionCountUpdated(json: JSONObject) {
        val count = json.optInt("question_count", _uiState.value.questionCount)

        _uiState.value = _uiState.value.copy(
            questionCount = count
        )
    }

    private fun handleQuizStarted() {
        val state = _uiState.value
        val isOwner = state.currentScreen is AppScreen.OwnerRoom

        _uiState.value = state.copy(
            currentScreen = AppScreen.Quiz(
                roomCode = state.roomCode,
                username = state.username,
                questionTime = state.questionTime,
                isOwner = isOwner
            ),
            statusText = "Quiz başladı."
        )
    }

    private fun handleNewQuestion(json: JSONObject) {
        timerJob?.cancel()

        val optionsJson = json.optJSONArray("options") ?: JSONArray()
        val options = mutableListOf<String>()

        for (i in 0 until optionsJson.length()) {
            options.add(optionsJson.optString(i))
        }

        val question = QuestionData(
            questionNumber = json.optInt("question_number"),
            totalQuestions = json.optInt("total_questions"),
            questionText = json.optString("question_text"),
            options = options,
            questionTime = json.optInt("question_time", _uiState.value.questionTime)
        )

        _uiState.value = _uiState.value.copy(
            currentQuestion = question,
            remainingTime = question.questionTime,
            selectedAnswerIndex = -1,
            currentCorrectIndex = -1,
            answeredCurrentQuestion = false,
            answerResultText = "",
            scoreboardText = buildScoreboardText(json.optJSONArray("scoreboard"))
        )

        startLocalTimer(question.questionTime)
    }

    private fun handleAnswerResult(json: JSONObject) {
        val isCorrect = json.optBoolean("is_correct")
        val earnedScore = json.optInt("earned_score")
        val totalScore = json.optInt("total_score")

        _uiState.value = _uiState.value.copy(
            answerResultText = if (isCorrect) {
                "Doğru cevap! +$earnedScore puan | Toplam: $totalScore"
            } else {
                "Yanlış cevap. Puan kazanamadın."
            }
        )
    }

    private fun handleScoreboardUpdated(json: JSONObject) {
        _uiState.value = _uiState.value.copy(
            scoreboardText = buildScoreboardText(json.optJSONArray("scoreboard"))
        )
    }

    private fun handleTimeUp(json: JSONObject) {
        timerJob?.cancel()

        val correctIndex = json.optInt("correct_index", -1)

        _uiState.value = _uiState.value.copy(
            remainingTime = 0,
            currentCorrectIndex = correctIndex,
            answerResultText = if (correctIndex >= 0) {
                "Süre bitti. Doğru cevap: ${indexToLetter(correctIndex)}"
            } else {
                "Süre bitti."
            },
            scoreboardText = buildScoreboardText(json.optJSONArray("scoreboard"))
        )
    }

    private fun handleQuizFinished(json: JSONObject) {
        timerJob?.cancel()

        val winners = json.optJSONArray("winners") ?: JSONArray()
        val scoreboard = json.optJSONArray("scoreboard") ?: JSONArray()

        _uiState.value = _uiState.value.copy(
            currentScreen = AppScreen.Winner(
                winnersJson = winners.toString(),
                scoreboardJson = scoreboard.toString()
            ),
            winnersJson = winners.toString(),
            scoreboardJson = scoreboard.toString()
        )
    }

    // ============================================================
    // TIMER
    // ============================================================

    private fun startLocalTimer(seconds: Int) {
        timerJob?.cancel()

        timerJob = viewModelScope.launch {
            var remaining = seconds

            while (remaining > 0) {
                delay(1000)
                remaining--

                _uiState.value = _uiState.value.copy(
                    remainingTime = remaining
                )
            }
        }
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private fun updateStatus(message: String) {
        _uiState.value = _uiState.value.copy(
            statusText = message
        )
    }

    fun disconnectAndGoHome() {
        timerJob?.cancel()
        WebSocketManager.disconnect()
        openHome()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        WebSocketManager.removeListener(this)
    }

    private enum class PendingAction {
        None,
        CreateRoom,
        JoinRoom
    }
}

// ============================================================
// JSON TEXT HELPERS
// ============================================================

fun buildPlayersText(players: JSONArray?): String {
    if (players == null || players.length() == 0) {
        return "Oyuncular bekleniyor..."
    }

    val builder = StringBuilder()
    builder.append("Oyuncular:\n\n")

    for (i in 0 until players.length()) {
        builder.append("${i + 1}. ${players.optString(i)}\n")
    }

    return builder.toString()
}

fun buildScoreboardText(scoreboard: JSONArray?): String {
    if (scoreboard == null || scoreboard.length() == 0) {
        return "Puan tablosu bekleniyor..."
    }

    val builder = StringBuilder()
    builder.append("Puan Tablosu:\n\n")

    for (i in 0 until scoreboard.length()) {
        val item = scoreboard.optJSONObject(i) ?: continue
        val name = item.optString("username", "-")
        val score = item.optInt("score", 0)

        builder.append("${i + 1}. $name - $score puan\n")
    }

    return builder.toString()
}

fun buildWinnersText(winners: JSONArray): String {
    if (winners.length() == 0) {
        return "Kazanan bulunamadı."
    }

    val builder = StringBuilder()

    for (i in 0 until winners.length()) {
        val item = winners.optJSONObject(i) ?: continue
        val username = item.optString("username", "-")
        val score = item.optInt("score", 0)

        val medal = when (i) {
            0 -> "🥇"
            1 -> "🥈"
            2 -> "🥉"
            else -> ""
        }

        builder.append("$medal $username\n$score puan\n\n")
    }

    return builder.toString().trim()
}

fun indexToLetter(index: Int): String {
    return if (index in 0..25) {
        ('A' + index).toString()
    } else {
        (index + 1).toString()
    }
}