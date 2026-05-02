import 'dart:convert';

import 'package:http/http.dart' as http;

import '../core/Constants.dart';
import '../core/session_manager.dart';

class ApiClient {
  final SessionManager sessionManager;

  ApiClient({
    required this.sessionManager,
  });

  Future<Map<String, dynamic>> post(
      String path,
      Map<String, dynamic> body,
      ) async {
    final token = await sessionManager.getToken();

    final response = await http.post(
      Uri.parse(Constants.baseUrl + path),
      headers: {
        "Content-Type": "application/json",
        if (token != null && token.isNotEmpty) "Authorization": "Bearer $token",
      },
      body: jsonEncode(body),
    );

    return _handleResponse(response);
  }

  Future<Map<String, dynamic>> put(
      String path,
      Map<String, dynamic> body,
      ) async {
    final token = await sessionManager.getToken();

    final response = await http.put(
      Uri.parse(Constants.baseUrl + path),
      headers: {
        "Content-Type": "application/json",
        if (token != null && token.isNotEmpty) "Authorization": "Bearer $token",
      },
      body: jsonEncode(body),
    );

    return _handleResponse(response);
  }

  Future<Map<String, dynamic>> get(String path) async {
    final token = await sessionManager.getToken();

    final response = await http.get(
      Uri.parse(Constants.baseUrl + path),
      headers: {
        "Content-Type": "application/json",
        if (token != null && token.isNotEmpty) "Authorization": "Bearer $token",
      },
    );

    return _handleResponse(response);
  }

  Map<String, dynamic> _handleResponse(http.Response response) {
    final decoded = response.body.isNotEmpty
        ? jsonDecode(response.body) as Map<String, dynamic>
        : <String, dynamic>{};

    if (response.statusCode >= 200 && response.statusCode < 300) {
      return decoded;
    }

    throw Exception(decoded["detail"]?.toString() ?? response.body);
  }
}
