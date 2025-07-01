package com.example.adisyonuygulamajava.anaekran;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adisyonuygulamajava.masadetay.MasaDetayFragment;
import com.example.adisyonuygulamajava.model.Masa;
import com.example.adisyonuygulamajava.utils.ViewUtils;
import com.example.adisyonuygulamajava.viewmodel.MasaDetayViewModel;

import java.util.List;

public class MainLayout {

    public interface OnMasaClickListener {
        void onMasaClick(Masa masa);
    }

    private final Context context;
    private List<Masa> masaListesi;
    private final FragmentManager fragmentManager;
    private final MasaDetayViewModel viewModel;
    private final OnMasaClickListener onMasaClick;

    public MainLayout(Context context, List<Masa> masaListesi, FragmentManager fragmentManager,
                      MasaDetayViewModel viewModel, OnMasaClickListener onMasaClick) {
        this.context = context;
        this.masaListesi = masaListesi;
        this.fragmentManager = fragmentManager;
        this.viewModel = viewModel;
        this.onMasaClick = onMasaClick;
    }

    public LinearLayout linearLayout() {
        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setBackgroundColor(Color.parseColor("#FFBABA"));
        mainLayout.setOrientation(LinearLayout.HORIZONTAL);
        ViewUtils.paddingEkle(mainLayout, 10, 10, 10, 10);

        // === MasaOzetLayout ===
        LinearLayout masaOzetLayout = new MasaOzetLayout(
                context,
                masaListesi,
                viewModel,
                new MasaOzetLayout.OnMasaDetayClickListener() {
                    @Override
                    public void onMasaDetayClick(Masa masa) {
                        ViewGroup parent = (ViewGroup) mainLayout.getParent();
                        int containerId = (parent != null) ? parent.getId() : View.generateViewId();

                        fragmentManager.beginTransaction()
                                .replace(containerId, MasaDetayFragment.newInstance(masa.getId()))
                                .addToBackStack(null)
                                .commit();
                    }
                }
        ).linearLayout();

        masaOzetLayout.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.MATCH_PARENT, 1f
        ));

        // === MasalarLayout ===
        RecyclerView masalarLayout = new MasalarLayout(context, masaListesi, new MasalarLayout.OnMasaClickListener() {
            @Override
            public void onMasaClick(Masa masa) {
                onMasaClick.onMasaClick(masa);
            }
        }).recyclerView();

        masalarLayout.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.MATCH_PARENT, 2f
        ));

        mainLayout.addView(masaOzetLayout);
        mainLayout.addView(masalarLayout);

        return mainLayout;
    }
}

