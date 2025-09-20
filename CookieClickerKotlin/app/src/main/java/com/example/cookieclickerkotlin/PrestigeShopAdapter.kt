package com.example.cookieclickerkotlin


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cookieclickerkotlin.databinding.ItemPerkBinding

class PrestigeShopAdapter(
    private val items: List<PrestigePerk>,
    private val canAfford: (Int) -> Boolean,
    private val onBuy: (PrestigePerk) -> Unit
) : RecyclerView.Adapter<PrestigeShopAdapter.VH>() {

    inner class VH(val b: ItemPerkBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inf = LayoutInflater.from(parent.context)
        return VH(ItemPerkBinding.inflate(inf, parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = items[position]
        val b = holder.b
        b.tvTitle.text = p.title
        b.tvDesc.text = p.desc
        val cost = p.costForNext()
        b.tvMeta.text = "Lv ${p.level} • Maliyet: $cost"
        val afford = canAfford(cost) && p.level < p.maxLevel
        b.btnBuyPerk.isEnabled = afford
        b.btnBuyPerk.alpha = if (afford) 1f else .5f
        b.btnBuyPerk.setOnClickListener { onBuy(p) }
    }

    override fun getItemCount() = items.size
}
