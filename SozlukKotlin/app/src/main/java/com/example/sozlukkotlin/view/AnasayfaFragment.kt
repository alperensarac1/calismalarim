package com.example.sozlukkotlin.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sozlukkotlin.R
import com.example.sozlukkotlin.adapter.EntryAdapter
import com.example.sozlukkotlin.databinding.FragmentAnasayfaBinding
import com.example.sozlukkotlin.model.Entry
import com.example.sozlukkotlin.viewmodel.AnaSayfaViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class AnasayfaFragment : Fragment() {
    private var _binding: FragmentAnasayfaBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerAdapter: EntryAdapter
    private lateinit var viewModel: AnaSayfaViewModel
    private var entryList: List<Entry> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnasayfaBinding.inflate(inflater, container, false)
        binding.fabEntryEkle.setOnClickListener {
            findNavController().navigate(R.id.action_anaSayfaFragment_to_entryEkleFragment)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this)[AnaSayfaViewModel::class.java]
        viewModel.loadMostCommentedEntriesToday()

        // RecyclerView ayarları
        binding.entryRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.entries.onEach { list ->
            recyclerAdapter = EntryAdapter(list , onClick = { entry ->
                val action = AnasayfaFragmentDirections.actionAnaSayfaFragmentToEntryDetayFragment(entry.id)
                findNavController().navigate(action)
            }, onLongClick = {} )
            binding.entryRecyclerView.adapter = recyclerAdapter
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.setSearchQuery(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchQuery(newText.orEmpty())
                return true
            }
        })


        binding.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_bugun -> {
                    findNavController().navigate(R.id.action_anaSayfaFragment_to_bugunFragment)
                    true
                }
                R.id.nav_profil -> {
                    findNavController().navigate(R.id.action_anaSayfaFragment_to_profilFragment)
                    true
                }
                else -> true // Gündem zaten burası
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
