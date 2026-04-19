abstract class SocketEventListener {
  void onConnected();
  void onDisconnected();
  void onMessage(String message);
  void onError(String errorMessage);
}

class SocketManager {
  SocketManager._();
  static final SocketManager instance = SocketManager._();

  WebSocketChannel? _channel;
  StreamSubscription? _subscription;
  SocketEventListener? _listener;
  bool _isConnected = false;

  bool get isConnected => _isConnected;

  void setListener(SocketEventListener? listener) {
    _listener = listener;
  }

  void clearListener(SocketEventListener owner) {
    if (identical(_listener, owner)) {
      _listener = null;
    }
  }

  Future<void> connect() async {
    if (_isConnected) {
      _listener?.onConnected();
      return;
    }

    try {
      _channel = WebSocketChannel.connect(Uri.parse(AppConfig.webSocketUrl));
      _isConnected = true;
      _listener?.onConnected();

      _subscription = _channel!.stream.listen(
            (event) {
          _listener?.onMessage(event.toString());
        },
        onError: (error) {
          _isConnected = false;
          _listener?.onError(error.toString());
        },
        onDone: () {
          _isConnected = false;
          _listener?.onDisconnected();
        },
        cancelOnError: true,
      );
    } catch (e) {
      _isConnected = false;
      _listener?.onError(e.toString());
    }
  }

  void sendMap(Map<String, dynamic> data) {
    sendText(jsonEncode(data));
  }

  void sendText(String text) {
    try {
      _channel?.sink.add(text);
    } catch (e) {
      _listener?.onError(e.toString());
    }
  }

  Future<void> disconnect() async {
    await _subscription?.cancel();
    await _channel?.sink.close();
    _subscription = null;
    _channel = null;
    _isConnected = false;
  }
}
