import 'package:dio/dio.dart';

class DioClient {
  static const String baseUrl = 'https://alperensaracdeneme.com/mesajlasma/';

  static final Dio dio = Dio(
    BaseOptions(
      baseUrl: baseUrl,
      connectTimeout: const Duration(seconds: 20),
      receiveTimeout: const Duration(seconds: 20),
      // PHP tarafında çoğu zaman form-encoded kullanılıyor:
      contentType: Headers.formUrlEncodedContentType,
      responseType: ResponseType.json,
    ),
  );
}
