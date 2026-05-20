package com.example.canliyayinkotlin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.canliyayinkotlin.adapter.RoomAdapter
import com.example.canliyayinkotlin.databinding.ActivityRoomListBinding
import com.example.canliyayinkotlin.model.RoomModel
import com.example.canliyayinkotlin.socket.LiveSocketListener
import com.example.canliyayinkotlin.socket.LiveSocketManager
import org.json.JSONObject

class RoomListActivity : AppCompatActivity(), LiveSocketListener {

    private lateinit var binding: ActivityRoomListBinding
    private lateinit var adapter: RoomAdapter
    private lateinit var socketManager: LiveSocketManager

    private val serverUrl = "ws://10.208.181.112:8765"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRoomListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = RoomAdapter(mutableListOf()) { room ->
            val intent = Intent(this, ViewerActivity::class.java)
            intent.putExtra("room_id", room.roomId)
            intent.putExtra("room_title", room.title)
            startActivity(intent)
        }

        binding.rvRooms.layoutManager = LinearLayoutManager(this)
        binding.rvRooms.adapter = adapter

        socketManager = LiveSocketManager(serverUrl, this)
        socketManager.connect()
    }

    override fun onConnected() {
        runOnUiThread {
            Toast.makeText(this, "Sunucuya bağlandı", Toast.LENGTH_SHORT).show()
        }

        val json = JSONObject()
        json.put("type", "get_rooms")

        socketManager.sendJson(json)
    }

    override fun onMessage(message: String) {
        try {
            val json = JSONObject(message)

            when (json.getString("type")) {
                "rooms_list" -> {
                    val roomsArray = json.getJSONArray("rooms")
                    val newRooms = mutableListOf<RoomModel>()

                    for (i in 0 until roomsArray.length()) {
                        val item = roomsArray.getJSONObject(i)

                        val room = RoomModel(
                            roomId = item.getString("room_id"),
                            title = item.getString("title"),
                            broadcasterName = item.getString("broadcaster_name"),
                            createdAt = item.getString("created_at"),
                            viewerCount = item.getInt("viewer_count")
                        )

                        newRooms.add(room)
                    }

                    runOnUiThread {
                        adapter.updateRooms(newRooms)
                        Toast.makeText(
                            this,
                            "Oda sayısı: ${newRooms.size}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                "error" -> {
                    runOnUiThread {
                        Toast.makeText(
                            this,
                            json.getString("message"),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(
                    this,
                    "JSON okuma hatası: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onError(error: String) {
        runOnUiThread {
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        }
    }

    override fun onDisconnected() {
        runOnUiThread {
            Toast.makeText(this, "Bağlantı kapandı", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        socketManager.disconnect()
    }
}