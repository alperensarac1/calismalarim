import 'package:amiral_batti_flutter/models/player_info.dart';

class RoomCreatedData {
  final String roomCode;
  final String playerId;
  final List<PlayerInfo> players;
  final String message;

  RoomCreatedData({
    required this.roomCode,
    required this.playerId,
    required this.players,
    required this.message,
  });

  factory RoomCreatedData.fromJson(Map<String, dynamic> json) {
    return RoomCreatedData(
      roomCode: json['roomCode'] ?? '',
      playerId: json['playerId'] ?? '',
      players: (json['players'] as List? ?? [])
          .map((e) => PlayerInfo.fromJson(e))
          .toList(),
      message: json['message'] ?? '',
    );
  }
}
