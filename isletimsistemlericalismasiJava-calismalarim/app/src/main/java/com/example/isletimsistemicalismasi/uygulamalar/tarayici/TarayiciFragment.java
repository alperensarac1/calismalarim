package com.example.isletimsistemicalismasi.uygulamalar.tarayici;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;

import com.example.isletimsistemicalismasi.R;
import com.example.isletimsistemicalismasi.databinding.FragmentTarayiciBinding;

public class TarayiciFragment extends Fragment {

    FragmentTarayiciBinding binding;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentTarayiciBinding.inflate(inflater,container,false);


        binding.webView.getSettings().setJavaScriptEnabled(true);


        binding.btnArama.setOnClickListener(view->{
            String aramaKelimesi = binding.etArama.getText().toString().replace(" ","+");
            binding.webView.loadUrl("https://www.google.com/search?q=" + aramaKelimesi);
        });


        return binding.getRoot();
    }



}