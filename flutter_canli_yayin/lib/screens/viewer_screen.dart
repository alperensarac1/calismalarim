import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/material.dart';

import '../config/app_config.dart';
import '../models/chat_message_model.dart';
import '../models/room_model.dart';
import '../services/live_socket_service.dart';

class ViewerScreen extends StatefulWidget {
  final RoomModel room;
  final VoidCallback onBack;

  const ViewerScreen({
    super.key,
    required this.room,
    required this.onBack,
  });

  @override
  State<ViewerScreen> createState() => _ViewerScreenState();
}

class _ViewerScreenState extends State<ViewerScreen> {
  LiveSocketService? socketService;

  String statusText = "Bağlanıyor...";
  int viewerCount = 0;
  Uint8List? currentFrame;

  final TextEditingController messageController = TextEditingController();
  final List<ChatMessageModel> chatMessages = [];

  @override
  void initState() {
    super.initState();
    viewerCount = widget.room.viewerCount;
    connectSocket();
  }

  void connectSocket() {
    socketService = LiveSocketService(
      serverUrl: AppConfig.serverUrl,

      onConnected: () {
        setState(() {
          statusText = "Sunucuya bağlandı, odaya giriliyor...";
        });

        socketService?.sendJson({
          "type": "join_room",
          "room_id": widget.room.roomId,
          "username": "Flutter İzleyici",
        });
      },

      onMessage: (message) {
        try {
          final data = jsonDecode(message);

          if (data["type"] == "joined_room") {
            setState(() {
              statusText = "Yayına bağlandı";
            });
          }

          if (data["type"] == "viewer_count") {
            setState(() {
              viewerCount = data["viewer_count"] ?? 0;
            });
          }

          if (data["type"] == "video_frame") {
            final base64Frame = data["frame"];

            setState(() {
              currentFrame = base64Decode(base64Frame);
            });
          }

          if (data["type"] == "chat_message") {
            final chat = ChatMessageModel.fromJson(data);

            setState(() {
              chatMessages.add(chat);
            });
          }

          if (data["type"] == "stream_ended") {
            setState(() {
              statusText = "Yayın sona erdi";
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

  void sendChatMessage() {
    final message = messageController.text.trim();

    if (message.isEmpty) return;

    socketService?.sendJson({
      "type": "chat_message",
      "message": message,
    });

    messageController.clear();
  }

  @override
  void dispose() {
    socketService?.disconnect();
    messageController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF020617),
      appBar: AppBar(
        title: Text(widget.room.title),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: widget.onBack,
        ),
      ),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(12),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  "İzleyici: $viewerCount",
                  style: const TextStyle(color: Colors.white),
                ),
                const SizedBox(height: 4),
                Text(
                  statusText,
                  style: const TextStyle(color: Colors.white70),
                ),
              ],
            ),
          ),

          Container(
            width: double.infinity,
            height: 260,
            margin: const EdgeInsets.symmetric(horizontal: 12),
            decoration: BoxDecoration(
              color: const Color(0xFF111827),
              borderRadius: BorderRadius.circular(12),
            ),
            clipBehavior: Clip.antiAlias,
            child: currentFrame == null
                ? const Center(
              child: Text(
                "Görüntü bekleniyor...",
                style: TextStyle(color: Colors.white54),
              ),
            )
                : Image.memory(
              currentFrame!,
              fit: BoxFit.contain,
              gaplessPlayback: true,
            ),
          ),

          const Padding(
            padding: EdgeInsets.all(12),
            child: Align(
              alignment: Alignment.centerLeft,
              child: Text(
                "Canlı Sohbet",
                style: TextStyle(
                  color: Colors.white,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ),
          ),

          Expanded(
            child: ListView.builder(
              padding: const EdgeInsets.symmetric(horizontal: 12),
              itemCount: chatMessages.length,
              itemBuilder: (context, index) {
                final chat = chatMessages[index];

                return Padding(
                  padding: const EdgeInsets.only(bottom: 8),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        chat.username,
                        style: const TextStyle(
                          color: Color(0xFF93C5FD),
                          fontWeight: FontWeight.bold,
                          fontSize: 13,
                        ),
                      ),
                      Text(
                        chat.message,
                        style: const TextStyle(color: Colors.white),
                      ),
                    ],
                  ),
                );
              },
            ),
          ),

          Container(
            padding: const EdgeInsets.all(8),
            color: const Color(0xFF111827),
            child: Row(
              children: [
                Expanded(
                  child: TextField(
                    controller: messageController,
                    style: const TextStyle(color: Colors.white),
                    decoration: const InputDecoration(
                      hintText: "Mesaj yaz...",
                      hintStyle: TextStyle(color: Colors.white54),
                      filled: true,
                      fillColor: Color(0xFF1E293B),
                      border: OutlineInputBorder(),
                    ),
                  ),
                ),

                const SizedBox(width: 8),

                ElevatedButton(
                  onPressed: sendChatMessage,
                  child: const Text("Gönder"),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}