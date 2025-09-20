package com.example.cookieclickerkotlin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cookieclickerkotlin.databinding.ActivityPrestigeShopBinding

class PrestigeShopActivity : AppCompatActivity() {

    private lateinit var b: ActivityPrestigeShopBinding
    private val prefs by lazy { getSharedPreferences("cookie_prefs", MODE_PRIVATE) }

    private var prestigePoints = 0
    private lateinit var perks: MutableList<PrestigePerk>   // <-- sınıf seviyesinde tanımla, içeride doldur

    private var onAdapterRefresh: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityPrestigeShopBinding.inflate(layoutInflater)
        setContentView(b.root)
        supportActionBar?.title = getString(R.string.shop_title)

        // State yükle
        prestigePoints = prefs.getInt("prestige_points", 0)

        // !!! getString kullanımı artık burada (Context hazır)
        perks = mutableListOf(
            PrestigePerk(
                key = "perk_gprod",
                title = getString(R.string.perk_gprod),
                // seviye başına %5
                desc  = getString(R.string.perk_gprod_desc, 5),
                baseCost = 1,
                costScaling = 1.6
            ),
            PrestigePerk(
                key = "perk_crit",
                title = getString(R.string.perk_crit),
                // seviye başına %1 şans, x3 çarpan
                desc  = getString(R.string.perk_crit_desc, 1, 3),
                baseCost = 2,
                costScaling = 1.7
            ),
            PrestigePerk(
                key = "perk_discount",
                title = getString(R.string.perk_discount),
                // seviye başına %2 indirim, tavan %50
                desc  = getString(R.string.perk_discount_desc, 2, 50),
                baseCost = 3,
                costScaling = 1.8,
                maxLevel = 25 // %50 tavan
            ),
            PrestigePerk(
                key = "perk_taptop",
                title = getString(R.string.perk_taptop),
                desc  = getString(R.string.perk_taptop_desc),
                baseCost = 2,
                costScaling = 1.5
            )
        )


        // Kayıtlı seviyeleri yükle
        perks.forEach { it.level = prefs.getInt(it.key, 0) }

        val adapter = PrestigeShopAdapter(
            items = perks,
            canAfford = { cost -> prestigePoints >= cost },
            onBuy = { perk -> buyPerk(perk) }
        )
        b.rvPerks.layoutManager = LinearLayoutManager(this)
        b.rvPerks.adapter = adapter

        updateHeader()

        fun refresh() { adapter.notifyDataSetChanged(); updateHeader() }
        onAdapterRefresh = ::refresh
    }

    private fun updateHeader() {
        b.tvPoints.text = getString(R.string.prestige_points, prestigePoints)
    }

    private fun buyPerk(perk: PrestigePerk) {
        val cost = perk.costForNext()
        if (prestigePoints < cost || perk.level >= perk.maxLevel) return
        prestigePoints -= cost
        perk.level += 1
        prefs.edit()
            .putInt("prestige_points", prestigePoints)
            .putInt(perk.key, perk.level)
            .apply()
        onAdapterRefresh?.invoke()
    }
}
