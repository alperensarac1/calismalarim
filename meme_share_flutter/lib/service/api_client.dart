import 'package:dio/dio.dart';

class ApiClient {
  static const String baseUrl = 'https://alperensaracdeneme.com/meme/';

  static Dio createDio() {
    final dio = Dio(
      BaseOptions(
        baseUrl: baseUrl,
        connectTimeout: const Duration(seconds: 20),
        receiveTimeout: const Duration(seconds: 30),
        sendTimeout: const Duration(seconds: 60),
        headers: {
          'Accept': 'application/json',
        },
        // PHP bazen text/plain dönebiliyor. Dio yine decode eder ama
        // gerekirse responseType ayarlanabilir.
      ),
    );

    // İstersen debug log interceptor ekleyebilirsin:
    // dio.interceptors.add(LogInterceptor(
    //   requestBody: true,
    //   responseBody: true,
    // ));

    return dio;
  }
}
