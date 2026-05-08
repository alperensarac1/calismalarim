import 'dart:convert';

import 'package:web_socket_channel/web_socket_channel.dart';

class RadioSocketManager {
  static final RadioSocketManager instance = RadioSocketManager._internal();

  RadioSocketManager._internal();

  // Python server çalışan bilgisayarın IP adresi.
  static const String serverUrl = "ws://192.168.1.10:8765";

  WebSocketChannel? _channel;

  Function()? onConnected;
  Function(String message)? onMessage;
  Function(String error)? onError;

  bool get isConnected => _channel != null;

  void connect() {
    if (_channel != null) return;

    try {
      _channel = WebSocketChannel.connect(Uri.parse(serverUrl));

      onConnected?.call();

      _channel!.stream.listen(
            (message) {
          if (message is String) {
            onMessage?.call(message);
          } else {
            onMessage?.call(message.toString());
          }
        },
        onError: (error) {
          onError?.call(error.toString());
          _channel = null;
        },
        onDone: () {
          _channel = null;
        },
      );
    } catch (e) {
      onError?.call(e.toString());
      _channel = null;
    }
  }

  void send(Map<String, dynamic> data) {
    if (_channel == null) return;

    _channel!.sink.add(jsonEncode(data));
  }

  void getRooms() {
    send({
      "type": "GET_ROOMS",
    });
  }

  void joinRoom(int roomId) {
    send({
      "type": "JOIN_ROOM",
      "roomId": roomId,
    });
  }

  void requestSync(int roomId) {
    send({
      "type": "SYNC_REQUEST",
      "roomId": roomId,
    });
  }

  void close() {
    _channel?.sink.close();
    _channel = null;
  }
}