package com.example.qrkodolusturucujava.view;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.qrkodolusturucujava.R;
import com.example.qrkodolusturucujava.databinding.FragmentUrunlerimizBinding;
import com.example.qrkodolusturucujava.model.UrunKategori;
import com.example.qrkodolusturucujava.viewmodel.UrunlerimizVM;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class UrunlerimizFragment extends Fragment {

    private FragmentUrunlerimizBinding binding;
    private UrunlerimizVM urunlerimizVM;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentUrunlerimizBinding.inflate(inflater, container, false);
        urunlerimizVM = new ViewModelProvider(this).get(UrunlerimizVM.class);

        urunlerimizVM.recyclerviewAyarla(UrunKategori.ICECEKLER, binding.rvIcecekler, requireActivity());
        urunlerimizVM.recyclerviewAyarla(UrunKategori.ATISTIRMALIKLAR, binding.rvAtistirmaliklar, requireActivity());
        urunlerimizVM.indirimliRecyclerAyarla(binding.rvKampanyalar, requireActivity());

        return binding.getRoot();
    }
}
