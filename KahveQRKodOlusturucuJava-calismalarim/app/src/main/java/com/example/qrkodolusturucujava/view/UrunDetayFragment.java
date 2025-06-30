package com.example.qrkodolusturucujava.view;

import android.graphics.Paint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.qrkodolusturucujava.R;
import com.example.qrkodolusturucujava.databinding.FragmentUrunDetayBinding;
import com.example.qrkodolusturucujava.model.Urun;
import com.squareup.picasso.Picasso;

import dagger.hilt.android.AndroidEntryPoint;


@AndroidEntryPoint
public class UrunDetayFragment extends Fragment {

    private FragmentUrunDetayBinding binding;
    private Urun gelenUrun;

    public UrunDetayFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUrunDetayBinding.inflate(inflater, container, false);

        // navArgs kullanımı
        if (getArguments() != null) {
            UrunDetayFragmentArgs args = UrunDetayFragmentArgs.fromBundle(getArguments());
            gelenUrun = args.getUrun();

            // Görsel yükleme
            Picasso.get()
                    .load(gelenUrun.getUrunResim())
                    .placeholder(R.drawable.kahve)
                    .error(R.drawable.kahve)
                    .into(binding.imgUrun);

            binding.tvUrunAdi.setText(gelenUrun.getUrunAd());
            binding.tvUrunAciklama.setText(gelenUrun.getUrunAciklama());
            binding.tvUrunFiyat.setText(gelenUrun.getUrunFiyat() + " TL");

            if (gelenUrun.getUrunIndirim() == 1) {
                binding.tvUrunFiyat.setPaintFlags(binding.tvUrunFiyat.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                binding.tvIndirimliFiyat.setText(gelenUrun.getUrunIndirimliFiyat() + " TL");
                binding.tvIndirimliFiyat.setVisibility(View.VISIBLE);
            }

            binding.imgGeri.setOnClickListener(v -> {
                Navigation.findNavController(v).popBackStack();
            });
        }

        return binding.getRoot();
    }
}
