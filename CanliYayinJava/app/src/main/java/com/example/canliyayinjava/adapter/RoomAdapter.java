package com.example.canliyayinjava.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.canliyayinjava.R;
import com.example.canliyayinjava.model.RoomModel;

import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {

    public interface OnRoomClickListener {
        void onRoomClick(RoomModel room);
    }

    private final List<RoomModel> roomList;
    private final OnRoomClickListener listener;

    public RoomAdapter(List<RoomModel> roomList, OnRoomClickListener listener) {
        this.roomList = roomList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_room, parent, false);

        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        RoomModel room = roomList.get(position);

        holder.tvRoomTitle.setText(room.getTitle());
        holder.tvBroadcasterName.setText("Yayıncı: " + room.getBroadcasterName());
        holder.tvViewerCount.setText("İzleyici: " + room.getViewerCount());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRoomClick(room);
            }
        });
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    public void updateRooms(List<RoomModel> newRooms) {
        roomList.clear();
        roomList.addAll(newRooms);
        notifyDataSetChanged();
    }

    static class RoomViewHolder extends RecyclerView.ViewHolder {

        TextView tvRoomTitle;
        TextView tvBroadcasterName;
        TextView tvViewerCount;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);

            tvRoomTitle = itemView.findViewById(R.id.tvRoomTitle);
            tvBroadcasterName = itemView.findViewById(R.id.tvBroadcasterName);
            tvViewerCount = itemView.findViewById(R.id.tvViewerCount);
        }
    }
}