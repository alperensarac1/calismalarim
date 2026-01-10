// haber_response.dart
import 'haber_model.dart';

class HaberResponse {
  final bool success;
  final HaberModel? data;
  final String? message;

  const HaberResponse({
    required this.success,
    this.data,
    this.message,
  });

  factory HaberResponse.fromJson(Map<String, dynamic> json) {
    return HaberResponse(
      success: (json['success'] ?? false) as bool,
      data: json['data'] == null
          ? null
          : HaberModel.fromJson(json['data'] as Map<String, dynamic>),
      message: json['message'] as String?,
    );
  }

  Map<String, dynamic> toJson() => {
    'success': success,
    'data': data?.toJson(),
    'message': message,
  };
}
