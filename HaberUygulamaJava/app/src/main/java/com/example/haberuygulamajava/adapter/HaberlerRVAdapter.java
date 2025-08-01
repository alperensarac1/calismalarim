package com.example.haberuygulamajava.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.haberuygulamajava.R;
import com.example.haberuygulamajava.model.HaberModel;

import java.util.List;

public class HaberlerRVAdapter extends RecyclerView.Adapter<HaberlerRVAdapter.HaberViewHolder> {

    private List<HaberModel> haberList;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(HaberModel haber);
    }

    public HaberlerRVAdapter(Context context, List<HaberModel> haberList, OnItemClickListener listener) {
        this.context = context;
        this.haberList = haberList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HaberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.haberlercell, parent, false);
        return new HaberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HaberViewHolder holder, int position) {
        HaberModel haber = haberList.get(position);
        holder.tvHaber.setText(haber.getBaslik());

        if ("video".equals(haber.getMedia_type())) {
            holder.imgHaber.setVisibility(View.GONE);
            holder.videoView.setVisibility(View.VISIBLE);
            holder.btnPlay.setVisibility(View.VISIBLE);

            Uri videoUri = Uri.parse(haber.getMedia_url());
            holder.videoView.setVideoURI(videoUri);

            holder.btnPlay.setOnClickListener(v -> {
                holder.videoView.start();
                holder.btnPlay.setVisibility(View.GONE);
            });

            holder.videoView.setOnCompletionListener(mp -> holder.btnPlay.setVisibility(View.VISIBLE));

        } else {
            holder.videoView.setVisibility(View.GONE);
            holder.btnPlay.setVisibility(View.GONE);
            holder.imgHaber.setVisibility(View.VISIBLE);

            Glide.with(context)
                    .load(haber.getMedia_url())
                    .placeholder(R.drawable.resim)
                    .into(holder.imgHaber);
        }

        holder.tvDevaminiOku.setOnClickListener(v -> listener.onItemClick(haber));
    }

    @Override
    public int getItemCount() {
        return haberList.size();
    }

    public class HaberViewHolder extends RecyclerView.ViewHolder {
        TextView tvHaber;
        TextView tvDevaminiOku;
        ImageView imgHaber;
        VideoView videoView;
        ImageButton btnPlay;

        public HaberViewHolder(@NonNull View itemView) {
            super(itemView);

            tvHaber = itemView.findViewById(R.id.tvHaber);
            tvDevaminiOku = itemView.findViewById(R.id.tvDevaminiOku);
            imgHaber = itemView.findViewById(R.id.imgHaber);
            videoView = itemView.findViewById(R.id.videoView);
            btnPlay = itemView.findViewById(R.id.btnPlay);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(haberList.get(position));
                }
            });
        }
    }
}

