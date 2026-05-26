import 'dart:async';
import 'dart:convert';

import 'package:flutter/material.dart';

import '../socket/socket_message_factory.dart';
import '../socket/web_socket_manager.dart';
import 'owner_room_screen.dart';

class CreateRoomScreen extends StatefulWidget {
  const CreateRoomScreen({super.key});

  @override
  State<CreateRoomScreen> createState() => _CreateRoomScreenState();
}

class _CreateRoomScreenState extends State<CreateRoomScreen> {
  final TextEditingController _usernameController = TextEditingController();
  final TextEditingController _questionTimeController =
  TextEditingController(text: "20");

  StreamSubscription<String>? _socketSubscription;

  String _statusText = "";

  String _pendingUsername = "";
  int _pendingQuestionTime = 20;
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
    _questionTimeController.dispose();
    super.dispose();
  }

  void _createRoom() {
    final username = _usernameController.text.trim();
    final questionTime =
        int.tryParse(_questionTimeController.text.trim()) ?? 20;

    if (username.isEmpty) {
      setState(() {
        _statusText = "Kullanıcı adı boş olamaz.";
      });
      return;
    }

    if (questionTime < 5) {
      setState(() {
        _statusText = "Soru süresi en az 5 saniye olmalı.";
      });
      return;
    }

    _pendingUsername = username;
    _pendingQuestionTime = questionTime;

    setState(() {
      _statusText = "Sunucuya bağlanılıyor...";
    });

    if (WebSocketManager.instance.isConnected) {
      _sendCreateRoomMessage();
    } else {
      _shouldSendAfterConnect = true;
      WebSocketManager.instance.connect();

      /*
        web_socket_channel bağlantıyı anında açar.
        Kısa bir gecikmeyle mesajı gönderiyoruz.
      */
      Future.delayed(const Duration(milliseconds: 300), () {
        if (!mounted) return;

        if (_shouldSendAfterConnect) {
          _sendCreateRoomMessage();
        }
      });
    }
  }

  void _sendCreateRoomMessage() {
    _shouldSendAfterConnect = false;

    final message = SocketMessageFactory.createRoom(
      username: _pendingUsername,
      questionTime: _pendingQuestionTime,
    );

    WebSocketManager.instance.send(message);

    setState(() {
      _statusText = "Oda oluşturma isteği gönderildi...";
    });
  }

  void _handleSocketMessage(String message) {
    final Map<String, dynamic> json = jsonDecode(message);
    final type = json["type"]?.toString() ?? "";

    if (type == "room_created") {
      final roomCode = json["room_code"]?.toString() ?? "";
      final username = json["username"]?.toString() ?? _pendingUsername;
      final questionTime =
      json["question_time"] is int ? json["question_time"] : _pendingQuestionTime;

      Navigator.pushReplacement(
        context,
        MaterialPageRoute(
          builder: (_) => OwnerRoomScreen(
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
        title: const Text("Oda Oluştur"),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              "Oda Oluştur",
              style: TextStyle(
                fontSize: 28,
                fontWeight: FontWeight.w800,
                color: Color(0xFF111827),
              ),
            ),

            const SizedBox(height: 8),

            const Text(
              "Kullanıcı adını ve soru başına süreyi gir.",
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
              controller: _questionTimeController,
              keyboardType: TextInputType.number,
              decoration: const InputDecoration(
                labelText: "Soru süresi örn: 20",
                border: OutlineInputBorder(),
              ),
            ),

            const SizedBox(height: 24),

            SizedBox(
              width: double.infinity,
              height: 56,
              child: ElevatedButton(
                onPressed: _createRoom,
                child: const Text(
                  "Odayı Oluştur",
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