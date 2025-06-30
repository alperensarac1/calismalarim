package com.example.qrkodolusturucujava.adapter;

import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrkodolusturucujava.R;
import com.example.qrkodolusturucujava.databinding.UrunItemBinding;
import com.example.qrkodolusturucujava.model.Urun;
import com.example.qrkodolusturucujava.view.UrunlerimizFragmentDirections;
import com.squareup.picasso.Picasso;

import java.util.List;

public class UrunAdapter extends RecyclerView.Adapter<UrunAdapter.UrunViewHolder> {

    private List<Urun> urunListesi;

    public UrunAdapter(List<Urun> urunListesi) {
        this.urunListesi = urunListesi;
    }

    @NonNull
    @Override
    public UrunViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        UrunItemBinding binding = UrunItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new UrunViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UrunViewHolder holder, int position) {
        holder.bind(urunListesi.get(position));
    }

    @Override
    public int getItemCount() {
        return urunListesi.size();
    }

    public class UrunViewHolder extends RecyclerView.ViewHolder {
        private UrunItemBinding binding;

        public UrunViewHolder(UrunItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Urun urun) {
            binding.tvUrunAdi.setText(urun.getUrunAd());
            binding.tvUrunFiyat.setText(urun.getUrunFiyat() + " TL");

            Picasso.get()
                    .load(urun.getUrunResim())
                    .placeholder(R.drawable.kahve)
                    .error(R.drawable.kahve)
                    .into(binding.imgUrun);

            binding.tvIndRMsizFiyat.setVisibility(View.INVISIBLE);

            if (urun.getUrunIndirim() == 1) {
                binding.tvIndRMsizFiyat.setText((int) urun.getUrunFiyat() + " TL");
                binding.tvUrunFiyat.setText((int) urun.getUrunIndirimliFiyat() + " TL");
                binding.tvIndRMsizFiyat.setPaintFlags(binding.tvIndRMsizFiyat.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                binding.tvIndRMsizFiyat.setVisibility(View.VISIBLE);
                binding.tvIndRMsizFiyat.setTextSize(10f);
                binding.tvUrunFiyat.setTextColor(Color.RED);
                binding.tvIndirimYuzdesi.setVisibility(View.VISIBLE);
                binding.tvIndirimYuzdesi.setText("%-" + indirimYuzdeHesapla(urun.getUrunFiyat(), urun.getUrunIndirimliFiyat()));
            }

            binding.cardUrun.setOnClickListener(v -> {
                UrunlerimizFragmentDirections.ToUrunDetay action = UrunlerimizFragmentDirections.toUrunDetay(urun);
                Navigation.findNavController(v).navigate(action);
            });
        }
    }

    // Eğer bu fonksiyon Kotlin'de başka bir yerdeyse, burada da Java versiyonunu yazman gerekir.
    private int indirimYuzdeHesapla(double orjFiyat, double indirimliFiyat) {
        double fark = orjFiyat - indirimliFiyat;
        return (int) ((fark / orjFiyat) * 100);
    }
}
