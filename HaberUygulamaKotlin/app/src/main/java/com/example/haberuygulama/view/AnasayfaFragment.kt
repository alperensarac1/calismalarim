package com.example.haberuygulama.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.haberuygulama.R
import com.example.haberuygulama.adapter.HaberlerRVAdapter
import com.example.haberuygulama.databinding.FragmentAnasayfaBinding
import com.example.haberuygulama.deo.HaberDao
import com.example.haberuygulama.model.HaberModel
import com.example.haberuygulama.model.HaberTuruModel
import com.example.haberuygulama.servis.ApiClient
import com.example.haberuygulama.viewmodel.HaberlerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AnasayfaFragment : Fragment() {

    private lateinit var binding: FragmentAnasayfaBinding
    private lateinit var viewModel: HaberlerViewModel
    private lateinit var sonDakikaAdapter: HaberlerRVAdapter
    private lateinit var gundemAdapter: HaberlerRVAdapter
    private lateinit var kategorilerAdapter: ArrayAdapter<HaberTuruModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = HaberlerViewModel(HaberDao(ApiClient.retrofit))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAnasayfaBinding.inflate(inflater, container, false)


        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadSonDakikaHaberler()
        viewModel.loadGundemHaberler()
        viewModel.loadKategoriler()

        observeSonDakika()
        observeGundemHaberler()
        observeKategoriler()
    }

    private fun observeSonDakika() {
        lifecycleScope.launchWhenStarted {
            viewModel.sonDakikaHaberler.collect { liste ->
                if (liste.isNotEmpty()) {
                    sonDakikaAdapter = HaberlerRVAdapter(liste) {

                        val gecis = AnasayfaFragmentDirections.anasayfaToHaberDetay(it)
                        Navigation.findNavController(requireView()).navigate(gecis)
                    }
                    binding.rvSonDakika.layoutManager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                    binding.rvSonDakika.adapter = sonDakikaAdapter
                }
            }
        }
    }

    private fun observeGundemHaberler() {
        lifecycleScope.launchWhenStarted {
            viewModel.gundemHaberler.collect { liste ->
                if (liste.isNotEmpty()) {
                    gundemAdapter = HaberlerRVAdapter(liste) {
                        val gecis = AnasayfaFragmentDirections.anasayfaToHaberDetay(it)
                        Navigation.findNavController(requireView()).navigate(gecis)
                    }
                    binding.rvGundem.layoutManager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                    binding.rvGundem.adapter = gundemAdapter
                }
            }
        }
    }

    private fun observeKategoriler() {
        lifecycleScope.launchWhenStarted {
            viewModel.kategoriler.collect { liste ->
                if (liste.isNotEmpty()) {
                    kategorilerAdapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_list_item_1,
                        liste
                    )
                    binding.lvKategoriler.adapter = kategorilerAdapter

                    binding.lvKategoriler.setOnItemClickListener { v, _, position, _ ->
                        val secilenKategori = liste[position]
                        viewModel.filtreleKategori(secilenKategori.id)
                        // örnek: filtrelenmiş haberleri başka sayfada göstermek istersen yönlendirebilirsin
                        println("Seçilen kategori: ${secilenKategori.tur_adi}")
                        val gecis = AnasayfaFragmentDirections.actionAnasayfaFragmentToKategoriFragment(secilenKategori.id)
                        Navigation.findNavController(v).navigate(gecis)
                    }
                }
            }
        }
    }
}
