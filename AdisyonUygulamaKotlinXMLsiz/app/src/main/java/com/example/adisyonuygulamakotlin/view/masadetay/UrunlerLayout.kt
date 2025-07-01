package com.example.adisyonuygulamakotlin.view.masadetay

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.adisyonuygulamakotlin.adapter.UrunRVAdapter
import com.example.adisyonuygulamakotlin.model.Kategori
import com.example.adisyonuygulamakotlin.model.Urun
import com.example.adisyonuygulamakotlin.utils.SizeType
import com.example.adisyonuygulamakotlin.utils.dpToPx
import com.example.adisyonuygulamakotlin.utils.fiyatYaz
import com.example.adisyonuygulamakotlin.utils.marginEkle
import com.example.adisyonuygulamakotlin.utils.sizeBelirle
import com.example.adisyonuygulamakotlin.viewmodel.MasaDetayViewModel
import com.example.adisyonuygulamakotlin.viewmodel.UrunViewModel
import com.squareup.picasso.Picasso

class UrunlerLayout(
    private val context: Context,
    private val viewModel: MasaDetayViewModel,
    private val urunVM:UrunViewModel
) {

    private var tumUrunListesi = mutableListOf<Urun>()
    private var guncelUrunListesi = mutableListOf<Urun>()
    private var kategoriListesi = listOf<Kategori>()

    private val urunAdapter = UrunRVAdapter(context, guncelUrunListesi, viewModel)

    private val recyclerView = RecyclerView(context).apply {
        layoutManager = GridLayoutManager(context, 3)
        adapter = urunAdapter
        sizeBelirle(SizeType.MATCH_PARENT, SizeType.MATCH_PARENT)
    }

    private val bosMesaj = TextView(context).apply {
        text = "Seçilen kategoriye ait ürün bulunamadı."
        textSize = 18f
        gravity = Gravity.CENTER
        setTextColor(Color.GRAY)
        visibility = View.GONE
        sizeBelirle(SizeType.MATCH_PARENT, SizeType.MATCH_PARENT)
    }

    private val kategoriListView = ListView(context).apply {
        sizeBelirle(SizeType.WRAP_CONTENT, SizeType.MATCH_PARENT)
    }

    fun show(): ViewGroup {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL

            addView(kategoriListView.apply {
                layoutParams = LinearLayout.LayoutParams(
                    dpToPx(150, context),
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
            })

            addView(FrameLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1f
                )
                addView(recyclerView)
                addView(bosMesaj)
            })
        }
    }

    fun setUrunListesi(yeniListe: List<Urun>) {
        tumUrunListesi = yeniListe.toMutableList()
        guncelUrunListesi.clear()
        guncelUrunListesi.addAll(tumUrunListesi)
        urunAdapter.notifyDataSetChanged()
        bosMesaj.visibility = if (guncelUrunListesi.isEmpty()) View.VISIBLE else View.GONE
    }

    fun setKategoriListesi(yeniKategoriler: List<Kategori>) {
        kategoriListesi = yeniKategoriler

        val kategoriAdlari = listOf("Tümü") + kategoriListesi.map { it.kategori_ad }

        kategoriListView.adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, kategoriAdlari)

        kategoriListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val filtreliListe = if (position == 0) {
                tumUrunListesi
            } else {
                val secilenKategori = kategoriListesi[position - 1]
                tumUrunListesi.filter { it.urunKategori.id == secilenKategori.id }
            }

            guncelUrunListesi.clear()
            guncelUrunListesi.addAll(filtreliListe)
            urunAdapter.notifyDataSetChanged()
            bosMesaj.visibility = if (filtreliListe.isEmpty()) View.VISIBLE else View.GONE
        }
    }
}
