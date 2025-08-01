package com.example.haberuygulamajava.view;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.example.haberuygulamajava.R;
import com.example.haberuygulamajava.adapter.HaberlerRVAdapter;
import com.example.haberuygulamajava.dao.HaberDao;
import com.example.haberuygulamajava.databinding.FragmentAnasayfaBinding;
import com.example.haberuygulamajava.factory.HaberlerViewModelFactory;
import com.example.haberuygulamajava.model.HaberTuruModel;
import com.example.haberuygulamajava.servis.ApiClient;
import com.example.haberuygulamajava.viewmodel.HaberlerViewModel;

public class AnasayfaFragment extends Fragment {

    private FragmentAnasayfaBinding binding;
    private HaberlerViewModel viewModel;
    private HaberlerRVAdapter sonDakikaAdapter;
    private HaberlerRVAdapter gundemAdapter;
    private ArrayAdapter<HaberTuruModel> kategorilerAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HaberDao haberDao = new HaberDao(ApiClient.getClient());
        viewModel = new ViewModelProvider(this, new HaberlerViewModelFactory(haberDao)).get(HaberlerViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAnasayfaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel.loadSonDakikaHaberler();
        viewModel.loadGundemHaberler();
        viewModel.getKategoriler();

        observeSonDakika();
        observeGundemHaberler();
        observeKategoriler();
    }

    private void observeSonDakika() {
        viewModel.getSonDakikaHaberler().observe(getViewLifecycleOwner(), liste -> {
            if (liste != null && !liste.isEmpty()) {
                sonDakikaAdapter = new HaberlerRVAdapter(getContext(), liste, haber -> {
                    NavDirections action = AnasayfaFragmentDirections.anasayfaToHaberDetay(haber);
                    Navigation.findNavController(requireView()).navigate(action);
                });
                binding.rvSonDakika.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                binding.rvSonDakika.setAdapter(sonDakikaAdapter);
            }
        });
    }

    private void observeGundemHaberler() {
        viewModel.getGundemHaberler().observe(getViewLifecycleOwner(), liste -> {
            if (liste != null && !liste.isEmpty()) {
                gundemAdapter = new HaberlerRVAdapter(getContext(), liste, haber -> {
                    NavDirections action = AnasayfaFragmentDirections.anasayfaToHaberDetay(haber);
                    Navigation.findNavController(requireView()).navigate(action);
                });
                binding.rvGundem.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                binding.rvGundem.setAdapter(gundemAdapter);
            }
        });
    }

    private void observeKategoriler() {
        viewModel.getKategoriler().observe(getViewLifecycleOwner(), liste -> {
            if (liste != null && !liste.isEmpty()) {
                kategorilerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, liste);
                binding.lvKategoriler.setAdapter(kategorilerAdapter);

                binding.lvKategoriler.setOnItemClickListener((parent, view, position, id) -> {
                    HaberTuruModel secilen = liste.get(position);
                    viewModel.filtreleKategori(secilen.getId());
                    NavDirections action = AnasayfaFragmentDirections.actionAnasayfaFragmentToKategoriFragment(secilen.getId());
                    Navigation.findNavController(view).navigate(action);
                });
            }
        });
    }
}
