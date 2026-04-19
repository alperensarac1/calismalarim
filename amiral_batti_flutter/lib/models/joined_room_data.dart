import 'package:amiral_batti_flutter/models/player_info.dart';

class JoinedRoomData {
  final String roomCode;
  final String playerId;
  final List<PlayerInfo> players;
  final String message;

  JoinedRoomData({
    required this.roomCode,
    required this.playerId,
    required this.players,
    required this.message,
  });

  factory JoinedRoomData.fromJson(Map<String, dynamic> json) {
    return JoinedRoomData(
      roomCode: json['roomCode'] ?? '',
      playerId: json['playerId'] ?? '',
      players: (json['players'] as List? ?? [])
          .map((e) => PlayerInfo.fromJson(e))
          .toList(),
      message: json['message'] ?? '',
    );
  }
}
