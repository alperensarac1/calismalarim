package com.example.adisyonuygulamajava.adapter;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adisyonuygulamajava.model.Urun;
import com.example.adisyonuygulamajava.utils.ViewUtils;
import com.example.adisyonuygulamajava.viewmodel.MasaDetayViewModel;
import com.example.adisyonuygulamajava.viewmodel.UseCases;
import com.squareup.picasso.Picasso;

import java.util.List;

public class UrunRVAdapter extends RecyclerView.Adapter<UrunRVAdapter.UrunViewHolder> {

    private final Context context;
    private final List<Urun> urunListesi;
    private final MasaDetayViewModel viewModel;

    public UrunRVAdapter(Context context, List<Urun> urunListesi, MasaDetayViewModel viewModel) {
        this.context = context;
        this.urunListesi = urunListesi;
        this.viewModel = viewModel;
    }

    public static class UrunViewHolder extends RecyclerView.ViewHolder {
        public final ImageView imageView;
        public final TextView nameTextView;
        public final TextView priceTextView;
        public final LinearLayout ekleCikarLayout;
        public final CardView rootView;

        public UrunViewHolder(ImageView imageView, TextView nameTextView, TextView priceTextView,
                              LinearLayout ekleCikarLayout, CardView rootView) {
            super(rootView);
            this.imageView = imageView;
            this.nameTextView = nameTextView;
            this.priceTextView = priceTextView;
            this.ekleCikarLayout = ekleCikarLayout;
            this.rootView = rootView;
        }
    }

    @Override
    public UrunViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ImageView imageView = new ImageView(context);
        ViewUtils.sizeBelirle(imageView, 250, 200);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        TextView nameTextView = new TextView(context);
        nameTextView.setTextSize(16f);

        TextView priceTextView = new TextView(context);
        priceTextView.setTextSize(14f);

        LinearLayout ekleCikarLayout = new LinearLayout(context);

        LinearLayout verticalLayout = new LinearLayout(context);
        verticalLayout.setOrientation(LinearLayout.VERTICAL);
        verticalLayout.setPadding(16, 16, 16, 16);
        verticalLayout.addView(imageView);
        verticalLayout.addView(nameTextView);
        verticalLayout.addView(priceTextView);
        verticalLayout.addView(ekleCikarLayout);

        CardView cardView = new CardView(context);
        cardView.setRadius(16f);
        cardView.setCardElevation(5f);
        cardView.addView(verticalLayout);

        return new UrunViewHolder(imageView, nameTextView, priceTextView, ekleCikarLayout, cardView);
    }

    @Override
    public void onBindViewHolder(UrunViewHolder holder, int position) {
        Urun urun = urunListesi.get(position);

        Picasso.get()
                .load(urun.getUrun_resim())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_delete)
                .into(holder.imageView);

        holder.nameTextView.setText(urun.getUrun_ad());
        holder.priceTextView.setText(UseCases.fiyatYaz(urun.getUrun_fiyat()) + " TL");

        holder.ekleCikarLayout.removeAllViews();

        Button btnEkle = new Button(context);
        btnEkle.setText("+");
        btnEkle.setOnClickListener(v -> {
            viewModel.urunEkle(urun.getId(), 1);
            urun.setUrun_adet(urun.getUrun_adet() + 1);
            viewModel.yukleTumVeriler();
            notifyDataSetChanged();
        });

        Button btnCikar = new Button(context);
        btnCikar.setText("-");
        btnCikar.setOnClickListener(v -> {
            if (urun.getUrun_adet() > 0) {
                viewModel.urunCikar(urun.getId());
                urun.setUrun_adet(urun.getUrun_adet() - 1);
                viewModel.yukleTumVeriler();
                notifyDataSetChanged();
            }
        });

        holder.ekleCikarLayout.setOrientation(LinearLayout.HORIZONTAL);
        holder.ekleCikarLayout.addView(btnEkle);
        holder.ekleCikarLayout.addView(btnCikar);
    }

    @Override
    public int getItemCount() {
        return urunListesi.size();
    }

    public void setData(List<Urun> yeniListe) {
        urunListesi.clear();
        urunListesi.addAll(yeniListe);
        notifyDataSetChanged();
    }
}

