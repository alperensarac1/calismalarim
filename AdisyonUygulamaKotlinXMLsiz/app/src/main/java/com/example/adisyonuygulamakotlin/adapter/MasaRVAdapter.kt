package com.example.adisyonuygulamakotlin.adapter

import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.adisyonuygulamakotlin.model.Masa
import com.example.adisyonuygulamakotlin.view.anaekran.MasaCard

class MasaRVAdapter(
    private val context: Context,
    private val masaList: List<Masa>,
    private val onMasaClick: (Masa) -> Unit
) : RecyclerView.Adapter<MasaRVAdapter.MasaViewHolder>() {

    inner class MasaViewHolder(val masaCard: MasaCard) : RecyclerView.ViewHolder(masaCard.cardView())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MasaViewHolder {
        return MasaViewHolder(
            MasaCard(context, onClick = onMasaClick) // ✅ callback'i ilet
        )
    }

    override fun onBindViewHolder(holder: MasaViewHolder, position: Int) {
        val masa = masaList[position]
        holder.masaCard.setData(masa)

        if (masa.acikMi == 1) {
            holder.masaCard.setBackgroundColor(Color.CYAN)
        } else {
            holder.masaCard.setBackgroundColor(Color.GRAY)
        }
    }

    override fun getItemCount(): Int = masaList.size
}
