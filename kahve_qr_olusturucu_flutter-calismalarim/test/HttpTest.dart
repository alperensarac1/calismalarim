import 'package:dio/dio.dart';

Future<void> main() async {
    final dio = Dio(BaseOptions(baseUrl: "https://alperensaracdeneme.com/kahveservis/"));
    final response = await dio.get("tum_kahveler.php");

    print("Status code: ${response.statusCode}");
    print("Body: ${response.data}");

}