class FireResultData {
  final String roomCode;
  final String shooterPlayerId;
  final String targetPlayerId;
  final int row;
  final int col;
  final bool hit;
  final String? nextTurnPlayerId;
  final bool gameOver;
  final String? winnerPlayerId;
  final String message;

  FireResultData({
    required this.roomCode,
    required this.shooterPlayerId,
    required this.targetPlayerId,
    required this.row,
    required this.col,
    required this.hit,
    required this.nextTurnPlayerId,
    required this.gameOver,
    required this.winnerPlayerId,
    required this.message,
  });

  factory FireResultData.fromJson(Map<String, dynamic> json) {
    return FireResultData(
      roomCode: json['roomCode'] ?? '',
      shooterPlayerId: json['shooterPlayerId'] ?? '',
      targetPlayerId: json['targetPlayerId'] ?? '',
      row: json['row'] ?? 0,
      col: json['col'] ?? 0,
      hit: json['hit'] ?? false,
      nextTurnPlayerId: json['nextTurnPlayerId'],
      gameOver: json['gameOver'] ?? false,
      winnerPlayerId: json['winnerPlayerId'],
      message: json['message'] ?? '',
    );
  }
}
