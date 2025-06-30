package com.example.isletimsistemlericalismasikotlin.uygulamalar.notdefteri

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.example.isletimsistemlericalismasikotlin.R
import com.example.isletimsistemlericalismasikotlin.databinding.FragmentNotDefteriDetaySayfaBinding
import com.example.isletimsistemlericalismasikotlin.uygulamalar.notdefteri.model.Not
import com.example.isletimsistemlericalismasikotlin.uygulamalar.notdefteri.model.NoteDatabase
import java.util.Date
import java.util.Locale

class NotDefteriDetaysayfaFragment : Fragment() {

    private lateinit var binding: FragmentNotDefteriDetaySayfaBinding
    private lateinit var db: NoteDatabase
    private var gelenNot: Not? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotDefteriDetaySayfaBinding.inflate(inflater, container, false)
        db = NoteDatabase(requireContext())

        gelenNot = arguments?.getSerializable("not") as? Not

        gelenNot?.let {
            binding.inputTitle.setText(it.baslik)
            binding.inputNote.setText(it.notMetin)
        }

        binding.textDate.text = SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm", Locale.getDefault()).format(
            Date()
        )

        binding.imageSave.setOnClickListener {
            when {
                binding.inputTitle.text.toString().trim().isEmpty() ->
                    binding.inputTitle.error = "Başlık Giriniz"

                binding.inputNote.text.toString().trim().isEmpty() ->
                    binding.inputNote.error = "Not Giriniz"

                else -> {
                    db.yeniNot(
                        Not(
                            0,
                            binding.inputTitle.text.toString(),
                            binding.inputNote.text.toString(),
                            "null",
                            "null",
                            binding.textDate.text.toString(),
                            ""
                        )
                    )
                    Navigation.findNavController(it).popBackStack()
                }
            }
        }

        binding.imageDelete.setOnClickListener {
            gelenNot?.let { db.notSil(it) }
            Navigation.findNavController(it).popBackStack()
        }

        binding.imageBack.setOnClickListener {
            gelenNot?.let {
                db.notGuncelle(
                    it.id, Not(
                        0,
                        binding.inputTitle.text.toString(),
                        binding.inputNote.text.toString(),
                        "null",
                        "null",
                        binding.textDate.text.toString(),
                        ""
                    )
                )
            }
            Navigation.findNavController(it).popBackStack()
        }

        return binding.root
    }
}
