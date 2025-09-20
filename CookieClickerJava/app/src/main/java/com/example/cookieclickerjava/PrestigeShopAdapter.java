package com.example.cookieclickerjava;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PrestigeShopAdapter extends RecyclerView.Adapter<PrestigeShopAdapter.PerkVH> {

    public interface CanAfford {
        boolean invoke(int cost);
    }

    public interface OnBuy {
        void invoke(@NonNull PrestigePerk perk);
    }

    @NonNull private final List<PrestigePerk> items;
    @NonNull private final CanAfford canAfford;
    @NonNull private final OnBuy onBuy;

    public PrestigeShopAdapter(@NonNull List<PrestigePerk> items,
                               @NonNull CanAfford canAfford,
                               @NonNull OnBuy onBuy) {
        this.items = items;
        this.canAfford = canAfford;
        this.onBuy = onBuy;
    }

    @NonNull
    @Override
    public PerkVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_perk, parent, false);
        return new PerkVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PerkVH h, int position) {
        PrestigePerk p = items.get(position);

        h.tvTitle.setText(p.getTitle());
        h.tvDesc.setText(p.getDesc());

        int cost = p.costForNext();
        String meta = "Lv " + p.getLevel() + " • Maliyet: " + cost;
        h.tvMeta.setText(meta);

        boolean can = canAfford.invoke(cost) && p.getLevel() < p.getMaxLevel();
        h.btnBuy.setEnabled(can);
        h.btnBuy.setAlpha(can ? 1f : 0.5f);

        h.btnBuy.setOnClickListener(v -> onBuy.invoke(p));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class PerkVH extends RecyclerView.ViewHolder {
        final TextView tvTitle;
        final TextView tvDesc;
        final TextView tvMeta;
        final Button btnBuy;

        PerkVH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDesc  = itemView.findViewById(R.id.tvDesc);
            tvMeta  = itemView.findViewById(R.id.tvMeta);
            btnBuy  = itemView.findViewById(R.id.btnBuy);
        }
    }
}
