package com.example.isletimsistemicalismasi.giris_ekrani;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.isletimsistemicalismasi.R;
import com.example.isletimsistemicalismasi.databinding.FragmentSifremiUnuttumBinding;
import com.example.isletimsistemicalismasi.giris_ekrani.repository.KullaniciBilgileri;


public class SifremiUnuttumFragment extends Fragment {

    FragmentSifremiUnuttumBinding binding;
    KullaniciBilgileri kullaniciBilgileri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSifremiUnuttumBinding.inflate(inflater,container,false);
        kullaniciBilgileri = new KullaniciBilgileri(requireContext());
        binding.tvGuvenlikSorusu.setText(kullaniciBilgileri.guvenlikSorusuGetir());
        binding.btnOnayla.setOnClickListener(view->{
            guvenlikSorusuDogrula(binding.etGuvenlikSorusuCevap.getText().toString(),binding.btnOnayla);
        });
        return binding.getRoot();
    }
    void guvenlikSorusuDogrula(String cevap,View view){
        boolean sonuc = kullaniciBilgileri.guvenlikSorusuDogrula(cevap);
        if (sonuc){
            Navigation.findNavController(view).navigate(R.id.kayitOlusturFragment);
        }
        else{
            Toast.makeText(requireContext(),"Yanlış Cevap Girdiniz",Toast.LENGTH_SHORT).show();
        }
    }
}