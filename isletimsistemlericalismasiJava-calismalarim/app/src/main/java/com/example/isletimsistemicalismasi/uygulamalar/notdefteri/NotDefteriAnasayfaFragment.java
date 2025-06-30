package com.example.isletimsistemicalismasi.uygulamalar.notdefteri;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.isletimsistemicalismasi.R;
import com.example.isletimsistemicalismasi.databinding.FragmentNotDefteriAnasayfaBinding;
import com.example.isletimsistemicalismasi.uygulamalar.notdefteri.adapter.NoteAdapter;
import com.example.isletimsistemicalismasi.uygulamalar.notdefteri.model.Not;
import com.example.isletimsistemicalismasi.uygulamalar.notdefteri.model.NoteDatabase;

import java.util.ArrayList;


public class NotDefteriAnasayfaFragment extends Fragment  {

    FragmentNotDefteriAnasayfaBinding binding;

    NoteDatabase db;
    NoteAdapter adapter;
    ArrayList<Not> notListesi;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentNotDefteriAnasayfaBinding.inflate(inflater,container,false);

        notListesi = new ArrayList<>();

        notListesi = db.getNotlarim();
        adapter = new NoteAdapter(notListesi);
        binding.recyclerviewNotlar.setAdapter(adapter);
        binding.recyclerviewNotlar.setHasFixedSize(true);


       binding.inputSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
           @Override
           public boolean onQueryTextSubmit(String query) {
               notListesi.clear();
               notListesi.addAll(db.NotAra(query));
               adapter.notifyDataSetChanged();
               return false;
           }

           @Override
           public boolean onQueryTextChange(String newText) {
               notListesi.clear();
               notListesi.addAll(db.NotAra(newText));
               adapter.notifyDataSetChanged();
               return false;
           }
       });



        binding.imageAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.toNotDefteriDetaySayfa);
            }
        });


        return binding.getRoot();
    }


    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new NoteDatabase(requireContext());

    }
}
