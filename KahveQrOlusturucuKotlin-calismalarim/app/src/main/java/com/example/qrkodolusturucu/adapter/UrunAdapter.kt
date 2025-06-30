package com.example.qrkodolusturucu.adapter

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.marginLeft
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.qrkodolusturucu.R
import com.example.qrkodolusturucu.databinding.UrunItemBinding
import com.example.qrkodolusturucu.model.Urun
import com.example.qrkodolusturucu.usecases.indirimYuzdeHesapla
import com.example.qrkodolusturucu.view.UrunDetayFragment
import com.example.qrkodolusturucu.view.UrunDetayFragmentArgs
import com.example.qrkodolusturucu.view.UrunlerimizFragmentDirections
import com.squareup.picasso.Picasso


class UrunAdapter(private val urunListesi: List<Urun>) :
    RecyclerView.Adapter<UrunAdapter.UrunViewHolder>() {

    inner class UrunViewHolder(private val binding: UrunItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(urun: Urun) {
            binding.tvUrunAdi.text = urun.urunAd
            binding.tvUrunFiyat.text = "${urun.urunFiyat} TL"

            Picasso.get()
                .load(urun.urunResim)
                .placeholder(R.drawable.kahve) // Yüklenirken gösterilecek resim
                .error(R.drawable.kahve) // Hata olursa gösterilecek resim
                .into(binding.imgUrun)
            binding.tvIndRMsizFiyat.visibility = View.INVISIBLE

            if (urun.urunIndirim == 1){
                binding.tvIndRMsizFiyat.setText(urun.urunFiyat.toInt().toString() + " TL")
                binding.tvUrunFiyat.setText(urun.urunIndirimliFiyat.toInt().toString() + "TL")
                binding.tvIndRMsizFiyat.paintFlags = binding.tvIndRMsizFiyat.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.tvIndRMsizFiyat.visibility = View.VISIBLE
                binding.tvIndRMsizFiyat.textSize = 10f
                binding.tvUrunFiyat.setTextColor(Color.RED)
                binding.tvIndirimYuzdesi.visibility = View.VISIBLE
                binding.tvIndirimYuzdesi.setText("%-" + indirimYuzdeHesapla(urun.urunFiyat,urun.urunIndirimliFiyat))
            }

            binding.cardUrun.setOnClickListener {


                val action = UrunlerimizFragmentDirections.toUrunDetay(urun)
                Navigation.findNavController(it).navigate(action)

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UrunViewHolder {
        val binding = UrunItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UrunViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UrunViewHolder, position: Int) {
        holder.bind(urunListesi[position])
    }

    override fun getItemCount(): Int = urunListesi.size
}
