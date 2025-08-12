package com.example.sozlukjava.view;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sozlukjava.R;
import com.example.sozlukjava.adapter.EntryAdapter;
import com.example.sozlukjava.databinding.FragmentProfilBinding;
import com.example.sozlukjava.util.SessionManager;
import com.example.sozlukjava.viewmodel.ProfilViewModel;

// ProfilFragment.java
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

public class ProfilFragment extends Fragment {
    private FragmentProfilBinding binding;
    private ProfilViewModel viewModel;
    private SessionManager session;
    private EntryAdapter recyclerAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfilBinding.inflate(inflater, container, false);
        binding.btnCikisYap.setOnClickListener(v ->
                new AlertDialog.Builder(requireContext())
                        .setTitle("Çıkış Yap")
                        .setMessage("Oturumunuzu kapatmak istediğinizden emin misiniz?")
                        .setPositiveButton("Evet", (dialog, which) -> {
                            session.clearSession();
                            NavHostFragment.findNavController(ProfilFragment.this)
                                    .navigate(R.id.action_profilFragment_to_girisFragment);
                        })
                        .setNegativeButton("İptal", null)
                        .show()
        );
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        session = new SessionManager(requireContext());
        viewModel = new ViewModelProvider(this).get(ProfilViewModel.class);

        int userId = session.getUserId();
        String username = session.getUsername();
        binding.tvKullaniciAdi.setText(username != null ? username : "Bilinmeyen Kullanıcı");

        binding.profilRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        viewModel.loadUserEntries(userId);

        binding.bottomNavBugun.setSelectedItemId(R.id.nav_profil);
        binding.bottomNavBugun.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_gundem) {
                NavHostFragment.findNavController(this).navigate(R.id.anaSayfaFragment);
                return true;
            } else if (id == R.id.nav_bugun) {
                NavHostFragment.findNavController(this).navigate(R.id.bugunFragment);
                return true;
            } else if (id == R.id.nav_profil) {
                return true;
            }
            return false;
        });

        recyclerAdapter = new EntryAdapter(
                new java.util.ArrayList<>(),
                entry -> {
                    ProfilFragmentDirections.ActionProfilFragmentToEntryDetayFragment action =
                            ProfilFragmentDirections.actionProfilFragmentToEntryDetayFragment(entry.getId());
                    NavHostFragment.findNavController(ProfilFragment.this).navigate(action);
                },
                entry -> new AlertDialog.Builder(requireContext())
                        .setTitle("Entry Sil")
                        .setMessage("Bu entry'i silmek istediğinizden emin misiniz?")
                        .setPositiveButton("Evet", (d, w) -> viewModel.deleteEntry(entry.getId(), userId))
                        .setNegativeButton("İptal", null)
                        .show()
        );
        binding.profilRecyclerView.setAdapter(recyclerAdapter);

        viewModel.getEntries().observe(getViewLifecycleOwner(), list -> {
            // basitçe yeni adapter verebilir veya diff util kullanabilirsiniz
            binding.profilRecyclerView.setAdapter(
                    new EntryAdapter(list,
                            entry -> {
                                ProfilFragmentDirections.ActionProfilFragmentToEntryDetayFragment action =
                                        ProfilFragmentDirections.actionProfilFragmentToEntryDetayFragment(entry.getId());
                                NavHostFragment.findNavController(ProfilFragment.this).navigate(action);
                            },
                            entry -> new AlertDialog.Builder(requireContext())
                                    .setTitle("Entry Sil")
                                    .setMessage("Bu entry'i silmek istediğinizden emin misiniz?")
                                    .setPositiveButton("Evet", (d, w) -> viewModel.deleteEntry(entry.getId(), userId))
                                    .setNegativeButton("İptal", null)
                                    .show()
                    )
            );
        });

        viewModel.deleteResult.observe(getViewLifecycleOwner(), it -> {
            if (it != null && it.isSuccess()) {
                Toast.makeText(requireContext(), "Silindi", Toast.LENGTH_SHORT).show();
            } else {
                String msg = (it != null && it.getMessage() != null) ? it.getMessage() : "Silinemedi";
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });

        binding.searchViewProfil.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
