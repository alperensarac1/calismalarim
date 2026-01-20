package com.example.eticaretkotlin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.eticaretkotlin.databinding.CartItemBinding

import com.example.eticaretkotlin.model.CartItemDto

class CartItemsAdapter(
    private val onPlus: (CartItemDto) -> Unit,
    private val onMinus: (CartItemDto) -> Unit,
    private val onDelete: (CartItemDto) -> Unit
) : ListAdapter<CartItemDto, CartItemsAdapter.VH>(Diff) {

    private var busyItemId: Int? = null

    fun setBusyItemId(id: Int?) {
        busyItemId = id
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = CartItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VH(private val b: CartItemBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: CartItemDto) {
            b.tvName.text = item.name
            b.tvPrice.text = "₺${"%.2f".format(item.sale_price)}"
            b.tvQty.text = item.quantity.toString()

            val busy = busyItemId == item.item_id
            b.progressRow.isVisible = busy

            b.btnPlus.isEnabled = !busy
            b.btnMinus.isEnabled = !busy
            b.btnDelete.isEnabled = !busy

            b.btnPlus.setOnClickListener { onPlus(item) }
            b.btnMinus.setOnClickListener { onMinus(item) }
            b.btnDelete.setOnClickListener { onDelete(item) }
        }
    }

    object Diff : DiffUtil.ItemCallback<CartItemDto>() {
        override fun areItemsTheSame(oldItem: CartItemDto, newItem: CartItemDto) =
            oldItem.item_id == newItem.item_id

        override fun areContentsTheSame(oldItem: CartItemDto, newItem: CartItemDto) =
            oldItem == newItem
    }
}
