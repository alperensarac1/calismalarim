import 'dart:convert';
import 'package:web_socket_channel/web_socket_channel.dart';
import '../core/constants.dart';
import '../core/session_manager.dart';

class SocketManager {
  final SessionManager sessionManager;

  WebSocketChannel? _channel;

  Function()? onConnected;
  Function()? onDisconnected;
  Function(String message)? onMessage;
  Function(String error)? onError;

  SocketManager({
    required this.sessionManager,
  });

  Future<void> connect() async {
    try {
      final token = await sessionManager.getToken();

      if (token == null || token.trim().isEmpty) {
        onError?.call("Token bulunamadı");
        return;
      }

      final uri = Uri.parse("${Constants.wsUrl}?token=$token");
      _channel = WebSocketChannel.connect(uri);

      onConnected?.call();

      _channel!.stream.listen(
            (event) {
          onMessage?.call(event.toString());
        },
        onError: (error) {
          onError?.call(error.toString());
          onDisconnected?.call();
        },
        onDone: () {
          onDisconnected?.call();
        },
      );
    } catch (e) {
      onError?.call(e.toString());
    }
  }

  void sendPing() {
    sendJson({
      "event": "PING",
      "data": {},
    });
  }

  void sendJson(Map<String, dynamic> json) {
    _channel?.sink.add(jsonEncode(json));
  }

  void disconnect() {
    _channel?.sink.close();
    _channel = null;
    onDisconnected?.call();
  }
}
