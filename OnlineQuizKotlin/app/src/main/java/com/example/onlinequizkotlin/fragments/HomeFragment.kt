package com.example.onlinequizkotlin.fragments


import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.onlinequizkotlin.MainActivity
import com.example.onlinequizkotlin.R

class HomeFragment : Fragment(R.layout.fragment_home) {

    /*
        Ana ekran.

        Kullanıcı burada iki işlemden birini seçer:
        1. Oda oluştur
        2. Odaya giriş yap
    */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnCreateRoom = view.findViewById<Button>(R.id.btnCreateRoom)
        val btnJoinRoom = view.findViewById<Button>(R.id.btnJoinRoom)

        btnCreateRoom.setOnClickListener {
            (requireActivity() as MainActivity).openCreateRoomFragment()
        }

        btnJoinRoom.setOnClickListener {
            (requireActivity() as MainActivity).openJoinRoomFragment()
        }
    }
}