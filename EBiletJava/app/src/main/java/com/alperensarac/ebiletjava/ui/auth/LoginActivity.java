package com.alperensarac.ebiletjava.ui.auth;
import android.app.Activity;
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
    LoginActivity.java

    Kullanıcı giriş ekranı.

    Bu sürümde:
    - TextInputLayout yok
    - TextInputEditText yok
    - Normal EditText var

    Görevi:
    1. Kullanıcıdan e-posta ve şifre almak
    2. Retrofit ile auth/login.php dosyasına istek atmak
    3. Başarılı cevapta gelen User bilgisini almak
    4. api_token değerini SharedPreferences içine kaydetmek
*/
public class LoginActivity extends AppCompatActivity {

    /*
        XML içindeki view değişkenleri.
    */
    private EditText etEmail;
    private EditText etPassword;

    private Button btnLogin;
    private TextView tvGoRegister;
    private TextView tvLoading;

    /*
        Kullanıcı oturum bilgilerini saklamak için kullanılır.
    */
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /*
            SessionManager başlatılır.
        */
        sessionManager = new SessionManager(this);

        /*
            XML view'ları Java tarafına bağlanır.
        */
        initViews();

        /*
            Kullanıcı daha önce giriş yaptıysa burada kontrol edebiliriz.

            HomeActivity henüz oluşturulmadığı için şimdilik sadece mesaj gösteriyoruz.
            HomeActivity yaptığımızda bu kısmı ana ekrana yönlendireceğiz.
        */
        if (sessionManager.isLoggedIn()) {
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
            return;
        }


        /*
            Giriş butonuna basılınca loginUser çalışır.
        */
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });

        /*
            Kayıt ol yazısına basılınca RegisterActivity açılır.
        */
        tvGoRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    /*
        XML'deki view'ları findViewById ile bağlıyoruz.
    */
    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        btnLogin = findViewById(R.id.btnLogin);
        tvGoRegister = findViewById(R.id.tvGoRegister);
        tvLoading = findViewById(R.id.tvLoading);
    }

    /*
        Kullanıcı giriş işlemi.
    */
    private void loginUser() {

        /*
            EditText içindeki değerleri alıyoruz.

            trim():
            Başta ve sonda gereksiz boşluk varsa temizler.
        */
        String email = etEmail.getText() == null
                ? ""
                : etEmail.getText().toString().trim();

        String password = etPassword.getText() == null
                ? ""
                : etPassword.getText().toString().trim();

        /*
            Normal EditText kullandığımız için hata mesajlarını setError ile direkt
            EditText üzerinde gösterebiliriz.
        */
        etEmail.setError(null);
        etPassword.setError(null);

        /*
            Form doğrulamaları.
        */
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

        /*
            Form doğruysa API isteğine geçiyoruz.
        */
        setLoading(true);

        /*
            Retrofit ile login isteği.

            PHP:
            auth/login.php

            POST:
            email
            password
        */
        ApiClient.getApiService()
                .login(email, password)
                .enqueue(new Callback<ApiResponse<User>>() {

                    @Override
                    public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                        setLoading(false);

                        /*
                            HTTP düzeyinde hata varsa:
                            Örnek 404, 500.
                        */
                        if (!response.isSuccessful()) {
                            Toast.makeText(
                                    LoginActivity.this,
                                    "Sunucu hatası: " + response.code(),
                                    Toast.LENGTH_LONG
                            ).show();
                            return;
                        }

                        /*
                            Backend JSON cevabını alıyoruz.
                        */
                        ApiResponse<User> apiResponse = response.body();

                        if (apiResponse == null) {
                            Toast.makeText(
                                    LoginActivity.this,
                                    "Boş sunucu cevabı",
                                    Toast.LENGTH_LONG
                            ).show();
                            return;
                        }

                        /*
                            Backend success false döndürdüyse
                            message değerini kullanıcıya gösteriyoruz.
                        */
                        if (!apiResponse.isSuccess()) {
                            Toast.makeText(
                                    LoginActivity.this,
                                    apiResponse.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                            return;
                        }

                        /*
                            success true ise data içinde User bekliyoruz.
                        */
                        User user = apiResponse.getData();

                        if (user == null) {
                            Toast.makeText(
                                    LoginActivity.this,
                                    "Kullanıcı bilgisi alınamadı",
                                    Toast.LENGTH_LONG
                            ).show();
                            return;
                        }

                        /*
                            Kullanıcıyı cihaza kaydediyoruz.
                            api_token burada saklanır.
                        */
                        sessionManager.saveUser(user);

                        Toast.makeText(
                                LoginActivity.this,
                                "Giriş başarılı. Hoş geldin " + user.getFullName(),
                                Toast.LENGTH_SHORT
                        ).show();

                        /*
                            HomeActivity sonraki adımda yapılacak.
                            Şimdilik giriş ekranında kalıyoruz.

                            HomeActivity gelince:

                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        */
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                        setLoading(false);

                        /*
                            Bu kısım genelde şu durumlarda çalışır:
                            - İnternet yok
                            - Backend kapalı
                            - BASE_URL yanlış
                            - Apache çalışmıyor
                        */
                        Toast.makeText(
                                LoginActivity.this,
                                "Bağlantı hatası: " + t.getLocalizedMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    /*
        API isteği sırasında kullanıcı tekrar tekrar butona basmasın diye
        butonları pasifleştiriyoruz.
    */
    private void setLoading(boolean isLoading) {
        btnLogin.setEnabled(!isLoading);
        tvGoRegister.setEnabled(!isLoading);

        if (isLoading) {
            tvLoading.setVisibility(View.VISIBLE);
        } else {
            tvLoading.setVisibility(View.GONE);
        }
    }
}