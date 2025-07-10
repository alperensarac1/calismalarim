package com.example.chatjava.adapter;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatjava.R;
import com.example.chatjava.model.Mesaj;
import com.example.chatjava.util.AppConfig;

import java.util.List;

public class MesajlarRVAdapter extends RecyclerView.Adapter<MesajlarRVAdapter.MesajViewHolder> {



    private final List<Mesaj> mesajList;
    private final OnItemClickListener onItemClick;

    public MesajlarRVAdapter(List<Mesaj> mesajList, OnItemClickListener onItemClick) {
        this.mesajList = mesajList;
        this.onItemClick = onItemClick;
    }

    public class MesajViewHolder extends RecyclerView.ViewHolder {
        TextView tvMesajMetni;
        ImageView ivMesajResim;
        TextView tvTarih;
        LinearLayout llMesajBalonu;

        public MesajViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMesajMetni = itemView.findViewById(R.id.tvMesajMetni);
            ivMesajResim = itemView.findViewById(R.id.ivMesajResim);
            tvTarih = itemView.findViewById(R.id.tvTarih);
            llMesajBalonu = itemView.findViewById(R.id.llMesajBalonu);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick.onItemClick(mesajList.get(position));
                }
            });
        }
    }

    @NonNull
    @Override
    public MesajViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.mesaj_item, parent, false);
        return new MesajViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MesajViewHolder holder, int position) {
        Mesaj mesaj = mesajList.get(position);

        int benimId = AppConfig.kullaniciId;
        boolean benimMesajim = mesaj.getGonderen_id() == benimId;

        FrameLayout.LayoutParams params =
                (FrameLayout.LayoutParams) holder.llMesajBalonu.getLayoutParams();
        if (benimMesajim) {
            holder.llMesajBalonu.setBackgroundResource(R.drawable.bg_mavi_mesaj);
            params.gravity = Gravity.END;
        } else {
            holder.llMesajBalonu.setBackgroundResource(R.drawable.bg_gri_mesaj);
            params.gravity = Gravity.START;
        }
        holder.llMesajBalonu.setLayoutParams(params);

        // Metin gösterimi
        if (mesaj.getMesaj_text() != null && !mesaj.getMesaj_text().isEmpty()) {
            holder.tvMesajMetni.setVisibility(View.VISIBLE);
            holder.tvMesajMetni.setText(mesaj.getMesaj_text());
        } else {
            holder.tvMesajMetni.setVisibility(View.GONE);
        }

        // Resim gösterimi
        if (mesaj.getResim_url() != null && !mesaj.getResim_url().isEmpty()) {
            holder.ivMesajResim.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(mesaj.getResim_url())
                    .into(holder.ivMesajResim);
        } else {
            holder.ivMesajResim.setVisibility(View.GONE);
        }

        holder.tvTarih.setText(mesaj.getTarih());
    }

    @Override
    public int getItemCount() {
        return mesajList.size();
    }
}
