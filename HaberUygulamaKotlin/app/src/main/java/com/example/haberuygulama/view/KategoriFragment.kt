package com.example.haberuygulama.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.haberuygulama.adapter.HaberlerRVAdapter
import com.example.haberuygulama.databinding.FragmentKategoriBinding
import com.example.haberuygulama.deo.HaberDao
import com.example.haberuygulama.servis.ApiClient
import com.example.haberuygulama.viewmodel.HaberlerViewModel
import com.example.haberuygulama.viewmodel.KategorilerViewModel


class KategoriFragment : Fragment() {

    lateinit var binding:FragmentKategoriBinding
    lateinit var adapter: HaberlerRVAdapter
    lateinit var viewModel: KategorilerViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentKategoriBinding.inflate(inflater,container,false)


        return binding.root
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = KategorilerViewModel(HaberDao(ApiClient.retrofit))

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bundle:KategoriFragmentArgs by navArgs()
        val gelen = bundle.kategoriId
        viewModel.loadKategoriHaberleri(gelen)

        observeKategoriHaber()
    }

    private fun observeKategoriHaber() {
        lifecycleScope.launchWhenStarted {
            viewModel.kategoriHaberleri.collect { liste ->
                if (liste.isNotEmpty()) {
                    adapter = HaberlerRVAdapter(liste) {
                        // tıklanınca yapılacak işlem
                        val gecis = KategoriFragmentDirections.kategoriToHaberDetay(it)
                        Navigation.findNavController(binding.rvKategoriHaber).navigate(gecis)
                    }
                    binding.rvKategoriHaber.layoutManager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                    binding.rvKategoriHaber.adapter = adapter
                }
            }
        }
    }

}
