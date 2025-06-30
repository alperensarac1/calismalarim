package com.example.isletimsistemicalismasi.uygulamalar.copkutusu;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.isletimsistemicalismasi.R;
import com.example.isletimsistemicalismasi.anaekran.adapter.AnaekranRVAdapter;
import com.example.isletimsistemicalismasi.anaekran.entity.Const;
import com.example.isletimsistemicalismasi.anaekran.model.UygulamalarModel;
import com.example.isletimsistemicalismasi.databinding.FragmentCopkutusuBinding;

import java.util.ArrayList;
import java.util.List;


public class copkutusu extends Fragment {

  FragmentCopkutusuBinding binding;
    ArrayList<UygulamalarModel> copkutusuUygulamaListesi;
    CopKutusuAdapter adapter;
    Const gelenconsts;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentCopkutusuBinding.inflate(inflater,container,false);


        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(),3);

        binding.copkutusuRV.setHasFixedSize(true);
        binding.copkutusuRV.setLayoutManager(layoutManager);
        binding.copkutusuRV.setAdapter(adapter);

        return binding.getRoot();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        gelenconsts = getArguments().getSerializable("copler", Const.class);
        if (gelenconsts != null){
            copkutusuUygulamaListesi = gelenconsts.getCopUygulamaListesi();
        }
        else{
            copkutusuUygulamaListesi = new ArrayList<UygulamalarModel>();
        }

        adapter = new CopKutusuAdapter(getActivity(),gelenconsts);
        super.onCreate(savedInstanceState);
    }
}