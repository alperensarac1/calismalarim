package com.example.isletimsistemicalismasi.uygulamalar.rehber.view;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.isletimsistemicalismasi.R;

import com.example.isletimsistemicalismasi.databinding.FragmentRehberEkleBinding;
import com.example.isletimsistemicalismasi.uygulamalar.rehber.entity.RehberDao;
import com.example.isletimsistemicalismasi.uygulamalar.rehber.entity.RehberVeritabaniYardimcisi;
import com.google.android.material.snackbar.Snackbar;

public class RehberEkleFragment extends Fragment {

   FragmentRehberEkleBinding binding;
    private RehberVeritabaniYardimcisi vt;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentRehberEkleBinding.inflate(inflater,container,false);
        vt = new RehberVeritabaniYardimcisi(requireContext());
        RehberDao dao = new RehberDao(vt);


        binding.btnEkle.setOnClickListener(view->{
            String kisi_isim = binding.etAd.getText().toString();
            String kisi_numara = binding.etKisiNumara.getText().toString();

            dao.kisiEkle(kisi_isim,kisi_numara);

            Snackbar.make(view,"Kişi Eklendi",Snackbar.LENGTH_SHORT).show();

            Navigation.findNavController(view).popBackStack();

        });
        return binding.getRoot();
    }
}