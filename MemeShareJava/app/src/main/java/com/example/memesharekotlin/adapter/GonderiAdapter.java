package com.example.memesharekotlin.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.memesharekotlin.R;
import com.example.memesharekotlin.model.GonderiModel;

import java.util.List;

public class GonderiAdapter extends RecyclerView.Adapter<GonderiAdapter.GonderiViewHolder> {

    private List<GonderiModel> gonderiList;
    private Context context;
    private int currentUserId;

    public GonderiAdapter(Context context, List<GonderiModel> gonderiList, int currentUserId) {
        this.context = context;
        this.gonderiList = gonderiList;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        GonderiModel item = gonderiList.get(position);
        return (item.getUserId() == currentUserId) ? 1 : 0;
    }

    @NonNull
    @Override
    public GonderiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 1) {
            view = LayoutInflater.from(context).inflate(R.layout.meme_item_right, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.meme_item_left, parent, false);
        }
        return new GonderiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GonderiViewHolder holder, int position) {
        GonderiModel item = gonderiList.get(position);

        holder.tvGonderenAdi.setText("Kullanıcı #" + item.getUserId());
        holder.tvGonderiTarih.setText(item.getUploadedAt());

        String fullUrl = "https://alperensaracdeneme.com/meme/" + item.getMediaUrl();

        if (item.getMediaType().equals("image")) {
            holder.imgPost.setVisibility(View.VISIBLE);
            holder.videoPost.setVisibility(View.GONE);
            holder.btnOynat.setVisibility(View.GONE);

            Glide.with(context)
                    .load(fullUrl)
                    .into(holder.imgPost);

        } else if (item.getMediaType().equals("video")) {
            // İlk olarak sadece thumbnail göster
            holder.imgPost.setVisibility(View.VISIBLE);
            holder.videoPost.setVisibility(View.GONE);
            holder.btnOynat.setVisibility(View.VISIBLE);

            Glide.with(context)
                    .load(fullUrl)
                    .frame(1000000) // 1. saniyeden kare
                    .into(holder.imgPost);

            holder.btnOynat.setOnClickListener(v -> {
                holder.imgPost.setVisibility(View.GONE);
                holder.videoPost.setVisibility(View.VISIBLE);
                holder.btnOynat.setVisibility(View.GONE);

                holder.videoPost.setVideoURI(Uri.parse(fullUrl));
                holder.videoPost.start();
            });

            holder.videoPost.setOnCompletionListener(mp -> {
                holder.videoPost.setVisibility(View.GONE);
                holder.imgPost.setVisibility(View.VISIBLE);
                holder.btnOynat.setVisibility(View.VISIBLE);
            });
        }
    }

    @Override
    public int getItemCount() {
        return gonderiList.size();
    }

    public static class GonderiViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPost;
        VideoView videoPost;
        Button btnOynat;
        TextView tvGonderenAdi, tvGonderiTarih;

        public GonderiViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPost = itemView.findViewById(R.id.imgPost);
            videoPost = itemView.findViewById(R.id.videoPost);
            btnOynat = itemView.findViewById(R.id.btnOynat);
            tvGonderenAdi = itemView.findViewById(R.id.tvGonderenAdi);
            tvGonderiTarih = itemView.findViewById(R.id.tvGonderiTarih);
        }
    }
}
