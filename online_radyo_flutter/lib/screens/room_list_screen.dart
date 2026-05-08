import 'dart:convert';

import 'package:flutter/material.dart';

import '../models/radio_room.dart';
import '../network/radio_socket_manager.dart';
import 'radio_player_screen.dart';

class RoomListScreen extends StatefulWidget {
  const RoomListScreen({super.key});

  @override
  State<RoomListScreen> createState() => _RoomListScreenState();
}

class _RoomListScreenState extends State<RoomListScreen> {
  final RadioSocketManager socket = RadioSocketManager.instance;

  List<RadioRoom> rooms = [];
  String statusText = "Sunucuya bağlanılıyor...";

  @override
  void initState() {
    super.initState();
    setupSocket();
  }

  void setupSocket() {
    socket.onConnected = () {
      setState(() {
        statusText = "Sunucuya bağlandı";
      });

      socket.getRooms();
    };

    socket.onMessage = (message) {
      handleSocketMessage(message);
    };

    socket.onError = (error) {
      setState(() {
        statusText = "Hata: $error";
      });
    };

    socket.connect();
    socket.getRooms();
  }

  void handleSocketMessage(String message) {
    final Map<String, dynamic> json = jsonDecode(message);
    final String type = json["type"] ?? "";

    if (type == "ROOM_LIST" || type == "ROOM_UPDATED") {
      final List roomArray = json["rooms"] ?? [];

      final List<RadioRoom> parsedRooms = roomArray
          .map((item) => RadioRoom.fromJson(item))
          .toList();

      setState(() {
        rooms = parsedRooms;
      });
    }
  }

  void openPlayer(RadioRoom room) {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (_) => RadioPlayerScreen(room: room),
      ),
    ).then((_) {
      socket.getRooms();
    });
  }

  @override
  void dispose() {
    // Socket kapatmıyoruz çünkü player ekranında da aynı bağlantı kullanılacak.
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("SyncRadio Odaları"),
      ),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(12),
            child: Align(
              alignment: Alignment.centerLeft,
              child: Text(statusText),
            ),
          ),

          Expanded(
            child: RefreshIndicator(
              onRefresh: () async {
                socket.getRooms();
              },
              child: ListView.builder(
                itemCount: rooms.length,
                itemBuilder: (context, index) {
                  final room = rooms[index];

                  return Card(
                    margin: const EdgeInsets.symmetric(
                      horizontal: 12,
                      vertical: 6,
                    ),
                    child: ListTile(
                      title: Text(
                        room.roomName,
                        style: const TextStyle(
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      subtitle: Text(
                        "${room.currentMusic == null || room.currentMusic!.isEmpty ? "Şu an: Müzik yok" : "Şu an: ${room.currentMusic}"}\nDinleyici: ${room.listenerCount}",
                      ),
                      isThreeLine: true,
                      trailing: const Icon(Icons.chevron_right),
                      onTap: () {
                        openPlayer(room);
                      },
                    ),
                  );
                },
              ),
            ),
          ),
        ],
      ),
    );
  }
}