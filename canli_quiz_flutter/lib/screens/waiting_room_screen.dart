import 'dart:async';
import 'dart:convert';

import 'package:flutter/material.dart';

import '../socket/web_socket_manager.dart';
import 'quiz_screen.dart';

class WaitingRoomScreen extends StatefulWidget {
  final String roomCode;
  final String username;
  final int questionTime;

  const WaitingRoomScreen({
    super.key,
    required this.roomCode,
    required this.username,
    required this.questionTime,
  });

  @override
  State<WaitingRoomScreen> createState() => _WaitingRoomScreenState();
}

class _WaitingRoomScreenState extends State<WaitingRoomScreen> {
  StreamSubscription<String>? _socketSubscription;

  String _playersText = "Oyuncular yükleniyor...";
  String _statusText = "";

  @override
  void initState() {
    super.initState();

    _socketSubscription = WebSocketManager.instance.messages.listen(
      _handleSocketMessage,
      onError: (error) {
        setState(() {
          _statusText = "Bağlantı hatası: $error";
        });
      },
    );
  }

  @override
  void dispose() {
    _socketSubscription?.cancel();
    super.dispose();
  }

  void _handleSocketMessage(String message) {
    final Map<String, dynamic> json = jsonDecode(message);
    final type = json["type"]?.toString() ?? "";

    if (type == "player_list_updated") {
      final players = json["players"];

      if (players is List) {
        setState(() {
          _playersText = _buildPlayersText(players);
        });
      }
    }

    if (type == "quiz_started") {
      setState(() {
        _statusText = "Quiz başladı.";
      });

      Navigator.pushReplacement(
        context,
        MaterialPageRoute(
          builder: (_) => QuizScreen(
            roomCode: widget.roomCode,
            username: widget.username,
            questionTime: widget.questionTime,
            isOwner: false,
          ),
        ),
      );
    }

    if (type == "error") {
      setState(() {
        _statusText = json["message"]?.toString() ?? "Bilinmeyen hata oluştu.";
      });
    }
  }

  String _buildPlayersText(List players) {
    if (players.isEmpty) {
      return "Oyuncular yükleniyor...";
    }

    final buffer = StringBuffer();
    buffer.writeln("Odada bulunan oyuncular:");
    buffer.writeln();

    for (int i = 0; i < players.length; i++) {
      buffer.writeln("${i + 1}. ${players[i]}");
    }

    return buffer.toString();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Bekleme Odası"),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              "Bekleme Odası",
              style: TextStyle(
                fontSize: 28,
                fontWeight: FontWeight.w800,
                color: Color(0xFF111827),
              ),
            ),
            const SizedBox(height: 18),
            Text(
              "Kullanıcı: ${widget.username}\n"
                  "Oda Kodu: ${widget.roomCode}\n"
                  "Soru Süresi: ${widget.questionTime} saniye\n\n"
                  "Oda sahibi quizi başlatınca sorular ekrana gelecek.",
              style: const TextStyle(
                fontSize: 16,
                color: Color(0xFF374151),
                height: 1.5,
              ),
            ),
            const SizedBox(height: 18),
            Text(
              _statusText,
              style: const TextStyle(
                fontSize: 15,
                color: Color(0xFF6D28D9),
                fontWeight: FontWeight.w700,
              ),
            ),
            const SizedBox(height: 24),
            Text(
              _playersText,
              style: const TextStyle(
                fontSize: 15,
                color: Color(0xFF111827),
                height: 1.5,
              ),
            ),
          ],
        ),
      ),
    );
  }
}