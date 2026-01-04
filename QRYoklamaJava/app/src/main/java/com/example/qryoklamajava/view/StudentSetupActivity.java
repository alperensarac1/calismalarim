package com.example.qryoklamajava.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.qryoklamajava.R;
import com.example.qryoklamajava.data.Prefs;
import com.example.qryoklamajava.databinding.ActivityStudentSetupBinding;

public class StudentSetupActivity extends AppCompatActivity {

    ActivityStudentSetupBinding binding;
    Prefs prefs;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        binding = ActivityStudentSetupBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        prefs = new Prefs(this);

        String savedNo = prefs.getStudentNo();
        if (savedNo != null && !savedNo.isEmpty()) {
            startActivity(new Intent(this, ScanActivity.class));
            finish();
            return;
        }

        binding.btnSave.setOnClickListener(v -> {
            String no = binding.etNo.getText().toString().trim();
            if (no.isEmpty()) {
                binding.etNo.setError("Öğrenci numarası gerekli");
                return;
            }
            prefs.setStudentNo(no);
            startActivity(new Intent(this, ScanActivity.class));
            finish();
        });
    }
}

