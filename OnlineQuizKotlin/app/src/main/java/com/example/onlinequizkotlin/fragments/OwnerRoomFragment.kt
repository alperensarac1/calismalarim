package com.example.onlinequizkotlin.fragments

import com.example.onlinequizkotlin.MainActivity
import com.example.onlinequizkotlin.R
import com.example.onlinequizkotlin.network.SocketMessageFactory
import com.example.onlinequizkotlin.network.WebSocketManager
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.onlinequizkotlin.network.SocketEventListener
import org.json.JSONArray
import org.json.JSONObject

class OwnerRoomFragment : Fragment(R.layout.fragment_owner_room), SocketEventListener {

    /*
        Oda sahibi ekranı.

        Bu sürümde şıklar artık sabit değildir.

        Önceki yapıda:
        A, B, C, D, E sabitti.

        Bu yapıda:
        - Kullanıcı + Şık Ekle butonuna basar.
        - Her basışta yeni bir şık satırı oluşturulur.
        - Her şık satırında:
            1. RadioButton
            2. EditText
            3. Sil butonu
          bulunur.

        Böylece oda sahibi istediği kadar şıklı soru oluşturabilir.
    */

    private lateinit var txtRoomCode: TextView
    private lateinit var txtInfo: TextView
    private lateinit var txtPlayers: TextView
    private lateinit var txtQuestionCount: TextView
    private lateinit var txtStatus: TextView

    private lateinit var edtQuestionText: EditText
    private lateinit var optionsRadioGroup: RadioGroup

    private var roomCode: String = ""
    private var username: String = ""
    private var questionTime: Int = 20

    private var questionCount: Int = 0

    /*
        Şık satırlarını takip etmek için liste tutuyoruz.

        Her OptionRow:
        - RadioButton
        - EditText
        - Ana satır LinearLayout

        bilgilerini saklar.
    */
    private val optionRows = mutableListOf<OptionRow>()

    companion object {
        fun newInstance(
            roomCode: String,
            username: String,
            questionTime: Int
        ): OwnerRoomFragment {
            val fragment = OwnerRoomFragment()

            val bundle = Bundle()
            bundle.putString("roomCode", roomCode)
            bundle.putString("username", username)
            bundle.putInt("questionTime", questionTime)

            fragment.arguments = bundle

            return fragment
        }
    }

    data class OptionRow(
        val rowLayout: LinearLayout,
        val radioButton: RadioButton,
        val editText: EditText
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        roomCode = requireArguments().getString("roomCode", "")
        username = requireArguments().getString("username", "")
        questionTime = requireArguments().getInt("questionTime", 20)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        txtRoomCode = view.findViewById(R.id.txtRoomCode)
        txtInfo = view.findViewById(R.id.txtInfo)
        txtPlayers = view.findViewById(R.id.txtPlayers)
        txtQuestionCount = view.findViewById(R.id.txtQuestionCount)
        txtStatus = view.findViewById(R.id.txtStatus)

        edtQuestionText = view.findViewById(R.id.edtQuestionText)
        optionsRadioGroup = view.findViewById(R.id.optionsRadioGroup)

        val btnAddOption = view.findViewById<Button>(R.id.btnAddOption)
        val btnAddQuestion = view.findViewById<Button>(R.id.btnAddQuestion)
        val btnStartQuiz = view.findViewById<Button>(R.id.btnStartQuiz)

        WebSocketManager.setListener(this)

        txtRoomCode.text = "Oda Kodu: $roomCode"

        txtInfo.text = """
            Kullanıcı: $username
            Soru Süresi: $questionTime saniye
            
            Bu kodu diğer kullanıcılara ver.
            Onlar bu kod ile odaya katılacak.
        """.trimIndent()

        /*
            Başlangıçta en az 2 şık hazır gelsin.
            Çünkü çoktan seçmeli soruda minimum 2 seçenek gerekir.
        */
        addOptionRow()
        addOptionRow()

        btnAddOption.setOnClickListener {
            addOptionRow()
        }

        btnAddQuestion.setOnClickListener {
            addQuestion()
        }

        btnStartQuiz.setOnClickListener {
            startQuiz()
        }
    }

    private fun addOptionRow(defaultText: String = "") {
        /*
            Şık satırını programatik olarak oluşturuyoruz.

            XML'de sabit EditText koymak yerine her şık burada dinamik üretiliyor.
        */

        val context = requireContext()

        val rowLayout = LinearLayout(context)
        rowLayout.orientation = LinearLayout.HORIZONTAL
        rowLayout.gravity = Gravity.CENTER_VERTICAL
        rowLayout.setPadding(0, 8, 0, 8)

        val rowParams = RadioGroup.LayoutParams(
            RadioGroup.LayoutParams.MATCH_PARENT,
            RadioGroup.LayoutParams.WRAP_CONTENT
        )

        rowLayout.layoutParams = rowParams

        val radioButton = RadioButton(context)

        /*
            Her RadioButton için benzersiz id oluşturuyoruz.

            RadioGroup tek seçim mantığını bu id üzerinden yönetir.
        */
        radioButton.id = View.generateViewId()

        val radioParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        radioButton.layoutParams = radioParams

        val editText = EditText(context)
        editText.hint = "Şık ${optionRows.size + 1}"
        editText.setText(defaultText)
        editText.isSingleLine = true
        editText.setPadding(14, 0, 14, 0)
        editText.setBackgroundColor(0xFFFFFFFF.toInt())

        val editParams = LinearLayout.LayoutParams(
            0,
            dpToPx(54),
            1f
        )

        editParams.setMargins(8, 0, 8, 0)

        editText.layoutParams = editParams

        val btnRemove = Button(context)
        btnRemove.text = "Sil"
        btnRemove.isAllCaps = false
        btnRemove.textSize = 13f

        val removeParams = LinearLayout.LayoutParams(
            dpToPx(72),
            dpToPx(54)
        )

        btnRemove.layoutParams = removeParams

        rowLayout.addView(radioButton)
        rowLayout.addView(editText)
        rowLayout.addView(btnRemove)

        optionsRadioGroup.addView(rowLayout)

        val optionRow = OptionRow(
            rowLayout = rowLayout,
            radioButton = radioButton,
            editText = editText
        )

        optionRows.add(optionRow)

        /*
            Sil butonu:
            - Eğer sadece 2 şık kaldıysa silmeye izin vermiyoruz.
            - Çünkü server tarafında en az 2 seçenek istiyoruz.
        */
        btnRemove.setOnClickListener {
            removeOptionRow(optionRow)
        }

        updateOptionHints()
    }

    private fun removeOptionRow(optionRow: OptionRow) {
        if (optionRows.size <= 2) {
            txtStatus.text = "En az 2 şık kalmalı."
            return
        }

        /*
            Silinen şık doğru cevap olarak seçiliyse RadioGroup seçimini temizliyoruz.
        */
        if (optionsRadioGroup.checkedRadioButtonId == optionRow.radioButton.id) {
            optionsRadioGroup.clearCheck()
        }

        optionsRadioGroup.removeView(optionRow.rowLayout)
        optionRows.remove(optionRow)

        updateOptionHints()
    }

    private fun updateOptionHints() {
        /*
            Şık silindikçe veya eklendikçe hint yazıları güncellenir.

            Örnek:
            Şık 1
            Şık 2
            Şık 3
        */
        optionRows.forEachIndexed { index, row ->
            row.editText.hint = "Şık ${index + 1}"
        }
    }

    private fun addQuestion() {
        val questionText = edtQuestionText.text.toString().trim()

        if (questionText.isEmpty()) {
            txtStatus.text = "Soru metni boş olamaz."
            return
        }

        /*
            Boş olmayan şıkları topluyoruz.

            Örneğin 5 şık satırı olabilir ama kullanıcı sadece 3 tanesini doldurmuş olabilir.
            Bu durumda sadece dolu 3 şık server'a gönderilir.
        */
        val filledOptions = mutableListOf<String>()

        /*
            selectedOriginalIndex:
            Kullanıcının RadioButton ile seçtiği şıkkın optionRows içindeki gerçek indexidir.
        */
        val selectedOriginalIndex = getSelectedOptionIndex()

        if (selectedOriginalIndex == -1) {
            txtStatus.text = "Doğru cevabı seçmelisin."
            return
        }

        var correctIndexInFilledOptions = -1

        optionRows.forEachIndexed { originalIndex, row ->
            val optionText = row.editText.text.toString().trim()

            if (optionText.isNotEmpty()) {
                /*
                    Eğer bu satır seçili doğru cevapsa,
                    server'a gönderilecek dolu şıklar listesindeki indexini hesaplıyoruz.
                */
                if (originalIndex == selectedOriginalIndex) {
                    correctIndexInFilledOptions = filledOptions.size
                }

                filledOptions.add(optionText)
            }
        }

        if (filledOptions.size < 2) {
            txtStatus.text = "En az 2 dolu şık girmelisin."
            return
        }

        /*
            Kullanıcı doğru cevap olarak boş bir şıkkı seçmiş olabilir.
            Bu durumda correctIndexInFilledOptions -1 kalır.
        */
        if (correctIndexInFilledOptions == -1) {
            txtStatus.text = "Doğru cevap olarak seçtiğin şık boş olamaz."
            return
        }

        val message = SocketMessageFactory.addQuestion(
            roomCode = roomCode,
            questionText = questionText,
            options = filledOptions,
            correctIndex = correctIndexInFilledOptions
        )

        WebSocketManager.sendMessage(message)

        txtStatus.text = "Soru gönderildi..."
    }

    private fun getSelectedOptionIndex(): Int {
        val checkedId = optionsRadioGroup.checkedRadioButtonId

        if (checkedId == -1) {
            return -1
        }

        optionRows.forEachIndexed { index, row ->
            if (row.radioButton.id == checkedId) {
                return index
            }
        }

        return -1
    }

    private fun clearQuestionForm() {
        edtQuestionText.setText("")

        optionsRadioGroup.removeAllViews()
        optionRows.clear()
        optionsRadioGroup.clearCheck()

        /*
            Yeni soru için tekrar minimum 2 boş şık açıyoruz.
        */
        addOptionRow()
        addOptionRow()
    }

    private fun startQuiz() {
        if (questionCount <= 0) {
            txtStatus.text = "Quiz başlatmak için en az 1 soru eklemelisin."
            return
        }

        val message = SocketMessageFactory.startQuiz(roomCode)
        WebSocketManager.sendMessage(message)

        txtStatus.text = "Quiz başlatma isteği gönderildi..."
    }

    override fun onSocketConnected() {
        // Bu ekrana gelindiğinde bağlantı genelde zaten açıktır.
    }

    override fun onSocketMessage(message: String) {
        val json = JSONObject(message)
        val type = json.optString("type")

        when (type) {
            "player_list_updated" -> {
                val players = json.optJSONArray("players")
                txtPlayers.text = buildPlayerText(players)
            }

            "question_added" -> {
                questionCount = json.optInt("question_count", questionCount + 1)

                txtQuestionCount.text = "Eklenen soru: $questionCount"
                txtStatus.text = json.optString("message", "Soru eklendi.")

                clearQuestionForm()
            }

            "room_question_count_updated" -> {
                questionCount = json.optInt("question_count", questionCount)
                txtQuestionCount.text = "Eklenen soru: $questionCount"
            }

            "quiz_started" -> {
                txtStatus.text = "Quiz başladı."

                (requireActivity() as MainActivity).openQuizFragment(
                    roomCode = roomCode,
                    username = username,
                    questionTime = questionTime,
                    isOwner = true
                )
            }

            "error" -> {
                txtStatus.text = json.optString("message", "Bilinmeyen hata oluştu.")
            }
        }
    }

    private fun buildPlayerText(players: JSONArray?): String {
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

    override fun onSocketDisconnected() {
        txtStatus.text = "Sunucu bağlantısı kapandı."
    }

    override fun onSocketError(error: String) {
        txtStatus.text = "Bağlantı hatası: $error"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        WebSocketManager.removeListener(this)
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}