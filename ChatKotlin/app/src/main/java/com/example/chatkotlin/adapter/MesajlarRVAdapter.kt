package com.example.chatkotlin.adapter

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatkotlin.R
import com.example.chatkotlin.model.Mesaj
import com.example.chatkotlin.util.AppConfig

class MesajlarRVAdapter(
    private val mesajList: List<Mesaj>,
    private val onItemClick: (Mesaj) -> Unit
) : RecyclerView.Adapter<MesajlarRVAdapter.MesajViewHolder>() {

    inner class MesajViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMesajMetni: TextView = itemView.findViewById(R.id.tvMesajMetni)
        val ivMesajResim: ImageView = itemView.findViewById(R.id.ivMesajResim)
        val tvTarih: TextView = itemView.findViewById(R.id.tvTarih)
        val llMesajBalonu:LinearLayout = itemView.findViewById(R.id.llMesajBalonu)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(mesajList[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MesajViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.mesaj_item, parent, false)

        return MesajViewHolder(view)
    }

    override fun onBindViewHolder(holder: MesajViewHolder, position: Int) {
        val mesaj = mesajList[position]

        val benimId = AppConfig.kullaniciId
        val benimMesajim = mesaj.gonderen_id == benimId


        val layoutParams = holder.llMesajBalonu.layoutParams as FrameLayout.LayoutParams
        holder.llMesajBalonu.layoutParams = layoutParams
        if (benimMesajim) {
            holder.llMesajBalonu.setBackgroundResource(R.drawable.bg_mavi_mesaj)
            layoutParams.gravity = Gravity.END
        } else {
            holder.llMesajBalonu.setBackgroundResource(R.drawable.bg_gri_mesaj)
            layoutParams.gravity = Gravity.START
        }
        holder.llMesajBalonu.layoutParams = layoutParams

        // Metin gösterimi
        if (!mesaj.mesaj_text.isNullOrEmpty()) {
            holder.tvMesajMetni.visibility = View.VISIBLE
            holder.tvMesajMetni.text = mesaj.mesaj_text
        } else {
            holder.tvMesajMetni.visibility = View.GONE
        }

        if (!mesaj.resim_url.isNullOrEmpty()) {
            holder.ivMesajResim.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(mesaj.resim_url)
                .into(holder.ivMesajResim)
        } else {
            holder.ivMesajResim.visibility = View.GONE
        }


        holder.tvTarih.text = mesaj.tarih
    }


    override fun getItemCount(): Int = mesajList.size
}
