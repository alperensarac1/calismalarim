package com.example.qrkodolusturucujava;

import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.qrkodolusturucujava.databinding.ActivityMainBinding;
import com.example.qrkodolusturucujava.viewmodel.MainVM;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MainVM viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MainVM.class);

        observeViewModel();

        binding.btnQRKod.setOnClickListener(v -> {
            binding.btnQRKod.setBackgroundColor(getResources().getColor(R.color.btnSeciliBackground, getTheme()));
            binding.btnUrunlerimiz.setBackgroundColor(Color.TRANSPARENT);
            toQRKodFragment();
        });

        binding.btnUrunlerimiz.setOnClickListener(v -> {
            binding.btnUrunlerimiz.setBackgroundColor(getResources().getColor(R.color.btnSeciliBackground, getTheme()));
            binding.btnQRKod.setBackgroundColor(Color.TRANSPARENT);
            toUrunlerFragment();
        });

        if (!viewModel.checkTelefonNumarasi()) {
            showInputDialog();
        }

        viewModel.getTelefonNumarasiLiveData().observe(this, numara -> {
            Log.e("MainVMActivity", "Gelen Telefon Numarası: " + numara);
            if (numara == null) {
                showInputDialog();
            } else {
                viewModel.kullaniciEkle(numara);
            }
        });
    }

    private void observeViewModel() {
        viewModel.getHediyeKahveLiveData().observe(this, hediyeKahve ->
                binding.tvHediyeKahve.setText("Hediye Kahve: " + hediyeKahve));

        viewModel.getKahveSayisiLiveData().observe(this, kahveSayisi ->
                binding.tvKullanimlar.setText("Kullanımlar: " + kahveSayisi + "/5"));
    }

    private void showInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Bilgi Girişi");

        EditText input = new EditText(this);
        input.setHint("Lütfen telefon numaranızı giriniz");
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Tamam", (dialog, which) -> {
            String userInput = input.getText().toString().trim();
            if (!userInput.isEmpty()) {
                viewModel.numarayiKaydet(userInput);
                Toast.makeText(this, "Numara kaydedildi!", Toast.LENGTH_SHORT).show();
                toQRKodFragment();
            }
            dialog.dismiss();
        });

        builder.setNegativeButton("İptal", (dialog, which) -> {
            finish();
            dialog.cancel();
        });

        builder.show();
    }

    private void toQRKodFragment() {
        View navHost = findViewById(R.id.fragmentContainerView);
        NavController navController = Navigation.findNavController(navHost);
        navController.navigate(R.id.QRKodOlusturFragment);
    }

    private void toUrunlerFragment() {
        View navHost = findViewById(R.id.fragmentContainerView);
        NavController navController = Navigation.findNavController(navHost);
        navController.navigate(R.id.urunlerimizFragment2);
    }
}
