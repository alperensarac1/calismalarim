package com.example.adisyonuygulamajava.masadetay;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;

import com.example.adisyonuygulamajava.model.Masa;
import com.example.adisyonuygulamajava.viewmodel.MasaDetayViewModel;
import com.example.adisyonuygulamajava.viewmodel.UrunViewModel;

public class MasaDetayLayout {

    private final Context context;
    private Masa masa;
    private final UrunViewModel urunVM;

    public final MasaAdisyonLayout masaAdisyonLayout;
    private final MasaDetayHeaderLayout masaDetayHeaderLayout;
    public final UrunlerLayout urunlerLayout;

    public MasaDetayLayout(Context context, Masa masa, MasaDetayViewModel viewModel, UrunViewModel urunVM) {
        this.context = context;
        this.masa = masa;
        this.urunVM = urunVM;
        this.masaAdisyonLayout = new MasaAdisyonLayout(context, masa, viewModel);
        this.masaDetayHeaderLayout = new MasaDetayHeaderLayout(context, masa);
        this.urunlerLayout = new UrunlerLayout(context, viewModel, urunVM);
    }

    public LinearLayout show() {
        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        mainLayout.setBackgroundColor(Color.parseColor("#FFBABA"));
        mainLayout.addView(createHeaderView());
        mainLayout.addView(createBodyView());
        return mainLayout;
    }

    private View createHeaderView() {
        LinearLayout header = masaDetayHeaderLayout.show(() -> {
            // default: do nothing
        });
        header.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(60)
        ));
        return header;
    }

    private View createBodyView() {
        LinearLayout body = new LinearLayout(context);
        body.setOrientation(LinearLayout.HORIZONTAL);
        body.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
        ));

        View adisyonView = masaAdisyonLayout.show();
        adisyonView.setLayoutParams(new LinearLayout.LayoutParams(
                dpToPx(200),
                LinearLayout.LayoutParams.MATCH_PARENT
        ));

        View urunlerView = urunlerLayout.show();
        urunlerView.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
        ));

        body.addView(adisyonView);
        body.addView(urunlerView);

        return body;
    }

    private int dpToPx(int dp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale);
    }

    public void guncelleMasa(Masa masa) {
        this.masa = masa;
        masaDetayHeaderLayout.guncelleMasa(masa);
    }
}