package com.example.qrkodolusturucu.view

import android.graphics.Paint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.example.qrkodolusturucu.R
import com.example.qrkodolusturucu.databinding.FragmentUrunDetayBinding
import com.squareup.picasso.Picasso


class UrunDetayFragment : Fragment() {

    lateinit var binding:FragmentUrunDetayBinding
    val bundle:UrunDetayFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentUrunDetayBinding.inflate(inflater,container,false)
        val gelenUrun = bundle.urun

        Picasso.get()
            .load(gelenUrun.urunResim)
            .placeholder(R.drawable.kahve) // Yüklenirken gösterilecek resim
            .error(R.drawable.kahve) // Hata olursa gösterilecek resim
            .into(binding.imgUrun)

        binding.tvUrunAdi.text = gelenUrun.urunAd
        binding.tvUrunAciklama.text = gelenUrun.urunAciklama
        binding.tvUrunFiyat.text = "${gelenUrun.urunFiyat.toInt()} TL"
        if (gelenUrun.urunIndirim == 1){
            binding.tvUrunFiyat.paintFlags = binding.tvUrunFiyat.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            binding.tvIndirimliFiyat.text = "${gelenUrun.urunIndirimliFiyat.toInt()} TL"
            binding.tvIndirimliFiyat.visibility = View.VISIBLE
        }

        binding.imgGeri.setOnClickListener {
            Navigation.findNavController(it).popBackStack()
        }

        return binding.root
    }

}