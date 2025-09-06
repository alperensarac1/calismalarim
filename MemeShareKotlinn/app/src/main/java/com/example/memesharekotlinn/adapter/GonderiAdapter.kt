package com.example.memesharekotlinn.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.VideoView
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.memesharekotlinn.R
import com.example.memesharekotlinn.model.GonderiModel

class GonderiAdapter(
    private val context: Context,
    private val gonderiList: MutableList<GonderiModel>,
    private val currentUserId: Int
) : RecyclerView.Adapter<GonderiAdapter.GonderiViewHolder>() {

    companion object {
        private const val VIEW_TYPE_LEFT = 0
        private const val VIEW_TYPE_RIGHT = 1
        private const val BASE = "https://alperensaracdeneme.com/meme/"
    }

    override fun getItemViewType(position: Int): Int {
        val item = gonderiList[position]
        return if (item.userId == currentUserId) VIEW_TYPE_RIGHT else VIEW_TYPE_LEFT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GonderiViewHolder {
        @LayoutRes val layoutRes = if (viewType == VIEW_TYPE_RIGHT) {
            R.layout.meme_item_right
        } else {
            R.layout.meme_item_left
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return GonderiViewHolder(view)
    }

    override fun onBindViewHolder(holder: GonderiViewHolder, position: Int) {
        val item = gonderiList[position]

        holder.tvGonderenAdi.text = "Kullanıcı #${item.userId}"
        holder.tvGonderiTarih.text = item.uploadedAt

        val fullUrl = BASE + item.mediaUrl

        when (item.mediaType) {
            "image" -> {
                holder.imgPost.visibility = View.VISIBLE
                holder.videoPost.visibility = View.GONE
                holder.btnOynat.visibility = View.GONE

                Glide.with(holder.itemView)
                    .load(fullUrl)
                    .into(holder.imgPost)
            }

            "video" -> {
                // Thumbnail + play butonu
                holder.imgPost.visibility = View.VISIBLE
                holder.videoPost.visibility = View.GONE
                holder.btnOynat.visibility = View.VISIBLE

                // Video’dan kare almak için (Glide’ın video frame desteği)
                Glide.with(holder.itemView)
                    .load(fullUrl)
                    .frame(1_000_000) // ~1. saniye
                    .into(holder.imgPost)

                holder.btnOynat.setOnClickListener {
                    holder.imgPost.visibility = View.GONE
                    holder.videoPost.visibility = View.VISIBLE
                    holder.btnOynat.visibility = View.GONE

                    holder.videoPost.setVideoURI(Uri.parse(fullUrl))
                    holder.videoPost.start()
                }

                holder.videoPost.setOnCompletionListener {
                    holder.videoPost.visibility = View.GONE
                    holder.imgPost.visibility = View.VISIBLE
                    holder.btnOynat.visibility = View.VISIBLE
                }
            }

            else -> {
                // Bilinmeyen tür için hepsini kapat
                holder.imgPost.visibility = View.GONE
                holder.videoPost.visibility = View.GONE
                holder.btnOynat.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int = gonderiList.size

    override fun onViewRecycled(holder: GonderiViewHolder) {
        super.onViewRecycled(holder)
        // VideoView temizliği: scroll/recycle sırasında leak ve ses karışmasını önler
        holder.videoPost.stopPlayback()
        holder.videoPost.setOnCompletionListener(null)
        holder.btnOynat.setOnClickListener(null)

        // Glide temizliği (isteğe bağlı)
        Glide.with(holder.itemView.context).clear(holder.imgPost)
    }

    fun submitList(newList: List<GonderiModel>) {
        gonderiList.clear()
        gonderiList.addAll(newList)
        notifyDataSetChanged()
        // İstersen DiffUtil ile geliştirebiliriz.
    }

    class GonderiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgPost: ImageView = itemView.findViewById(R.id.imgPost)
        val videoPost: VideoView = itemView.findViewById(R.id.videoPost)
        val btnOynat: Button = itemView.findViewById(R.id.btnOynat)
        val tvGonderenAdi: TextView = itemView.findViewById(R.id.tvGonderenAdi)
        val tvGonderiTarih: TextView = itemView.findViewById(R.id.tvGonderiTarih)
    }
}
