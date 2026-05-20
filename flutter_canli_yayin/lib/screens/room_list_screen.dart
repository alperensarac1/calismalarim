import 'dart:convert';

import 'package:flutter/material.dart';

import '../config/app_config.dart';
import '../models/room_model.dart';
import '../services/live_socket_service.dart';

class RoomListScreen extends StatefulWidget {
  final VoidCallback onBack;
  final void Function(RoomModel room) onRoomTap;

  const RoomListScreen({
    super.key,
    required this.onBack,
    required this.onRoomTap,
  });

  @override
  State<RoomListScreen> createState() => _RoomListScreenState();
}

class _RoomListScreenState extends State<RoomListScreen> {
  LiveSocketService? socketService;

  List<RoomModel> rooms = [];
  String statusText = "Sunucuya bağlanıyor...";

  @override
  void initState() {
    super.initState();
    connectSocket();
  }

  void connectSocket() {
    socketService = LiveSocketService(
      serverUrl: AppConfig.serverUrl,

      onConnected: () {
        setState(() {
          statusText = "Sunucuya bağlandı";
        });

        socketService?.sendJson({
          "type": "get_rooms",
        });
      },

      onMessage: (message) {
        try {
          final data = jsonDecode(message);

          if (data["type"] == "rooms_list") {
            final List<dynamic> roomArray = data["rooms"];

            final newRooms = roomArray
                .map((item) => RoomModel.fromJson(item))
                .toList();

            setState(() {
              rooms = newRooms;
            });
          }

          if (data["type"] == "error") {
            setState(() {
              statusText = data["message"] ?? "Bilinmeyen hata";
            });
          }
        } catch (e) {
          setState(() {
            statusText = "Sunucudan gelen veri okunamadı";
          });
        }
      },

      onError: (error) {
        setState(() {
          statusText = "Hata: $error";
        });
      },

      onDisconnected: () {
        setState(() {
          statusText = "Bağlantı kapandı";
        });
      },
    );

    socketService?.connect();
  }

  @override
  void dispose() {
    socketService?.disconnect();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF8FAFC),
      appBar: AppBar(
        title: const Text("Aktif Yayınlar"),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: widget.onBack,
        ),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            Text(statusText),

            const SizedBox(height: 16),

            if (rooms.isEmpty)
              const Expanded(
                child: Center(
                  child: Text("Aktif yayın yok"),
                ),
              )
            else
              Expanded(
                child: ListView.builder(
                  itemCount: rooms.length,
                  itemBuilder: (context, index) {
                    final room = rooms[index];

                    return Card(
                      margin: const EdgeInsets.only(bottom: 12),
                      child: ListTile(
                        title: Text(
                          room.title,
                          style: const TextStyle(
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        subtitle: Text(
                          "Yayıncı: ${room.broadcasterName}\n"
                              "İzleyici: ${room.viewerCount}\n"
                              "Başlama: ${room.createdAt}",
                        ),
                        onTap: () {
                          widget.onRoomTap(room);
                        },
                      ),
                    );
                  },
                ),
              ),
          ],
        ),
      ),
    );
  }
}