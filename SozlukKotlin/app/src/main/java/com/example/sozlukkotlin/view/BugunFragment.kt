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
import com.example.sozlukkotlin.databinding.FragmentBugunBinding
import com.example.sozlukkotlin.model.Entry
import com.example.sozlukkotlin.viewmodel.BugunViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class BugunFragment : Fragment() {
    private var _binding: FragmentBugunBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: BugunViewModel
    private lateinit var recyclerAdapter: EntryAdapter
    private var entryList: List<Entry> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBugunBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this)[BugunViewModel::class.java]

        binding.bugunRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.loadTodayEntries()
        binding.bottomNavBugun.selectedItemId = R.id.nav_bugun
        binding.bottomNavBugun.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_gundem -> { findNavController().navigate(R.id.anaSayfaFragment); true }
                R.id.nav_bugun -> true // zaten burası
                R.id.nav_profil -> { findNavController().navigate(R.id.profilFragment); true }
                else -> false
            }
        }
        viewModel.entries.onEach { list ->
            entryList = list
            recyclerAdapter = EntryAdapter(entryList, onClick =  { entry ->
                val action = BugunFragmentDirections.actionBugunFragmentToEntryDetayFragment(entry.id)
                findNavController().navigate(action)
            }, onLongClick = {})
            binding.bugunRecyclerView.adapter = recyclerAdapter
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        binding.searchViewBugun.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.setSearchQuery(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchQuery(newText.orEmpty())
                return true
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
