package com.example.kargopaylasimjava.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.kargopaylasimjava.R;
import com.example.kargopaylasimjava.di.AppContainer;
import com.example.kargopaylasimjava.factory.VMFactories;
import com.example.kargopaylasimjava.model.UiState;
import com.example.kargopaylasimjava.viewmodel.AuthViewModel;

public class LoginActivity extends ComponentActivity {

    private AuthViewModel vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        AppContainer container = new AppContainer(getApplicationContext());

        if (container.tokenStore.isLoggedIn()) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        vm = new ViewModelProvider(this, new VMFactories.AuthVmFactory(container.repo, container.tokenStore))
                .get(AuthViewModel.class);

        EditText etPhone = findViewById(R.id.etPhone);
        EditText etPass  = findViewById(R.id.etPassword);
        Button btn       = findViewById(R.id.btnLogin);
        ProgressBar prog = findViewById(R.id.progress);
        TextView tvReg   = findViewById(R.id.tvRegister);

        btn.setOnClickListener(v -> vm.login(etPhone.getText().toString().trim(),
                etPass.getText().toString()));

        tvReg.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));

        vm.loginState.observe(this, st -> {
            if (st instanceof UiState.Loading) {
                prog.setVisibility(View.VISIBLE);
            } else if (st instanceof UiState.Success) {
                prog.setVisibility(View.GONE);
                startActivity(new Intent(this, HomeActivity.class));
                finish();
            } else if (st instanceof UiState.Error) {
                prog.setVisibility(View.GONE);
                String msg = ((UiState.Error<?>) st).message;
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            } else {
                prog.setVisibility(View.GONE);
            }
        });
    }
}

