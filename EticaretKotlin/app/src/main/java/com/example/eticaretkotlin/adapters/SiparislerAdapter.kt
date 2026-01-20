package com.example.eticaretkotlin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.eticaretkotlin.databinding.SiparisItemBinding
import com.example.eticaretkotlin.model.OrderSummaryDto

class SiparislerAdapter(
    private val onClick: (Int) -> Unit
) : ListAdapter<OrderSummaryDto, SiparislerAdapter.VH>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = SiparisItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    inner class VH(private val b: SiparisItemBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(o: OrderSummaryDto) {
            b.tvTitle.text = "Sipariş #${o.id}"
            b.tvSub.text = "${o.status} • ₺${"%.2f".format(o.totalAmount)}"
            b.root.setOnClickListener { onClick(o.id) }
        }
    }

    object Diff : DiffUtil.ItemCallback<OrderSummaryDto>() {
        override fun areItemsTheSame(o: OrderSummaryDto, n: OrderSummaryDto) = o.id == n.id
        override fun areContentsTheSame(o: OrderSummaryDto, n: OrderSummaryDto) = o == n
    }
}
