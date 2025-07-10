package com.example.chatjava.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.chatjava.R;
import com.example.chatjava.model.KonusulanKisi;

import java.util.List;

public class KonusulanKisilerAdapter
        extends RecyclerView.Adapter<KonusulanKisilerAdapter.KisiViewHolder> {


    private final List<KonusulanKisi> kisiListesi;
    private final KonusulanKisiOnItemClickListener onItemClick;

    public KonusulanKisilerAdapter(List<KonusulanKisi> kisiListesi,
                                   KonusulanKisiOnItemClickListener onItemClick) {
        this.kisiListesi = kisiListesi;
        this.onItemClick = onItemClick;
    }

    class KisiViewHolder extends RecyclerView.ViewHolder {
        TextView tvKullaniciAdi, tvMesajOzet, tvTarih;

        KisiViewHolder(@NonNull View itemView) {
            super(itemView);
            tvKullaniciAdi = itemView.findViewById(R.id.tvKullaniciAdi);
            tvMesajOzet    = itemView.findViewById(R.id.tvMesajOzet);
            tvTarih        = itemView.findViewById(R.id.tvTarih);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    onItemClick.onItemClick(kisiListesi.get(pos));
                }
            });
        }
    }

    @NonNull
    @Override
    public KisiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.mesajlar_item, parent, false);
        return new KisiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull KisiViewHolder holder, int position) {
        KonusulanKisi kisi = kisiListesi.get(position);

        holder.tvKullaniciAdi.setText(kisi.getAd());

        String sonMesaj = kisi.getSon_mesaj() != null ? kisi.getSon_mesaj() : "";
        String ozet = sonMesaj.length() > 30
                ? sonMesaj.substring(0, 30) + "..."
                : sonMesaj;
        holder.tvMesajOzet.setText(ozet);

        holder.tvTarih.setText(kisi.getTarih());
    }

    @Override
    public int getItemCount() {
        return kisiListesi.size();
    }
}

