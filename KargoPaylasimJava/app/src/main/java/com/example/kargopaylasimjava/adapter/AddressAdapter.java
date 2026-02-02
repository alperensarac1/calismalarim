package com.example.kargopaylasimjava.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kargopaylasimjava.R;
import com.example.kargopaylasimjava.dto.AddressDtos;

import java.util.ArrayList;
import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.VH> {

    public interface OnAction {
        void on(AddressDtos.AddressDto a);
    }

    private final OnAction onEdit;
    private final OnAction onSetDefault;
    private final OnAction onDelete;

    private final ArrayList<AddressDtos.AddressDto> items = new ArrayList<>();

    public AddressAdapter(OnAction onEdit, OnAction onSetDefault, OnAction onDelete) {
        this.onEdit = onEdit;
        this.onSetDefault = onSetDefault;
        this.onDelete = onDelete;
    }

    public void submit(List<AddressDtos.AddressDto> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_address, parent, false);
        return new VH(v);
    }

    @Override
    public int getItemCount() { return items.size(); }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(items.get(position));
    }

    class VH extends RecyclerView.ViewHolder {
        private final TextView tvTitle, tvDefault, tvCityDist, tvLine;
        private final Button btnEdit, btnDefault, btnDelete;

        VH(@NonNull View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvAddrTitle);
            tvDefault = v.findViewById(R.id.tvDefault);
            tvCityDist = v.findViewById(R.id.tvCityDist);
            tvLine = v.findViewById(R.id.tvLine);
            btnEdit = v.findViewById(R.id.btnEdit);
            btnDefault = v.findViewById(R.id.btnDefault);
            btnDelete = v.findViewById(R.id.btnDelete);
        }

        void bind(AddressDtos.AddressDto a) {
            tvTitle.setText(a.title);
            tvCityDist.setText(a.city + " / " + a.district);
            tvLine.setText(a.address_line);

            boolean isDef = a.is_default == 1;
            tvDefault.setVisibility(isDef ? View.VISIBLE : View.GONE);
            btnDefault.setEnabled(!isDef);

            btnEdit.setOnClickListener(v -> onEdit.on(a));
            btnDefault.setOnClickListener(v -> onSetDefault.on(a));
            btnDelete.setOnClickListener(v -> onDelete.on(a));
        }
    }
}

