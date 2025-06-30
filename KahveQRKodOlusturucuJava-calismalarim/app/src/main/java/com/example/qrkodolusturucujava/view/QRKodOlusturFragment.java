package com.example.qrkodolusturucujava.view;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.qrkodolusturucujava.R;
import com.example.qrkodolusturucujava.databinding.FragmentQRKodOlusturBinding;
import com.example.qrkodolusturucujava.viewmodel.QRKodOlusturVM;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class QRKodOlusturFragment extends Fragment {

    private QRKodOlusturVM qrKodOlusturVM;
    private FragmentQRKodOlusturBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentQRKodOlusturBinding.inflate(inflater, container, false);
        qrKodOlusturVM = new ViewModelProvider(this).get(QRKodOlusturVM.class);

        qrKodOlusturVM.getDogrulamaKoduLiveData().observe(getViewLifecycleOwner(), yeniKod -> {
            Log.d("QRKodOlusturFragment", "Yeni doğrulama kodu alındı: " + yeniKod);
            qrKodOlusturVM.QRKodOlustur(yeniKod);
            qrKodOlusturVM.koduOlustur(yeniKod);
        });

        qrKodOlusturVM.getQrKodLiveData().observe(getViewLifecycleOwner(), bitmap -> {
            binding.idIVQrcode.setImageBitmap(bitmap);
        });

        qrKodOlusturVM.getKalanSureLiveData().observe(getViewLifecycleOwner(), kalanSure -> {
            binding.tvKalanSure.setText("Yenileme için kalan süre: " + kalanSure);
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        qrKodOlusturVM.yeniKodUret();
    }
}
