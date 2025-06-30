package com.example.isletimsistemicalismasi.uygulamalar.notdefteri;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.isletimsistemicalismasi.R;
import com.example.isletimsistemicalismasi.databinding.FragmentNotDefteriDetaysayfaBinding;
import com.example.isletimsistemicalismasi.uygulamalar.notdefteri.model.Not;
import com.example.isletimsistemicalismasi.uygulamalar.notdefteri.model.NoteDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class NotDefteriDetaysayfaFragment extends Fragment {


    NoteDatabase db;
    FragmentNotDefteriDetaysayfaBinding binding;
    Not gelenNot;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        binding = FragmentNotDefteriDetaysayfaBinding.inflate(inflater,container,false);
        gelenNot = getArguments().getSerializable("not", Not.class);
        db = new NoteDatabase(requireContext());
        if (gelenNot != null){
            binding.inputTitle.setText(gelenNot.getBaslik());
            binding.inputNote.setText(gelenNot.getNotMetin());
        }

        binding.textDate.setText(new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm", Locale.getDefault()).format(new Date()));

        binding.imageSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.inputTitle.getText().toString().trim().isEmpty()) {
                    binding.inputTitle.setError("Başlık Giriniz");
                } else if (binding.inputNote.getText().toString().trim().isEmpty()) {
                    binding.inputNote.setError("Not Giriniz");
                } else {
                    db.yeniNot (new Not(0,binding.inputTitle.getText().toString(), binding.inputNote.getText().toString(), "null", "null", binding.textDate.getText().toString(),""));
                    Navigation.findNavController(v).popBackStack();
                }
            }
        });

        binding.imageDelete.setOnClickListener(view->{
            if (gelenNot!=null){
                db.NotSil(gelenNot);
                Navigation.findNavController(view).popBackStack();
            }else{
                Navigation.findNavController(view).popBackStack();
            }

        });

        binding.imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.NotGuncelle(gelenNot.getId(),new Not(0,binding.inputTitle.getText().toString(), binding.inputNote.getText().toString(), "null", "null", binding.textDate.getText().toString(),""));
                Navigation.findNavController(v).popBackStack();
            }
        });

        return binding.getRoot();
    }

}