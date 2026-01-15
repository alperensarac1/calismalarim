class SimpleResponse {
  final bool success;
  final String message;
  final String? roomCode;
  final int? roomId;

  SimpleResponse({
    required this.success,
    required this.message,
    this.roomCode,
    this.roomId,
  });

  factory SimpleResponse.fromJson(Map<String, dynamic> json) {
    return SimpleResponse(
      success: json['success'],
      message: json['message'],
      roomCode: json['room_code'],
      roomId: json['room_id'],
    );
  }
}
