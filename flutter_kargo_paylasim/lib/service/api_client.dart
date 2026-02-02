import 'dart:convert';
import 'package:http/http.dart' as http;
import '../storage/token_store.dart';

class ApiError implements Exception {
  final String message;
  ApiError(this.message);
  @override
  String toString() => message;
}

class ApiClient {
  final TokenStore tokenStore;
  ApiClient(this.tokenStore);

  Future<Map<String, String>> _headers({bool jsonBody = true}) async {
    final h = <String, String>{};
    if (jsonBody) h["Content-Type"] = "application/json";
    final token = await tokenStore.getToken();
    if (token != null && token.isNotEmpty) {
      h["X-Auth-Token"] = token;
    }
    return h;
  }

  Future<Map<String, dynamic>> postJson(String url, Map<String, dynamic> body) async {
    final res = await http.post(
      Uri.parse(url),
      headers: await _headers(),
      body: jsonEncode(body),
    );
    return _decodeOrThrow(res);
  }

  Future<Map<String, dynamic>> getJson(String url, {Map<String, String>? query}) async {
    final uri = Uri.parse(url).replace(queryParameters: query);
    final res = await http.get(uri, headers: await _headers(jsonBody: false));
    return _decodeOrThrow(res);
  }

  Map<String, dynamic> _decodeOrThrow(http.Response res) {
    final raw = res.body;
    try {
      final j = jsonDecode(raw);
      if (j is Map<String, dynamic>) return j;
      throw ApiError("Invalid JSON");
    } catch (_) {
      throw ApiError("Decode error. Raw:\n$raw");
    }
  }
}
