import 'dart:convert';

import 'package:amiral_batti_flutter/screens/placement_screen.dart';
import 'package:flutter/material.dart';

import '../core/socket_manager.dart';
import '../models/error_data.dart';
import '../models/joined_room_data.dart';
import '../models/player_info.dart';
import '../models/player_joined_data.dart';
import '../models/room_created_data.dart';

class LobbyScreen extends StatefulWidget {
  const LobbyScreen({super.key});

  @override
  State<LobbyScreen> createState() => _LobbyScreenState();
}

class _LobbyScreenState extends State<LobbyScreen> implements SocketEventListener {
  final TextEditingController playerNameController = TextEditingController();
  final TextEditingController roomCodeController = TextEditingController();

  String roomInfo = "Oda: -";
  String playersInfo = "Oyuncular: -";
  String statusText = "Durum: Hazır";

  String currentRoomCode = "";
  String currentPlayerId = "";

  @override
  void initState() {
    super.initState();
    SocketManager.instance.setListener(this);
  }

  @override
  void dispose() {
    SocketManager.instance.clearListener(this);
    playerNameController.dispose();
    roomCodeController.dispose();
    super.dispose();
  }

  void connectToServer() {
    setState(() {
      statusText = "Durum: Sunucuya bağlanılıyor...";
    });
    SocketManager.instance.setListener(this);
    SocketManager.instance.connect();
  }

  void createRoom() {
    final playerName = playerNameController.text.trim();

    if (playerName.isEmpty) {
      setState(() {
        statusText = "Hata: Oyuncu adı gir";
      });
      return;
    }

    SocketManager.instance.sendMap({
      "type": "CREATE_ROOM",
      "data": {
        "playerName": playerName,
      }
    });
  }

  void joinRoom() {
    final playerName = playerNameController.text.trim();
    final roomCode = roomCodeController.text.trim();

    if (playerName.isEmpty || roomCode.isEmpty) {
      setState(() {
        statusText = "Hata: Oyuncu adı ve oda kodu gir";
      });
      return;
    }

    SocketManager.instance.sendMap({
      "type": "JOIN_ROOM",
      "data": {
        "playerName": playerName,
        "roomCode": roomCode,
      }
    });
  }

  String formatPlayers(List<PlayerInfo> players) {
    if (players.isEmpty) return "-";
    return players.map((e) => e.name).join(" | ");
  }

  void navigateToPlacement() {
    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (_) => PlacementScreen(
          roomCode: currentRoomCode,
          playerId: currentPlayerId,
          playerName: playerNameController.text.trim(),
        ),
      ),
    );
  }

  @override
  void onConnected() {
    if (!mounted) return;
    setState(() {
      statusText = "Durum: Sunucuya bağlandı";
    });
  }

  @override
  void onDisconnected() {
    if (!mounted) return;
    setState(() {
      statusText = "Durum: Bağlantı kesildi";
    });
  }

  @override
  void onError(String errorMessage) {
    if (!mounted) return;
    setState(() {
      statusText = "Hata: $errorMessage";
    });
  }

  @override
  void onMessage(String message) {
    if (!mounted) return;

    final map = jsonDecode(message) as Map<String, dynamic>;
    final type = map["type"] as String? ?? "";
    final data = (map["data"] as Map?)?.cast<String, dynamic>() ?? {};

    switch (type) {
      case "ROOM_CREATED":
        final decoded = RoomCreatedData.fromJson(data);
        setState(() {
          currentRoomCode = decoded.roomCode;
          currentPlayerId = decoded.playerId;
          roomCodeController.text = decoded.roomCode;
          roomInfo = "Oda: ${decoded.roomCode}";
          playersInfo = "Oyuncular: ${formatPlayers(decoded.players)}";
          statusText = decoded.message;
        });
        break;

      case "JOINED_ROOM":
        final decoded = JoinedRoomData.fromJson(data);
        setState(() {
          currentRoomCode = decoded.roomCode;
          currentPlayerId = decoded.playerId;
          roomCodeController.text = decoded.roomCode;
          roomInfo = "Oda: ${decoded.roomCode}";
          playersInfo = "Oyuncular: ${formatPlayers(decoded.players)}";
          statusText = decoded.message;
        });
        break;

      case "PLAYER_JOINED":
        final decoded = PlayerJoinedData.fromJson(data);
        setState(() {
          roomInfo = "Oda: ${decoded.roomCode}";
          playersInfo = "Oyuncular: ${formatPlayers(decoded.players)}";
          statusText = decoded.message;
        });

        if (decoded.players.length == 2) {
          navigateToPlacement();
        }
        break;

      case "PLAYER_LEFT":
        final decoded = PlayerJoinedData.fromJson(data);
        setState(() {
          playersInfo = "Oyuncular: ${formatPlayers(decoded.players)}";
          statusText = decoded.message;
        });
        break;

      case "ERROR":
        final decoded = ErrorData.fromJson(data);
        setState(() {
          statusText = "Hata: ${decoded.message}";
        });
        break;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Lobi"),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            const Text(
              "Amiral Battı",
              style: TextStyle(fontSize: 30, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 16),
            TextField(
              controller: playerNameController,
              decoration: const InputDecoration(
                labelText: "Oyuncu adı",
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: roomCodeController,
              keyboardType: TextInputType.number,
              decoration: const InputDecoration(
                labelText: "Oda kodu",
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 12),
            SizedBox(
              width: double.infinity,
              child: FilledButton(
                onPressed: connectToServer,
                child: const Text("Sunucuya Bağlan"),
              ),
            ),
            const SizedBox(height: 12),
            SizedBox(
              width: double.infinity,
              child: FilledButton(
                onPressed: createRoom,
                child: const Text("Oda Oluştur"),
              ),
            ),
            const SizedBox(height: 12),
            SizedBox(
              width: double.infinity,
              child: FilledButton(
                onPressed: joinRoom,
                child: const Text("Odaya Katıl"),
              ),
            ),
            const SizedBox(height: 20),
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  children: [
                    Align(
                      alignment: Alignment.centerLeft,
                      child: Text(roomInfo),
                    ),
                    const SizedBox(height: 8),
                    Align(
                      alignment: Alignment.centerLeft,
                      child: Text(playersInfo),
                    ),
                    const SizedBox(height: 8),
                    Align(
                      alignment: Alignment.centerLeft,
                      child: Text(statusText),
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
