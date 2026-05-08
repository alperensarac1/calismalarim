import 'dart:async';
import 'dart:convert';
import 'dart:math';

import 'package:flutter/material.dart';
import 'package:just_audio/just_audio.dart';

import '../models/radio_room.dart';
import '../network/radio_socket_manager.dart';

class RadioPlayerScreen extends StatefulWidget {
  final RadioRoom room;

  const RadioPlayerScreen({
    super.key,
    required this.room,
  });

  @override
  State<RadioPlayerScreen> createState() => _RadioPlayerScreenState();
}

class _RadioPlayerScreenState extends State<RadioPlayerScreen> {
  final RadioSocketManager socket = RadioSocketManager.instance;
  final AudioPlayer player = AudioPlayer();

  Timer? syncTimer;

  String musicTitle = "Çalan müzik bekleniyor...";
  String statusText = "Odaya bağlanılıyor...";
  String? currentMusicUrl;

  @override
  void initState() {
    super.initState();

    setupSocket();
    socket.joinRoom(widget.room.id);

    syncTimer = Timer.periodic(const Duration(seconds: 5), (_) {
      socket.requestSync(widget.room.id);
    });
  }

  void setupSocket() {
    socket.onMessage = (message) {
      handleSocketMessage(message);
    };

    socket.onError = (error) {
      setState(() {
        statusText = "Bağlantı hatası: $error";
      });
    };

    socket.connect();
  }

  Future<void> handleSocketMessage(String message) async {
    final Map<String, dynamic> json = jsonDecode(message);
    final String type = json["type"] ?? "";

    if (type == "PLAYBACK_STATE") {
      final int incomingRoomId = json["roomId"] ?? -1;

      if (incomingRoomId != widget.room.id) return;

      final String title = json["title"] ?? "Bilinmeyen müzik";
      final String musicUrl = json["musicUrl"] ?? "";
      final double positionSeconds =
      (json["positionSeconds"] ?? 0).toDouble();

      await playOrSyncMusic(
        title: title,
        musicUrl: musicUrl,
        positionSeconds: positionSeconds,
      );
    }

    else if (type == "NO_MUSIC") {
      await player.pause();

      setState(() {
        musicTitle = "Bu odada şu an müzik yok";
        statusText = "Bekleniyor...";
      });
    }
  }

  Future<void> playOrSyncMusic({
    required String title,
    required String musicUrl,
    required double positionSeconds,
  }) async {
    setState(() {
      musicTitle = title;
      statusText = "Dinleniyor...";
    });

    final Duration targetPosition =
    Duration(milliseconds: (positionSeconds * 1000).toInt());

    if (currentMusicUrl != musicUrl) {
      currentMusicUrl = musicUrl;

      await player.setUrl(musicUrl);
      await player.seek(targetPosition);
      await player.play();
      return;
    }

    final Duration? currentPosition = player.position;
    final int diffMs =
    (currentPosition!.inMilliseconds - targetPosition.inMilliseconds).abs();

    if (diffMs > 1200) {
      await player.seek(targetPosition);
    }

    if (!player.playing) {
      await player.play();
    }
  }

  @override
  void dispose() {
    syncTimer?.cancel();
    player.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.room.roomName),
      ),
      body: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          children: [
            Text(
              widget.room.roomName,
              style: Theme.of(context).textTheme.headlineMedium,
              textAlign: TextAlign.center,
            ),

            const SizedBox(height: 16),

            Text(
              musicTitle,
              style: Theme.of(context).textTheme.titleLarge,
              textAlign: TextAlign.center,
            ),

            const SizedBox(height: 8),

            Text(
              statusText,
              textAlign: TextAlign.center,
            ),

            const Spacer(),

            const Icon(
              Icons.radio,
              size: 90,
            ),

            const SizedBox(height: 16),

            const Text(
              "Bu ekran sadece dinleyici modudur.",
              textAlign: TextAlign.center,
            ),

            const Spacer(),
          ],
        ),
      ),
    );
  }
}