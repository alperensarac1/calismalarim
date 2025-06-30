package com.example.isletimsistemlericalismasikotlin.uygulamalar.notdefteri.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.isletimsistemlericalismasikotlin.R
import com.example.isletimsistemlericalismasikotlin.databinding.RowNoteBinding
import com.example.isletimsistemlericalismasikotlin.uygulamalar.notdefteri.NotDefteriDetaysayfaFragment
import com.example.isletimsistemlericalismasikotlin.uygulamalar.notdefteri.model.Not

class NoteAdapter(private val notListesi: ArrayList<Not>) :
    RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = RowNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val not = notListesi[position]

        holder.binding.apply {
            textTitle.text = not.baslik
            textNote.text = not.notMetin
            textDate.text = not.tarih
            cardViewContainer.setOnClickListener { view ->
                val notDetayFragment = NotDefteriDetaysayfaFragment().apply {
                    arguments = Bundle().apply {
                        putSerializable("not", not)
                    }
                }
                Navigation.findNavController(view).navigate(R.id.toNotDefteriDetaySayfa, notDetayFragment.arguments)
            }
        }
    }

    override fun getItemCount(): Int = notListesi.size

    class NoteViewHolder(val binding: RowNoteBinding) : RecyclerView.ViewHolder(binding.root)
}
