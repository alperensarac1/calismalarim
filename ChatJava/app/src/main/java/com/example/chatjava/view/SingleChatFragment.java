package com.example.chatjava.view;

import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chatjava.R;
import com.example.chatjava.adapter.MesajlarRVAdapter;
import com.example.chatjava.databinding.FragmentSingleChatBinding;
import com.example.chatjava.util.AppConfig;
import com.example.chatjava.viewmodel.MesajlarViewModel;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.io.InputStream;

public class SingleChatFragment extends Fragment {

    private FragmentSingleChatBinding binding;
    private MesajlarViewModel viewModel;
    private MesajlarRVAdapter adapter;

    private int aliciId = -1;
    private String aliciAd = "";

    private String secilenResimBase64 = null;

    private final ActivityResultLauncher<String> resimSecLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    binding.ivOnizleme.setImageURI(uri);
                    binding.ivOnizleme.setVisibility(View.VISIBLE);
                    secilenResimBase64 = uriToBase64(uri);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentSingleChatBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(MesajlarViewModel.class);

        if (getArguments() != null) {
            aliciId = getArguments().getInt("alici_id", -1);
            aliciAd = getArguments().getString("alici_ad", "");
        }

        requireActivity().setTitle(aliciAd);

        adapter = new MesajlarRVAdapter(java.util.Collections.emptyList(), mesaj -> {});
        binding.rvMesajlar.setAdapter(adapter);
        binding.rvMesajlar.setLayoutManager(new LinearLayoutManager(requireContext()));

        observeViewModel();
        viewModel.mesajlariYuklePeriyodik(AppConfig.kullaniciId, aliciId);

        binding.btnResimSec.setOnClickListener(v -> {
            resimSecLauncher.launch("image/*");
        });

        binding.btnGonder.setOnClickListener(v -> {
            String mesajText = binding.etMesaj.getText().toString().trim();

            if (mesajText.isEmpty() && secilenResimBase64 == null) {
                Toast.makeText(requireContext(), "Boş mesaj gönderilemez", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.mesajGonder(AppConfig.kullaniciId, aliciId, mesajText, secilenResimBase64);

            binding.etMesaj.setText("");
            binding.ivOnizleme.setImageDrawable(null);
            binding.ivOnizleme.setVisibility(View.GONE);
            secilenResimBase64 = null;
        });

        return binding.getRoot();
    }

    private void observeViewModel() {
        viewModel.getMesajlar().observe(getViewLifecycleOwner(), liste -> {
            adapter = new MesajlarRVAdapter(liste, mesaj -> {});
            binding.rvMesajlar.setAdapter(adapter);
            binding.rvMesajlar.scrollToPosition(liste.size() - 1);
        });

        viewModel.getHataMesaji().observe(getViewLifecycleOwner(), hata -> {
            if (hata != null) {
                Toast.makeText(requireContext(), hata, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String uriToBase64(Uri uri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            byte[] bytes = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bytes = inputStream != null ? inputStream.readAllBytes() : null;
            }
            if (inputStream != null) inputStream.close();
            return bytes != null ? Base64.encodeToString(bytes, Base64.NO_WRAP) : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
