package com.example.adisyonuygulamajava.masadetay;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.adisyonuygulamajava.model.Masa;
import com.example.adisyonuygulamajava.model.MasaUrun;
import com.example.adisyonuygulamajava.utils.SizeType;
import com.example.adisyonuygulamajava.utils.ViewUtils;
import com.example.adisyonuygulamajava.viewmodel.MasaDetayViewModel;

import java.util.ArrayList;
import java.util.List;

public class MasaAdisyonLayout {

    private final Context context;
    private final Masa masa;
    private final MasaDetayViewModel viewModel;

    private ListView urunListView;
    private List<MasaUrun> urunListesi = new ArrayList<>();
    private TextView toplamFiyatTextView;

    public MasaAdisyonLayout(Context context, Masa masa, MasaDetayViewModel viewModel) {
        this.context = context;
        this.masa = masa;
        this.viewModel = viewModel;
    }

    public LinearLayout show() {
        urunListView = masaUrunListesi();

        viewModel.getUrunler().observeForever(liste -> {
            urunListesi = liste;
            BaseAdapter adapter = (BaseAdapter) urunListView.getAdapter();
            if (adapter != null) adapter.notifyDataSetChanged();

            float toplam = 0f;
            for (MasaUrun u : liste) {
                toplam += u.getToplam_fiyat();
            }
            guncelleToplamFiyat(toplam);
        });

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(urunListView);
        layout.addView(masaFiyatLayout());
        return layout;
    }

    public void guncelleToplamFiyat(float fiyat) {
        toplamFiyatTextView.setText(String.format("%.2f TL", fiyat));
    }

    private ListView masaUrunListesi() {
        ListView listView = new ListView(context);
        listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return urunListesi.size();
            }

            @Override
            public Object getItem(int position) {
                return urunListesi.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                MasaUrun urun = urunListesi.get(position);
                TextView tv = new TextView(context);
                tv.setText(urun.getUrun_ad() + " (" + urun.getAdet() + ")");
                tv.setTextSize(16f);
                return tv;
            }
        });
        return listView;
    }

    private LinearLayout masaFiyatLayout() {
        toplamFiyatTextView = new TextView(context);
        toplamFiyatTextView.setTextSize(20f);
        toplamFiyatTextView.setTextColor(Color.GRAY);
        toplamFiyatTextView.setGravity(Gravity.START);
        ViewUtils.sizeBelirle(toplamFiyatTextView, SizeType.MATCH_PARENT, SizeType.MATCH_PARENT);

        Button button = new Button(context);
        button.setText("Ödeme Al");
        button.setBackgroundColor(Color.BLUE);
        button.setOnClickListener(v -> {
            viewModel.odemeAl(() -> {
                Toast.makeText(context, "Ödeme alındı", Toast.LENGTH_SHORT).show();
                viewModel.yukleTumVeriler();
            });
        });

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(20, 20, 20, 20);
        layout.addView(toplamFiyatTextView);
        layout.addView(button);

        return layout;
    }
}
