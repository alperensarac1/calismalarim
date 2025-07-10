package com.example.chatjava.view;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.chatjava.R;
import com.example.chatjava.adapter.KonusulanKisilerAdapter;
import com.example.chatjava.databinding.FragmentMesajlarBinding;
import com.example.chatjava.model.Kullanici;

import com.example.chatjava.service.RetrofitClient;
import com.example.chatjava.service.response.KullaniciListResponse;
import com.example.chatjava.util.AppConfig;

import com.example.chatjava.viewmodel.SohbetListesiViewModel;

import java.util.Collections;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MesajlarFragment extends Fragment {


    private FragmentMesajlarBinding binding;


    private KonusulanKisilerAdapter adapter;
    private SohbetListesiViewModel viewModel;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentMesajlarBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(SohbetListesiViewModel.class);

        /* --------- RecyclerView ilk kurulumu ---------- */
        adapter = new KonusulanKisilerAdapter(
                Collections.emptyList(),
                secilenKisi -> {
                    Toast.makeText(requireContext(),
                            secilenKisi.getAd() + " seçildi",
                            Toast.LENGTH_SHORT).show();
                    kisiyeGoreKonusmayaGit(secilenKisi.getNumara());
                });

        binding.rvKonusulanKisiler.setAdapter(adapter);
        binding.rvKonusulanKisiler.setLayoutManager(new LinearLayoutManager(requireContext()));

        /* --------- LiveData gözlemleri ve periyodik yenileme ---------- */
        observeViewModel();
        viewModel.sohbetListesiniBaslat(AppConfig.kullaniciId);

        /* --------- FAB: yeni mesaj oluştur --------- */
        binding.fabMesajOlustur.setOnClickListener(v -> showYeniKisiDialog());

        return binding.getRoot();
    }

    private void observeViewModel() {

        viewModel.getKonusulanKisiler().observe(getViewLifecycleOwner(), kisiler -> {
            adapter = new KonusulanKisilerAdapter(
                    kisiler,
                    secilenKisi -> {
                        Toast.makeText(requireContext(),
                                secilenKisi.getAd() + " seçildi",
                                Toast.LENGTH_SHORT).show();
                        kisiyeGoreKonusmayaGit(secilenKisi.getNumara());
                    });
            binding.rvKonusulanKisiler.setAdapter(adapter);
        });

        viewModel.getHataMesaji().observe(getViewLifecycleOwner(), hata -> {
            if (hata != null) {
                Toast.makeText(requireContext(), hata, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showYeniKisiDialog() {
        EditText editText = new EditText(requireContext());
        editText.setHint("Alıcı numarası");
        editText.setInputType(InputType.TYPE_CLASS_PHONE);
        int pad = (int) (24 * getResources().getDisplayMetrics().density);
        editText.setPadding(pad, pad, pad, pad);

        new AlertDialog.Builder(requireContext())
                .setTitle("Yeni Mesaj")
                .setMessage("Mesaj göndermek istediğiniz numarayı girin:")
                .setView(editText)
                .setPositiveButton("Gönder", (d, which) -> {
                    String numara = editText.getText().toString().trim();
                    if (!numara.isEmpty()) {
                        kisiyeGoreKonusmayaGit(numara);
                    } else {
                        Toast.makeText(requireContext(),
                                "Numara girilmedi",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("İptal", null)
                .show();
    }

    private void kisiyeGoreKonusmayaGit(String numara) {

        RetrofitClient.getApiService()
                .kullanicilariGetir()
                .enqueue(new Callback<KullaniciListResponse>() {

                    @Override
                    public void onResponse(Call<KullaniciListResponse> call,
                                           Response<KullaniciListResponse> response) {

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess()) {

                            Kullanici hedef = null;
                            for (Kullanici k : response.body().getKullanicilar()) {
                                if (numara.equals(k.getNumara())) {
                                    hedef = k;
                                    break;
                                }
                            }

                            if (hedef != null) {
                                Bundle bundle = new Bundle();
                                bundle.putInt("alici_id", hedef.getId());
                                bundle.putString("alici_ad", hedef.getAd());

                                NavHostFragment.findNavController(MesajlarFragment.this)
                                        .navigate(R.id.action_mesajlarFragment_to_singleChatFragment,
                                                bundle);
                            } else {
                                Toast.makeText(requireContext(),
                                        "Bu numara kayıtlı değil",
                                        Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Toast.makeText(requireContext(),
                                    "Liste alınamadı",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<KullaniciListResponse> call, Throwable t) {
                        Toast.makeText(requireContext(),
                                "Sunucu hatası: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
