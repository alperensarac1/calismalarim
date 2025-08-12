package com.example.sozlukjava.view;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sozlukjava.R;
import com.example.sozlukjava.databinding.FragmentEntryEkleBinding;
import com.example.sozlukjava.util.SessionManager;
import com.example.sozlukjava.viewmodel.EntryEkleViewModel;

// EntryEkleFragment.java
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

public class EntryEkleFragment extends Fragment {
    private FragmentEntryEkleBinding binding;
    private EntryEkleViewModel viewModel;
    private SessionManager session;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentEntryEkleBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        session = new SessionManager(requireContext());
        viewModel = new ViewModelProvider(this).get(EntryEkleViewModel.class);

        binding.btnEntryKaydet.setOnClickListener(v -> {
            String title = binding.etEntryTitle.getText().toString();
            String content = binding.etEntryContent.getText().toString();
            int userId = session.getUserId();

            if (title.isBlank() || content.isBlank()) {
                Toast.makeText(requireContext(), "Tüm alanları doldurun", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.addEntry(userId, title, content);
        });

        viewModel.getAddResult().observe(getViewLifecycleOwner(), it -> {
            if (it != null && it.isSuccess()) {
                Toast.makeText(requireContext(), "Entry eklendi", Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(EntryEkleFragment.this).navigateUp();
            } else {
                String msg = (it != null && it.getMessage() != null) ? it.getMessage() : "Hata oluştu";
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
