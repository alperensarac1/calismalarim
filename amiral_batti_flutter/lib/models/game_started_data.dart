import 'package:amiral_batti_flutter/models/player_info.dart';

class GameStartedData {
  final String roomCode;
  final String firstTurnPlayerId;
  final List<PlayerInfo> players;
  final String message;

  GameStartedData({
    required this.roomCode,
    required this.firstTurnPlayerId,
    required this.players,
    required this.message,
  });

  factory GameStartedData.fromJson(Map<String, dynamic> json) {
    return GameStartedData(
      roomCode: json['roomCode'] ?? '',
      firstTurnPlayerId: json['firstTurnPlayerId'] ?? '',
      players: (json['players'] as List? ?? [])
          .map((e) => PlayerInfo.fromJson(e))
          .toList(),
      message: json['message'] ?? '',
    );
  }
}
