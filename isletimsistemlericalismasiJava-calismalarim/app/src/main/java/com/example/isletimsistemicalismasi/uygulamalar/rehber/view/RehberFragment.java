package com.example.isletimsistemicalismasi.uygulamalar.rehber.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.PopupMenu;

import com.example.isletimsistemicalismasi.R;
import com.example.isletimsistemicalismasi.anaekran.repository.UygulamalarRepository;
import com.example.isletimsistemicalismasi.databinding.FragmentRehberBinding;
import com.example.isletimsistemicalismasi.uygulamalar.copkutusu.copkutusu;
import com.example.isletimsistemicalismasi.uygulamalar.rehber.entity.RehberDao;
import com.example.isletimsistemicalismasi.uygulamalar.rehber.entity.RehberVeritabaniYardimcisi;
import com.example.isletimsistemicalismasi.uygulamalar.rehber.model.RehberKisiler;

import java.util.ArrayList;


public class RehberFragment extends Fragment {

    FragmentRehberBinding binding;
    private RehberVeritabaniYardimcisi vt;

    ArrayList<RehberKisiler> kisilerArrayList;
    ArrayAdapter<String> adapter;
    ArrayList<String> kisiAdlar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentRehberBinding.inflate(inflater,container,false);

        vt = new RehberVeritabaniYardimcisi(requireContext());
        RehberDao dao = new RehberDao(vt);

        kisiAdlar = new ArrayList<>();

        kisilerArrayList = dao.tumKisiler();
        kisilerArrayList.forEach(rehberKisiler -> {
            kisiAdlar.add(rehberKisiler.getKisi_isim());
        });
        adapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1,android.R.id.text1,kisiAdlar);
        binding.lvRehber.setAdapter(adapter);
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CALL_PHONE}, 101);
        }

        binding.lvRehber.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:+9" + kisilerArrayList.get(position).getKisi_numara()));
                    startActivity(callIntent);
                }

        });

        binding.lvRehber.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                PopupMenu popup = new PopupMenu(requireContext(),view);
                popup.getMenuInflater().inflate(R.menu.rehber_duzenle_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.rehber_duzenle_action){
                            RehberKisiler kisi = kisilerArrayList.get(position);
                            RehberDetayFragment fragmentCopKutusu = new RehberDetayFragment();
                            Bundle args = new Bundle();
                            args.putSerializable("kisi",kisi);
                            fragmentCopKutusu.setArguments(args);
                            Navigation.findNavController(view).navigate(R.id.toRehberDetayFragment,args);
                            return true;
                        }
                        if (item.getItemId() == R.id.rehber_sil_action){
                            dao.kisiSil(kisilerArrayList.get(position).getKisi_id());
                            return true;
                        }
                        return false;
                    }
                });
                popup.show();
                return true;
            }
        });


        binding.fabEkle.setOnClickListener(view->{
            Navigation.findNavController(view).navigate(R.id.rehberEkleFragment);
        });


        return binding.getRoot();
    }

    @Override
    public void onResume() {
        adapter.notifyDataSetChanged();
        super.onResume();
    }
}