// api_client.dart
import 'package:dio/dio.dart';

class ApiClient {
  ApiClient._();

  static const String baseUrl = 'https://alperensaracdeneme.com/haberservis/';

  static final Dio dio = Dio(
    BaseOptions(
      baseUrl: baseUrl,
      connectTimeout: const Duration(seconds: 20),
      receiveTimeout: const Duration(seconds: 20),
      headers: {
        'Accept': 'application/json',
        // 'Content-Type': 'application/json', // JSON body atıyorsan aç
      },
    ),
  );

  // İstersen basit log interceptor:
  static void addLogging() {
    dio.interceptors.add(
      LogInterceptor(
        request: true,
        requestBody: true,
        responseBody: true,
        error: true,
      ),
    );
  }
}
