package com.example.kargopaylasimjava.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.kargopaylasimjava.R;
import com.example.kargopaylasimjava.di.AppContainer;
import com.example.kargopaylasimjava.dto.AuthDtos;
import com.example.kargopaylasimjava.factory.VMFactories;
import com.example.kargopaylasimjava.model.UiState;
import com.example.kargopaylasimjava.viewmodel.AuthViewModel;

public class RegisterActivity extends ComponentActivity {

    private AuthViewModel vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        AppContainer container = new AppContainer(getApplicationContext());
        vm = new ViewModelProvider(this, new VMFactories.AuthVmFactory(container.repo, container.tokenStore))
                .get(AuthViewModel.class);

        EditText etFirst = findViewById(R.id.etFirst);
        EditText etLast  = findViewById(R.id.etLast);
        EditText etPhone = findViewById(R.id.etPhone);
        EditText etTc    = findViewById(R.id.etTc);
        EditText etPass  = findViewById(R.id.etPassword);

        EditText etAddrTitle = findViewById(R.id.etAddressTitle);
        EditText etCity      = findViewById(R.id.etCity);
        EditText etDistrict  = findViewById(R.id.etDistrict);
        EditText etNeighborhood = findViewById(R.id.etNeighborhood);
        EditText etAddrLine  = findViewById(R.id.etAddressLine);
        EditText etPostal    = findViewById(R.id.etPostal);

        Button btn = findViewById(R.id.btnRegister);
        ProgressBar prog = findViewById(R.id.progress);

        btn.setOnClickListener(v -> {
            String neighborhood = etNeighborhood.getText().toString().trim();
            if (neighborhood.isEmpty()) neighborhood = null;

            String postal = etPostal.getText().toString().trim();
            if (postal.isEmpty()) postal = null;

            AuthDtos.RegisterReq req = new AuthDtos.RegisterReq(
                    etFirst.getText().toString().trim(),
                    etLast.getText().toString().trim(),
                    etPhone.getText().toString().trim(),
                    etTc.getText().toString().trim(),
                    etPass.getText().toString(),
                    etAddrTitle.getText().toString().trim(),
                    etCity.getText().toString().trim(),
                    etDistrict.getText().toString().trim(),
                    neighborhood,
                    etAddrLine.getText().toString().trim(),
                    postal
            );

            // Kotlin'de register(req) var; istersen auto-login için registerAndSetup(req) kullan
            vm.register(req);
        });

        vm.registerState.observe(this, st -> {
            if (st instanceof UiState.Loading) {
                prog.setVisibility(View.VISIBLE);
            } else if (st instanceof UiState.Success) {
                prog.setVisibility(View.GONE);
                Toast.makeText(this, "Kayıt başarılı", Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            } else if (st instanceof UiState.Error) {
                prog.setVisibility(View.GONE);
                Toast.makeText(this, ((UiState.Error<?>) st).message, Toast.LENGTH_LONG).show();
            } else {
                prog.setVisibility(View.GONE);
            }
        });
    }
}
