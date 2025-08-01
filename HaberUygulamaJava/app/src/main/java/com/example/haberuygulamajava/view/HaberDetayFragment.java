package com.example.haberuygulamajava.view;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.haberuygulamajava.R;
import com.example.haberuygulamajava.adapter.SonUcHaberRVAdapter;
import com.example.haberuygulamajava.dao.HaberDao;
import com.example.haberuygulamajava.databinding.FragmentHaberDetayBinding;
import com.example.haberuygulamajava.model.HaberModel;
import com.example.haberuygulamajava.model.YorumModel;
import com.example.haberuygulamajava.servis.ApiClient;
import com.example.haberuygulamajava.viewmodel.HaberDetayViewModel;
import com.example.haberuygulamajava.viewmodel.HaberlerViewModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Retrofit;

public class HaberDetayFragment extends Fragment {

    private FragmentHaberDetayBinding binding;
    private SonUcHaberRVAdapter adapter;
    private HaberlerViewModel viewModel;
    private HaberDetayViewModel haberDetayVM;
    private int haberId = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHaberDetayBinding.inflate(inflater, container, false);

        HaberDetayFragmentArgs args = HaberDetayFragmentArgs.fromBundle(getArguments());
        HaberModel haber = args.getHaber();
        haberId = haber.getId();

        binding.tvHaberBaslik.setText(haber.getBaslik());
        binding.tvAdSoyadDepartman.setText(haber.getAd() + " " + haber.getSoyad() + " " + haber.getUnvan());
        binding.tvTarih.setText(haber.getYayinlanma_tarihi());
        binding.tvHaberIcerik.setText(haber.getIcerik());

        if (haber.getMedia_type().equals("video")) {
            binding.imgHaber.setVisibility(View.GONE);
            binding.videoView.setVisibility(View.VISIBLE);
            binding.btnPlay.setVisibility(View.VISIBLE);
            binding.videoView.setVideoURI(Uri.parse(haber.getMedia_url()));

            binding.btnPlay.setOnClickListener(v -> {
                binding.videoView.start();
                binding.btnPlay.setVisibility(View.GONE);
            });

            binding.videoView.setOnCompletionListener(mp -> binding.btnPlay.setVisibility(View.VISIBLE));
        } else {
            binding.imgHaber.setVisibility(View.VISIBLE);
            binding.videoView.setVisibility(View.GONE);
            binding.btnPlay.setVisibility(View.GONE);

            Glide.with(requireContext())
                    .load(haber.getMedia_url())
                    .placeholder(R.drawable.resim)
                    .into(binding.imgHaber);
        }

        binding.btnYorumGonder.setOnClickListener(v -> {
            String rumuz = binding.etRumuz.getText().toString();
            String yorum = binding.etYorum.getText().toString();
            if (!rumuz.isEmpty() && !yorum.isEmpty()) {
                haberDetayVM.yorumEkle(haberId, rumuz, yorum);
                binding.etRumuz.setText("");
                binding.etYorum.setText("");
            } else {
                Toast.makeText(requireContext(), "İlgili alanlar boş bırakılamaz", Toast.LENGTH_SHORT).show();
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Retrofit retrofit = ApiClient.getClient();
        HaberDao haberDao = new HaberDao(retrofit);
        viewModel = new HaberlerViewModel(haberDao);
        haberDetayVM = new HaberDetayViewModel();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel.loadSon3Haber();
        haberDetayVM.loadYorumlar(haberId);

        observeSonUcHaber();
        observeYorumlar();
    }

    private void observeSonUcHaber() {
        LifecycleOwner lifecycleOwner = getViewLifecycleOwner();
        lifecycleOwner.getLifecycle().addObserver((LifecycleEventObserver) (source, event) -> {
            if (event == Lifecycle.Event.ON_START) {
                viewModel.getSonHaberler().observe(lifecycleOwner, liste -> {
                    if (liste != null && !liste.isEmpty()) {
                        adapter = new SonUcHaberRVAdapter(requireContext(),liste, new SonUcHaberRVAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(HaberModel haber) {
                                NavDirections action = HaberDetayFragmentDirections.haberDetayToHaberDetay(haber);
                                Navigation.findNavController(requireView()).navigate(action);
                            }
                        });

                        binding.rvSonUcHaber.setLayoutManager(new LinearLayoutManager(requireContext()));
                        binding.rvSonUcHaber.setAdapter(adapter);
                    }
                });
            }
        });
    }

    private void observeYorumlar() {
        LifecycleOwner lifecycleOwner = getViewLifecycleOwner();
        lifecycleOwner.getLifecycle().addObserver((LifecycleEventObserver) (source, event) -> {
            if (event == Lifecycle.Event.ON_START) {
                haberDetayVM.getYorumlar().observe(lifecycleOwner, yorumlar -> {
                    if (yorumlar != null && !yorumlar.isEmpty()) {
                        List<String> yorumIcerik = new ArrayList<>();
                        for (YorumModel y : yorumlar) {
                            yorumIcerik.add(y.getYorum_metni() + "\n" + y.getTakma_ad());
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, yorumIcerik);
                        binding.yorumlarListView.setAdapter(adapter);
                    }
                });
            }
        });
    }
}
