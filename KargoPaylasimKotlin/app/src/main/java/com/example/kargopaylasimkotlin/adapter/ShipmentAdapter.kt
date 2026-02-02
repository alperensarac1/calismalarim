package com.example.kargopaylasimkotlin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kargopaylasimkotlin.R
import com.example.kargopaylasimkotlin.dto.ShipmentDto
class ShipmentAdapter(
    private var items: List<ShipmentDto>,
    private val onClick: (ShipmentDto) -> Unit
) : RecyclerView.Adapter<ShipmentAdapter.VH>() {

    fun submit(newItems: List<ShipmentDto>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvCode: TextView = v.findViewById(R.id.tvCode)
        val tvStatus: TextView = v.findViewById(R.id.tvStatus)
        val tvRemaining: TextView = v.findViewById(R.id.tvRemaining)

        val tvCompany: TextView = v.findViewById(R.id.tvCompany)

        fun bind(item: ShipmentDto) {

            if (item.role == "RECEIVER" && item.visible == false) {
                tvCode.text = "Henüz firma onaylamadı"
                tvStatus.text = "Durum: ${item.status}"
                tvRemaining.text = "-"
                tvCompany.text = "-"
                itemView.setOnClickListener(null)
                return
            }

            val companyLine = item.cargoCompanyName?.takeIf { it.isNotBlank() }
                ?.let { "Firma: $it" } ?: "Firma: -"
            tvCompany.text = companyLine

            if (item.role == "SENDER") {
                tvCode.text = "Kod: ${item.pickupCode}"
                tvStatus.text = "Durum: ${item.status}"
                tvRemaining.text = "Kalan: ${DateUtil.remainingText(item.codeExpiresAt)}"
            } else {
                // RECEIVER
                tvCode.text = "Gönderici: ${item.senderInitials ?: "-"}"
                tvStatus.text = "Durum: ${item.status}"
                tvRemaining.text = "Adres: ${item.receiverAddressTitle ?: "-"}"
            }

            itemView.setOnClickListener { onClick(item) }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position].role == "SENDER") 1 else 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_shipment, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])
    override fun getItemCount(): Int = items.size
}
