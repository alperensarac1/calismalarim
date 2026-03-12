package com.example.yardimuygulamajava.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yardimuygulamajava.R;
import com.example.yardimuygulamajava.model.ConfirmedHelpItem;

import java.util.ArrayList;
import java.util.List;

public class ConfirmedHelpAdapter extends RecyclerView.Adapter<ConfirmedHelpAdapter.VH> {

    private final List<ConfirmedHelpItem> items = new ArrayList<>();

    public void submit(List<ConfirmedHelpItem> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone, tvService, tvRoom, tvConfirmedAt;
        VH(@NonNull View v) {
            super(v);
            tvName = v.findViewById(R.id.tvName);
            tvPhone = v.findViewById(R.id.tvPhone);
            tvService = v.findViewById(R.id.tvService);
            tvRoom = v.findViewById(R.id.tvRoom);
            tvConfirmedAt = v.findViewById(R.id.tvConfirmedAt);
        }
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_confirmed_help, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ConfirmedHelpItem it = items.get(position);
        h.tvName.setText(safe(it.patient_name));
        h.tvPhone.setText("Telefon: " + safe(it.patient_phone));
        h.tvService.setText("Servis: " + safe(it.servis_adi));
        h.tvRoom.setText("Oda: " + safe(it.oda_no));
        h.tvConfirmedAt.setText("Onay: " + safe(it.confirmed_at));
    }

    @Override public int getItemCount() { return items.size(); }

    private String safe(String s) { return s != null ? s : "-"; }
}
