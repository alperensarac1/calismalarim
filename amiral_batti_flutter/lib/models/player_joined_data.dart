import 'package:amiral_batti_flutter/models/player_info.dart';

class PlayerJoinedData {
  final String roomCode;
  final List<PlayerInfo> players;
  final String message;

  PlayerJoinedData({
    required this.roomCode,
    required this.players,
    required this.message,
  });

  factory PlayerJoinedData.fromJson(Map<String, dynamic> json) {
    return PlayerJoinedData(
      roomCode: json['roomCode'] ?? '',
      players: (json['players'] as List? ?? [])
          .map((e) => PlayerInfo.fromJson(e))
          .toList(),
      message: json['message'] ?? '',
    );
  }
}
