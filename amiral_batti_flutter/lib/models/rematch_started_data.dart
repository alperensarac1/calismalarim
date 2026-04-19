class RematchStartedData {
  final String roomCode;
  final String message;

  RematchStartedData({
    required this.roomCode,
    required this.message,
  });

  factory RematchStartedData.fromJson(Map<String, dynamic> json) {
    return RematchStartedData(
      roomCode: json['roomCode'] ?? '',
      message: json['message'] ?? '',
    );
  }
}


