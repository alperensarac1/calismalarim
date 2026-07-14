package com.alperensarac.ebiletjava.ui.auth;


import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alperensarac.ebiletjava.R;
import com.alperensarac.ebiletjava.data.api.ApiClient;
import com.alperensarac.ebiletjava.data.model.ApiResponse;
import com.alperensarac.ebiletjava.data.model.User;
import com.alperensarac.ebiletjava.data.session.SessionManager;
import com.alperensarac.ebiletjava.ui.home.HomeActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
    RegisterActivity.java

    Kullanıcı kayıt ekranı.

    Bu versiyonda normal EditText kullanıyoruz.

    Görevi:
    1. Kullanıcıdan ad soyad, e-posta, telefon ve şifre almak
    2. Retrofit ile auth/register.php dosyasına istek atmak
    3. Backend'den gelen User bilgisini almak
    4. api_token değerini SharedPreferences içine kaydetmek
*/
public class RegisterActivity extends AppCompatActivity {

    /*
        Normal EditText alanlarımız.
    */
    private EditText etFullName;
    private EditText etEmail;
    private EditText etPhone;
    private EditText etPassword;

    private Button btnRegister;
    private TextView tvGoLogin;
    private TextView tvLoading;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        sessionManager = new SessionManager(this);

        initViews();

        /*
            Kayıt ol butonuna basıldığında çalışır.
        */
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });

        /*
            Giriş ekranına geri dön.
        */
        tvGoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    /*
        XML view'larını Java değişkenlerine bağlıyoruz.
    */
    private void initViews() {
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);

        btnRegister = findViewById(R.id.btnRegister);
        tvGoLogin = findViewById(R.id.tvGoLogin);
        tvLoading = findViewById(R.id.tvLoading);
    }

    /*
        Kullanıcı kayıt işlemi.
    */
    private void registerUser() {

        String fullName = etFullName.getText() == null
                ? ""
                : etFullName.getText().toString().trim();

        String email = etEmail.getText() == null
                ? ""
                : etEmail.getText().toString().trim();

        String phone = etPhone.getText() == null
                ? ""
                : etPhone.getText().toString().trim();

        String password = etPassword.getText() == null
                ? ""
                : etPassword.getText().toString().trim();

        /*
            Normal EditText hata mesajlarını temizliyoruz.
        */
        etFullName.setError(null);
        etEmail.setError(null);
        etPhone.setError(null);
        etPassword.setError(null);

        /*
            Form kontrolleri.
        */
        if (fullName.isEmpty()) {
            etFullName.setError("Ad soyad zorunludur");
            etFullName.requestFocus();
            return;
        }

        if (fullName.length() < 3) {
            etFullName.setError("Ad soyad en az 3 karakter olmalıdır");
            etFullName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("E-posta zorunludur");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Geçerli bir e-posta giriniz");
            etEmail.requestFocus();
            return;
        }

        /*
            Telefon zorunlu değil.
            Ama kullanıcı telefon girdiyse çok kısa olmamalı.
        */
        if (!phone.isEmpty() && phone.length() < 10) {
            etPhone.setError("Telefon numarası eksik görünüyor");
            etPhone.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Şifre zorunludur");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Şifre en az 6 karakter olmalıdır");
            etPassword.requestFocus();
            return;
        }

        setLoading(true);

        /*
            Retrofit kayıt isteği.

            PHP:
            auth/register.php

            POST:
            full_name
            email
            phone
            password
        */
        ApiClient.getApiService()
                .register(fullName, email, phone, password)
                .enqueue(new Callback<ApiResponse<User>>() {

                    @Override
                    public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                        setLoading(false);

                        /*
                            HTTP 404/500 gibi hata kontrolü.
                        */
                        if (!response.isSuccessful()) {
                            Toast.makeText(
                                    RegisterActivity.this,
                                    "Sunucu hatası: " + response.code(),
                                    Toast.LENGTH_LONG
                            ).show();
                            return;
                        }

                        ApiResponse<User> apiResponse = response.body();

                        if (apiResponse == null) {
                            Toast.makeText(
                                    RegisterActivity.this,
                                    "Boş sunucu cevabı",
                                    Toast.LENGTH_LONG
                            ).show();
                            return;
                        }

                        if (!apiResponse.isSuccess()) {
                            Toast.makeText(
                                    RegisterActivity.this,
                                    apiResponse.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                            return;
                        }

                        User user = apiResponse.getData();

                        if (user == null) {
                            Toast.makeText(
                                    RegisterActivity.this,
                                    "Kullanıcı bilgisi alınamadı",
                                    Toast.LENGTH_LONG
                            ).show();
                            return;
                        }

                        sessionManager.saveUser(user);

                        Toast.makeText(
                                RegisterActivity.this,
                                "Kayıt başarılı. Hoş geldin " + user.getFullName(),
                                Toast.LENGTH_SHORT
                        ).show();

                        /*
                            Kayıt sonrası direkt ana ekrana geçiyoruz.
                            finishAffinity() önceki LoginActivity ekranını da kapatır.
                        */
                        Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finishAffinity();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                        setLoading(false);

                        Toast.makeText(
                                RegisterActivity.this,
                                "Bağlantı hatası: " + t.getLocalizedMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    /*
        API isteği devam ederken butonları kapatır.
    */
    private void setLoading(boolean isLoading) {
        btnRegister.setEnabled(!isLoading);
        tvGoLogin.setEnabled(!isLoading);

        if (isLoading) {
            tvLoading.setVisibility(View.VISIBLE);
        } else {
            tvLoading.setVisibility(View.GONE);
        }
    }
}
