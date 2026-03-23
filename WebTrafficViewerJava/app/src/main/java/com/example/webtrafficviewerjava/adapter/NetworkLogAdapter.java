package com.example.webtrafficviewerjava.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.webtrafficviewerjava.R;
import com.example.webtrafficviewerjava.model.NetworkLog;

import java.util.ArrayList;
import java.util.List;

public class NetworkLogAdapter extends RecyclerView.Adapter<NetworkLogAdapter.NetworkLogViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(NetworkLog log);
    }

    private final List<NetworkLog> items = new ArrayList<>();
    private final OnItemClickListener listener;

    public NetworkLogAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public NetworkLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_network_log, parent, false);
        return new NetworkLogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NetworkLogViewHolder holder, int position) {
        NetworkLog item = items.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void submitList(List<NetworkLog> newList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
                new NetworkLogDiffCallback(this.items, newList)
        );

        this.items.clear();
        this.items.addAll(newList);
        diffResult.dispatchUpdatesTo(this);
    }

    public static class NetworkLogViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvMethod;
        private final TextView tvType;
        private final TextView tvUrl;
        private final TextView tvHost;
        private final TextView tvTime;
        private final TextView tvBodyPreview;

        public NetworkLogViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMethod = itemView.findViewById(R.id.tvMethod);
            tvType = itemView.findViewById(R.id.tvType);
            tvUrl = itemView.findViewById(R.id.tvUrl);
            tvHost = itemView.findViewById(R.id.tvHost);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvBodyPreview = itemView.findViewById(R.id.tvBodyPreview);
        }

        public void bind(final NetworkLog item, final OnItemClickListener listener) {
            tvMethod.setText("Method: " + item.getMethod() + " | Kaynak: " + item.getSource());
            tvType.setText("Tip: " + item.getResourceType());
            tvUrl.setText("URL: " + item.getUrl());
            tvHost.setText("Host: " + item.getHost());
            tvTime.setText("Zaman: " + item.getTime());

            String preview = (item.getRequestBody() != null && !item.getRequestBody().trim().isEmpty())
                    ? item.getRequestBody().substring(0, Math.min(item.getRequestBody().length(), 150))
                    : "yok";

            tvBodyPreview.setText("Body: " + preview);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });
        }
    }

    static class NetworkLogDiffCallback extends DiffUtil.Callback {

        private final List<NetworkLog> oldList;
        private final List<NetworkLog> newList;

        public NetworkLogDiffCallback(List<NetworkLog> oldList, List<NetworkLog> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            NetworkLog oldItem = oldList.get(oldItemPosition);
            NetworkLog newItem = newList.get(newItemPosition);

            return safeEquals(oldItem.getMethod(), newItem.getMethod())
                    && safeEquals(oldItem.getUrl(), newItem.getUrl())
                    && safeEquals(oldItem.getSource(), newItem.getSource())
                    && safeEquals(oldItem.getTime(), newItem.getTime());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            NetworkLog oldItem = oldList.get(oldItemPosition);
            NetworkLog newItem = newList.get(newItemPosition);

            return safeEquals(oldItem.getMethod(), newItem.getMethod())
                    && safeEquals(oldItem.getUrl(), newItem.getUrl())
                    && safeEquals(oldItem.getHost(), newItem.getHost())
                    && safeEquals(oldItem.getTime(), newItem.getTime())
                    && safeEquals(oldItem.getResourceType(), newItem.getResourceType())
                    && safeEquals(oldItem.getRequestBody(), newItem.getRequestBody())
                    && safeEquals(oldItem.getSource(), newItem.getSource())
                    && oldItem.isMainFrame() == newItem.isMainFrame();
        }

        private boolean safeEquals(String a, String b) {
            if (a == null && b == null) return true;
            if (a == null || b == null) return false;
            return a.equals(b);
        }
    }
}
