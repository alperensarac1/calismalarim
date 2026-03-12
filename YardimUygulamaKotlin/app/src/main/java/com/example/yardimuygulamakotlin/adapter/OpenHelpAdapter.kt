package com.example.yardimuygulamakotlin.adapter

// OpenHelpAdapter.kt
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.yardimuygulamakotlin.R
import com.example.yardimuygulamakotlin.model.OpenHelpItem

class OpenHelpAdapter(
    private var items: List<OpenHelpItem>,
    private val onAccept: (OpenHelpItem) -> Unit
) : RecyclerView.Adapter<OpenHelpAdapter.VH>() {

    fun submit(newItems: List<OpenHelpItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvName: TextView = v.findViewById(R.id.tvName)
        val tvAge: TextView = v.findViewById(R.id.tvAge)
        val tvCreated: TextView = v.findViewById(R.id.tvCreated)
        val btnAccept: Button = v.findViewById(R.id.btnAccept)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_open_help, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.tvName.text = item.patient_name
        holder.tvAge.text = "Yaş: " + (item.patient_age?.toString() ?: "-")
        holder.tvCreated.text = "İstek: ${item.created_at}"
        holder.btnAccept.setOnClickListener { onAccept(item) }
    }

    override fun getItemCount(): Int = items.size
}