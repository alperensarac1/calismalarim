class KodUretCevap {
  final int success;
  final String message;

  KodUretCevap({
    required this.success,
    required this.message,
  });

  factory KodUretCevap.fromJson(Map<String, dynamic> json) {
    return KodUretCevap(
      success: json['success'],
      message: json['message'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'success': success,
      'message': message,
    };
  }
}
