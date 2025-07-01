package com.example.adisyonuygulamakotlin.view.masadetay

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.Icon
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import com.example.adisyonuygulamakotlin.R
import com.example.adisyonuygulamakotlin.model.Masa
import com.example.adisyonuygulamakotlin.model.MasaUrun
import com.example.adisyonuygulamakotlin.model.Urun
import com.example.adisyonuygulamakotlin.utils.SizeType
import com.example.adisyonuygulamakotlin.utils.marginEkle
import com.example.adisyonuygulamakotlin.utils.paddingEkle
import com.example.adisyonuygulamakotlin.utils.paddingEkleAll
import com.example.adisyonuygulamakotlin.utils.paddingEkleVertical
import com.example.adisyonuygulamakotlin.utils.sizeBelirle
import com.example.adisyonuygulamakotlin.viewmodel.MasaDetayViewModel

class MasaAdisyonLayout(
    private val context: Context,
    private val masa: Masa,
    private val viewModel: MasaDetayViewModel
) {

    private lateinit var urunListView: ListView
    private var urunListesi: List<MasaUrun> = emptyList()
    private lateinit var toplamFiyatTextView: TextView

    fun show(): LinearLayout {
        urunListView = masaUrunListesi()

        // ViewModel'den ürünleri çek
        viewModel.urunler.observeForever { liste ->
            urunListesi = liste
            (urunListView.adapter as? BaseAdapter)?.notifyDataSetChanged()

            // Toplam fiyatı da güncelle
            val toplam = liste.sumOf { it.toplam_fiyat.toDouble() }
            guncelleToplamFiyat(toplam.toFloat())
        }

        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(urunListView)
            addView(masaFiyatLayout())
        }
    }

    fun guncelleToplamFiyat(fiyat: Float) {
        toplamFiyatTextView.text = "${String.format("%.2f", fiyat)} TL"
    }

    private fun masaUrunListesi(): ListView {
        return ListView(context).apply {
            adapter = object : BaseAdapter() {
                override fun getCount(): Int = urunListesi.size
                override fun getItem(position: Int): Any = urunListesi[position]
                override fun getItemId(position: Int): Long = position.toLong()
                override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                    val urun = urunListesi[position]
                    return TextView(context).apply {
                        text = "${urun.urun_ad} (${urun.adet})"
                        textSize = 16f
                    }
                }
            }
        }
    }

    private fun masaFiyatLayout(): LinearLayout {
        toplamFiyatTextView = TextView(context).apply {
            textSize = 20f
            setTextColor(Color.GRAY)
            gravity = Gravity.START
            sizeBelirle(SizeType.MATCH_PARENT, SizeType.MATCH_PARENT)
        }

        val button = Button(context).apply {
            text = "Ödeme Al"
            setBackgroundColor(Color.BLUE)
            setOnClickListener {
                viewModel.odemeAl {
                    Toast.makeText(context, "Ödeme alındı", Toast.LENGTH_SHORT).show()
                    viewModel.yukleTumVeriler()
                }
            }
        }

        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(20, 20, 20, 20)
            addView(toplamFiyatTextView)
            addView(button)
        }
    }
}
