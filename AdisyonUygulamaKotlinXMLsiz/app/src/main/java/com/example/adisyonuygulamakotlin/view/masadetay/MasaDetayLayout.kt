package com.example.adisyonuygulamakotlin.view.masadetay

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.LinearLayout
import com.example.adisyonuygulamakotlin.model.Kategori
import com.example.adisyonuygulamakotlin.model.Masa
import com.example.adisyonuygulamakotlin.model.Urun
import com.example.adisyonuygulamakotlin.utils.dpToPx
import com.example.adisyonuygulamakotlin.viewmodel.MasaDetayViewModel
import com.example.adisyonuygulamakotlin.viewmodel.UrunViewModel

class MasaDetayLayout(
    private val context: Context,
    private var masa: Masa,
    viewModel: MasaDetayViewModel,
    private var urunVM:UrunViewModel
) {

    val masaAdisyonLayout = MasaAdisyonLayout(context, masa,viewModel)
    val masaDetayHeaderLayout = MasaDetayHeaderLayout(context, masa)
    val urunlerLayout = UrunlerLayout(context, viewModel,urunVM)

    fun show(): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.parseColor("#FFBABA"))
            addView(createHeaderView())
            addView(createBodyView())
        }
    }

    private fun createHeaderView(): View {
        return masaDetayHeaderLayout.show().apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(60)
            )
        }
    }

    private fun createBodyView(): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )

            addView(masaAdisyonLayout.show().apply {
                layoutParams = LinearLayout.LayoutParams(
                    dpToPx(200),
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
            })

            addView(urunlerLayout.show().apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1f
                )
            })
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    fun guncelleMasa(masa: Masa) {
        this.masa = masa
        masaDetayHeaderLayout.guncelleMasa(masa)
    }
}

