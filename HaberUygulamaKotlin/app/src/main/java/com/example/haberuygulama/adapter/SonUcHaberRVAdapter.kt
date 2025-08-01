package com.example.haberuygulama.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.VideoView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.haberuygulama.R
import com.example.haberuygulama.model.HaberModel

class SonUcHaberRVAdapter(
    private val haberList: List<HaberModel>,
    private val onItemClick: (HaberModel) -> Unit
) : RecyclerView.Adapter<SonUcHaberRVAdapter.HaberViewHolder>() {

    inner class HaberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvHaberBaslik: TextView = itemView.findViewById(R.id.tvHaberBaslik)
        val haberCard: CardView = itemView.findViewById(R.id.sonHaberCard)
        val imgHaber: ImageView = itemView.findViewById(R.id.imgHaber)
        val videoView: VideoView = itemView.findViewById(R.id.videoHaber)
        val btnPlay: ImageButton = itemView.findViewById(R.id.btnPlay)

        init {
            haberCard.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(haberList[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HaberViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.sonuchabercell, parent, false)
        return HaberViewHolder(view)
    }

    override fun onBindViewHolder(holder: HaberViewHolder, position: Int) {
        val haber = haberList[position]
        holder.tvHaberBaslik.text = haber.baslik

        if (haber.media_type == "video") {
            holder.imgHaber.visibility = View.GONE
            holder.videoView.visibility = View.VISIBLE
            holder.btnPlay.visibility = View.VISIBLE

            val videoUri = Uri.parse(haber.media_url)
            holder.videoView.setVideoURI(videoUri)

            holder.btnPlay.setOnClickListener {
                holder.videoView.start()
                holder.btnPlay.visibility = View.GONE
            }

            holder.videoView.setOnCompletionListener {
                holder.btnPlay.visibility = View.VISIBLE
            }

        } else {
            holder.imgHaber.visibility = View.VISIBLE
            holder.videoView.visibility = View.GONE
            holder.btnPlay.visibility = View.GONE

            Glide.with(holder.itemView.context)
                .load(haber.media_url)
                .placeholder(R.drawable.resim)
                .into(holder.imgHaber)
        }
    }

    override fun getItemCount(): Int = haberList.size
}
