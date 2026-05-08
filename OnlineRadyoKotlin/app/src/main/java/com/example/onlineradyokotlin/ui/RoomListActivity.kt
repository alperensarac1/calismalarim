package com.example.onlineradyokotlin.ui


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.onlineradyokotlin.data.RadioRoom
import com.example.onlineradyokotlin.databinding.ActivityRoomListBinding
import com.example.onlineradyokotlin.network.RadioSocketManager
import org.json.JSONObject

class RoomListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoomListBinding
    private lateinit var roomAdapter: RoomAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRoomListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSocket()
    }

    private fun setupRecyclerView() {
        roomAdapter = RoomAdapter { selectedRoom ->

            val intent = Intent(this, RadioPlayerActivity::class.java)
            intent.putExtra("roomId", selectedRoom.id)
            intent.putExtra("roomName", selectedRoom.roomName)
            startActivity(intent)
        }

        binding.rvRooms.layoutManager = LinearLayoutManager(this)
        binding.rvRooms.adapter = roomAdapter
    }

    private fun setupSocket() {
        RadioSocketManager.onConnected = {
            runOnUiThread {
                binding.tvConnectionStatus.text = "Sunucuya bağlandı"
            }

            RadioSocketManager.getRooms()
        }

        RadioSocketManager.onMessageReceived = { message ->
            handleSocketMessage(message)
        }

        RadioSocketManager.onError = { error ->
            runOnUiThread {
                binding.tvConnectionStatus.text = "Hata: $error"
            }
        }

        RadioSocketManager.connect()
    }

    private fun handleSocketMessage(message: String) {
        val json = JSONObject(message)
        val type = json.optString("type")

        if (type == "ROOM_LIST" || type == "ROOM_UPDATED") {
            val roomsJsonArray = json.getJSONArray("rooms")
            val rooms = mutableListOf<RadioRoom>()

            for (i in 0 until roomsJsonArray.length()) {
                val roomJson = roomsJsonArray.getJSONObject(i)

                rooms.add(
                    RadioRoom(
                        id = roomJson.getInt("id"),
                        roomName = roomJson.getString("roomName"),
                        currentMusic = roomJson.optString("currentMusic", null),
                        isPlaying = roomJson.optBoolean("isPlaying"),
                        listenerCount = roomJson.optInt("listenerCount", 0)
                    )
                )
            }

            runOnUiThread {
                roomAdapter.updateRooms(rooms)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        RadioSocketManager.getRooms()
    }
}