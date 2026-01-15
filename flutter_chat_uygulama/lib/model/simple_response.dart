class SimpleResponse {
  final bool success;
  final String? message;
  final int? id;
  final String? error;

  SimpleResponse({
    required this.success,
    this.message,
    this.id,
    this.error,
  });

  factory SimpleResponse.fromJson(Map<String, dynamic> json) {
    return SimpleResponse(
      success: json['success'],
      message: json['message'],
      id: json['id'] != null ? int.parse(json['id'].toString()) : null,
      error: json['error'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'success': success,
      'message': message,
      'id': id,
      'error': error,
    };
  }
}
