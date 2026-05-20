package com.example.canliyayinkotlin.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.canliyayinkotlin.databinding.ItemRoomBinding
import com.example.canliyayinkotlin.model.RoomModel

class RoomAdapter(
    private val roomList: MutableList<RoomModel>,
    private val onRoomClick: (RoomModel) -> Unit
) : RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {

    inner class RoomViewHolder(
        private val binding: ItemRoomBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(room: RoomModel) {
            binding.tvRoomTitle.text = room.title
            binding.tvBroadcasterName.text = "Yayıncı: ${room.broadcasterName}"
            binding.tvViewerCount.text = "İzleyici: ${room.viewerCount}"

            binding.root.setOnClickListener {
                onRoomClick(room)
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

    fun updateRooms(newRooms: List<RoomModel>) {
        roomList.clear()
        roomList.addAll(newRooms)
        notifyDataSetChanged()
    }
}