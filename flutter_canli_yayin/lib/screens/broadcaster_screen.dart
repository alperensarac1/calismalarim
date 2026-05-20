import 'dart:async';
import 'dart:convert';

import 'package:camera/camera.dart';
import 'package:flutter/material.dart';

import '../config/app_config.dart';
import '../models/chat_message_model.dart';
import '../services/live_socket_service.dart';

class BroadcasterScreen extends StatefulWidget {
  final VoidCallback onBack;

  const BroadcasterScreen({
    super.key,
    required this.onBack,
  });

  @override
  State<BroadcasterScreen> createState() => _BroadcasterScreenState();
}

class _BroadcasterScreenState extends State<BroadcasterScreen> {
  LiveSocketService? socketService;
  CameraController? cameraController;

  Timer? frameTimer;

  String statusText = "Hazırlanıyor...";
  String? roomId;
  int viewerCount = 0;

  final TextEditingController titleController = TextEditingController();
  final TextEditingController messageController = TextEditingController();

  final List<ChatMessageModel> chatMessages = [];

  bool isSendingFrame = false;

  @override
  void initState() {
    super.initState();

    connectSocket();
    initCamera();
  }

  Future<void> initCamera() async {
    try {
      final cameras = await availableCameras();

      final frontCamera = cameras.firstWhere(
            (camera) => camera.lensDirection == CameraLensDirection.front,
        orElse: () => cameras.first,
      );

      cameraController = CameraController(
        frontCamera,
        ResolutionPreset.low,
        enableAudio: false,
        imageFormatGroup: ImageFormatGroup.jpeg,
      );

      await cameraController!.initialize();

      if (!mounted) return;

      setState(() {
        statusText = "Kamera hazır";
      });
    } catch (e) {
      setState(() {
        statusText = "Kamera başlatılamadı: $e";
      });
    }
  }

  void connectSocket() {
    socketService = LiveSocketService(
      serverUrl: AppConfig.serverUrl,

      onConnected: () {
        setState(() {
          statusText = "Sunucuya bağlandı. Başlık yazıp yayını başlat.";
        });
      },

      onMessage: (message) {
        try {
          final data = jsonDecode(message);

          if (data["type"] == "room_created") {
            setState(() {
              roomId = data["room_id"];
              statusText = "Yayın başladı";
            });

            startFrameSender();
          }

          if (data["type"] == "viewer_count") {
            setState(() {
              viewerCount = data["viewer_count"] ?? 0;
            });
          }

          if (data["type"] == "chat_message") {
            final chat = ChatMessageModel.fromJson(data);

            setState(() {
              chatMessages.add(chat);
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

  void startBroadcast() {
    final title = titleController.text.trim();

    if (title.isEmpty) {
      setState(() {
        statusText = "Yayın başlığı yazmalısın";
      });
      return;
    }

    socketService?.sendJson({
      "type": "create_room",
      "title": title,
      "broadcaster_name": "Flutter Yayıncı",
    });

    setState(() {
      statusText = "Oda oluşturuluyor...";
    });
  }

  void startFrameSender() {
    stopFrameSender();

    frameTimer = Timer.periodic(
      const Duration(milliseconds: 350),
          (_) => sendCameraFrame(),
    );
  }

  void stopFrameSender() {
    frameTimer?.cancel();
    frameTimer = null;
  }

  Future<void> sendCameraFrame() async {
    if (cameraController == null) return;
    if (!cameraController!.value.isInitialized) return;
    if (roomId == null) return;
    if (isSendingFrame) return;

    try {
      isSendingFrame = true;

      final XFile file = await cameraController!.takePicture();
      final bytes = await file.readAsBytes();

      final base64Frame = base64Encode(bytes);

      socketService?.sendJson({
        "type": "video_frame",
        "frame": base64Frame,
      });
    } catch (_) {
      // Kamera o anda meşgulse sessiz geçiyoruz.
    } finally {
      isSendingFrame = false;
    }
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
    stopFrameSender();
    socketService?.disconnect();
    cameraController?.dispose();
    titleController.dispose();
    messageController.dispose();

    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final cameraReady =
        cameraController != null && cameraController!.value.isInitialized;

    return Scaffold(
      backgroundColor: const Color(0xFF020617),
      appBar: AppBar(
        title: const Text("Yayın Aç"),
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
              children: [
                Text(
                  statusText,
                  style: const TextStyle(color: Colors.white),
                ),

                const SizedBox(height: 8),

                TextField(
                  controller: titleController,
                  enabled: roomId == null,
                  style: const TextStyle(color: Colors.white),
                  decoration: const InputDecoration(
                    hintText: "Yayın başlığı yaz...",
                    hintStyle: TextStyle(color: Colors.white54),
                    filled: true,
                    fillColor: Color(0xFF1E293B),
                    border: OutlineInputBorder(),
                  ),
                ),

                const SizedBox(height: 8),

                SizedBox(
                  width: double.infinity,
                  child: ElevatedButton(
                    onPressed: roomId == null ? startBroadcast : null,
                    child: Text(
                      roomId == null ? "Yayını Başlat" : "Yayın Başladı",
                    ),
                  ),
                ),

                Align(
                  alignment: Alignment.centerLeft,
                  child: Text(
                    "İzleyici: $viewerCount",
                    style: const TextStyle(color: Colors.white70),
                  ),
                ),
              ],
            ),
          ),

          Container(
            width: double.infinity,
            height: 240,
            margin: const EdgeInsets.symmetric(horizontal: 12),
            decoration: BoxDecoration(
              color: const Color(0xFF111827),
              borderRadius: BorderRadius.circular(12),
            ),
            clipBehavior: Clip.antiAlias,
            child: cameraReady
                ? CameraPreview(cameraController!)
                : const Center(
              child: Text(
                "Kamera hazırlanıyor...",
                style: TextStyle(color: Colors.white54),
              ),
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

          Padding(
            padding: const EdgeInsets.all(8),
            child: SizedBox(
              width: double.infinity,
              child: ElevatedButton(
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.red,
                ),
                onPressed: widget.onBack,
                child: const Text("Yayını Bitir"),
              ),
            ),
          ),
        ],
      ),
    );
  }
}