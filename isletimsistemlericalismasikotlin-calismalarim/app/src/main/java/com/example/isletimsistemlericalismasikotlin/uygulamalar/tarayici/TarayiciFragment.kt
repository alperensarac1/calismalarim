package com.example.isletimsistemlericalismasikotlin.uygulamalar.tarayici

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.isletimsistemlericalismasikotlin.R
import com.example.isletimsistemlericalismasikotlin.databinding.FragmentTarayiciBinding


class TarayiciFragment : Fragment() {

    lateinit var binding : FragmentTarayiciBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentTarayiciBinding.inflate(inflater,container,false)
        binding.webView.settings.javaScriptEnabled = true

        binding.btnArama.setOnClickListener {
            var aramaKelimesi = binding.etArama.text.toString().replace(" ","+")
            binding.webView.loadUrl("https://www.google.com/search?q=$aramaKelimesi")
        }

        return binding.root
    }

}
