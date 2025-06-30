package com.example.isletimsistemicalismasi.giris_ekrani;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.isletimsistemicalismasi.R;
import com.example.isletimsistemicalismasi.databinding.FragmentGuvenlikSorusuSecBinding;
import com.example.isletimsistemicalismasi.giris_ekrani.repository.KullaniciBilgileri;


public class GuvenlikSorusuSecFragment extends Fragment {

    FragmentGuvenlikSorusuSecBinding binding;
    KullaniciBilgileri kullaniciBilgileri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        binding = FragmentGuvenlikSorusuSecBinding.inflate(inflater, container, false);

        kullaniciBilgileri = new KullaniciBilgileri(requireContext());

        setupRadioButton(
                binding.radioButtonEnSevilenRenk,
                binding.etEnsevilenRenk,
                binding.tvEnsevilenRenk,
                binding.btnEnSevilenRenk,
                binding.radioButtonMuzikGrubu,
                binding.radioButtonSevilenHayvan
        );

        setupRadioButton(
                binding.radioButtonSevilenHayvan,
                binding.etEnSevilenHayvan,
                binding.tvEnSevilenHayvan,
                binding.buttonEnSecilenHayvan,
                binding.radioButtonMuzikGrubu,
                binding.radioButtonEnSevilenRenk
        );

        setupRadioButton(
                binding.radioButtonMuzikGrubu,
                binding.etEnSevilenMuzikGrubu,
                binding.tvEnSevilenMuzikGrubu,
                binding.buttonEnSevilenMuzikGrubu,
                binding.radioButtonEnSevilenRenk,
                binding.radioButtonSevilenHayvan
        );

        binding.buttonEnSecilenHayvan.setOnClickListener(view -> {
            saveSecurityQuestion(binding.tvEnSevilenHayvan, binding.etEnSevilenHayvan);
            navigateToNext(view);
        });

        binding.buttonEnSevilenMuzikGrubu.setOnClickListener(view -> {
            saveSecurityQuestion(binding.tvEnSevilenMuzikGrubu, binding.etEnSevilenMuzikGrubu);
            navigateToNext(view);
        });

        binding.btnEnSevilenRenk.setOnClickListener(view -> {
            saveSecurityQuestion(binding.tvEnsevilenRenk, binding.etEnsevilenRenk);
            navigateToNext(view);
        });

        return binding.getRoot();
    }

    private void setupRadioButton(
            RadioButton radioButton,
            EditText editText,
            TextView textView,
            View button,
            RadioButton otherRadio1,
            RadioButton otherRadio2
    ) {
        radioButton.setOnClickListener(view -> {
            if (radioButton.isChecked()) {
                otherRadio1.setChecked(false);
                otherRadio2.setChecked(false);
                toggleVisibility(true, editText, button);
            }
        });

        radioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            toggleVisibility(isChecked, editText, button);
        });
    }

    private void toggleVisibility(boolean isVisible, EditText editText, View button) {
        int visibility = isVisible ? View.VISIBLE : View.GONE;
        editText.setVisibility(visibility);
        button.setVisibility(visibility);
    }

    private void saveSecurityQuestion(TextView textView, EditText editText) {
        if (!editText.getText().toString().isEmpty()) {
            kullaniciBilgileri.guvenlikSorusuKaydet(textView.getText().toString());
            kullaniciBilgileri.guvenlikSorusuCevapKaydet(editText.getText().toString());
        }
    }

    private void navigateToNext(View view) {
        Navigation.findNavController(view).navigate(R.id.girisYapFragment);
    }
}
