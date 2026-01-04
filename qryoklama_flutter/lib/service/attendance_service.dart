import 'dart:convert';
import '../util/constants.dart';
import 'api_client.dart';
import 'device_service.dart';

import 'location_service.dart';
import 'qr_parser.dart';

class AttendanceService {
  final ApiClient api;
  final DeviceService device;
  final LocationService location;
  final QRParser qr;

  AttendanceService({
    required this.api,
    required this.device,
    required this.location,
    required this.qr,
  });

  Future<Result<String>> sendByQR({
    required String studentNo,
    required String qrRaw,
  }) async {
    final pos = await location.getBestLocation();
    if (pos == null) return Result.err("Konum alınamadı veya izin yok.");

    Map<String, dynamic> payload;
    try {
      payload = qr.parsePayload(qrRaw);
    } catch (e) {
      return Result.err("QR JSON değil: $e");
    }

    final deviceId = await device.getOrCreateDeviceId();
    final deviceInfo = await device.getDeviceInfo();

    final body = {
      "student_no": studentNo,
      "method": "QR",
      "qr_payload": payload,
      "lat": pos.latitude,
      "lng": pos.longitude,
      "device_id": deviceId,
      "device_info": deviceInfo,
    };

    try {
      final resp = await api.postJson(kMarkUrl, body);
      if (resp.statusCode >= 200 && resp.statusCode < 300) {
        return Result.ok(utf8.decode(resp.bodyBytes));
      }
      return Result.err(_prettyError(resp.body, resp.statusCode));
    } catch (e) {
      return Result.err("Ağ hatası: $e");
    }
  }

  Future<Result<String>> sendByCode({
    required String studentNo,
    required String joinCode,
  }) async {
    final code = joinCode.trim();
    if (code.isEmpty) return Result.err("Kod boş olamaz.");

    final pos = await location.getBestLocation();
    if (pos == null) return Result.err("Konum alınamadı veya izin yok.");

    final deviceId = await device.getOrCreateDeviceId();
    final deviceInfo = await device.getDeviceInfo();

    final body = {
      "student_no": studentNo,
      "method": "CODE",
      "join_code": code,
      "lat": pos.latitude,
      "lng": pos.longitude,
      "device_id": deviceId,
      "device_info": deviceInfo,
    };

    try {
      final resp = await api.postJson(kMarkUrl, body);
      if (resp.statusCode >= 200 && resp.statusCode < 300) {
        return Result.ok(utf8.decode(resp.bodyBytes));
      }
      return Result.err(_prettyError(resp.body, resp.statusCode));
    } catch (e) {
      return Result.err("Ağ hatası: $e");
    }
  }

  String _prettyError(String resp, int code) {
    final s = resp.trim();
    final clean = (s.startsWith("{") || s.startsWith("["))
        ? s
        : s
        .replaceAll(RegExp(r'(?s)<[^>]*>'), ' ')
        .replaceAll("&quot;", '"')
        .replaceAll("&lt;", "<")
        .replaceAll("&gt;", ">")
        .replaceAll("&amp;", "&")
        .replaceAll(RegExp(r'\s+'), ' ')
        .trim();
    return "Sunucu Hatası ($code)\n$clean";
  }
}

class Result<T> {
  final T? data;
  final String? error;
  const Result._(this.data, this.error);

  factory Result.ok(T data) => Result._(data, null);
  factory Result.err(String msg) => Result._(null, msg);

  bool get isOk => error == null;
}
