package com.example.sozlukjava.view;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sozlukjava.R;
import com.example.sozlukjava.adapter.EntryAdapter;
import com.example.sozlukjava.databinding.FragmentBugunBinding;
import com.example.sozlukjava.viewmodel.BugunViewModel;

// BugunFragment.java
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

public class BugunFragment extends Fragment {
    private FragmentBugunBinding binding;
    private BugunViewModel viewModel;
    private EntryAdapter recyclerAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentBugunBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(BugunViewModel.class);
        binding.bugunRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        viewModel.loadTodayEntries();

        binding.bottomNavBugun.setSelectedItemId(R.id.nav_bugun);
        binding.bottomNavBugun.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_gundem) {
                NavHostFragment.findNavController(this).navigate(R.id.anaSayfaFragment);
                return true;
            } else if (id == R.id.nav_bugun) {
                return true; // zaten burası
            } else if (id == R.id.nav_profil) {
                NavHostFragment.findNavController(this).navigate(R.id.profilFragment);
                return true;
            }
            return false;
        });

        viewModel.getEntries().observe(getViewLifecycleOwner(), list -> {
            recyclerAdapter = new EntryAdapter(list,
                    entry -> {
                        BugunFragmentDirections.ActionBugunFragmentToEntryDetayFragment action =
                                BugunFragmentDirections.actionBugunFragmentToEntryDetayFragment(entry.getId());
                        NavHostFragment.findNavController(BugunFragment.this).navigate(action);
                    },
                    entry -> { /* long click boş */ }
            );
            binding.bugunRecyclerView.setAdapter(recyclerAdapter);
        });

        binding.searchViewBugun.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) {
                viewModel.setSearchQuery(query != null ? query : "");
                return true;
            }
            @Override public boolean onQueryTextChange(String newText) {
                viewModel.setSearchQuery(newText != null ? newText : "");
                return true;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
