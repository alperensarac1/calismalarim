package com.example.sozlukkotlin.view

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.sozlukkotlin.R
import com.example.sozlukkotlin.adapter.EntryAdapter
import com.example.sozlukkotlin.databinding.FragmentProfilBinding
import com.example.sozlukkotlin.model.Entry
import com.example.sozlukkotlin.util.SessionManager
import com.example.sozlukkotlin.viewmodel.ProfilViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ProfilFragment : Fragment() {
    private var _binding: FragmentProfilBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProfilViewModel
    private lateinit var session: SessionManager
    private lateinit var recyclerAdapter: EntryAdapter
    private var entryList: List<Entry> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfilBinding.inflate(inflater, container, false)
        binding.btnCikisYap.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Çıkış Yap")
                .setMessage("Oturumunuzu kapatmak istediğinizden emin misiniz?")
                .setPositiveButton("Evet") { _, _ ->
                    session.clearSession()
                    findNavController().navigate(R.id.action_profilFragment_to_girisFragment)
                }
                .setNegativeButton("İptal", null)
                .show()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        session = SessionManager(requireContext())
        viewModel = ViewModelProvider(this)[ProfilViewModel::class.java]

        val userId = session.getUserId()
        val username = session.getUsername()

        binding.tvKullaniciAdi.text = username ?: "Bilinmeyen Kullanıcı"

        viewModel.loadUserEntries(userId)
        binding.bottomNavBugun.selectedItemId = R.id.nav_profil
        binding.bottomNavBugun.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_gundem -> { findNavController().navigate(R.id.anaSayfaFragment); true }
                R.id.nav_bugun ->{ findNavController().navigate(R.id.bugunFragment); true }
                R.id.nav_profil -> true

                else -> false
            }
        }

        recyclerAdapter = EntryAdapter(entryList,
            onClick = { entry ->
                val action = ProfilFragmentDirections.actionProfilFragmentToEntryDetayFragment(entry.id)
                findNavController().navigate(action)
            },
            onLongClick = { entry ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Entry Sil")
                    .setMessage("Bu entry'i silmek istediğinizden emin misiniz?")
                    .setPositiveButton("Evet") { _, _ ->
                        viewModel.deleteEntry(entry.id, userId)
                    }
                    .setNegativeButton("İptal", null)
                    .show()
            }
        )
        binding.profilRecyclerView.adapter = recyclerAdapter
        viewModel.deleteResult.observe(viewLifecycleOwner) {
            if (it.success) {
                Toast.makeText(requireContext(), "Silindi", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), it.message ?: "Silinemedi", Toast.LENGTH_SHORT).show()
            }
        }
        binding.searchViewProfil.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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
