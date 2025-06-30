package com.example.isletimsistemlericalismasikotlin.uygulamalar.takvim

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.isletimsistemlericalismasikotlin.R
import com.example.isletimsistemlericalismasikotlin.databinding.FragmentTakvimBinding
import java.util.Date
import java.util.Locale

class TakvimFragment : Fragment() {

    private var _binding: FragmentTakvimBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTakvimBinding.inflate(inflater, container, false)

        binding.tvTarih.text = getCurrentDateTime()

        binding.calendar.date = System.currentTimeMillis()
        binding.calendar.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val date = "$dayOfMonth-${month + 1}-$year"
            binding.dateView.text = date
        }

        return binding.root
    }

    private fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
