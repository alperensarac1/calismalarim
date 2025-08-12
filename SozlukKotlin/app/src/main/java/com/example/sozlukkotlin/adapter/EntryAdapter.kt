package com.example.sozlukkotlin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sozlukkotlin.R
import com.example.sozlukkotlin.model.Entry

class EntryAdapter(
    private val entryList: List<Entry>,
    private val onClick: (Entry) -> Unit,
    private val onLongClick: (Entry) -> Unit
) : RecyclerView.Adapter<EntryAdapter.EntryViewHolder>() {

    class EntryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvBaslik: TextView = itemView.findViewById(R.id.tvBaslik)
        val tvIcerik: TextView = itemView.findViewById(R.id.tvIcerik)
        val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        val tvTarih: TextView = itemView.findViewById(R.id.tvTarih)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.entry_card, parent, false)
        return EntryViewHolder(view)
    }

    override fun getItemCount(): Int = entryList.size

    override fun onBindViewHolder(holder: EntryViewHolder, position: Int) {
        val entry = entryList[position]
        holder.tvBaslik.text = entry.title
        holder.tvIcerik.text = entry.content
        holder.tvUsername.text = entry.username

        // Tarihi formatla (örnek: "2025-08-07 13:45:00")
        holder.tvTarih.text = entry.created_at.substring(0, 10)

        holder.itemView.setOnClickListener {
            onClick(entry)
        }
        holder.itemView.setOnLongClickListener {
            onLongClick(entry)
            true
        }
    }

}
