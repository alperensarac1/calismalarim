import 'dart:async';
import 'dart:convert';

import 'package:flutter/material.dart';

import '../socket/socket_message_factory.dart';
import '../socket/web_socket_manager.dart';
import 'waiting_room_screen.dart';

class JoinRoomScreen extends StatefulWidget {
  const JoinRoomScreen({super.key});

  @override
  State<JoinRoomScreen> createState() => _JoinRoomScreenState();
}

class _JoinRoomScreenState extends State<JoinRoomScreen> {
  final TextEditingController _usernameController = TextEditingController();
  final TextEditingController _roomCodeController = TextEditingController();

  StreamSubscription<String>? _socketSubscription;

  String _statusText = "";

  String _pendingUsername = "";
  String _pendingRoomCode = "";
  bool _shouldSendAfterConnect = false;

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
    _usernameController.dispose();
    _roomCodeController.dispose();
    super.dispose();
  }

  void _joinRoom() {
    final username = _usernameController.text.trim();
    final roomCode = _roomCodeController.text.trim();

    if (username.isEmpty) {
      setState(() {
        _statusText = "Kullanıcı adı boş olamaz.";
      });
      return;
    }

    if (roomCode.isEmpty) {
      setState(() {
        _statusText = "Oda kodu boş olamaz.";
      });
      return;
    }

    _pendingUsername = username;
    _pendingRoomCode = roomCode;

    setState(() {
      _statusText = "Sunucuya bağlanılıyor...";
    });

    if (WebSocketManager.instance.isConnected) {
      _sendJoinRoomMessage();
    } else {
      _shouldSendAfterConnect = true;
      WebSocketManager.instance.connect();

      Future.delayed(const Duration(milliseconds: 300), () {
        if (!mounted) return;

        if (_shouldSendAfterConnect) {
          _sendJoinRoomMessage();
        }
      });
    }
  }

  void _sendJoinRoomMessage() {
    _shouldSendAfterConnect = false;

    final message = SocketMessageFactory.joinRoom(
      roomCode: _pendingRoomCode,
      username: _pendingUsername,
    );

    WebSocketManager.instance.send(message);

    setState(() {
      _statusText = "Odaya katılma isteği gönderildi...";
    });
  }

  void _handleSocketMessage(String message) {
    final Map<String, dynamic> json = jsonDecode(message);
    final type = json["type"]?.toString() ?? "";

    if (type == "room_joined") {
      final roomCode = json["room_code"]?.toString() ?? _pendingRoomCode;
      final username = json["username"]?.toString() ?? _pendingUsername;
      final questionTime =
      json["question_time"] is int ? json["question_time"] : 20;

      Navigator.pushReplacement(
        context,
        MaterialPageRoute(
          builder: (_) => WaitingRoomScreen(
            roomCode: roomCode,
            username: username,
            questionTime: questionTime,
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

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Odaya Giriş Yap"),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              "Odaya Giriş Yap",
              style: TextStyle(
                fontSize: 28,
                fontWeight: FontWeight.w800,
                color: Color(0xFF111827),
              ),
            ),

            const SizedBox(height: 8),

            const Text(
              "Kullanıcı adını ve oda kodunu gir.",
              style: TextStyle(
                fontSize: 15,
                color: Color(0xFF6B7280),
              ),
            ),

            const SizedBox(height: 24),

            TextField(
              controller: _usernameController,
              decoration: const InputDecoration(
                labelText: "Kullanıcı adı",
                border: OutlineInputBorder(),
              ),
            ),

            const SizedBox(height: 14),

            TextField(
              controller: _roomCodeController,
              keyboardType: TextInputType.number,
              decoration: const InputDecoration(
                labelText: "Oda kodu",
                border: OutlineInputBorder(),
              ),
            ),

            const SizedBox(height: 24),

            SizedBox(
              width: double.infinity,
              height: 56,
              child: ElevatedButton(
                onPressed: _joinRoom,
                child: const Text(
                  "Odaya Katıl",
                  style: TextStyle(fontSize: 17),
                ),
              ),
            ),

            const SizedBox(height: 14),

            TextButton(
              onPressed: () => Navigator.pop(context),
              child: const Text("Geri dön"),
            ),

            const SizedBox(height: 20),

            Text(
              _statusText,
              style: const TextStyle(
                fontSize: 15,
                color: Color(0xFF374151),
              ),
            ),
          ],
        ),
      ),
    );
  }
}