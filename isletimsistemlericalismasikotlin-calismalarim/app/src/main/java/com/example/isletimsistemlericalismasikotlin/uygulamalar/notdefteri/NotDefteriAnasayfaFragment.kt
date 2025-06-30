package com.example.isletimsistemlericalismasikotlin.uygulamalar.notdefteri

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.navigation.Navigation
import com.example.isletimsistemlericalismasikotlin.R
import com.example.isletimsistemlericalismasikotlin.databinding.FragmentNotDefteriAnasayfaBinding
import com.example.isletimsistemlericalismasikotlin.uygulamalar.notdefteri.adapter.NoteAdapter
import com.example.isletimsistemlericalismasikotlin.uygulamalar.notdefteri.model.Not
import com.example.isletimsistemlericalismasikotlin.uygulamalar.notdefteri.model.NoteDatabase

class NotDefteriAnasayfaFragment : Fragment() {

    private lateinit var binding: FragmentNotDefteriAnasayfaBinding
    private lateinit var db: NoteDatabase
    private lateinit var adapter: NoteAdapter
    private var notListesi = ArrayList<Not>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = NoteDatabase(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotDefteriAnasayfaBinding.inflate(inflater, container, false)

        notListesi = db.getNotlarim()
        adapter = NoteAdapter(notListesi)

        binding.recyclerviewNotlar.apply {
            adapter = this@NotDefteriAnasayfaFragment.adapter
            setHasFixedSize(true)
        }

        binding.inputSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    notListesi.clear()
                    notListesi.addAll(db.notAra(it))
                    adapter.notifyDataSetChanged()
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    notListesi.clear()
                    notListesi.addAll(db.notAra(it))
                    adapter.notifyDataSetChanged()
                }
                return false
            }
        })

        binding.imageAddNote.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.toNotDefteriDetaySayfa)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }
}
