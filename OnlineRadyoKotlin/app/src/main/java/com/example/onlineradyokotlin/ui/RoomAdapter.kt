package com.example.onlineradyokotlin.ui


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.onlineradyokotlin.data.RadioRoom
import com.example.onlineradyokotlin.databinding.ItemRoomBinding

class RoomAdapter(
    private val onRoomClicked: (RadioRoom) -> Unit
) : RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {

    private val roomList = mutableListOf<RadioRoom>()

    fun updateRooms(newRooms: List<RadioRoom>) {
        roomList.clear()
        roomList.addAll(newRooms)
        notifyDataSetChanged()
    }

    inner class RoomViewHolder(
        private val binding: ItemRoomBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(room: RadioRoom) {
            binding.tvRoomName.text = room.roomName

            binding.tvCurrentMusic.text =
                if (room.currentMusic.isNullOrBlank()) {
                    "Şu an: Müzik yok"
                } else {
                    "Şu an: ${room.currentMusic}"
                }

            binding.tvListenerCount.text = "Dinleyici: ${room.listenerCount}"

            binding.root.setOnClickListener {
                onRoomClicked(room)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val binding = ItemRoomBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RoomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        holder.bind(roomList[position])
    }

    override fun getItemCount(): Int = roomList.size
}