package com.example.chatjava;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chatjava.service.RetrofitClient;
import com.example.chatjava.service.response.SimpleResponse;
import com.example.chatjava.util.AppConfig;
import com.example.chatjava.util.PrefManager;
import com.example.chatjava.view.dialog.RegistrationDialog;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private PrefManager pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pref = new PrefManager(this);

        if (!pref.kullaniciVarMi()) {
            new RegistrationDialog().show(getSupportFragmentManager(), "register");
        } else {
            AppConfig.kullaniciId = pref.getirKullaniciId();
            Toast.makeText(this, "ID yüklendi: " + AppConfig.kullaniciId, Toast.LENGTH_SHORT).show();
        }

        RetrofitClient.getApiService().testConnection().enqueue(new Callback<SimpleResponse>() {
            @Override
            public void onResponse(Call<SimpleResponse> call, Response<SimpleResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(MainActivity.this,
                            "Bağlantı başarılı: " + response.body().getMessage(),
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this,
                            "Sunucu bağlantısı başarısız",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<SimpleResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this,
                        "Bağlantı hatası: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
                System.out.println(t.getMessage());
            }
        });
    }
}