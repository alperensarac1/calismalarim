package com.example.isletimsistemlericalismasikotlin.uygulamalar.copkutusu

import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.isletimsistemlericalismasikotlin.R
import com.example.isletimsistemlericalismasikotlin.anaekran.entity.Const
import com.example.isletimsistemlericalismasikotlin.anaekran.repository.UygulamalarRepository
import com.example.isletimsistemlericalismasikotlin.databinding.UygulamalarRecyclerCardBinding

class CopKutusuAdapter(private val mcontext: Context, private val consts: Const) :
    RecyclerView.Adapter<CopKutusuAdapter.CopKutusuTasarimTutucu>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CopKutusuTasarimTutucu {
        val binding = UygulamalarRecyclerCardBinding.inflate(LayoutInflater.from(mcontext), parent, false)
        return CopKutusuTasarimTutucu(binding)
    }

    override fun onBindViewHolder(holder: CopKutusuTasarimTutucu, position: Int) {
        val uygulama = consts.getCopUygulamaListesi()[position]

        holder.binding.tvUygulama.text = uygulama.uygulamaAdi
        holder.binding.imgUygulama.setImageResource(
            mcontext.resources.getIdentifier(
                uygulama.uygulamaResimAdi, "drawable", mcontext.packageName
            )
        )

        holder.binding.cardUygulama.setOnLongClickListener { v ->
            val popup = PopupMenu(mcontext, v)
            popup.menuInflater.inflate(R.menu.uygulama_geriyukle_menu, popup.menu)
            popup.setOnMenuItemClickListener { item: MenuItem ->
                if (item.itemId == R.id.action_geri_yukle) {
                    val uygulamalarRepository = UygulamalarRepository(mcontext)
                    consts.getCopUygulamaListesi().remove(uygulama)
                    consts.getUygulamalarListesi().add(uygulama)
                    uygulamalarRepository.copKutusundanCikar(uygulama)
                    notifyDataSetChanged()
                    true
                } else {
                    false
                }
            }
            popup.show()
            true
        }
    }

    override fun getItemCount(): Int = consts.getCopUygulamaListesi().size

    class CopKutusuTasarimTutucu(val binding: UygulamalarRecyclerCardBinding) :
        RecyclerView.ViewHolder(binding.root)
}