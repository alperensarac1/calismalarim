package com.example.eticaretkotlin.adapters



import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.eticaretkotlin.databinding.ItemOrderLineBinding

data class OrderLineUi(
    val name: String,
    val qty: Int,
    val lineTotal: Double
)

class OrderItemsAdapter : RecyclerView.Adapter<OrderItemsAdapter.VH>() {

    private val data = mutableListOf<OrderLineUi>()

    fun submit(list: List<OrderLineUi>) {
        data.clear()
        data.addAll(list)
        notifyDataSetChanged()
    }

    inner class VH(val b: ItemOrderLineBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemOrderLineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = data[position]
        holder.b.tvName.text = item.name
        holder.b.tvQty.text = "x${item.qty}"
        holder.b.tvLineTotal.text = "₺${"%.2f".format(item.lineTotal)}"
    }

    override fun getItemCount() = data.size
}
