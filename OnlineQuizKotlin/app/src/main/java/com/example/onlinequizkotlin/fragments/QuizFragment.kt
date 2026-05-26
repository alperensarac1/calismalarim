package com.example.onlinequizkotlin.fragments

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.onlinequizkotlin.MainActivity
import com.example.onlinequizkotlin.R
import com.example.onlinequizkotlin.network.SocketEventListener
import com.example.onlinequizkotlin.network.SocketMessageFactory
import com.example.onlinequizkotlin.network.WebSocketManager
import org.json.JSONArray
import org.json.JSONObject

class QuizFragment : Fragment(R.layout.fragment_quiz), SocketEventListener {

    /*
        Quiz ekranı.

        Bu gelişmiş sürümde:
        - Şık butonları programatik olarak tasarlanır.
        - Kullanıcı cevap verince seçilen şık renklendirilir.
        - Doğru cevap yeşil, yanlış cevap kırmızı gösterilir.
        - Süre bitince doğru cevap herkesin ekranında yeşil görünür.
        - Kullanıcı yanlış cevap verdiyse kendi yanlış seçimi kırmızı kalır.
    */

    private lateinit var txtQuestionCounter: TextView
    private lateinit var txtTimer: TextView
    private lateinit var txtQuestionText: TextView
    private lateinit var txtAnswerResult: TextView
    private lateinit var txtScoreboard: TextView
    private lateinit var optionsContainer: LinearLayout

    private var roomCode: String = ""
    private var username: String = ""
    private var questionTime: Int = 20
    private var isOwner: Boolean = false

    private var answeredCurrentQuestion = false

    /*
        Kullanıcının mevcut soruda seçtiği cevap indexi.

        Örnek:
        A seçtiyse 0
        B seçtiyse 1
        C seçtiyse 2
    */
    private var selectedAnswerIndex: Int = -1

    /*
        Server süre bitince doğru cevabı gönderiyor.

        time_up mesajında:
        correct_index
        gelir.

        Bu değeri burada saklıyoruz.
    */
    private var currentCorrectIndex: Int = -1

    /*
        Dinamik üretilen şık butonlarını burada tutuyoruz.
        Böylece sonradan renklerini değiştirebiliyoruz.
    */
    private val optionButtons = mutableListOf<Button>()

    private var countDownTimer: CountDownTimer? = null

    companion object {
        fun newInstance(
            roomCode: String,
            username: String,
            questionTime: Int,
            isOwner: Boolean
        ): QuizFragment {
            val fragment = QuizFragment()

            val bundle = Bundle()
            bundle.putString("roomCode", roomCode)
            bundle.putString("username", username)
            bundle.putInt("questionTime", questionTime)
            bundle.putBoolean("isOwner", isOwner)

            fragment.arguments = bundle

            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        roomCode = requireArguments().getString("roomCode", "")
        username = requireArguments().getString("username", "")
        questionTime = requireArguments().getInt("questionTime", 20)
        isOwner = requireArguments().getBoolean("isOwner", false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        txtQuestionCounter = view.findViewById(R.id.txtQuestionCounter)
        txtTimer = view.findViewById(R.id.txtTimer)
        txtQuestionText = view.findViewById(R.id.txtQuestionText)
        txtAnswerResult = view.findViewById(R.id.txtAnswerResult)
        txtScoreboard = view.findViewById(R.id.txtScoreboard)
        optionsContainer = view.findViewById(R.id.optionsContainer)

        WebSocketManager.setListener(this)

        txtQuestionCounter.text = "Soru bekleniyor..."
        txtTimer.text = "Süre: $questionTime"
        txtQuestionText.text = "Quiz başladı. İlk soru bekleniyor."
        txtAnswerResult.text = ""
    }

    override fun onSocketConnected() {
        /*
            Bu ekrana geçildiğinde bağlantı zaten açıktır.
        */
    }

    override fun onSocketMessage(message: String) {
        val json = JSONObject(message)
        val type = json.optString("type")

        when (type) {
            "new_question" -> {
                handleNewQuestion(json)
            }

            "answer_result" -> {
                handleAnswerResult(json)
            }

            "scoreboard_updated" -> {
                val scoreboard = json.optJSONArray("scoreboard")
                txtScoreboard.text = buildScoreboardText(scoreboard)
            }

            "time_up" -> {
                handleTimeUp(json)
            }

            "quiz_finished" -> {
                handleQuizFinished(json)
            }

            "error" -> {
                txtAnswerResult.text = json.optString("message", "Bilinmeyen hata oluştu.")
            }

            "answer_rejected" -> {
                txtAnswerResult.text = json.optString("message", "Cevap reddedildi.")
            }
        }
    }

    private fun handleNewQuestion(json: JSONObject) {
        /*
            Yeni soru geldiğinde önce eski soru bilgilerini temizliyoruz.
        */
        answeredCurrentQuestion = false
        selectedAnswerIndex = -1
        currentCorrectIndex = -1

        countDownTimer?.cancel()

        txtAnswerResult.text = ""

        val questionNumber = json.optInt("question_number")
        val totalQuestions = json.optInt("total_questions")
        val questionText = json.optString("question_text")
        val options = json.optJSONArray("options") ?: JSONArray()
        val serverQuestionTime = json.optInt("question_time", questionTime)
        val scoreboard = json.optJSONArray("scoreboard")

        questionTime = serverQuestionTime

        txtQuestionCounter.text = "Soru $questionNumber / $totalQuestions"
        txtQuestionText.text = questionText
        txtScoreboard.text = buildScoreboardText(scoreboard)

        renderOptions(options)

        startLocalTimer(questionTime)
    }

    private fun renderOptions(options: JSONArray) {
        /*
            Eski şık butonlarını temizliyoruz.
        */
        optionsContainer.removeAllViews()
        optionButtons.clear()

        for (i in 0 until options.length()) {
            val optionText = options.optString(i)

            val button = Button(requireContext())

            button.text = "${indexToLetter(i)}) $optionText"
            button.textSize = 16f
            button.isAllCaps = false
            button.setTextColor(Color.parseColor("#111827"))
            button.typeface = Typeface.DEFAULT_BOLD
            button.background = createOptionBackground(
                backgroundColor = Color.WHITE,
                strokeColor = Color.parseColor("#D1D5DB")
            )

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(58)
            )

            params.setMargins(0, 0, 0, dpToPx(14))

            button.layoutParams = params

            button.setOnClickListener {
                submitAnswer(i)
            }

            optionButtons.add(button)
            optionsContainer.addView(button)
        }
    }

    private fun submitAnswer(answerIndex: Int) {
        if (answeredCurrentQuestion) {
            txtAnswerResult.text = "Bu soruya zaten cevap verdin."
            return
        }

        answeredCurrentQuestion = true
        selectedAnswerIndex = answerIndex

        /*
            Kullanıcı cevap verir vermez seçtiği şıkkı bekleme rengine alıyoruz.
            Server cevap sonucunu gönderince yeşil/kırmızıya çevireceğiz.
        */
        markOptionAsSelectedWaiting(answerIndex)

        setOptionButtonsEnabled(false)

        val message = SocketMessageFactory.submitAnswer(
            roomCode = roomCode,
            username = username,
            answerIndex = answerIndex
        )

        WebSocketManager.sendMessage(message)

        txtAnswerResult.text = "Cevabın gönderildi..."
    }

    private fun handleAnswerResult(json: JSONObject) {
        val isCorrect = json.optBoolean("is_correct")
        val earnedScore = json.optInt("earned_score")
        val totalScore = json.optInt("total_score")

        if (selectedAnswerIndex >= 0) {
            if (isCorrect) {
                markOptionAsCorrect(selectedAnswerIndex)
            } else {
                markOptionAsWrong(selectedAnswerIndex)
            }
        }

        txtAnswerResult.text = if (isCorrect) {
            "Doğru cevap! +$earnedScore puan | Toplam: $totalScore"
        } else {
            "Yanlış cevap. Puan kazanamadın."
        }
    }

    private fun handleTimeUp(json: JSONObject) {
        countDownTimer?.cancel()

        setOptionButtonsEnabled(false)

        currentCorrectIndex = json.optInt("correct_index", -1)

        val scoreboard = json.optJSONArray("scoreboard")

        txtTimer.text = "Süre bitti"

        /*
            Süre bitince doğru cevap herkese gösterilir.
        */
        if (currentCorrectIndex >= 0) {
            markOptionAsCorrect(currentCorrectIndex)
        }

        /*
            Eğer kullanıcı cevap verdiyse ve seçtiği cevap doğru değilse
            kendi seçtiği yanlış cevap kırmızı kalsın.
        */
        if (
            selectedAnswerIndex >= 0 &&
            currentCorrectIndex >= 0 &&
            selectedAnswerIndex != currentCorrectIndex
        ) {
            markOptionAsWrong(selectedAnswerIndex)
        }

        txtAnswerResult.text = if (currentCorrectIndex >= 0) {
            "Süre bitti. Doğru cevap: ${indexToLetter(currentCorrectIndex)}"
        } else {
            "Süre bitti."
        }

        txtScoreboard.text = buildScoreboardText(scoreboard)
    }

    private fun handleQuizFinished(json: JSONObject) {
        countDownTimer?.cancel()

        val winners = json.optJSONArray("winners") ?: JSONArray()
        val scoreboard = json.optJSONArray("scoreboard") ?: JSONArray()

        (requireActivity() as MainActivity).openWinnerFragment(
            winnersJson = winners.toString(),
            scoreboardJson = scoreboard.toString()
        )
    }

    private fun startLocalTimer(seconds: Int) {
        txtTimer.text = "Süre: $seconds"

        countDownTimer = object : CountDownTimer(seconds * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val remaining = (millisUntilFinished / 1000L).toInt()
                txtTimer.text = "Süre: $remaining"
            }

            override fun onFinish() {
                /*
                    Server zaten time_up mesajı gönderecek.
                    Burada sadece kullanıcı tarafında butonları kapatıyoruz.
                */
                txtTimer.text = "Süre bitti"
                setOptionButtonsEnabled(false)
            }
        }

        countDownTimer?.start()
    }

    private fun setOptionButtonsEnabled(enabled: Boolean) {
        for (button in optionButtons) {
            button.isEnabled = enabled
        }
    }

    private fun markOptionAsSelectedWaiting(index: Int) {
        val button = optionButtons.getOrNull(index) ?: return

        button.background = createOptionBackground(
            backgroundColor = Color.parseColor("#FEF3C7"),
            strokeColor = Color.parseColor("#F59E0B")
        )

        button.setTextColor(Color.parseColor("#92400E"))
    }

    private fun markOptionAsCorrect(index: Int) {
        val button = optionButtons.getOrNull(index) ?: return

        button.background = createOptionBackground(
            backgroundColor = Color.parseColor("#DCFCE7"),
            strokeColor = Color.parseColor("#16A34A")
        )

        button.setTextColor(Color.parseColor("#166534"))
    }

    private fun markOptionAsWrong(index: Int) {
        val button = optionButtons.getOrNull(index) ?: return

        button.background = createOptionBackground(
            backgroundColor = Color.parseColor("#FEE2E2"),
            strokeColor = Color.parseColor("#DC2626")
        )

        button.setTextColor(Color.parseColor("#991B1B"))
    }

    private fun createOptionBackground(
        backgroundColor: Int,
        strokeColor: Int
    ): GradientDrawable {
        /*
            Şık butonlarına modern kart görünümü verir.

            backgroundColor:
            Butonun iç rengi.

            strokeColor:
            Butonun kenarlık rengi.
        */
        val drawable = GradientDrawable()

        drawable.shape = GradientDrawable.RECTANGLE
        drawable.cornerRadius = dpToPx(14).toFloat()
        drawable.setColor(backgroundColor)
        drawable.setStroke(dpToPx(1), strokeColor)

        return drawable
    }

    private fun buildScoreboardText(scoreboard: JSONArray?): String {
        if (scoreboard == null || scoreboard.length() == 0) {
            return "Puan tablosu bekleniyor..."
        }

        val builder = StringBuilder()
        builder.append("Puan Tablosu:\n\n")

        for (i in 0 until scoreboard.length()) {
            val item = scoreboard.optJSONObject(i)
            val name = item?.optString("username") ?: "-"
            val score = item?.optInt("score") ?: 0

            builder.append("${i + 1}. $name - $score puan\n")
        }

        return builder.toString()
    }

    private fun indexToLetter(index: Int): String {
        return when (index) {
            0 -> "A"
            1 -> "B"
            2 -> "C"
            3 -> "D"
            4 -> "E"
            5 -> "F"
            6 -> "G"
            7 -> "H"
            8 -> "I"
            9 -> "J"
            10 -> "K"
            11 -> "L"
            12 -> "M"
            13 -> "N"
            14 -> "O"
            15 -> "P"
            16 -> "Q"
            17 -> "R"
            18 -> "S"
            19 -> "T"
            20 -> "U"
            21 -> "V"
            22 -> "W"
            23 -> "X"
            24 -> "Y"
            25 -> "Z"
            else -> "${index + 1}"
        }
    }

    override fun onSocketDisconnected() {
        txtAnswerResult.text = "Sunucu bağlantısı kapandı."
    }

    override fun onSocketError(error: String) {
        txtAnswerResult.text = "Bağlantı hatası: $error"
    }

    override fun onDestroyView() {
        super.onDestroyView()

        countDownTimer?.cancel()
        WebSocketManager.removeListener(this)
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}