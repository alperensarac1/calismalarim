package com.example.memesharekotlinn.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.memesharekotlinn.R
import com.example.memesharekotlinn.model.OdaModel

class OdaAdapter(
    private val odaListesi: MutableList<OdaModel>,
    private val listener: OnOdaClickListener?
) : RecyclerView.Adapter<OdaAdapter.OdaViewHolder>() {

    interface OnOdaClickListener {
        fun onOdaClick(oda: OdaModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OdaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.oda_item, parent, false)
        return OdaViewHolder(view)
    }

    override fun onBindViewHolder(holder: OdaViewHolder, position: Int) {
        val oda = odaListesi[position]
        holder.tvOdaIsmi.text = oda.roomCode
        holder.tvSonMesajTarihi.text = " ${oda.createdBy}"

        holder.odaCard.setOnClickListener {
            listener?.onOdaClick(oda)
        }
    }

    override fun getItemCount(): Int = odaListesi.size

    fun submitList(newList: List<OdaModel>) {
        odaListesi.clear()
        odaListesi.addAll(newList)
        notifyDataSetChanged()
    }

    class OdaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvOdaIsmi: TextView = itemView.findViewById(R.id.tvOdaIsmi)
        val tvSonMesajTarihi: TextView = itemView.findViewById(R.id.tvSonMesajTarihi)
        val odaCard: CardView = itemView.findViewById(R.id.cardView)
    }
}