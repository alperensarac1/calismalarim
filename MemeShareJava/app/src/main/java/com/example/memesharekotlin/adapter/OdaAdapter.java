package com.example.memesharekotlin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memesharekotlin.R;
import com.example.memesharekotlin.model.OdaModel;

import java.util.List;

public class OdaAdapter extends RecyclerView.Adapter<OdaAdapter.OdaViewHolder> {

    private List<OdaModel> odaListesi;
    private OnOdaClickListener listener;

    public interface OnOdaClickListener {
        void onOdaClick(OdaModel oda);
    }

    public OdaAdapter(List<OdaModel> odaListesi, OnOdaClickListener listener) {
        this.odaListesi = odaListesi;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OdaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.oda_item, parent, false);
        return new OdaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OdaViewHolder holder, int position) {
        OdaModel oda = odaListesi.get(position);
        holder.tvOdaIsmi.setText("" + oda.getRoomCode());
        holder.tvSonMesajTarihi.setText(" " + oda.getCreatedBy());


        holder.odaCard.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOdaClick(oda);
            }
        });
    }

    @Override
    public int getItemCount() {
        return odaListesi.size();
    }

    public static class OdaViewHolder extends RecyclerView.ViewHolder {
        TextView tvOdaIsmi, tvSonMesajTarihi;
        CardView odaCard;

        public OdaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOdaIsmi = itemView.findViewById(R.id.tvOdaIsmi);
            tvSonMesajTarihi = itemView.findViewById(R.id.tvSonMesajTarihi);
            odaCard = itemView.findViewById(R.id.cardView);
        }
    }
}
