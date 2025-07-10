package com.example.chatkotlin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatkotlin.R
import com.example.chatkotlin.model.KonusulanKisi

class KonusulanKisilerAdapter(
    private val kisiListesi: List<KonusulanKisi>,
    private val onItemClick: (kisi: KonusulanKisi) -> Unit
) : RecyclerView.Adapter<KonusulanKisilerAdapter.KisiViewHolder>() {

    inner class KisiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvKullaniciAdi: TextView = itemView.findViewById(R.id.tvKullaniciAdi)
        val tvMesajOzet: TextView = itemView.findViewById(R.id.tvMesajOzet)
        val tvTarih: TextView = itemView.findViewById(R.id.tvTarih)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(kisiListesi[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KisiViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.mesajlar_item, parent, false)
        return KisiViewHolder(view)
    }

    override fun onBindViewHolder(holder: KisiViewHolder, position: Int) {
        val kisi = kisiListesi[position]
        holder.tvKullaniciAdi.text = kisi.ad
        holder.tvMesajOzet.text = kisi.son_mesaj.take(30) + if (kisi.son_mesaj.length > 30) "..." else ""
        holder.tvTarih.text = kisi.tarih
    }

    override fun getItemCount(): Int = kisiListesi.size
}



