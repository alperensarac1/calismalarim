package com.example.kargopaylasimjava.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kargopaylasimjava.R;
import com.example.kargopaylasimjava.dto.ShipmentDtos;
import com.example.kargopaylasimjava.util.DateUtil;

import java.util.ArrayList;
import java.util.List;

public class ShipmentAdapter extends RecyclerView.Adapter<ShipmentAdapter.VH> {

    public interface OnClick {
        void onClick(ShipmentDtos.ShipmentDto item);
    }

    private List<ShipmentDtos.ShipmentDto> items = new ArrayList<>();
    private final OnClick onClick;

    public ShipmentAdapter(List<ShipmentDtos.ShipmentDto> items, OnClick onClick) {
        if (items != null) this.items = items;
        this.onClick = onClick;
    }

    public void submit(List<ShipmentDtos.ShipmentDto> newItems) {
        items = (newItems != null) ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    class VH extends RecyclerView.ViewHolder {
        TextView tvCode, tvStatus, tvRemaining, tvCompany;

        VH(@NonNull View v) {
            super(v);
            tvCode = v.findViewById(R.id.tvCode);
            tvStatus = v.findViewById(R.id.tvStatus);
            tvRemaining = v.findViewById(R.id.tvRemaining);
            tvCompany = v.findViewById(R.id.tvCompany);
        }

        void bind(ShipmentDtos.ShipmentDto item) {
            if ("RECEIVER".equals(item.role) && Boolean.FALSE.equals(item.visible)) {
                tvCode.setText("Henüz firma onaylamadı");
                tvStatus.setText("Durum: " + item.status);
                tvRemaining.setText("-");
                tvCompany.setText("-");
                itemView.setOnClickListener(null);
                return;
            }

            String companyLine = (item.cargoCompanyName != null && !item.cargoCompanyName.trim().isEmpty())
                    ? "Firma: " + item.cargoCompanyName
                    : "Firma: -";
            tvCompany.setText(companyLine);

            if ("SENDER".equals(item.role)) {
                tvCode.setText("Kod: " + item.pickupCode);
                tvStatus.setText("Durum: " + item.status);
                tvRemaining.setText("Kalan: " + DateUtil.remainingText(item.codeExpiresAt));
            } else {
                tvCode.setText("Gönderici: " + (item.senderInitials != null ? item.senderInitials : "-"));
                tvStatus.setText("Durum: " + item.status);
                tvRemaining.setText("Adres: " + (item.receiverAddressTitle != null ? item.receiverAddressTitle : "-"));
            }

            itemView.setOnClickListener(v -> onClick.onClick(item));
        }
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shipment, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}

