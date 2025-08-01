package com.example.haberuygulamajava.view;

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

import com.example.haberuygulamajava.R;
import com.example.haberuygulamajava.adapter.HaberlerRVAdapter;
import com.example.haberuygulamajava.dao.HaberDao;
import com.example.haberuygulamajava.databinding.FragmentKategoriBinding;
import com.example.haberuygulamajava.model.HaberModel;
import com.example.haberuygulamajava.servis.ApiClient;
import com.example.haberuygulamajava.viewmodel.KategorilerViewModel;

import retrofit2.Retrofit;

public class KategoriFragment extends Fragment {

    private FragmentKategoriBinding binding;
    private HaberlerRVAdapter adapter;
    private KategorilerViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentKategoriBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Retrofit retrofit = ApiClient.getClient();
        HaberDao haberDao = new HaberDao(retrofit);
        viewModel = new KategorilerViewModel(haberDao);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        KategoriFragmentArgs args = KategoriFragmentArgs.fromBundle(getArguments());
        int kategoriId = args.getId();

        viewModel.loadKategoriHaberleri(kategoriId);
        observeKategoriHaber();
    }

    private void observeKategoriHaber() {
        LifecycleOwner lifecycleOwner = getViewLifecycleOwner();
        lifecycleOwner.getLifecycle().addObserver((LifecycleEventObserver) (source, event) -> {
            if (event == Lifecycle.Event.ON_START) {
                viewModel.getKategoriHaberleri().observe(lifecycleOwner, liste -> {
                    if (liste != null && !liste.isEmpty()) {
                        adapter = new HaberlerRVAdapter(requireContext(),liste, new HaberlerRVAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(HaberModel haber) {
                                NavDirections action = KategoriFragmentDirections.kategoriToHaberDetay(haber);
                                Navigation.findNavController(binding.rvKategoriHaber).navigate(action);
                            }
                        });



                        binding.rvKategoriHaber.setLayoutManager(
                                new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
                        binding.rvKategoriHaber.setAdapter(adapter);
                    }
                });
            }
        });
    }
}
