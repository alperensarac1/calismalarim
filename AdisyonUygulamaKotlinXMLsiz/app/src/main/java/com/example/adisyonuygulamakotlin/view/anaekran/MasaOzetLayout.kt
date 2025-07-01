package com.example.adisyonuygulamakotlin.view.anaekran

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.adisyonuygulamakotlin.model.Masa
import com.example.adisyonuygulamakotlin.utils.SizeType
import com.example.adisyonuygulamakotlin.utils.marginEkle
import com.example.adisyonuygulamakotlin.utils.sizeBelirle
import com.example.adisyonuygulamakotlin.viewmodel.MasaDetayViewModel

class MasaOzetLayout(
    private val context: Context,
    private val masaListesi: List<Masa>,
    private val viewModel: MasaDetayViewModel, // viewmodel erişimi için
    private val onMasaDetayTikla: (Masa) -> Unit                // fragment geçiş için
) {

    fun linearLayout(): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 50, 50, 50)

            addView(aciklamaTextView())
            addView(acikMasalarListView())
        }
    }

    private fun aciklamaTextView(): TextView {
        val acikMasaSayisi = masaListesi.count { it.acikMi == 1 }

        return TextView(context).apply {
            text = "$acikMasaSayisi adet masa açık."
            textSize = 20f
            setTextColor(Color.RED)
            gravity = Gravity.CENTER
            marginEkle(top = 20)
            sizeBelirle(SizeType.MATCH_PARENT, SizeType.WRAP_CONTENT)
        }
    }

    private fun acikMasalarListView(): ListView {
        val acikMasalar = masaListesi.filter { it.acikMi == 1 }

        val masaAdlari = acikMasalar.map {
            "Masa ${it.id} \t ${String.format("%.2f ₺", it.toplam_fiyat)}"
        }

        return ListView(context).apply {
            adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, masaAdlari)
            marginEkle(top = 20)
            sizeBelirle(SizeType.MATCH_PARENT, SizeType.MATCH_PARENT)

            setOnItemClickListener { _, _, position, _ ->
                val secilenMasa = acikMasalar[position]
                gosterMasaSecenekleri(secilenMasa)
            }
        }
    }

    private fun gosterMasaSecenekleri(masa: Masa) {
        AlertDialog.Builder(context).apply {
            setTitle("Masa ${masa.id}")
            setItems(arrayOf("Ürün Ekle", "Ödeme Al")) { dialog, which ->
                when (which) {
                    0 -> onMasaDetayTikla(masa) // Fragment geçiş
                    1 -> {

                        viewModel.odemeAl {
                            Toast.makeText(context, "Ödeme alındı", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                dialog.dismiss()
            }
        }.show()
    }
}
