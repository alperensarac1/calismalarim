package com.example.haberuygulama.view

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.haberuygulama.R
import com.example.haberuygulama.adapter.HaberlerRVAdapter
import com.example.haberuygulama.adapter.SonUcHaberRVAdapter
import com.example.haberuygulama.databinding.FragmentHaberDetayBinding
import com.example.haberuygulama.deo.HaberDao
import com.example.haberuygulama.model.YorumModel
import com.example.haberuygulama.servis.ApiClient
import com.example.haberuygulama.viewmodel.HaberDetayViewModel
import com.example.haberuygulama.viewmodel.HaberlerViewModel
import com.example.haberuygulama.viewmodel.KategorilerViewModel
import retrofit2.Retrofit


class HaberDetayFragment : Fragment() {

    private lateinit var binding: FragmentHaberDetayBinding
    private lateinit var adapter: SonUcHaberRVAdapter
    private lateinit var viewModel: HaberlerViewModel
    private lateinit var haberDetayVM: HaberDetayViewModel
    private var haberId = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentHaberDetayBinding.inflate(inflater, container, false)
        val bundle: HaberDetayFragmentArgs by navArgs()
        val haber = bundle.haber

        haberId = haber.id
        // Başlığı ayarlayalım
        binding.tvHaberBaslik.text = haber.baslik

        if (haber.media_type == "video") {
            // Video göster
            binding.imgHaber.visibility = View.GONE
            binding.videoView.visibility = View.VISIBLE
            binding.btnPlay.visibility = View.VISIBLE

            binding.videoView.setVideoURI(Uri.parse(haber.media_url))

            binding.btnPlay.setOnClickListener {
                binding.videoView.start()
                binding.btnPlay.visibility = View.GONE
            }

            binding.videoView.setOnCompletionListener {
                binding.btnPlay.visibility = View.VISIBLE
            }

        } else {
            // Resim göster
            binding.imgHaber.visibility = View.VISIBLE
            binding.videoView.visibility = View.GONE
            binding.btnPlay.visibility = View.GONE

            Glide.with(requireContext())
                .load(haber.media_url)
                .placeholder(R.drawable.resim)
                .into(binding.imgHaber)
        }
        binding.tvAdSoyadDepartman.text = haber.ad + " " + haber.soyad + " " + haber.unvan
        binding.tvTarih.text = haber.yayinlanma_tarihi

        binding.tvHaberIcerik.text = haber.icerik
        binding.btnYorumGonder.setOnClickListener {
            if (binding.etRumuz.text.toString().isNotEmpty() && binding.etYorum.text.toString().isNotEmpty()){
                haberDetayVM.yorumEkle(haberId,binding.etRumuz.text.toString(),binding.etYorum.text.toString())
                binding.etRumuz.text.clear()
                binding.etYorum.text.clear()
            }else{
                Toast.makeText(requireContext(),"İlgili alanlar boş bırakılamaz",Toast.LENGTH_SHORT).show()
            }
        }


        return binding.root
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val retrofit = ApiClient.retrofit
        val haberDao = HaberDao(retrofit)
        viewModel = HaberlerViewModel(haberDao)
        haberDetayVM = HaberDetayViewModel(haberDao)

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadSon3Haber()
        haberDetayVM.loadYorumlar(haberId = haberId)
        observeSonUcHaber()
        observeYorumlar()
    }
    private fun observeSonUcHaber() {
        lifecycleScope.launchWhenStarted {
            viewModel.sonHaberler.collect { liste ->
                if (liste.isNotEmpty()) {
                    adapter = SonUcHaberRVAdapter(liste){
                        val gecis = HaberDetayFragmentDirections.haberDetayToHaberDetay(it)
                        Navigation.findNavController(requireView()).navigate(gecis)
                    }
                    binding.rvSonUcHaber.layoutManager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                    binding.rvSonUcHaber.adapter = adapter
                }
            }
        }
    }
    private fun observeYorumlar(){
        lifecycleScope.launchWhenStarted {
            haberDetayVM.yorumlar.collect{yorumlar->
                if (yorumlar.isNotEmpty()){
                    val yorumIcerik = ArrayList<String>()
                    yorumlar.forEach {
                        yorumIcerik.add(it.yorum_metni  + " " + "\n" + it.takma_ad)
                    }
                    val adapter = ArrayAdapter<String>(requireContext(),android.R.layout.simple_list_item_1,yorumIcerik)
                    binding.yorumlarListView.adapter = adapter
                }
            }
        }
    }

}
