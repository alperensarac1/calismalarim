package com.example.canliyayinkotlin.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.canliyayinkotlin.databinding.ItemChatMessageBinding
import com.example.canliyayinkotlin.model.ChatMessageModel

class ChatAdapter(
    private val messages: MutableList<ChatMessageModel>
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(
        private val binding: ItemChatMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ChatMessageModel) {
            binding.tvChatUsername.text = item.username
            binding.tvChatMessage.text = item.message
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size

    fun addMessage(message: ChatMessageModel) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }
}