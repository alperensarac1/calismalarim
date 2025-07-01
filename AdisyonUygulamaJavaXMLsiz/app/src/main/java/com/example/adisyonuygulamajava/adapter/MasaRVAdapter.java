package com.example.adisyonuygulamajava.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adisyonuygulamajava.anaekran.MasaCard;
import com.example.adisyonuygulamajava.model.Masa;

import java.util.List;

public class MasaRVAdapter extends RecyclerView.Adapter<MasaRVAdapter.MasaViewHolder> {

    public interface OnMasaClickListener {
        void onMasaClick(Masa masa);
    }

    private final Context context;
    private final List<Masa> masaList;
    private final OnMasaClickListener onMasaClick;

    public MasaRVAdapter(Context context, List<Masa> masaList, OnMasaClickListener onMasaClick) {
        this.context = context;
        this.masaList = masaList;
        this.onMasaClick = onMasaClick;
    }

    public static class MasaViewHolder extends RecyclerView.ViewHolder {
        public final MasaCard masaCard;

        public MasaViewHolder(MasaCard masaCard) {
            super(masaCard.getCardView());
            this.masaCard = masaCard;
        }
    }

    @NonNull
    @Override
    public MasaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MasaViewHolder(new MasaCard(context, new MasaCard.OnMasaClickListener() {
            @Override
            public void onClick(Masa masa) {
                onMasaClick.onMasaClick(masa);
            }
        }));
    }

    @Override
    public void onBindViewHolder(@NonNull MasaViewHolder holder, int position) {
        Masa masa = masaList.get(position);
        holder.masaCard.setData(masa);

        if (masa.getAcikMi() == 1) {
            holder.masaCard.setBackgroundColor(Color.CYAN);
        } else {
            holder.masaCard.setBackgroundColor(Color.GRAY);
        }
    }

    @Override
    public int getItemCount() {
        return masaList.size();
    }
}

