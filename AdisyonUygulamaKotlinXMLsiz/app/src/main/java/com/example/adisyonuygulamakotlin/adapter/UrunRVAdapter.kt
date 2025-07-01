package com.example.adisyonuygulamakotlin.adapter

import android.content.Context
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.adisyonuygulamakotlin.model.Urun
import com.example.adisyonuygulamakotlin.utils.fiyatYaz
import com.example.adisyonuygulamakotlin.utils.sizeBelirle
import com.example.adisyonuygulamakotlin.viewmodel.MasaDetayViewModel
import com.squareup.picasso.Picasso

class UrunRVAdapter(
    private val context: Context,
    private val urunListesi: MutableList<Urun>,
    private val viewModel: MasaDetayViewModel
) : RecyclerView.Adapter<UrunRVAdapter.UrunViewHolder>() {

    inner class UrunViewHolder(
        val imageView: ImageView,
        val nameTextView: TextView,
        val priceTextView: TextView,
        val ekleCikarLayout: LinearLayout,
        val rootView: CardView
    ) : RecyclerView.ViewHolder(rootView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UrunViewHolder {
        val imageView = ImageView(context).apply {
            sizeBelirle(250, 200)
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        val nameTextView = TextView(context).apply { textSize = 16f }
        val priceTextView = TextView(context).apply { textSize = 14f }
        val ekleCikarLayout = LinearLayout(context)

        val verticalLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
            addView(imageView)
            addView(nameTextView)
            addView(priceTextView)
            addView(ekleCikarLayout)
        }

        val cardView = CardView(context).apply {
            radius = 16f
            cardElevation = 5f
            addView(verticalLayout)
        }

        return UrunViewHolder(imageView, nameTextView, priceTextView, ekleCikarLayout, cardView)
    }

    override fun onBindViewHolder(holder: UrunViewHolder, position: Int) {
        val urun = urunListesi[position]

        Picasso.get()
            .load(urun.urun_resim)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_delete)
            .into(holder.imageView)

        holder.nameTextView.text = urun.urun_ad
        holder.priceTextView.text = urun.urun_fiyat.fiyatYaz() + " TL"

        holder.ekleCikarLayout.removeAllViews()
        

        val btnEkle = Button(context).apply {
            text = "+"
            setOnClickListener {
                viewModel.urunEkle(urun.id)
                urun.urun_adet += 1
                viewModel.yukleTumVeriler()
                notifyDataSetChanged()
            }
        }

        val btnCikar = Button(context).apply {
            text = "-"
            setOnClickListener {
                if (urun.urun_adet > 0) {
                    viewModel.urunCikar(urun.id)
                    urun.urun_adet -= 1
                    viewModel.yukleTumVeriler()

                    notifyDataSetChanged()
                }
            }
        }

        holder.ekleCikarLayout.apply {
            orientation = LinearLayout.HORIZONTAL
            addView(btnEkle)
            addView(btnCikar)
        }
    }

    override fun getItemCount(): Int = urunListesi.size

    fun setData(yeniListe: List<Urun>) {
        urunListesi.clear()
        urunListesi.addAll(yeniListe)
        notifyDataSetChanged()
    }
}

