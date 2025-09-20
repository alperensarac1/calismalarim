package com.example.cookieclickerkotlin


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.cookieclickerkotlin.databinding.ItemUpgradeBinding
import java.text.DecimalFormat

class UpgradeAdapter(
    private val items: List<Upgrade>,
    private val listener: Listener
) : RecyclerView.Adapter<UpgradeAdapter.VH>() {

    interface Listener {
        fun onBuyClicked(item: Upgrade)
    }

    private val df = DecimalFormat("#,###")
    private var currentScore: Double = 0.0
    private var discountPct: Double = 0.0
    fun updateAffordability(score: Double, discountPct: Double) {
        currentScore = score
        this.discountPct = discountPct
        notifyDataSetChanged()
    }


    fun updateAffordability(score: Double) {
        currentScore = score
        notifyDataSetChanged()
    }

    inner class VH(val b: ItemUpgradeBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemUpgradeBinding.inflate(inflater, parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val b = holder.b
        val raw = item.currentPrice()
        val price = raw * (1.0 - discountPct)



        b.imgIcon.setImageResource(item.iconRes)
        b.tvTitle.text = if (item.level > 0) "${item.title} (Lv ${item.level})" else item.title
        b.tvDesc.text = item.desc
        b.tvPrice.text = df.format(price)

        val canAfford = currentScore >= price
        b.btnBuy.isEnabled = canAfford
        b.btnBuy.alpha = if (canAfford) 1f else 0.5f

        // "0" cps/tap sağlayan bir şey varsa açıklamayı gizle (opsiyonel)
        b.tvDesc.isVisible = item.desc.isNotBlank()

        b.btnBuy.setOnClickListener {
            listener.onBuyClicked(item)
        }
    }

    override fun getItemCount(): Int = items.size
}
