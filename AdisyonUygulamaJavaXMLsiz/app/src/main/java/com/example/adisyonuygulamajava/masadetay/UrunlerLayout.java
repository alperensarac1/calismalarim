package com.example.adisyonuygulamajava.masadetay;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adisyonuygulamajava.adapter.UrunRVAdapter;
import com.example.adisyonuygulamajava.model.Kategori;
import com.example.adisyonuygulamajava.model.Urun;
import com.example.adisyonuygulamajava.utils.SizeType;
import com.example.adisyonuygulamajava.utils.ViewUtils;
import com.example.adisyonuygulamajava.viewmodel.MasaDetayViewModel;
import com.example.adisyonuygulamajava.viewmodel.UrunViewModel;

import java.util.ArrayList;
import java.util.List;

public class UrunlerLayout {

    private final Context context;
    private final MasaDetayViewModel viewModel;
    private final UrunViewModel urunVM;

    private List<Urun> tumUrunListesi = new ArrayList<>();
    private List<Urun> guncelUrunListesi = new ArrayList<>();
    private List<Kategori> kategoriListesi = new ArrayList<>();

    private final UrunRVAdapter urunAdapter;
    private final RecyclerView recyclerView;
    private final TextView bosMesaj;
    private final ListView kategoriListView;

    public UrunlerLayout(Context context, MasaDetayViewModel viewModel, UrunViewModel urunVM) {
        this.context = context;
        this.viewModel = viewModel;
        this.urunVM = urunVM;

        urunAdapter = new UrunRVAdapter(context, guncelUrunListesi, viewModel);

        recyclerView = new RecyclerView(context);
        recyclerView.setLayoutManager(new GridLayoutManager(context, 3));
        recyclerView.setAdapter(urunAdapter);
        ViewUtils.sizeBelirle(recyclerView, SizeType.MATCH_PARENT, SizeType.MATCH_PARENT);

        bosMesaj = new TextView(context);
        bosMesaj.setText("Seçilen kategoriye ait ürün bulunamadı.");
        bosMesaj.setTextSize(18f);
        bosMesaj.setGravity(Gravity.CENTER);
        bosMesaj.setTextColor(Color.GRAY);
        bosMesaj.setVisibility(View.GONE);
        ViewUtils.sizeBelirle(bosMesaj, SizeType.MATCH_PARENT, SizeType.MATCH_PARENT);

        kategoriListView = new ListView(context);
        ViewUtils.sizeBelirle(kategoriListView, SizeType.WRAP_CONTENT, SizeType.MATCH_PARENT);
    }

    public ViewGroup show() {
        LinearLayout rootLayout = new LinearLayout(context);
        rootLayout.setOrientation(LinearLayout.HORIZONTAL);

        kategoriListView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewUtils.dpToPx(150, context),
                LinearLayout.LayoutParams.MATCH_PARENT
        ));

        FrameLayout frame = new FrameLayout(context);
        frame.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
        ));
        frame.addView(recyclerView);
        frame.addView(bosMesaj);

        rootLayout.addView(kategoriListView);
        rootLayout.addView(frame);

        return rootLayout;
    }

    public void setUrunListesi(List<Urun> yeniListe) {
        tumUrunListesi = new ArrayList<>(yeniListe);
        guncelUrunListesi.clear();
        guncelUrunListesi.addAll(tumUrunListesi);
        urunAdapter.notifyDataSetChanged();
        bosMesaj.setVisibility(guncelUrunListesi.isEmpty() ? View.VISIBLE : View.GONE);
    }

    public void setKategoriListesi(List<Kategori> yeniKategoriler) {
        kategoriListesi = yeniKategoriler;

        List<String> kategoriAdlari = new ArrayList<>();
        kategoriAdlari.add("Tümü");
        for (Kategori kategori : kategoriListesi) {
            kategoriAdlari.add(kategori.getKategori_ad());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_1, kategoriAdlari);
        kategoriListView.setAdapter(adapter);

        kategoriListView.setOnItemClickListener((parent, view, position, id) -> {
            List<Urun> filtreliListe;
            if (position == 0) {
                filtreliListe = tumUrunListesi;
            } else {
                Kategori secilenKategori = kategoriListesi.get(position - 1);
                filtreliListe = new ArrayList<>();
                for (Urun u : tumUrunListesi) {
                    if (u.getUrunKategori().getId() == secilenKategori.getId()) {
                        filtreliListe.add(u);
                    }
                }
            }

            guncelUrunListesi.clear();
            guncelUrunListesi.addAll(filtreliListe);
            urunAdapter.notifyDataSetChanged();
            bosMesaj.setVisibility(filtreliListe.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }
}

