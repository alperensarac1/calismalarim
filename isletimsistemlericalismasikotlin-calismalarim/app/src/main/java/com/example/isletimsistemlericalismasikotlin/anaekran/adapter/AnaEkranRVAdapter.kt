package com.example.isletimsistemlericalismasikotlin.anaekran.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.isletimsistemlericalismasikotlin.R
import com.example.isletimsistemlericalismasikotlin.anaekran.entity.Const
import com.example.isletimsistemlericalismasikotlin.anaekran.repository.UygulamalarRepository
import com.example.isletimsistemlericalismasikotlin.databinding.UygulamalarRecyclerCardBinding
import com.example.isletimsistemlericalismasikotlin.uygulamalar.copkutusu.copkutusu

class AnaekranRVAdapter(private val mContext: Context, private val consts: Const) :
    RecyclerView.Adapter<AnaekranRVAdapter.AnaekranTasarimTutucu>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnaekranTasarimTutucu {
        val binding = UygulamalarRecyclerCardBinding.inflate(LayoutInflater.from(mContext), parent, false)
        return AnaekranTasarimTutucu(binding)
    }

    override fun onBindViewHolder(holder: AnaekranTasarimTutucu, position: Int) {
        val uygulama = consts.getUygulamalarListesi()[position]

        holder.binding.tvUygulama.text = uygulama.uygulamaAdi
        holder.binding.imgUygulama.setImageResource(
            mContext.resources.getIdentifier(uygulama.uygulamaResimAdi, "drawable", mContext.packageName)
        )

        holder.binding.cardUygulama.setOnClickListener { view ->
            if (uygulama.navId == 1) {
                val fragmentCopKutusu = copkutusu()
                val args = Bundle()
                args.putSerializable("copler", consts)
                fragmentCopKutusu.arguments = args
                Navigation.findNavController(view).navigate(R.id.toCopkutusu, args)
            } else {
                Navigation.findNavController(view).navigate(uygulama.navId)
            }
        }

        if (uygulama.navId != 1) {
            holder.binding.cardUygulama.setOnLongClickListener { v ->
                val popup = PopupMenu(mContext, v)
                popup.menuInflater.inflate(R.menu.uygulama_kaldir_menu, popup.menu)
                popup.setOnMenuItemClickListener { item: MenuItem ->
                    if (item.itemId == R.id.action_kaldir) {
                        val uygulamalarRepository = UygulamalarRepository(mContext)
                        consts.getUygulamalarListesi().remove(uygulama)
                        consts.getCopUygulamaListesi().add(uygulama)
                        uygulamalarRepository.copKutusunaTasi(uygulama)
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
    }

    override fun getItemCount(): Int = consts.getUygulamalarListesi().size

    inner class AnaekranTasarimTutucu(val binding: UygulamalarRecyclerCardBinding) : RecyclerView.ViewHolder(binding.root)
}
