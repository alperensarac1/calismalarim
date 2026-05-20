package com.example.canliyayinjava.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.canliyayinjava.R;
import com.example.canliyayinjava.model.ChatMessageModel;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private final List<ChatMessageModel> messages;

    public ChatAdapter(List<ChatMessageModel> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);

        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessageModel item = messages.get(position);

        holder.tvChatUsername.setText(item.getUsername());
        holder.tvChatMessage.setText(item.getMessage());
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void addMessage(ChatMessageModel message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {

        TextView tvChatUsername;
        TextView tvChatMessage;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);

            tvChatUsername = itemView.findViewById(R.id.tvChatUsername);
            tvChatMessage = itemView.findViewById(R.id.tvChatMessage);
        }
    }
}
