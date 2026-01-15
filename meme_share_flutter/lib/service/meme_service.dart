import 'dart:convert';
import 'package:dio/dio.dart';

import '../model/gonderi_model.dart';
import '../model/image_upload_request.dart';
import '../model/kullanici_response.dart';
import '../model/oda_model.dart';
import '../model/simple_response.dart';
import '../model/upload_response.dart';

class MemeApiService {
  final Dio _dio;

  MemeApiService(this._dio);

  /// Bazı PHP endpointleri JSON yerine string döndürebiliyor.
  /// Bu helper hem Map hem String durumunu toparlar.
  Map<String, dynamic> _asJsonMap(dynamic data) {
    if (data is Map<String, dynamic>) return data;
    if (data is String) return jsonDecode(data) as Map<String, dynamic>;
    return Map<String, dynamic>.from(data as Map);
  }

  List<dynamic> _asJsonList(dynamic data) {
    if (data is List) return data;
    if (data is String) return jsonDecode(data) as List<dynamic>;
    return List<dynamic>.from(data as List);
  }

  // ✅ POST media-upload-image.php (Base64 JSON body)
  Future<UploadResponse> uploadImageBase64(ImageUploadRequest request) async {
    final res = await _dio.post(
      'media-upload-image.php',
      data: request.toJson(),
      options: Options(contentType: Headers.jsonContentType),
    );

    final map = _asJsonMap(res.data);
    return UploadResponse.fromJson(map);
  }

  // ✅ POST media-upload-video.php (Multipart)
  Future<UploadResponse> uploadVideoMultipart({
    required int roomId,
    required int userId,
    required String caption,
    required String filePath, // cihazdaki video path
  }) async {
    final formData = FormData.fromMap({
      'room_id': roomId.toString(),
      'user_id': userId.toString(),
      'caption': caption,
      'video_file': await MultipartFile.fromFile(
        filePath,
        filename: filePath.split('/').last,
        // contentType: MediaType('video', 'mp4') // istersen eklenir
      ),
    });

    final res = await _dio.post(
      'media-upload-video.php',
      data: formData,
      options: Options(contentType: 'multipart/form-data'),
    );

    final map = _asJsonMap(res.data);
    return UploadResponse.fromJson(map);
  }

  // ✅ GET media-get-all.php?room_id=...
  Future<List<GonderiModel>> getAllMedia(int roomId) async {
    final res = await _dio.get(
      'media-get-all.php',
      queryParameters: {'room_id': roomId},
    );

    final list = _asJsonList(res.data);
    return list.map((e) => GonderiModel.fromJson(Map<String, dynamic>.from(e))).toList();
  }

  // ✅ GET rooms-join.php?user_id=...&room_code=...
  Future<SimpleResponse> joinRoom({
    required int userId,
    required String roomCode,
  }) async {
    final res = await _dio.get(
      'rooms-join.php',
      queryParameters: {'user_id': userId, 'room_code': roomCode},
    );

    final map = _asJsonMap(res.data);
    return SimpleResponse.fromJson(map);
  }

  // ✅ POST users-register.php (x-www-form-urlencoded)
  Future<KullaniciResponse> registerUser({
    required String username,
    required String password,
  }) async {
    final res = await _dio.post(
      'users-register.php',
      data: FormData.fromMap({
        'username': username,
        'password': password,
      }),
      options: Options(contentType: Headers.formUrlEncodedContentType),
    );

    final map = _asJsonMap(res.data);
    return KullaniciResponse.fromJson(map);
  }

  // ✅ POST users-login.php (x-www-form-urlencoded)
  Future<KullaniciResponse> loginUser({
    required String username,
    required String password,
  }) async {
    final res = await _dio.post(
      'users-login.php',
      data: FormData.fromMap({
        'username': username,
        'password': password,
      }),
      options: Options(contentType: Headers.formUrlEncodedContentType),
    );

    final map = _asJsonMap(res.data);
    return KullaniciResponse.fromJson(map);
  }

  // ✅ POST rooms-create.php (x-www-form-urlencoded)
  Future<SimpleResponse> createRoom({required int userId}) async {
    final res = await _dio.post(
      'rooms-create.php',
      data: FormData.fromMap({'user_id': userId}),
      options: Options(contentType: Headers.formUrlEncodedContentType),
    );

    final map = _asJsonMap(res.data);
    return SimpleResponse.fromJson(map);
  }

  // ✅ GET rooms-get-joined.php?user_id=...
  Future<List<OdaModel>> getJoinedRooms(int userId) async {
    final res = await _dio.get(
      'rooms-get-joined.php',
      queryParameters: {'user_id': userId},
    );

    final list = _asJsonList(res.data);
    return list.map((e) => OdaModel.fromJson(Map<String, dynamic>.from(e))).toList();
  }
}
