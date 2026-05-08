class RadioRoom {
  final int id;
  final String roomName;
  final String? currentMusic;
  final bool isPlaying;
  final int listenerCount;

  RadioRoom({
    required this.id,
    required this.roomName,
    required this.currentMusic,
    required this.isPlaying,
    required this.listenerCount,
  });

  factory RadioRoom.fromJson(Map<String, dynamic> json) {
    return RadioRoom(
      id: json["id"] ?? 0,
      roomName: json["roomName"] ?? "",
      currentMusic: json["currentMusic"],
      isPlaying: json["isPlaying"] ?? false,
      listenerCount: json["listenerCount"] ?? 0,
    );
  }
}