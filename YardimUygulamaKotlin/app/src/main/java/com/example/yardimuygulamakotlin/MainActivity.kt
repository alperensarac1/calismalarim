package com.example.yardimuygulamakotlin

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import androidx.fragment.app.Fragment
import com.example.yardimuygulamakotlin.entity.Session
import com.example.yardimuygulamakotlin.view.HelperOpenListFragment
import com.example.yardimuygulamakotlin.view.LoginFragment
import com.example.yardimuygulamakotlin.view.PatientHelpFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            val start: Fragment = if (Session.isLoggedIn(this)) {
                val id = Session.userId(this)
                val role = Session.role(this) ?: ""
                if (role == "YARDIMCI") HelperOpenListFragment.newInstance(id)
                else PatientHelpFragment.newInstance(id)
            } else {
                LoginFragment()
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.container, start)
                .commit()
        }
    }
}