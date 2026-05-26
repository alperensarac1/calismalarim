package com.example.onlinequizkotlin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.onlinequizkotlin.fragments.CreateRoomFragment
import com.example.onlinequizkotlin.fragments.HomeFragment
import com.example.onlinequizkotlin.fragments.JoinRoomFragment
import com.example.onlinequizkotlin.fragments.OwnerRoomFragment
import com.example.onlinequizkotlin.fragments.QuizFragment
import com.example.onlinequizkotlin.fragments.WaitingRoomFragment
import com.example.onlinequizkotlin.fragments.WinnerFragment


class MainActivity : AppCompatActivity() {

    /*
        Uygulamada tek Activity kullanıyoruz.

        Bu Activity sadece Fragment ekranlarını değiştirir.
        Asıl iş mantığı Fragment'larda ve WebSocketManager içinde bulunur.
    */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            openHomeFragment()
        }
    }

    private fun replaceFragment(
        fragment: Fragment,
        addToBackStack: Boolean = true
    ) {
        val transaction = supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer, fragment)

        if (addToBackStack) {
            transaction.addToBackStack(null)
        }

        transaction.commit()
    }

    fun openHomeFragment() {
        replaceFragment(HomeFragment(), addToBackStack = false)
    }

    fun openCreateRoomFragment() {
        replaceFragment(CreateRoomFragment())
    }

    fun openJoinRoomFragment() {
        replaceFragment(JoinRoomFragment())
    }

    fun openOwnerRoomFragment(
        roomCode: String,
        username: String,
        questionTime: Int
    ) {
        replaceFragment(
            OwnerRoomFragment.newInstance(
                roomCode = roomCode,
                username = username,
                questionTime = questionTime
            )
        )
    }

    fun openWaitingRoomFragment(
        roomCode: String,
        username: String,
        questionTime: Int
    ) {
        replaceFragment(
            WaitingRoomFragment.newInstance(
                roomCode = roomCode,
                username = username,
                questionTime = questionTime
            )
        )
    }

    fun openQuizFragment(
        roomCode: String,
        username: String,
        questionTime: Int,
        isOwner: Boolean
    ) {
        replaceFragment(
            QuizFragment.newInstance(
                roomCode = roomCode,
                username = username,
                questionTime = questionTime,
                isOwner = isOwner
            )
        )
    }

    fun openWinnerFragment(
        winnersJson: String,
        scoreboardJson: String
    ) {
        replaceFragment(
            WinnerFragment.newInstance(
                winnersJson = winnersJson,
                scoreboardJson = scoreboardJson
            ),
            addToBackStack = false
        )
    }
}