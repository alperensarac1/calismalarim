package com.example.memesharekotlin.view;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.memesharekotlin.R;
import com.example.memesharekotlin.adapter.OdaAdapter;
import com.example.memesharekotlin.databinding.FragmentAnasayfaBinding;
import com.example.memesharekotlin.model.OdaModel;
import com.example.memesharekotlin.model.SimpleResponse;
import com.example.memesharekotlin.service.ApiClient;
import com.example.memesharekotlin.viewmodel.LoginViewModel;
import com.example.memesharekotlin.viewmodel.OdaViewModel;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnasayfaFragment extends Fragment {

    FragmentAnasayfaBinding binding;
    int userId = 0;
    private OdaViewModel odaViewModel;
    private List<OdaModel> odaListesi = new ArrayList<>();
    private OdaAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        odaViewModel = new ViewModelProvider(requireActivity()).get(OdaViewModel.class);
        userId = getArguments().getInt("userId", 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAnasayfaBinding.inflate(inflater, container, false);

        // RecyclerView ayarları
        adapter = new OdaAdapter(odaListesi, oda -> {
            // odaya tıklanınca geçiş yap
            Bundle bundle = new Bundle();
            bundle.putInt("roomId", oda.getOdaId());
            bundle.putInt("userId", userId);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_anasayfaFragment_to_fragmentOda, bundle);
            System.out.println("Odaya tıklandı " + oda.getOdaId());
        });

        binding.rvOdalar.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvOdalar.setAdapter(adapter);

        binding.btnOdaKayit.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Oda Katılım");

            final EditText input = new EditText(requireContext());
            input.setHint("Oda ID girin");
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            builder.setPositiveButton("Katıl", (dialog, which) -> {
                String odaIdStr = input.getText().toString().trim();
                if (!odaIdStr.isEmpty()) {

                    ApiClient.getService().joinRoom(userId, odaIdStr).enqueue(new Callback<SimpleResponse>() {
                        @Override
                        public void onResponse(Call<SimpleResponse> call, Response<SimpleResponse> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().success) {
                                Toast.makeText(requireContext(), "Odaya katıldınız", Toast.LENGTH_SHORT).show();
                                fetchOdalar(); // güncelleme
                            } else {
                                Toast.makeText(requireContext(), "Katılım başarısız", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<SimpleResponse> call, Throwable t) {
                            Toast.makeText(requireContext(), "Hata: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

            builder.setNegativeButton("İptal", (dialog, which) -> dialog.cancel());

            builder.show();
        });


        // Odaları getir
        fetchOdalar();

        // Oda oluştur butonu
        binding.btnOdaGiris.setOnClickListener(v -> odaOlustur());

        return binding.getRoot();
    }

    private void fetchOdalar() {

        ApiClient.getService().getJoinedRooms(userId).enqueue(new Callback<List<OdaModel>>() {
            @Override
            public void onResponse(Call<List<OdaModel>> call, Response<List<OdaModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    odaListesi.clear();
                    odaListesi.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    response.body().forEach(odaModel -> {
                        System.out.println(odaModel.getRoomCode());
                    });
                } else {
                    Toast.makeText(requireContext(), "Odalar getirilemedi", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<OdaModel>> call, Throwable t) {
                Toast.makeText(requireContext(), "Hata: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void odaOlustur() {
        odaViewModel.createRoom(userId); // ViewModel'e istek gönder

        odaViewModel.getOdaOlusturmaSonucu().observe(getViewLifecycleOwner(), sonuc -> {
            if (sonuc.success) {
                Toast.makeText(requireContext(), "Oda oluşturuldu: " + sonuc.roomCode, Toast.LENGTH_SHORT).show();

                OdaModel yeniOda = new OdaModel();
                yeniOda.setOdaId(sonuc.roomId);           // ✅ room_id doğru gelmeli
                yeniOda.setRoomCode(sonuc.roomCode);      // ✅ room_code doğru gelmeli
                yeniOda.setCreatedBy(userId);

                odaListesi.add(yeniOda);
                adapter.notifyItemInserted(odaListesi.size() - 1);
            } else {
                Toast.makeText(requireContext(), "Hata: " + sonuc.message, Toast.LENGTH_SHORT).show();
            }
        });
    }



}
