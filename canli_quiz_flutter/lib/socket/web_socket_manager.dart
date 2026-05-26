import 'dart:async';
import 'package:web_socket_channel/web_socket_channel.dart';

class WebSocketManager {
  /*
    Flutter Android Emulator için:
      ws://10.0.2.2:8765

    iOS Simulator için:
      ws://127.0.0.1:8765

    Fiziksel telefon için:
      ws://BILGISAYAR_IP_ADRESI:8765
  */

  static final WebSocketManager instance = WebSocketManager._internal();

  WebSocketManager._internal();

  static const String serverUrl = "ws://10.0.2.2:8765";

  WebSocketChannel? _channel;

  bool _connected = false;

  final StreamController<String> _messageController =
  StreamController<String>.broadcast();

  Stream<String> get messages => _messageController.stream;

  bool get isConnected => _connected && _channel != null;

  void connect() {
    if (isConnected) {
      return;
    }

    try {
      _channel = WebSocketChannel.connect(Uri.parse(serverUrl));
      _connected = true;

      _channel!.stream.listen(
            (event) {
          _messageController.add(event.toString());
        },
        onError: (error) {
          _connected = false;
          _messageController.addError(error);
        },
        onDone: () {
          _connected = false;
        },
      );
    } catch (e) {
      _connected = false;
      _messageController.addError(e);
    }
  }

  void send(String message) {
    if (!isConnected) {
      _messageController.addError("WebSocket bağlı değil.");
      return;
    }

    _channel?.sink.add(message);
  }

  void disconnect() {
    _connected = false;
    _channel?.sink.close();
    _channel = null;
  }

  void dispose() {
    disconnect();
    _messageController.close();
  }
}