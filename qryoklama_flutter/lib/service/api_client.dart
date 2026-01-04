import 'dart:convert';
import 'package:http/http.dart' as http;

class ApiClient {
  Future<http.Response> postJson(String url, Map<String, dynamic> body,
      {Map<String, String>? headers}) {
    return http.post(
      Uri.parse(url),
      headers: {
        "Content-Type": "application/json; charset=utf-8",
        "X-Platform": "flutter",
        ...?headers,
      },
      body: jsonEncode(body),
    );
  }

  Future<http.Response> get(String url, {Map<String, String>? headers}) {
    return http.get(Uri.parse(url), headers: headers);
  }
}
