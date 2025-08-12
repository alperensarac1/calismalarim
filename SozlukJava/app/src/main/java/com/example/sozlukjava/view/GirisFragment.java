package com.example.sozlukjava.view;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sozlukjava.R;
import com.example.sozlukjava.databinding.FragmentGirisBinding;
import com.example.sozlukjava.model.SimpleResponse;
import com.example.sozlukjava.util.SessionManager;
import com.example.sozlukjava.viewmodel.GirisViewModel;

// GirisFragment.java
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

public class GirisFragment extends Fragment {

    private FragmentGirisBinding binding;
    private GirisViewModel viewModel;
    private SessionManager session;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentGirisBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        session = new SessionManager(requireContext());

        // Oturum açık mı?
        if (session.isLoggedIn()) {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_girisFragment_to_anaSayfaFragment);
            return;
        }

        viewModel = new ViewModelProvider(this).get(GirisViewModel.class);

        binding.btnGiris.setOnClickListener(v -> {
            String username = binding.etUsername.getText().toString();
            String password = binding.etPassword.getText().toString();
            viewModel.login(username, password);
        });

        viewModel.getLoginResult().observe(getViewLifecycleOwner(), new Observer<SimpleResponse>() {
            @Override
            public void onChanged(SimpleResponse it) {
                if (it == null) return;

                if (it.isSuccess()) {
                    int uid = (it.getUser_id() != null) ? it.getUser_id() : -1;
                    session.saveUserSession(uid, binding.etUsername.getText().toString());
                    NavHostFragment.findNavController(GirisFragment.this)
                            .navigate(R.id.action_girisFragment_to_anaSayfaFragment);
                } else {
                    String msg = (it.getMessage() != null) ? it.getMessage() : "Giriş başarısız";
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.btnKayitSayfasinaGit.setOnClickListener(v ->
                NavHostFragment.findNavController(GirisFragment.this)
                        .navigate(R.id.action_girisFragment_to_kayitFragment)
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
