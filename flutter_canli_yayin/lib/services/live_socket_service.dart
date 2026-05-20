import 'dart:convert';

import 'package:web_socket_channel/web_socket_channel.dart';

class LiveSocketService {
  WebSocketChannel? _channel;

  final String serverUrl;

  final void Function()? onConnected;
  final void Function(String message)? onMessage;
  final void Function(String error)? onError;
  final void Function()? onDisconnected;

  LiveSocketService({
    required this.serverUrl,
    this.onConnected,
    this.onMessage,
    this.onError,
    this.onDisconnected,
  });

  void connect() {
    try {
      _channel = WebSocketChannel.connect(
        Uri.parse(serverUrl),
      );

      onConnected?.call();

      _channel!.stream.listen(
            (event) {
          if (event is String) {
            onMessage?.call(event);
          }
        },
        onError: (error) {
          onError?.call(error.toString());
        },
        onDone: () {
          onDisconnected?.call();
        },
      );
    } catch (e) {
      onError?.call(e.toString());
    }
  }

  void sendJson(Map<String, dynamic> data) {
    if (_channel == null) return;

    final jsonText = jsonEncode(data);
    _channel!.sink.add(jsonText);
  }

  void disconnect() {
    _channel?.sink.close();
    _channel = null;
  }
}