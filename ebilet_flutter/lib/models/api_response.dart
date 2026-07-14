/// Backend'den gelen ortak cevap modelidir.
///
/// Örnek başarılı cevap:
/// {
///   "success": true,
///   "message": "İşlem başarılı",
///   "data": {...}
/// }
///
/// Örnek hatalı cevap:
/// {
///   "success": false,
///   "message": "E-posta veya şifre hatalı"
/// }
class ApiResponse<T> {
  final bool success;
  final String message;
  final T? data;

  ApiResponse({
    required this.success,
    required this.message,
    this.data,
  });

  /// Generic model olduğu için data parse işlemini dışarıdan alıyoruz.
  ///
  /// fromData:
  /// data alanını hangi modele çevireceğini söyler.
  factory ApiResponse.fromJson(
      Map<String, dynamic> json,
      T Function(dynamic json)? fromData,
      ) {
    return ApiResponse<T>(
      success: json['success'] == true,
      message: json['message']?.toString() ?? '',
      data: json['data'] != null && fromData != null
          ? fromData(json['data'])
          : null,
    );
  }
}