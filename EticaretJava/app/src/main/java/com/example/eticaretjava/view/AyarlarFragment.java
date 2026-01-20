package com.example.eticaretjava.view;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import com.example.eticaretjava.databinding.FragmentAyarlarBinding;


public class AyarlarFragment extends Fragment {


    private FragmentAyarlarBinding binding;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAyarlarBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}
