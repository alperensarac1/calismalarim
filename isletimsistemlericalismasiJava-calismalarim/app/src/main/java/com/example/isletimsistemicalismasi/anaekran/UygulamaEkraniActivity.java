package com.example.isletimsistemicalismasi.anaekran;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.os.Bundle;

import com.example.isletimsistemicalismasi.R;
import com.example.isletimsistemicalismasi.anaekran.adapter.AnaekranRVAdapter;
import com.example.isletimsistemicalismasi.anaekran.entity.Const;
import com.example.isletimsistemicalismasi.anaekran.model.UygulamalarModel;
import com.example.isletimsistemicalismasi.databinding.ActivityUygulamaEkraniBinding;

import java.util.ArrayList;

public class UygulamaEkraniActivity extends AppCompatActivity  {
    private ActivityUygulamaEkraniBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        binding = ActivityUygulamaEkraniBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


    }
}