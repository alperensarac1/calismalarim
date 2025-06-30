package com.example.isletimsistemicalismasi.uygulamalar.notdefteri.adapter;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import com.example.isletimsistemicalismasi.R;
import com.example.isletimsistemicalismasi.databinding.RowNoteBinding;
import com.example.isletimsistemicalismasi.uygulamalar.notdefteri.NotDefteriDetaysayfaFragment;

import com.example.isletimsistemicalismasi.uygulamalar.notdefteri.model.Not;


import java.util.ArrayList;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    ArrayList<Not> notListesi;

    public NoteAdapter(ArrayList<Not> notListesi) {
        /*public NoteAdapter(ArrayList<Not> notListesi, Activity activity, NoteListener listener) */
        this.notListesi = notListesi;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RowNoteBinding binding = RowNoteBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new NoteViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Not not = notListesi.get(position);

            holder.binding.textTitle.setText(not.getBaslik());
            holder.binding.textNote.setText(not.getNotMetin());
            holder.binding.textDate.setText(not.getTarih());
            holder.binding.cardViewContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    NotDefteriDetaysayfaFragment notDetayFragment = new NotDefteriDetaysayfaFragment();
                    Bundle args = new Bundle();
                    args.putSerializable("not",not);
                    notDetayFragment.setArguments(args);
                    Navigation.findNavController(v).navigate(R.id.toNotDefteriDetaySayfa,args);
                }
            });

    }

    @Override
    public int getItemCount() {
        return notListesi.size();
    }

    class NoteViewHolder extends RecyclerView.ViewHolder {
        private RowNoteBinding binding;

        public NoteViewHolder(RowNoteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }
}

