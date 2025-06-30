package com.example.isletimsistemicalismasi.uygulamalar.rehber.view;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.isletimsistemicalismasi.R;
import com.example.isletimsistemicalismasi.anaekran.entity.Const;
import com.example.isletimsistemicalismasi.databinding.FragmentRehberBinding;
import com.example.isletimsistemicalismasi.databinding.FragmentRehberDetayBinding;
import com.example.isletimsistemicalismasi.uygulamalar.rehber.entity.RehberDao;
import com.example.isletimsistemicalismasi.uygulamalar.rehber.entity.RehberVeritabaniYardimcisi;
import com.example.isletimsistemicalismasi.uygulamalar.rehber.model.RehberKisiler;
import com.google.android.material.snackbar.Snackbar;


public class RehberDetayFragment extends Fragment {

    RehberKisiler gelenKisi;
    private RehberVeritabaniYardimcisi vt;
   FragmentRehberDetayBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        vt = new RehberVeritabaniYardimcisi(requireContext());
        RehberDao dao = new RehberDao(vt);
        // Inflate the layout for this fragment
        binding = FragmentRehberDetayBinding.inflate(inflater,container,false);

        gelenKisi  = getArguments().getSerializable("kisi", RehberKisiler.class);

        binding.etAd.setText(gelenKisi.getKisi_isim());
        binding.etKisiNumara.setText(gelenKisi.getKisi_numara());

        binding.btnKaydet.setOnClickListener(view->{
            int kisi_id = gelenKisi.getKisi_id();
            String kisi_isim = binding.etAd.getText().toString();
            String kisi_numara = binding.etKisiNumara.getText().toString();
            dao.kisiGuncelle(kisi_id,kisi_isim,kisi_numara);
            Snackbar.make(view,"Kişi Güncellendi",Snackbar.LENGTH_SHORT).show();

            Navigation.findNavController(view).popBackStack();
        });

        return binding.getRoot();
    }
}