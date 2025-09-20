package com.example.cookieclickerjava;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookieclickerjava.databinding.ItemUpgradeBinding;

import java.text.DecimalFormat;
import java.util.List;

public class UpgradeAdapter extends RecyclerView.Adapter<UpgradeAdapter.VH> {

    public interface Listener {
        void onBuyClicked(@NonNull Upgrade item);
    }

    @NonNull
    private final List<Upgrade> items;
    @NonNull
    private final Listener listener;

    private final DecimalFormat df = new DecimalFormat("#,###");
    private double currentScore = 0.0;
    private double discountPct = 0.0;

    public UpgradeAdapter(@NonNull List<Upgrade> items,
                          @NonNull Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void updateAffordability(double score, double discountPct) {
        this.currentScore = score;
        this.discountPct = discountPct;
        notifyDataSetChanged();
    }

    public void updateAffordability(double score) {
        this.currentScore = score;
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemUpgradeBinding b;
        VH(ItemUpgradeBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemUpgradeBinding binding = ItemUpgradeBinding.inflate(inflater, parent, false);
        return new VH(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Upgrade item = items.get(position);
        ItemUpgradeBinding b = holder.b;

        double raw = item.currentPrice();
        double price = raw * (1.0 - discountPct);

        b.imgIcon.setImageResource(item.getIconRes());
        if (item.getLevel() > 0) {
            b.tvTitle.setText(item.getTitle() + " (Lv " + item.getLevel() + ")");
        } else {
            b.tvTitle.setText(item.getTitle());
        }

        b.tvDesc.setText(item.getDesc());
        b.tvPrice.setText(df.format(price));

        boolean canAfford = currentScore >= price;
        b.btnBuy.setEnabled(canAfford);
        b.btnBuy.setAlpha(canAfford ? 1f : 0.5f);

        // Açıklamayı boşsa gizle
        ViewCompat.setTransitionName(b.tvDesc, "tvDesc");
        b.tvDesc.setVisibility(item.getDesc() != null && !item.getDesc().isEmpty()
                ? android.view.View.VISIBLE : android.view.View.GONE);

        b.btnBuy.setOnClickListener(v -> listener.onBuyClicked(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
