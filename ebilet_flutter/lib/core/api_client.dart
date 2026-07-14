import 'dart:convert';

import 'package:http/http.dart' as http;

/// APIClient
///
/// PHP backend'e POST isteği atan temel sınıftır.
///
/// PHP tarafı $_POST beklediği için
/// application/x-www-form-urlencoded formatında veri gönderiyoruz.
class ApiClient {
  /// Android Emulator için:
  static const String baseUrl = 'https://alperensaracdeneme.com/event_ticket_api/';

  /// iOS Simulator için:
  /// static const String baseUrl = 'http://localhost/event_ticket_api/';

  /// Gerçek telefon için:
  /// static const String baseUrl = 'http://192.168.1.35/event_ticket_api/';

  /// Genel POST isteği.
  static Future<Map<String, dynamic>> post(
      String endpoint,
      Map<String, String> parameters,
      ) async {
    final uri = Uri.parse(baseUrl + endpoint);

    final response = await http.post(
      uri,
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      body: parameters,
    );

    if (response.statusCode < 200 || response.statusCode >= 300) {
      throw Exception('HTTP sunucu hatası: ${response.statusCode}');
    }

    try {
      final decoded = jsonDecode(response.body);

      if (decoded is Map<String, dynamic>) {
        return decoded;
      }

      throw Exception('Geçersiz JSON formatı');
    } catch (e) {
      print('JSON Decode Error Raw Response:');
      print(response.body);
      rethrow;
    }
  }
}