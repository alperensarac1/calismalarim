package com.example.isletimsistemlericalismasikotlin.uygulamalar.hesapmakinesi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.isletimsistemlericalismasikotlin.R
import com.example.isletimsistemlericalismasikotlin.databinding.FragmentHesapMakinesiBinding

class HesapMakinesiFragment : Fragment() {

    private lateinit var binding: FragmentHesapMakinesiBinding
    private var val1 = Double.NaN
    private var val2 = 0.0
    private var action: Char = ' '

    private val ADDITION = '+'
    private val SUBTRACTION = '-'
    private val MULTIPLICATION = '*'
    private val DIVISION = '/'
    private val EQU = '='

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHesapMakinesiBinding.inflate(inflater, container, false)

        val numberButtons = listOf(
            binding.btn0, binding.btn1, binding.btn2, binding.btn3, binding.btn4,
            binding.btn5, binding.btn6, binding.btn7, binding.btn8, binding.btn9
        )

        numberButtons.forEachIndexed { index, button ->
            button.setOnClickListener { binding.tvControl.append(index.toString()) }
        }

        binding.btnadd.setOnClickListener { operate(ADDITION) }
        binding.btnsub.setOnClickListener { operate(SUBTRACTION) }
        binding.btnmul.setOnClickListener { operate(MULTIPLICATION) }
        binding.btndivide.setOnClickListener { operate(DIVISION) }

        binding.btnequal.setOnClickListener {
            compute()
            action = EQU
            binding.tvResult.text = ""
            binding.tvControl.text = ""
        }

        binding.btnclear.setOnClickListener {
            if (binding.tvControl.text.isNotEmpty()) {
                binding.tvControl.text = binding.tvControl.text.dropLast(1)
            } else {
                val1 = Double.NaN
                val2 = Double.NaN
                binding.tvControl.text = ""
                binding.tvResult.text = ""
            }
        }

        return binding.root
    }

    private fun operate(op: Char) {
        compute()
        action = op
        binding.tvResult.text = "$val1 $op"
        binding.tvControl.text = ""
    }

    private fun compute() {
        if (!val1.isNaN()) {
            val2 = binding.tvControl.text.toString().toDoubleOrNull() ?: 0.0
            val1 = when (action) {
                ADDITION -> val1 + val2
                SUBTRACTION -> val1 - val2
                MULTIPLICATION -> val1 * val2
                DIVISION -> if (val2 != 0.0) val1 / val2 else Double.NaN
                else -> val1
            }
        } else {
            val1 = binding.tvControl.text.toString().toDoubleOrNull() ?: Double.NaN
        }
    }
}
