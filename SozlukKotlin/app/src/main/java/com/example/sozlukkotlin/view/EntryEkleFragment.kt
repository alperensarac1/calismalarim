package com.example.sozlukkotlin.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.sozlukkotlin.R
import com.example.sozlukkotlin.databinding.FragmentEntryEkleBinding
import com.example.sozlukkotlin.util.SessionManager
import com.example.sozlukkotlin.viewmodel.EntryEkleViewModel

class EntryEkleFragment : Fragment() {
    private var _binding: FragmentEntryEkleBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: EntryEkleViewModel
    private lateinit var session: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEntryEkleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        session = SessionManager(requireContext())
        viewModel = ViewModelProvider(this)[EntryEkleViewModel::class.java]

        binding.btnEntryKaydet.setOnClickListener {
            val title = binding.etEntryTitle.text.toString()
            val content = binding.etEntryContent.text.toString()
            val userId = session.getUserId()

            if (title.isBlank() || content.isBlank()) {
                Toast.makeText(requireContext(), "Tüm alanları doldurun", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.addEntry(userId, title, content)
        }

        viewModel.addResult.observe(viewLifecycleOwner) {
            if (it.success) {
                Toast.makeText(requireContext(), "Entry eklendi", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp() // Geri git
            } else {
                Toast.makeText(requireContext(), it.message ?: "Hata oluştu", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
