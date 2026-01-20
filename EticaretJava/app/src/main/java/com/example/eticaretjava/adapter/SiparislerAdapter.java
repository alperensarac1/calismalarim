package com.example.eticaretjava.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eticaretjava.databinding.SiparisItemBinding;
import com.example.eticaretjava.model.Order;

public class SiparislerAdapter
        extends ListAdapter<Order.OrderSummaryDto, SiparislerAdapter.VH> {

    public interface OnClick {
        void onClick(int id);
    }

    private final OnClick onClick;

    public SiparislerAdapter(OnClick onClick) {
        super(DIFF);
        this.onClick = onClick;
    }

    static class VH extends RecyclerView.ViewHolder {
        SiparisItemBinding b;

        VH(SiparisItemBinding b) {
            super(b.getRoot());
            this.b = b;
        }

        void bind(Order.OrderSummaryDto o, OnClick onClick) {
            b.tvTitle.setText("Sipariş #" + o.id);
            b.tvSub.setText(o.status + " • ₺" + String.format("%.2f", o.totalAmount));
            b.getRoot().setOnClickListener(v -> onClick.onClick(o.id));
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        SiparisItemBinding b = SiparisItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(getItem(position), onClick);
    }

    private static final DiffUtil.ItemCallback<Order.OrderSummaryDto> DIFF =
            new DiffUtil.ItemCallback<Order.OrderSummaryDto>() {
                @Override
                public boolean areItemsTheSame(@NonNull Order.OrderSummaryDto o,
                                               @NonNull Order.OrderSummaryDto n) {
                    return o.id == n.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull Order.OrderSummaryDto o,
                                                  @NonNull Order.OrderSummaryDto n) {
                    return false;
                }
            };
}
