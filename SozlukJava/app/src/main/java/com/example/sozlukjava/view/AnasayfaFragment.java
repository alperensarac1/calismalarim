package com.example.sozlukjava.view;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sozlukjava.R;
import com.example.sozlukjava.adapter.EntryAdapter;
import com.example.sozlukjava.databinding.FragmentAnasayfaBinding;
import com.example.sozlukjava.viewmodel.AnaSayfaViewModel;

// AnasayfaFragment.java
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

public class AnasayfaFragment extends Fragment {
    private FragmentAnasayfaBinding binding;
    private EntryAdapter recyclerAdapter;
    private AnaSayfaViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAnasayfaBinding.inflate(inflater, container, false);
        binding.fabEntryEkle.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_anaSayfaFragment_to_entryEkleFragment)
        );
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AnaSayfaViewModel.class);
        binding.entryRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        viewModel.loadMostCommentedEntriesToday();

        viewModel.getEntries().observe(getViewLifecycleOwner(), list -> {
            recyclerAdapter = new EntryAdapter(list,
                    entry -> {
                       AnasayfaFragmentDirections.ActionAnaSayfaFragmentToEntryDetayFragment action =
                               AnasayfaFragmentDirections.actionAnaSayfaFragmentToEntryDetayFragment(entry.getId());
                        NavHostFragment.findNavController(AnasayfaFragment.this).navigate(action);
                    },
                    entry -> { /* long click boş */ }
            );
            binding.entryRecyclerView.setAdapter(recyclerAdapter);
        });

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) {
                viewModel.setSearchQuery(query != null ? query : "");
                return true;
            }
            @Override public boolean onQueryTextChange(String newText) {
                viewModel.setSearchQuery(newText != null ? newText : "");
                return true;
            }
        });

        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_bugun) {
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_anaSayfaFragment_to_bugunFragment);
                return true;
            } else if (id == R.id.nav_profil) {
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_anaSayfaFragment_to_profilFragment);
                return true;
            } else {
                return true; // Gündem zaten burası
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
