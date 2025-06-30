class CRUDCevap {
  final int success;
  final String message;
  final int kahveSayisi;
  final int hediyeKahve;

  CRUDCevap({
    required this.success,
    required this.message,
    required this.kahveSayisi,
    required this.hediyeKahve,
  });

  factory CRUDCevap.fromJson(Map<String, dynamic> json) {
    return CRUDCevap(
      success: json['success'] ?? 0,
      message: json['message'] ?? '',
      kahveSayisi: json['icilen_kahve'] ?? 0,
      hediyeKahve: json['hediye_kahve'] ?? 0,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'success': success,
      'message': message,
      'icilen_kahve': kahveSayisi,
      'hediye_kahve': hediyeKahve,
    };
  }
}
