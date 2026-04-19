class AppConfig {
  static String serverIp = "10.19.82.112";
  static int serverPort = 8080;

  static String get webSocketUrl => "ws://$serverIp:$serverPort";
  static String get httpBaseUrl => "http://$serverIp:$serverPort/";
}
