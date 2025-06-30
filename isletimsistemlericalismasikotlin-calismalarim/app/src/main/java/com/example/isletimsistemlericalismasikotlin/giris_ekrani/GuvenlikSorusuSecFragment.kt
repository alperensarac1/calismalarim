package com.example.isletimsistemlericalismasikotlin.giris_ekrani

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.isletimsistemlericalismasikotlin.R
import com.example.isletimsistemlericalismasikotlin.databinding.FragmentGuvenlikSorusuSecBinding

import androidx.navigation.Navigation;


import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import com.example.isletimsistemlericalismasikotlin.giris_ekrani.repository.KullaniciBilgileri


class GuvenlikSorusuSecFragment : Fragment() {

    private lateinit var binding: FragmentGuvenlikSorusuSecBinding
    private lateinit var kullaniciBilgileri: KullaniciBilgileri

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentGuvenlikSorusuSecBinding.inflate(inflater, container, false)
        kullaniciBilgileri = KullaniciBilgileri(requireContext())

        setupRadioButton(
            binding.radioButtonEnSevilenRenk,
            binding.etEnsevilenRenk,
            binding.tvEnsevilenRenk,
            binding.btnEnSevilenRenk,
            binding.radioButtonMuzikGrubu,
            binding.radioButtonSevilenHayvan
        )

        setupRadioButton(
            binding.radioButtonSevilenHayvan,
            binding.etEnSevilenHayvan,
            binding.tvEnSevilenHayvan,
            binding.buttonEnSecilenHayvan,
            binding.radioButtonMuzikGrubu,
            binding.radioButtonEnSevilenRenk
        )

        setupRadioButton(
            binding.radioButtonMuzikGrubu,
            binding.etEnSevilenMuzikGrubu,
            binding.tvEnSevilenMuzikGrubu,
            binding.buttonEnSevilenMuzikGrubu,
            binding.radioButtonEnSevilenRenk,
            binding.radioButtonSevilenHayvan
        )

        binding.buttonEnSecilenHayvan.setOnClickListener {
            saveSecurityQuestion(binding.tvEnSevilenHayvan, binding.etEnSevilenHayvan)
            navigateToNext(it)
        }

        binding.buttonEnSevilenMuzikGrubu.setOnClickListener {
            saveSecurityQuestion(binding.tvEnSevilenMuzikGrubu, binding.etEnSevilenMuzikGrubu)
            navigateToNext(it)
        }

        binding.btnEnSevilenRenk.setOnClickListener {
            saveSecurityQuestion(binding.tvEnsevilenRenk, binding.etEnsevilenRenk)
            navigateToNext(it)
        }

        return binding.root
    }

    private fun setupRadioButton(
        radioButton: RadioButton,
        editText: EditText,
        textView: TextView,
        button: View,
        otherRadio1: RadioButton,
        otherRadio2: RadioButton
    ) {
        radioButton.setOnClickListener {
            if (radioButton.isChecked) {
                otherRadio1.isChecked = false
                otherRadio2.isChecked = false
                toggleVisibility(true, editText, button)
            }
        }

        radioButton.setOnCheckedChangeListener { _, isChecked ->
            toggleVisibility(isChecked, editText, button)
        }
    }

    private fun toggleVisibility(isVisible: Boolean, editText: EditText, button: View) {
        val visibility = if (isVisible) View.VISIBLE else View.GONE
        editText.visibility = visibility
        button.visibility = visibility
    }

    private fun saveSecurityQuestion(textView: TextView, editText: EditText) {
        if (editText.text.toString().isNotEmpty()) {
            kullaniciBilgileri.guvenlikSorusuKaydet(textView.text.toString())
            kullaniciBilgileri.guvenlikSorusuCevapKaydet(editText.text.toString())
        }
    }

    private fun navigateToNext(view: View) {
        Navigation.findNavController(view).navigate(R.id.girisYapFragment)
    }
}
