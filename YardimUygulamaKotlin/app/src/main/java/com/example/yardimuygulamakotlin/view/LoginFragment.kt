package com.example.yardimuygulamakotlin.view

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.yardimuygulamakotlin.R
import com.example.yardimuygulamakotlin.entity.Session
import com.example.yardimuygulamakotlin.repo.AuthRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginFragment : Fragment(R.layout.fragment_login) {

    private val repo = AuthRepo()

    private lateinit var etPhone: EditText
    private lateinit var etPass: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnGoRegister: Button
    private lateinit var progress: ProgressBar
    private lateinit var tvInfo: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        etPhone = view.findViewById(R.id.etPhone)
        etPass = view.findViewById(R.id.etPass)
        btnLogin = view.findViewById(R.id.btnLogin)
        btnGoRegister = view.findViewById(R.id.btnGoRegister)
        progress = view.findViewById(R.id.progress)
        tvInfo = view.findViewById(R.id.tvInfo)

        btnGoRegister.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, RegisterFragment())
                .addToBackStack(null)
                .commit()
        }

        btnLogin.setOnClickListener { doLogin() }
    }

    private fun doLogin() {
        val phone = etPhone.text.toString().trim()
        val pass = etPass.text.toString().trim()
        if (phone.isEmpty() || pass.isEmpty()) {
            tvInfo.text = "Telefon ve şifre zorunlu"
            return
        }

        progress.visibility = View.VISIBLE
        tvInfo.text = ""

        lifecycleScope.launch(Dispatchers.IO) {
            val res = repo.login(phone, pass)
            withContext(Dispatchers.Main) {
                progress.visibility = View.GONE

                if (res?.ok == true && res.user != null) {
                    val u = res.user!!
                    Session.save(requireContext(), u.id, u.role)
                    goHomeByRole(u.id, u.role)
                } else {
                    tvInfo.text = res?.error ?: "Giriş başarısız"
                }
            }
        }
    }

    private fun goHomeByRole(userId: Long, role: String) {
        val f: Fragment = if (role == "YARDIMCI") {
            HelperOpenListFragment.newInstance(helperId = userId)
        } else {
            PatientHelpFragment.newInstance(patientId = userId)
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.container, f)
            .commit()
    }
}