package com.example.adisyonuygulamajava.masadetay;

import android.content.Context;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.adisyonuygulamajava.R;
import com.example.adisyonuygulamajava.model.Masa;
import com.example.adisyonuygulamajava.utils.SizeType;
import com.example.adisyonuygulamajava.utils.ViewUtils;

public class MasaDetayHeaderLayout {

    private final Context context;
    private Masa masa;
    private TextView masaAdiTextView;

    public MasaDetayHeaderLayout(Context context, Masa masa) {
        this.context = context;
        this.masa = masa;
    }

    public LinearLayout show(Runnable onBackPressed) {
        Button geriButton = new Button(context);
        geriButton.setOnClickListener(v -> onBackPressed.run());
        geriButton.setBackgroundResource(R.drawable.ic_remove);
        ViewUtils.sizeBelirle(geriButton, SizeType.WRAP_CONTENT, SizeType.WRAP_CONTENT);

        masaAdiTextView = new TextView(context);
        masaAdiTextView.setText("Masa " + masa.getId());
        masaAdiTextView.setTextSize(20f);

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        ViewUtils.sizeBelirle(layout, SizeType.MATCH_PARENT, SizeType.WRAP_CONTENT);
        ViewUtils.paddingEkleVertical(layout, 50);

        layout.addView(geriButton);
        layout.addView(masaAdiTextView);

        return layout;
    }

    public void guncelleMasa(Masa yeniMasa) {
        this.masa = yeniMasa;
        masaAdiTextView.setText("Masa " + masa.getId());
    }
}

