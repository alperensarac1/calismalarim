package com.example.sozlukjava.adapter;

// EntryAdapter.java
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sozlukjava.R;
import com.example.sozlukjava.model.Entry;

import java.util.List;

public class EntryAdapter extends RecyclerView.Adapter<EntryAdapter.EntryViewHolder> {

    public interface OnEntryClick {
        void onClick(Entry entry);
    }

    public interface OnEntryLongClick {
        void onLongClick(Entry entry);
    }

    private final List<Entry> entryList;
    private final OnEntryClick onClick;
    private final OnEntryLongClick onLongClick;

    public EntryAdapter(List<Entry> entryList, OnEntryClick onClick, OnEntryLongClick onLongClick) {
        this.entryList = entryList;
        this.onClick = onClick;
        this.onLongClick = onLongClick;
    }

    static class EntryViewHolder extends RecyclerView.ViewHolder {
        TextView tvBaslik, tvIcerik, tvUsername, tvTarih;

        EntryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBaslik = itemView.findViewById(R.id.tvBaslik);
            tvIcerik = itemView.findViewById(R.id.tvIcerik);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvTarih = itemView.findViewById(R.id.tvTarih);
        }
    }

    @NonNull
    @Override
    public EntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.entry_card, parent, false);
        return new EntryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull EntryViewHolder holder, int position) {
        Entry entry = entryList.get(position);
        holder.tvBaslik.setText(entry.getTitle());
        holder.tvIcerik.setText(entry.getContent());
        holder.tvUsername.setText(entry.getUsername());

        String created = entry.getCreated_at();
        if (created != null && created.length() >= 10) {
            holder.tvTarih.setText(created.substring(0, 10));
        } else {
            holder.tvTarih.setText("");
        }

        holder.itemView.setOnClickListener(v -> {
            if (onClick != null) onClick.onClick(entry);
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (onLongClick != null) onLongClick.onLongClick(entry);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return entryList != null ? entryList.size() : 0;
    }
}

