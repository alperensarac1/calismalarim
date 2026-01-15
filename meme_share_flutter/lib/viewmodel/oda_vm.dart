import 'dart:convert';
import 'dart:io';

import 'package:flutter/foundation.dart';
import '../model/image_upload_request.dart';
import '../model/oda_model.dart';
import '../model/simple_response.dart';
import '../service/meme_service.dart';

class OdaVM extends ChangeNotifier {
  final MemeApiService api;

  OdaVM({required this.api});

  // Kotlin: uploadResult (String)
  String? uploadResult;

  // Kotlin: joinedRooms LiveData
  List<OdaModel> joinedRooms = [];

  // Kotlin: odaOlusturmaSonucu LiveData
  SimpleResponse? odaOlusturmaSonucu;

  // Kotlin: joinResult LiveData
  SimpleResponse? joinResult;

  bool isLoadingRooms = false;
  bool isLoadingJoin = false;
  bool isLoadingCreate = false;
  bool isLoadingUploadImage = false;

  String? error;

  Future<void> fetchJoinedRooms(int userId) async {
    isLoadingRooms = true;
    error = null;
    notifyListeners();

    try {
      final list = await api.getJoinedRooms(userId);
      joinedRooms = list;
    } catch (e) {
      // Kotlin'de sessiz geçilmiş; ister error'a yazalım:
      error = 'Odalar alınamadı: $e';
    } finally {
      isLoadingRooms = false;
      notifyListeners();
    }
  }

  Future<void> joinRoom({required int userId, required String roomCode}) async {
    isLoadingJoin = true;
    error = null;
    notifyListeners();

    try {
      final res = await api.joinRoom(userId: userId, roomCode: roomCode);
      joinResult = res;

      if (res.success != true) {
        error = res.message.isNotEmpty ? res.message : 'Katılım başarısız';
      }
    } catch (e) {
      joinResult = SimpleResponse(
        success: false,
        message: 'Hata: $e',
        roomCode: null,
        roomId: null,
      );
      error = joinResult!.message;
    } finally {
      isLoadingJoin = false;
      notifyListeners();
    }
  }

  Future<void> createRoom({required int userId}) async {
    isLoadingCreate = true;
    error = null;
    notifyListeners();

    try {
      final res = await api.createRoom(userId: userId);
      odaOlusturmaSonucu = res;

      if (res.success != true) {
        error = res.message.isNotEmpty ? res.message : 'Sunucu yanıtı başarısız';
      }
    } catch (e) {
      odaOlusturmaSonucu = SimpleResponse(
        success: false,
        message: 'Bağlantı hatası: $e',
        roomCode: null,
        roomId: null,
      );
      error = odaOlusturmaSonucu!.message;
    } finally {
      isLoadingCreate = false;
      notifyListeners();
    }
  }

  /// Flutter: base64 image upload (Kotlin'de Uri -> Bitmap -> base64)
  /// Burada filePath alıyoruz (image_picker ile gelir).
  Future<void> uploadImageBase64({
    required String filePath,
    required int roomId,
    required int userId,
    required String caption,
  }) async {
    isLoadingUploadImage = true;
    uploadResult = null;
    error = null;
    notifyListeners();

    try {
      final file = File(filePath);
      if (!await file.exists()) {
        uploadResult = 'Görsel alınamadı. Dosya yok.';
        isLoadingUploadImage = false;
        notifyListeners();
        return;
      }

      final bytes = await file.readAsBytes();
      final base64Image = base64Encode(bytes);

      final request = ImageUploadRequest(
        roomId: roomId,
        userId: userId,
        base64Image: base64Image,
        caption: caption,
      );

      final res = await api.uploadImageBase64(request);
      uploadResult = res.success ? 'Görsel yüklendi' : 'Görsel yükleme hatası: ${res.message}';

      if (!res.success) {
        error = uploadResult;
      }
    } catch (e) {
      uploadResult = 'Bağlantı/Okuma hatası: $e';
      error = uploadResult;
    } finally {
      isLoadingUploadImage = false;
      notifyListeners();
    }
  }
}
