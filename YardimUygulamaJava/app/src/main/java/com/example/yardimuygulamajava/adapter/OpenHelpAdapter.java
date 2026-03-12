package com.example.yardimuygulamajava.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yardimuygulamajava.R;
import com.example.yardimuygulamajava.model.OpenHelpItem;

import java.util.ArrayList;
import java.util.List;

public class OpenHelpAdapter extends RecyclerView.Adapter<OpenHelpAdapter.VH> {

    public interface OnAccept { void onAccept(OpenHelpItem item); }

    private final List<OpenHelpItem> items = new ArrayList<>();
    private final OnAccept onAccept;

    public OpenHelpAdapter(OnAccept onAccept) {
        this.onAccept = onAccept;
    }

    public void submit(List<OpenHelpItem> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvAge, tvCreated;
        Button btnAccept;

        VH(@NonNull View v) {
            super(v);
            tvName = v.findViewById(R.id.tvName);
            tvAge = v.findViewById(R.id.tvAge);
            tvCreated = v.findViewById(R.id.tvCreated);
            btnAccept = v.findViewById(R.id.btnAccept);
        }
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_open_help, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        OpenHelpItem it = items.get(pos);
        h.tvName.setText(it.patient_name);
        h.tvAge.setText("Yaş: " + (it.patient_age != null ? it.patient_age : "-"));
        h.tvCreated.setText("İstek: " + it.created_at);
        h.btnAccept.setOnClickListener(v -> onAccept.onAccept(it));
    }

    @Override public int getItemCount() { return items.size(); }
}
