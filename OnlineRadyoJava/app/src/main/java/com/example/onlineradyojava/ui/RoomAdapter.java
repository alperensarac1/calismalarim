package com.example.onlineradyojava.ui;



import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlineradyojava.data.RadioRoom;
import com.example.onlineradyojava.databinding.ItemRoomBinding;

import java.util.ArrayList;
import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {

    private List<RadioRoom> roomList = new ArrayList<>();
    private OnRoomClickListener listener;

    public RoomAdapter(OnRoomClickListener listener) {
        this.listener = listener;
    }

    public void updateRooms(List<RadioRoom> newRooms) {
        roomList.clear();
        roomList.addAll(newRooms);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        ItemRoomBinding binding = ItemRoomBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );

        return new RoomViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(
            @NonNull RoomViewHolder holder,
            int position
    ) {
        holder.bind(roomList.get(position));
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    class RoomViewHolder extends RecyclerView.ViewHolder {

        private ItemRoomBinding binding;

        public RoomViewHolder(ItemRoomBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(RadioRoom room) {
            binding.tvRoomName.setText(room.getRoomName());

            if (room.getCurrentMusic() == null || room.getCurrentMusic().isEmpty()) {
                binding.tvCurrentMusic.setText("Şu an: Müzik yok");
            } else {
                binding.tvCurrentMusic.setText("Şu an: " + room.getCurrentMusic());
            }

            binding.tvListenerCount.setText("Dinleyici: " + room.getListenerCount());

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRoomClick(room);
                }
            });
        }
    }

    public interface OnRoomClickListener {
        void onRoomClick(RadioRoom room);
    }
}
