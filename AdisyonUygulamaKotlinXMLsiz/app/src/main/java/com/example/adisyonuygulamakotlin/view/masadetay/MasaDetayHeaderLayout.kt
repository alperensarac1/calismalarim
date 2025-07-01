package com.example.adisyonuygulamakotlin.view.masadetay

import android.content.Context
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.example.adisyonuygulamakotlin.R
import com.example.adisyonuygulamakotlin.model.Masa
import com.example.adisyonuygulamakotlin.utils.SizeType
import com.example.adisyonuygulamakotlin.utils.paddingEkleVertical
import com.example.adisyonuygulamakotlin.utils.sizeBelirle

class MasaDetayHeaderLayout(
    private val context: Context,
    private var masa: Masa // artık güncellenebilir
) {

    private lateinit var masaAdiTextView: TextView

    fun show(onBackPressed: () -> Unit = {}): LinearLayout {
        val geriButton = Button(context).apply {
            setOnClickListener {
                onBackPressed()
            }
            setBackgroundResource(R.drawable.ic_remove)
            sizeBelirle(SizeType.WRAP_CONTENT, SizeType.WRAP_CONTENT)
        }

        masaAdiTextView = TextView(context).apply {
            text = "Masa ${masa.id}"
            textSize = 20f
        }

        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            sizeBelirle(width = SizeType.MATCH_PARENT, height = SizeType.WRAP_CONTENT)
            paddingEkleVertical(vertical = 50)
            addView(geriButton)
            addView(masaAdiTextView)
        }
    }

    fun guncelleMasa(yeniMasa: Masa) {
        this.masa = yeniMasa
        masaAdiTextView.text = "Masa ${masa.id}"
    }
}
