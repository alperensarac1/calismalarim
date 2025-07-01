package com.example.adisyonuygulamajava.anaekran;

import android.content.Context;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adisyonuygulamajava.adapter.MasaRVAdapter;
import com.example.adisyonuygulamajava.model.Masa;

import java.util.List;

public class MasalarLayout {

    public interface OnMasaClickListener extends MasaRVAdapter.OnMasaClickListener {
        void onMasaClick(Masa masa);
    }

    private final Context context;
    private final List<Masa> masaList;
    private final OnMasaClickListener onMasaClick;

    public MasalarLayout(Context context, List<Masa> masaList, OnMasaClickListener onMasaClick) {
        this.context = context;
        this.masaList = masaList;
        this.onMasaClick = onMasaClick;
    }

    public RecyclerView recyclerView() {
        RecyclerView recyclerView = new RecyclerView(context);
        recyclerView.setAdapter(new MasaRVAdapter(context, masaList, onMasaClick));
        recyclerView.setLayoutManager(new GridLayoutManager(context, 3, RecyclerView.VERTICAL, false));
        recyclerView.setHasFixedSize(true);
        return recyclerView;
    }
}

