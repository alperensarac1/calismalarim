package com.example.adisyonuygulamajava.anaekran;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.example.adisyonuygulamajava.model.Masa;
import com.example.adisyonuygulamajava.viewmodel.UseCases;

public class MasaCard {

    public interface OnMasaClickListener {
        void onClick(Masa masa);
    }

    private final CardView cardView;
    private final TextView masaAdiTV;
    private final TextView masaFiyatTV;
    private final TextView masaSureTV;
    private Masa masa;

    public MasaCard(Context context, OnMasaClickListener listener) {
        cardView = new CardView(context);
        masaAdiTV = new TextView(context);
        masaFiyatTV = new TextView(context);
        masaSureTV = new TextView(context);

        // CardView özellikleri
        cardView.setRadius(24f);
        cardView.setCardElevation(8f);
        cardView.setUseCompatPadding(true);

        ViewGroup.MarginLayoutParams layoutParams = new ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(20, 20, 20, 20);
        cardView.setLayoutParams(layoutParams);

        // Dikey LinearLayout
        LinearLayout verticalLayout = new LinearLayout(context);
        verticalLayout.setOrientation(LinearLayout.VERTICAL);
        verticalLayout.setPadding(30, 30, 30, 30);

        masaAdiTV.setTextSize(20f);
        masaAdiTV.setTextColor(Color.BLACK);

        masaFiyatTV.setTextSize(18f);
        masaFiyatTV.setTextColor(Color.DKGRAY);

        masaSureTV.setTextSize(16f);
        masaSureTV.setTextColor(Color.GRAY);

        verticalLayout.addView(masaAdiTV);
        verticalLayout.addView(masaFiyatTV);
        verticalLayout.addView(masaSureTV);

        cardView.addView(verticalLayout);

        // Tıklama olayı
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (masa != null) {
                    listener.onClick(masa);
                }
            }
        });
    }

    public CardView getCardView() {
        return cardView;
    }

    public void setData(Masa masa) {
        this.masa = masa;
        masaAdiTV.setText("Masa " + masa.getId());
        masaFiyatTV.setText("Tutar: " + UseCases.fiyatYaz(masa.getToplam_fiyat()));
        masaSureTV.setText("Süre: " + masa.getSure());
    }

    public void setBackgroundColor(int color) {
        cardView.setCardBackgroundColor(color);
    }

    // Kotlin'deki fiyatYaz uzantısına karşılık Java fonksiyonu
    private String fiyatYaz(float fiyat) {
        return String.format("%.2f ₺", fiyat);
    }
}

