package com.example.onlinequizkotlin.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.onlinequizkotlin.MainActivity
import com.example.onlinequizkotlin.R
import com.example.onlinequizkotlin.network.WebSocketManager
import org.json.JSONArray

class WinnerFragment : Fragment(R.layout.fragment_winner) {

    /*
        Quiz bitince herkesin ekranında açılan sonuç ekranı.

        Burada:
        - İlk 3 kazanan gösterilir
        - Tüm puan tablosu gösterilir
    */

    private lateinit var txtWinners: TextView
    private lateinit var txtFinalScoreboard: TextView

    private var winnersJson: String = "[]"
    private var scoreboardJson: String = "[]"

    companion object {
        fun newInstance(
            winnersJson: String,
            scoreboardJson: String
        ): WinnerFragment {
            val fragment = WinnerFragment()

            val bundle = Bundle()
            bundle.putString("winnersJson", winnersJson)
            bundle.putString("scoreboardJson", scoreboardJson)

            fragment.arguments = bundle

            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        winnersJson = requireArguments().getString("winnersJson", "[]")
        scoreboardJson = requireArguments().getString("scoreboardJson", "[]")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        txtWinners = view.findViewById(R.id.txtWinners)
        txtFinalScoreboard = view.findViewById(R.id.txtFinalScoreboard)

        val btnBackHome = view.findViewById<Button>(R.id.btnBackHome)

        txtWinners.text = buildWinnersText(JSONArray(winnersJson))
        txtFinalScoreboard.text = buildFinalScoreboardText(JSONArray(scoreboardJson))

        btnBackHome.setOnClickListener {
            /*
                Quiz bitince bağlantıyı kapatıyoruz.

                Çünkü yeni quiz için yeni oda oluşturmak daha temiz olur.
            */
            WebSocketManager.disconnect()
            (requireActivity() as MainActivity).openHomeFragment()
        }
    }

    private fun buildWinnersText(winners: JSONArray): String {
        if (winners.length() == 0) {
            return "Kazanan bulunamadı."
        }

        val builder = StringBuilder()

        for (i in 0 until winners.length()) {
            val item = winners.optJSONObject(i)
            val username = item?.optString("username") ?: "-"
            val score = item?.optInt("score") ?: 0

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

    private fun buildFinalScoreboardText(scoreboard: JSONArray): String {
        if (scoreboard.length() == 0) {
            return "Puan tablosu yok."
        }

        val builder = StringBuilder()
        builder.append("Genel Sıralama:\n\n")

        for (i in 0 until scoreboard.length()) {
            val item = scoreboard.optJSONObject(i)
            val username = item?.optString("username") ?: "-"
            val score = item?.optInt("score") ?: 0

            builder.append("${i + 1}. $username - $score puan\n")
        }

        return builder.toString()
    }
}