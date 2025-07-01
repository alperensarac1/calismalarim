package com.example.adisyonuygulamajava;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.FrameLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.adisyonuygulamajava.anaekran.MainFragment;
import com.example.adisyonuygulamajava.model.Masa;
import com.example.adisyonuygulamajava.services.Services;
import com.example.adisyonuygulamajava.services.retrofit.ServiceImpl;

import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private FrameLayout container;
    private final int containerId = View.generateViewId();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        container = new FrameLayout(this);
        container.setId(containerId);
        container.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        setContentView(container);

        if (savedInstanceState == null) {
            Services servis = ServiceImpl.getInstance();

            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    List<Masa> masalar = servis.masalariGetir();
                    new Handler(Looper.getMainLooper()).post(() -> {
                        getSupportFragmentManager().beginTransaction()
                                .replace(containerId, MainFragment.newInstance(masalar))
                                .commit();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
