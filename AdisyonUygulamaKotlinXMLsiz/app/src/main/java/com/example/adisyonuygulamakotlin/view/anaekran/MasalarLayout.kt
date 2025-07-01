package com.example.adisyonuygulamakotlin.view.anaekran

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.adisyonuygulamakotlin.adapter.MasaRVAdapter
import com.example.adisyonuygulamakotlin.model.Masa

class MasalarLayout(
    private val context: Context,
    private val masaList: List<Masa>,
    private val onMasaClick: (Masa) -> Unit // ✅ tıklama callback’i eklendi
) {

    fun recyclerView(): RecyclerView {
        return RecyclerView(context).apply {
            adapter = MasaRVAdapter(context, masaList, onMasaClick) // ✅ callback ile adapter verildi
            layoutManager = GridLayoutManager(context, 3, GridLayoutManager.VERTICAL, false)
            setHasFixedSize(true)
        }
    }
}
