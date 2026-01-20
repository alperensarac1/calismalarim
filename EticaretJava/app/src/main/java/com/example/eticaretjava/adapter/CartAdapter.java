package com.example.eticaretjava.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;


import com.example.eticaretjava.databinding.CartItemBinding;
import com.example.eticaretjava.model.Cart;

public class CartAdapter extends ListAdapter<Cart.CartItemDto, CartAdapter.VH> {

    public interface OnPlus { void onPlus(Cart.CartItemDto item); }
    public interface OnMinus { void onMinus(Cart.CartItemDto item); }
    public interface OnDelete { void onDelete(Cart.CartItemDto item); }

    private final OnPlus onPlus;
    private final OnMinus onMinus;
    private final OnDelete onDelete;

    private Integer busyItemId = null;

    public CartAdapter(OnPlus onPlus, OnMinus onMinus, OnDelete onDelete) {
        super(DIFF);
        this.onPlus = onPlus;
        this.onMinus = onMinus;
        this.onDelete = onDelete;
    }

    /** CartViewModel.state.busyItemId buraya ver */
    public void setBusyItemId(Integer id) {
        this.busyItemId = id;
        notifyDataSetChanged(); // satır kilidi için yeterli
    }

    static class VH extends RecyclerView.ViewHolder {
        final CartItemBinding b;

        VH(CartItemBinding b) {
            super(b.getRoot());
            this.b = b;
        }

        void bind(Cart.CartItemDto item,
                  Integer busyId,
                  OnPlus onPlus,
                  OnMinus onMinus,
                  OnDelete onDelete) {

            b.tvName.setText(item.name != null ? item.name : "Ürün");
            b.tvQty.setText(String.valueOf(item.quantity));

            // sale_price her zaman dolu değilse fallback yapalım
            double shownPrice = item.sale_price > 0 ? item.sale_price : item.price;
            b.tvPrice.setText("₺" + String.format("%.2f", shownPrice));

            boolean busy = busyId != null && busyId == item.item_id;

            b.progressRow.setVisibility(busy ? View.VISIBLE : View.GONE);

            b.btnPlus.setEnabled(!busy);
            b.btnMinus.setEnabled(!busy);
            b.btnDelete.setEnabled(!busy);

            b.btnPlus.setOnClickListener(v -> onPlus.onPlus(item));
            b.btnMinus.setOnClickListener(v -> onMinus.onMinus(item));
            b.btnDelete.setOnClickListener(v -> onDelete.onDelete(item));
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CartItemBinding b = CartItemBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(getItem(position), busyItemId, onPlus, onMinus, onDelete);
    }

    private static final DiffUtil.ItemCallback<Cart.CartItemDto> DIFF =
            new DiffUtil.ItemCallback<Cart.CartItemDto>() {
                @Override
                public boolean areItemsTheSame(@NonNull Cart.CartItemDto o,
                                               @NonNull Cart.CartItemDto n) {
                    return o.item_id == n.item_id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull Cart.CartItemDto o,
                                                  @NonNull Cart.CartItemDto n) {
                    return o.quantity == n.quantity
                            && o.product_id == n.product_id
                            && safeEq(o.name, n.name)
                            && Double.compare(o.price, n.price) == 0
                            && Double.compare(o.sale_price, n.sale_price) == 0;
                }

                private boolean safeEq(Object a, Object b) {
                    return a == b || (a != null && a.equals(b));
                }
            };
}
