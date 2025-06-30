package com.example.isletimsistemicalismasi.uygulamalar.copkutusu;

import android.content.Context;
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

public class CopKutusuAdapter extends RecyclerView.Adapter<CopKutusuAdapter.CopKutusuTasarimTutucu>{
    private Context mcontext;
    Const consts;

    public CopKutusuAdapter(Context context, Const consts) {
        this.mcontext = context;
        this.consts = consts;
    }

    @NonNull
    @Override
    public CopKutusuAdapter.CopKutusuTasarimTutucu onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        UygulamalarRecyclerCardBinding binding = UygulamalarRecyclerCardBinding.inflate(LayoutInflater.from(mcontext),parent,false);
        return new CopKutusuAdapter.CopKutusuTasarimTutucu(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CopKutusuTasarimTutucu holder, int position) {
        UygulamalarModel uygulama = consts.getCopUygulamaListesi().get(position);

        holder.binding.tvUygulama.setText(uygulama.getUygulamaAdi());
        holder.binding.imgUygulama.setImageResource(mcontext.getResources().getIdentifier(uygulama.getUygulamaResimAdi(),"drawable",mcontext.getPackageName()));

        holder.binding.cardUygulama.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                PopupMenu popup = new PopupMenu(mcontext,v);
                popup.getMenuInflater().inflate(R.menu.uygulama_geriyukle_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.action_geri_yukle){
                            UygulamalarRepository uygulamalarRepository = new UygulamalarRepository(mcontext);
                            consts.getCopUygulamaListesi().remove(uygulama);
                            consts.getUygulamalarListesi().add(uygulama);
                            uygulamalarRepository.copKutusundanCikar(uygulama);
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


    @Override
    public int getItemCount() {
        return consts.getCopUygulamaListesi().size();
    }

    public class CopKutusuTasarimTutucu extends RecyclerView.ViewHolder{

        UygulamalarRecyclerCardBinding binding;

        public CopKutusuTasarimTutucu(UygulamalarRecyclerCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}

