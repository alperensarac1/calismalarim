import 'package:dio/dio.dart';
import '../model/upload_response.dart';

class VideoUploader {
  final Dio dio;
  VideoUploader(this.dio);

  Future<UploadResponse> uploadVideo({
    required String uploadUrl, // full url da verebilirsin
    required int roomId,
    required int userId,
    required String caption,
    required String filePath,
  }) async {
    final form = FormData.fromMap({
      'room_id': roomId.toString(),
      'user_id': userId.toString(),
      'caption': caption,
      'video_file': await MultipartFile.fromFile(
        filePath,
        filename: filePath.split('/').last,
      ),
    });

    final res = await dio.post(
      uploadUrl,
      data: form,
      options: Options(contentType: 'multipart/form-data'),
    );

    final data = res.data is Map<String, dynamic>
        ? res.data as Map<String, dynamic>
        : Map<String, dynamic>.from(res.data);

    return UploadResponse.fromJson(data);
  }
}
