class ErrorData {
  final String message;

  ErrorData({required this.message});

  factory ErrorData.fromJson(Map<String, dynamic> json) {
    return ErrorData(message: json['message'] ?? '');
  }
}
