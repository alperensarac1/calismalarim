package com.example.onlinetaksijava.ui.driver;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlinetaksijava.data.remote.model.AvailableRideItem;
import com.example.onlinetaksijava.databinding.ItemAvailableRideBinding;

import java.util.ArrayList;
import java.util.List;

public class AvailableRideAdapter extends RecyclerView.Adapter<AvailableRideAdapter.AvailableRideVH> {

    public interface OnAcceptClickListener {
        void onAccept(AvailableRideItem item);
    }

    private final List<AvailableRideItem> items = new ArrayList<>();
    private final OnAcceptClickListener listener;

    public AvailableRideAdapter(OnAcceptClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public AvailableRideVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAvailableRideBinding binding = ItemAvailableRideBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new AvailableRideVH(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AvailableRideVH holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void submitList(List<AvailableRideItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    class AvailableRideVH extends RecyclerView.ViewHolder {

        private final ItemAvailableRideBinding binding;

        public AvailableRideVH(ItemAvailableRideBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(AvailableRideItem item) {
            binding.tvRideId.setText("Ride ID: " + item.getId());
            binding.tvPickup.setText("Pickup: " + item.getPickup_address());
            binding.tvDropoff.setText("Dropoff: " + item.getDropoff_address());
            binding.tvFare.setText("Tahmini Ücret: " + (item.getEstimated_fare() != null ? item.getEstimated_fare() : "-"));

            binding.btnAcceptRide.setOnClickListener(v -> listener.onAccept(item));
        }
    }
}

