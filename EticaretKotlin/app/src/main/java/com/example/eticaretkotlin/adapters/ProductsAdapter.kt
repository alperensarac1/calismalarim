package com.example.eticaretkotlin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.eticaretkotlin.databinding.ItemProductBinding
import com.example.eticaretkotlin.model.ProductListDto

class ProductsAdapter(
    private val onClick: (ProductListDto) -> Unit
) : ListAdapter<ProductListDto, ProductsAdapter.VH>(DIFF) {

    inner class VH(val b: ItemProductBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.b.tvName.text = item.name
        holder.b.tvPrice.text = "₺${"%.2f".format(item.price)}"

        // image field adını kendi DTO’na göre güncelle (imageUrl varsayıldı)
        Glide.with(holder.b.img).load(item.imageUrl).into(holder.b.img)

        holder.itemView.setOnClickListener { onClick(item) }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ProductListDto>() {
            override fun areItemsTheSame(o: ProductListDto, n: ProductListDto) = o.id == n.id
            override fun areContentsTheSame(o: ProductListDto, n: ProductListDto) = o == n
        }
    }
}
