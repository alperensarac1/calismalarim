package com.example.onlinequizkotlin.fragments


import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.onlinequizkotlin.MainActivity
import com.example.onlinequizkotlin.R
import com.example.onlinequizkotlin.network.SocketEventListener
import com.example.onlinequizkotlin.network.WebSocketManager
import org.json.JSONArray
import org.json.JSONObject

class WaitingRoomFragment : Fragment(R.layout.fragment_waiting_room), SocketEventListener {

    /*
        Odaya katılan normal oyuncunun bekleme ekranı.

        Burada oyuncu quiz başlamasını bekler.
        Oda sahibi quizi başlatınca QuizFragment'a geçer.
    */

    private lateinit var txtWaitingInfo: TextView
    private lateinit var txtPlayers: TextView

    private var roomCode: String = ""
    private var username: String = ""
    private var questionTime: Int = 20

    companion object {
        fun newInstance(
            roomCode: String,
            username: String,
            questionTime: Int
        ): WaitingRoomFragment {
            val fragment = WaitingRoomFragment()

            val bundle = Bundle()
            bundle.putString("roomCode", roomCode)
            bundle.putString("username", username)
            bundle.putInt("questionTime", questionTime)

            fragment.arguments = bundle

            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        roomCode = requireArguments().getString("roomCode", "")
        username = requireArguments().getString("username", "")
        questionTime = requireArguments().getInt("questionTime", 20)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        txtWaitingInfo = view.findViewById(R.id.txtWaitingInfo)
        txtPlayers = view.findViewById(R.id.txtPlayers)

        WebSocketManager.setListener(this)

        txtWaitingInfo.text = """
            Kullanıcı: $username
            Oda Kodu: $roomCode
            Soru Süresi: $questionTime saniye
            
            Oda sahibi quizi başlatınca sorular ekrana gelecek.
        """.trimIndent()
    }

    override fun onSocketConnected() {
        // Bağlantı genelde bu ekrana gelmeden önce kurulmuş olur.
    }

    override fun onSocketMessage(message: String) {
        val json = JSONObject(message)
        val type = json.optString("type")

        when (type) {
            "player_list_updated" -> {
                val players = json.optJSONArray("players")
                txtPlayers.text = buildPlayerText(players)
            }

            "quiz_started" -> {
                txtWaitingInfo.text = "Quiz başladı."

                (requireActivity() as MainActivity).openQuizFragment(
                    roomCode = roomCode,
                    username = username,
                    questionTime = questionTime,
                    isOwner = false
                )
            }

            "error" -> {
                txtWaitingInfo.text = json.optString("message", "Bilinmeyen hata oluştu.")
            }
        }
    }

    private fun buildPlayerText(players: JSONArray?): String {
        if (players == null || players.length() == 0) {
            return "Oyuncular yükleniyor..."
        }

        val builder = StringBuilder()
        builder.append("Odada bulunan oyuncular:\n\n")

        for (i in 0 until players.length()) {
            builder.append("${i + 1}. ${players.optString(i)}\n")
        }

        return builder.toString()
    }

    override fun onSocketDisconnected() {
        txtWaitingInfo.text = "Sunucu bağlantısı kapandı."
    }

    override fun onSocketError(error: String) {
        txtWaitingInfo.text = "Bağlantı hatası: $error"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        WebSocketManager.removeListener(this)
    }
}