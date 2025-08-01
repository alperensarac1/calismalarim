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
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.haberuygulamajava.R;
import com.example.haberuygulamajava.model.HaberModel;

import java.util.List;

public class SonUcHaberRVAdapter extends RecyclerView.Adapter<SonUcHaberRVAdapter.HaberViewHolder> {

    private List<HaberModel> haberList;
    private OnItemClickListener listener;
    private Context context;

    public interface OnItemClickListener {
        void onItemClick(HaberModel haber);
    }

    public SonUcHaberRVAdapter(Context context,List<HaberModel> haberList, OnItemClickListener listener) {
        this.context = context;
        this.haberList = haberList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HaberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sonuchabercell, parent, false);
        return new HaberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HaberViewHolder holder, int position) {
        HaberModel haber = haberList.get(position);
        holder.tvHaberBaslik.setText(haber.getBaslik());

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
            holder.imgHaber.setVisibility(View.VISIBLE);
            holder.videoView.setVisibility(View.GONE);
            holder.btnPlay.setVisibility(View.GONE);

            Glide.with(context)
                    .load(haber.getMedia_url())
                    .placeholder(R.drawable.resim)
                    .into(holder.imgHaber);
        }
    }

    @Override
    public int getItemCount() {
        return haberList.size();
    }

    public class HaberViewHolder extends RecyclerView.ViewHolder {
        TextView tvHaberBaslik;
        CardView haberCard;
        ImageView imgHaber;
        VideoView videoView;
        ImageButton btnPlay;

        public HaberViewHolder(@NonNull View itemView) {
            super(itemView);

            tvHaberBaslik = itemView.findViewById(R.id.tvHaberBaslik);
            haberCard = itemView.findViewById(R.id.sonHaberCard);
            imgHaber = itemView.findViewById(R.id.imgHaber);
            videoView = itemView.findViewById(R.id.videoHaber);
            btnPlay = itemView.findViewById(R.id.btnPlay);

            haberCard.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(haberList.get(position));
                }
            });
        }
    }
}
