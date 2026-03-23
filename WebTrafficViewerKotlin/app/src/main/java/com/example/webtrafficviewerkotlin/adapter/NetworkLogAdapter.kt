package com.example.webtrafficviewerkotlin.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.webtrafficviewerkotlin.R
import com.example.webtrafficviewerkotlin.databinding.ItemNetworkLogBinding
import com.example.webtrafficviewerkotlin.model.NetworkLog

class NetworkLogAdapter(
    private val onItemClick: (NetworkLog) -> Unit
) : ListAdapter<NetworkLog, NetworkLogAdapter.NetworkLogViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NetworkLogViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemNetworkLogBinding.inflate(inflater, parent, false)
        return NetworkLogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NetworkLogViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, onItemClick)
    }

    class NetworkLogViewHolder(
        private val binding: ItemNetworkLogBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: NetworkLog, onItemClick: (NetworkLog) -> Unit) {
            binding.tvMethod.text = "Method: ${item.method} | Kaynak: ${item.source}"
            binding.tvType.text = "Tip: ${item.resourceType}"
            binding.tvUrl.text = "URL: ${item.url}"
            binding.tvHost.text = "Host: ${item.host}"
            binding.tvTime.text = "Zaman: ${item.time}"

            val previewText = item.requestBody?.takeIf { it.isNotBlank() }?.take(150) ?: "yok"
            binding.tvBodyPreview.text = "Body: $previewText"

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<NetworkLog>() {

        override fun areItemsTheSame(oldItem: NetworkLog, newItem: NetworkLog): Boolean {
            return oldItem.method == newItem.method &&
                    oldItem.url == newItem.url &&
                    oldItem.source == newItem.source &&
                    oldItem.time == newItem.time
        }

        override fun areContentsTheSame(oldItem: NetworkLog, newItem: NetworkLog): Boolean {
            return oldItem == newItem
        }
    }
}