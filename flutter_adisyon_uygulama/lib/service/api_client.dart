import 'package:dio/dio.dart';

class ApiClient {
  static const String baseUrl = 'https://alperensaracdeneme.com/adisyon/';

  final Dio dio;

  ApiClient._(this.dio);

  factory ApiClient.create() {
    final dio = Dio(
      BaseOptions(
        baseUrl: baseUrl,
        connectTimeout: const Duration(seconds: 20),
        receiveTimeout: const Duration(seconds: 20),
        // PHP tarafı genelde form-url-encoded beklediği için defaultu böyle tutmak iyi:
        contentType: Headers.formUrlEncodedContentType,
      ),
    );

    return ApiClient._(dio);
  }
}