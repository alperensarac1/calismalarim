package com.example.onlinetaksijava.ui.auth;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.onlinetaksijava.data.local.SessionManager;
import com.example.onlinetaksijava.data.remote.api.ApiClient;
import com.example.onlinetaksijava.data.remote.api.ApiService;
import com.example.onlinetaksijava.data.remote.model.AuthResponse;
import com.example.onlinetaksijava.data.remote.model.RegisterRequest;
import com.example.onlinetaksijava.databinding.ActivityRegisterBinding;
import com.example.onlinetaksijava.ui.home.CustomerHomeActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = ApiClient.create(this);
        sessionManager = new SessionManager(this);

        binding.btnRegister.setOnClickListener(v -> registerCustomer());
        binding.tvGoLogin.setOnClickListener(v -> finish());
    }

    private void registerCustomer() {
        String fullName = binding.etFullName.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (fullName.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Ad soyad, telefon ve şifre zorunlu", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnRegister.setEnabled(false);
        binding.btnRegister.setText("Kayıt yapılıyor...");

        RegisterRequest request = new RegisterRequest(
                fullName,
                phone,
                email.isEmpty() ? null : email,
                password,
                "customer"
        );

        apiService.register(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                binding.btnRegister.setEnabled(true);
                binding.btnRegister.setText("Kayıt Ol");

                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse body = response.body();

                    sessionManager.saveAuth(
                            body.getAccess_token(),
                            body.getUser_id(),
                            body.getFull_name(),
                            body.getRole()
                    );

                    Toast.makeText(RegisterActivity.this, "Kayıt başarılı", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, CustomerHomeActivity.class));
                    finishAffinity();
                } else {
                    Toast.makeText(RegisterActivity.this, "Kayıt başarısız", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                binding.btnRegister.setEnabled(true);
                binding.btnRegister.setText("Kayıt Ol");
                Toast.makeText(RegisterActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
