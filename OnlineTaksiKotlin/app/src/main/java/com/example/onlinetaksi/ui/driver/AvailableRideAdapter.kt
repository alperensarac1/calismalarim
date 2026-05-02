package com.example.onlinetaksi.ui.driver


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.onlinetaksi.data.remote.model.AvailableRideItem
import com.example.onlinetaksi.databinding.ItemAvailableRideBinding

class AvailableRideAdapter(
    private val onAcceptClick: (AvailableRideItem) -> Unit
) : RecyclerView.Adapter<AvailableRideAdapter.AvailableRideVH>() {

    private val items = mutableListOf<AvailableRideItem>()

    inner class AvailableRideVH(
        private val binding: ItemAvailableRideBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AvailableRideItem) {
            binding.tvRideId.text = "Ride ID: ${item.id}"
            binding.tvPickup.text = "Pickup: ${item.pickup_address}"
            binding.tvDropoff.text = "Dropoff: ${item.dropoff_address}"
            binding.tvFare.text = "Tahmini Ücret: ${item.estimated_fare ?: "-"}"

            binding.btnAcceptRide.setOnClickListener {
                onAcceptClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvailableRideVH {
        val binding = ItemAvailableRideBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AvailableRideVH(binding)
    }

    override fun onBindViewHolder(holder: AvailableRideVH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<AvailableRideItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}