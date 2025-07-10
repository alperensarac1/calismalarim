package com.example.chatjava.view.dialog;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.chatjava.databinding.DialogRegisterBinding;
import com.example.chatjava.service.RetrofitClient;
import com.example.chatjava.service.response.SimpleResponse;
import com.example.chatjava.util.AppConfig;
import com.example.chatjava.util.PrefManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistrationDialog extends DialogFragment {

    private DialogRegisterBinding binding;
    private PrefManager pref;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogRegisterBinding.inflate(getLayoutInflater());
        pref = new PrefManager(requireContext()); // güvenli kullanım

        binding.btnKayitOl.setOnClickListener(v -> {
            String ad = binding.etAd.getText().toString().trim();
            String numara = binding.etNumara.getText().toString().trim();

            if (ad.isEmpty() || numara.isEmpty()) {
                Toast.makeText(requireContext(), "Boş alan bırakma!", Toast.LENGTH_SHORT).show();
                return;
            }

            RetrofitClient.getApiService()
                    .kullaniciKayit(ad, numara)
                    .enqueue(new Callback<SimpleResponse>() {
                        @Override
                        public void onResponse(Call<SimpleResponse> call, Response<SimpleResponse> response) {
                            SimpleResponse body = response.body();

                            if (response.isSuccessful() && body != null && body.isSuccess() && body.getId() != null) {
                                AppConfig.kullaniciId = body.getId();
                                pref.kaydetKullaniciId(body.getId());

                                Toast.makeText(requireContext(), "Kayıt başarılı!", Toast.LENGTH_SHORT).show();
                                dismiss();
                            } else {
                                String hata = (body != null && body.getError() != null)
                                        ? body.getError()
                                        : "Kayıt başarısız";
                                Toast.makeText(requireContext(), hata, Toast.LENGTH_SHORT).show();
                                if (body != null) System.out.println(body.getError());
                            }
                        }

                        @Override
                        public void onFailure(Call<SimpleResponse> call, Throwable t) {
                            Toast.makeText(requireContext(), "Hata: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            System.out.println(t.getMessage());
                        }
                    });
        });

        return new AlertDialog.Builder(requireContext()) // güvenli context
                .setView(binding.getRoot())
                .create();
    }
}