package com.example.onlinetaksijava.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.onlinetaksijava.data.local.SessionManager;
import com.example.onlinetaksijava.data.remote.api.ApiClient;
import com.example.onlinetaksijava.data.remote.api.ApiService;
import com.example.onlinetaksijava.data.remote.model.AuthResponse;
import com.example.onlinetaksijava.data.remote.model.LoginRequest;
import com.example.onlinetaksijava.databinding.ActivityLoginBinding;
import com.example.onlinetaksijava.ui.driver.DriverHomeActivity;
import com.example.onlinetaksijava.ui.home.CustomerHomeActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = ApiClient.create(this);
        sessionManager = new SessionManager(this);

        binding.btnLogin.setOnClickListener(v -> login());
        binding.tvGoRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );
    }

    private void login() {
        String phone = binding.etPhone.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Telefon ve şifre zorunlu", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnLogin.setEnabled(false);
        binding.btnLogin.setText("Giriş yapılıyor...");

        apiService.login(new LoginRequest(phone, password)).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                binding.btnLogin.setEnabled(true);
                binding.btnLogin.setText("Giriş Yap");

                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse body = response.body();

                    sessionManager.saveAuth(
                            body.getAccess_token(),
                            body.getUser_id(),
                            body.getFull_name(),
                            body.getRole()
                    );

                    Toast.makeText(LoginActivity.this, "Giriş başarılı", Toast.LENGTH_SHORT).show();

                    if ("driver".equals(body.getRole())) {
                        startActivity(new Intent(LoginActivity.this, DriverHomeActivity.class));
                    } else {
                        startActivity(new Intent(LoginActivity.this, CustomerHomeActivity.class));
                    }
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Giriş başarısız", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                binding.btnLogin.setEnabled(true);
                binding.btnLogin.setText("Giriş Yap");
                Toast.makeText(LoginActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
