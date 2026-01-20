import 'package:dio/dio.dart';
import 'package:pretty_dio_logger/pretty_dio_logger.dart';
import 'endpoints.dart';
import 'token_store.dart';

class ApiClient {
  final Dio dio;
  final TokenStore tokenStore;

  ApiClient({required this.tokenStore})
      : dio = Dio(BaseOptions(
    baseUrl: Endpoints.baseUrl,
    connectTimeout: const Duration(seconds: 20),
    receiveTimeout: const Duration(seconds: 20),
  )) {
    dio.interceptors.add(InterceptorsWrapper(
      onRequest: (options, handler) async {
        // Authorization gerektiren endpointlerde otomatik ekleyelim:
        // Ürünlerde token gerekmediği için ister ekler ister eklemeyiz; sorun olmaz.
        final token = await tokenStore.getToken();
        if (token != null && token.isNotEmpty) {
          options.headers["Authorization"] = "Bearer $token";
        }
        handler.next(options);
      },
    ));

    dio.interceptors.add(PrettyDioLogger(
      requestHeader: true,
      requestBody: true,
      responseHeader: false,
      responseBody: true,
      error: true,
      compact: true,
      maxWidth: 120,
    ));
  }
}
