package com.example.adisyonuygulamakotlin.view.anaekran

import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.adisyonuygulamakotlin.model.Masa
import com.example.adisyonuygulamakotlin.utils.SizeType
import com.example.adisyonuygulamakotlin.utils.fiyatYaz
import com.example.adisyonuygulamakotlin.utils.marginEkle
import com.example.adisyonuygulamakotlin.utils.paddingEkle
import com.example.adisyonuygulamakotlin.utils.sizeBelirle

class MasaCard(
    private val context: Context,
    private val onClick: (Masa) -> Unit
) {

    private val cardView = CardView(context)
    private val masaAdiTV = TextView(context)
    private val masaFiyatTV = TextView(context)
    private val masaSureTV = TextView(context)

    private var masa: Masa? = null

    init {
        cardView.apply {
            radius = 24f
            cardElevation = 8f
            useCompatPadding = true
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(20, 20, 20, 20)
            }
        }

        val verticalLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 30, 30, 30)
            addView(masaAdiTV.apply {
                textSize = 20f
                setTextColor(Color.BLACK)
            })
            addView(masaFiyatTV.apply {
                textSize = 18f
                setTextColor(Color.DKGRAY)
            })
            addView(masaSureTV.apply {
                textSize = 16f
                setTextColor(Color.GRAY)
            })
        }

        cardView.addView(verticalLayout)

        cardView.setOnClickListener {
            masa?.let { onClick(it) }
        }
    }

    fun cardView(): CardView = cardView

    fun setData(masa: Masa) {
        this.masa = masa
        masaAdiTV.text = "Masa ${masa.id}"
        masaFiyatTV.text = "Tutar: ${masa.toplam_fiyat.fiyatYaz()}"
        masaSureTV.text = "Süre: ${masa.sure}"
    }

    fun setBackgroundColor(color: Int) {
        cardView.setCardBackgroundColor(color)
    }
}


