package com.example.isletimsistemicalismasi.anaekran.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.isletimsistemicalismasi.R;
import com.example.isletimsistemicalismasi.anaekran.entity.Const;
import com.example.isletimsistemicalismasi.anaekran.model.UygulamalarModel;
import com.example.isletimsistemicalismasi.databinding.UygulamalarRecyclerCardBinding;
import com.example.isletimsistemicalismasi.anaekran.repository.UygulamalarRepository;
import com.example.isletimsistemicalismasi.uygulamalar.copkutusu.copkutusu;

public class AnaekranRVAdapter extends RecyclerView.Adapter<AnaekranRVAdapter.AnaekranTasarimTutucu>{
    private Context mcontext;
    Const consts;

    public AnaekranRVAdapter(Context context,Const consts) {
        this.mcontext = context;
        this.consts = consts;
    }

    @NonNull
    @Override
    public AnaekranTasarimTutucu onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        UygulamalarRecyclerCardBinding binding = UygulamalarRecyclerCardBinding.inflate(LayoutInflater.from(mcontext),parent,false);
        return new AnaekranTasarimTutucu(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AnaekranTasarimTutucu holder, int position) {
        UygulamalarModel uygulama = consts.getUygulamalarListesi().get(position);

        holder.binding.tvUygulama.setText(uygulama.getUygulamaAdi());
        holder.binding.imgUygulama.setImageResource(mcontext.getResources().getIdentifier(uygulama.getUygulamaResimAdi(),"drawable",mcontext.getPackageName()));


        holder.binding.cardUygulama.setOnClickListener(view->{

                if (uygulama.getNavId() == 1){
                    copkutusu fragmentCopKutusu = new copkutusu();
                    Bundle args = new Bundle();
                    args.putSerializable("copler",consts);
                    fragmentCopKutusu.setArguments(args);
                    Navigation.findNavController(view).navigate(R.id.toCopkutusu,args);
                    //Navigation.findNavController(view).navigate(R.id.copkutusu);
                }else{
                    Navigation.findNavController(view).navigate(uygulama.getNavId());
                }


        });

        if (uygulama.getNavId() != 1){
            holder.binding.cardUygulama.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    PopupMenu popup = new PopupMenu(mcontext,v);
                    popup.getMenuInflater().inflate(R.menu.uygulama_kaldir_menu, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            if (item.getItemId() == R.id.action_kaldir){
                                UygulamalarRepository uygulamalarRepository = new UygulamalarRepository(mcontext);
                                consts.getUygulamalarListesi().remove(uygulama);
                                consts.getCopUygulamaListesi().add(uygulama);
                                uygulamalarRepository.copKutusunaTasi(uygulama);
                                notifyDataSetChanged();
                                return true;
                            }
                            return false;
                        }
                    });
                    popup.show();
                    return true;
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return consts.getUygulamalarListesi().size();
    }

    public class AnaekranTasarimTutucu extends RecyclerView.ViewHolder{

        UygulamalarRecyclerCardBinding binding;

        public AnaekranTasarimTutucu(UygulamalarRecyclerCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}
