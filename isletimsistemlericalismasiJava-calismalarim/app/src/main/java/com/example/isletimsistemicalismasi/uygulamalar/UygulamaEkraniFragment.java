package com.example.isletimsistemicalismasi.uygulamalar;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.isletimsistemicalismasi.anaekran.adapter.AnaekranRVAdapter;
import com.example.isletimsistemicalismasi.anaekran.entity.Const;
import com.example.isletimsistemicalismasi.anaekran.model.UygulamalarModel;
import com.example.isletimsistemicalismasi.databinding.FragmentUygulamaEkraniBinding;

import java.util.ArrayList;


public class UygulamaEkraniFragment extends Fragment {

    FragmentUygulamaEkraniBinding binding;
    Const uygulamalar;
    GridLayoutManager layoutManager;

    AnaekranRVAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentUygulamaEkraniBinding.inflate(inflater,container,false);





        adapter = new AnaekranRVAdapter(requireContext(),uygulamalar);

        layoutManager = new GridLayoutManager(requireContext(),3);

        binding.uygulamalarRV.setHasFixedSize(true);
        binding.uygulamalarRV.setLayoutManager(layoutManager);
        binding.uygulamalarRV.setAdapter(adapter);
        return binding.getRoot();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        uygulamalar = new Const(getActivity());
        super.onCreate(savedInstanceState);
    }
}