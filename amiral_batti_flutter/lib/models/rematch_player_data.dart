import 'package:amiral_batti_flutter/models/rematch_player_info.dart';

class RematchStatusData {
  final String roomCode;
  final List<RematchPlayerInfo> players;
  final String message;

  RematchStatusData({
    required this.roomCode,
    required this.players,
    required this.message,
  });

  factory RematchStatusData.fromJson(Map<String, dynamic> json) {
    return RematchStatusData(
      roomCode: json['roomCode'] ?? '',
      players: (json['players'] as List? ?? [])
          .map((e) => RematchPlayerInfo.fromJson(e))
          .toList(),
      message: json['message'] ?? '',
    );
  }
}
