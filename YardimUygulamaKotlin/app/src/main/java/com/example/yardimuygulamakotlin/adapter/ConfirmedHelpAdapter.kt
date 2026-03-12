package com.example.yardimuygulamakotlin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.yardimuygulamakotlin.R
import com.example.yardimuygulamakotlin.model.ConfirmedHelpItem

class ConfirmedHelpAdapter(
    private var items: List<ConfirmedHelpItem>
) : RecyclerView.Adapter<ConfirmedHelpAdapter.VH>() {

    fun submit(newItems: List<ConfirmedHelpItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvName: TextView = v.findViewById(R.id.tvName)
        val tvPhone: TextView = v.findViewById(R.id.tvPhone)
        val tvService: TextView = v.findViewById(R.id.tvService)
        val tvRoom: TextView = v.findViewById(R.id.tvRoom)
        val tvConfirmedAt: TextView = v.findViewById(R.id.tvConfirmedAt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_confirmed_help, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val it = items[position]
        holder.tvName.text = it.patient_name
        holder.tvPhone.text = "Telefon: ${it.patient_phone}"
        holder.tvService.text = "Servis: ${it.servis_adi}"
        holder.tvRoom.text = "Oda: ${it.oda_no}"
        holder.tvConfirmedAt.text = "Onay: ${it.confirmed_at ?: "-"}"
    }

    override fun getItemCount(): Int = items.size
}