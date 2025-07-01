package com.example.adisyonuygulamajava.anaekran;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.adisyonuygulamajava.model.Masa;
import com.example.adisyonuygulamajava.utils.SizeType;
import com.example.adisyonuygulamajava.utils.ViewUtils;
import com.example.adisyonuygulamajava.viewmodel.MasaDetayViewModel;
import com.example.adisyonuygulamajava.viewmodel.UseCases;

import java.util.ArrayList;
import java.util.List;

public class MasaOzetLayout {

    public interface OnMasaDetayClickListener {
        void onMasaDetayClick(Masa masa);
    }

    private final Context context;
    private final List<Masa> masaListesi;
    private final MasaDetayViewModel viewModel;
    private final OnMasaDetayClickListener onMasaDetayTikla;

    public MasaOzetLayout(Context context, List<Masa> masaListesi,
                          MasaDetayViewModel viewModel, OnMasaDetayClickListener onMasaDetayTikla) {
        this.context = context;
        this.masaListesi = masaListesi;
        this.viewModel = viewModel;
        this.onMasaDetayTikla = onMasaDetayTikla;
    }

    public LinearLayout linearLayout() {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);

        layout.addView(aciklamaTextView());
        layout.addView(acikMasalarListView());

        return layout;
    }

    private TextView aciklamaTextView() {
        int acikMasaSayisi = 0;
        for (Masa m : masaListesi) {
            if (m.getAcikMi() == 1) acikMasaSayisi++;
        }

        TextView textView = new TextView(context);
        textView.setText(acikMasaSayisi + " adet masa açık.");
        textView.setTextSize(20f);
        textView.setTextColor(Color.RED);
        textView.setGravity(Gravity.CENTER);

        ViewUtils.marginEkle(textView, 20, 0, 0, 0);
        ViewUtils.sizeBelirle(textView, SizeType.MATCH_PARENT, SizeType.WRAP_CONTENT);

        return textView;
    }

    private ListView acikMasalarListView() {
        List<Masa> acikMasalar = new ArrayList<>();
        List<String> masaAdlari = new ArrayList<>();

        for (Masa masa : masaListesi) {
            if (masa.getAcikMi() == 1) {
                acikMasalar.add(masa);
                masaAdlari.add("Masa " + masa.getId() + "\t" + UseCases.fiyatYaz(masa.getToplam_fiyat()));
            }
        }

        ListView listView = new ListView(context);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_1, masaAdlari);
        listView.setAdapter(adapter);

        ViewUtils.marginEkle(listView, 20, 0, 0, 0);
        ViewUtils.sizeBelirle(listView, SizeType.MATCH_PARENT, SizeType.MATCH_PARENT);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Masa secilenMasa = acikMasalar.get(position);
            gosterMasaSecenekleri(secilenMasa);
        });

        return listView;
    }

    private void gosterMasaSecenekleri(Masa masa) {
        new AlertDialog.Builder(context)
                .setTitle("Masa " + masa.getId())
                .setItems(new CharSequence[]{"Ürün Ekle", "Ödeme Al"}, (dialog, which) -> {
                    if (which == 0) {
                        onMasaDetayTikla.onMasaDetayClick(masa);
                    } else if (which == 1) {
                        viewModel.odemeAl(() ->
                                Toast.makeText(context, "Ödeme alındı", Toast.LENGTH_SHORT).show()
                        );
                    }
                    dialog.dismiss();
                })
                .show();
    }
}
