package com.example.haberuygulama.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.haberuygulama.R
import com.example.haberuygulama.model.HaberModel


class HaberlerRVAdapter(
    private val haberList: List<HaberModel>,
    private val onItemClick: (HaberModel) -> Unit
) : RecyclerView.Adapter<HaberlerRVAdapter.HaberViewHolder>() {

    inner class HaberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvHaber: TextView = itemView.findViewById(R.id.tvHaber)
        val tvDevaminiOku: TextView = itemView.findViewById(R.id.tvDevaminiOku)
        val imgHaber: ImageView = itemView.findViewById(R.id.imgHaber)
        val videoView: VideoView = itemView.findViewById(R.id.videoView)
        val btnPlay: ImageButton = itemView.findViewById(R.id.btnPlay)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(haberList[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HaberViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.haberlercell, parent, false)
        return HaberViewHolder(view)
    }

    override fun onBindViewHolder(holder: HaberViewHolder, position: Int) {
        val haber = haberList[position]
        holder.tvHaber.text = haber.baslik

        if (haber.media_type == "video") {
            holder.imgHaber.visibility = View.GONE
            holder.videoView.visibility = View.VISIBLE
            holder.btnPlay.visibility = View.VISIBLE

            val uri = Uri.parse(haber.media_url)
            holder.videoView.setVideoURI(uri)

            holder.btnPlay.setOnClickListener {
                holder.videoView.start()
                holder.btnPlay.visibility = View.GONE
            }

            holder.videoView.setOnCompletionListener {
                holder.btnPlay.visibility = View.VISIBLE
            }

        } else {
            holder.videoView.visibility = View.GONE
            holder.btnPlay.visibility = View.GONE
            holder.imgHaber.visibility = View.VISIBLE

            Glide.with(holder.itemView.context)
                .load(haber.media_url)
                .placeholder(R.drawable.resim)
                .into(holder.imgHaber)
        }

        holder.tvDevaminiOku.setOnClickListener {
            onItemClick.invoke(haber)
        }
    }

    override fun getItemCount(): Int = haberList.size
}
