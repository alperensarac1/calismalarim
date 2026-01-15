import 'dart:async';

import 'package:flutter/material.dart';

import '../model/mesaj.dart';
import '../service/api_service.dart';

class MesajlarViewModel extends ChangeNotifier {
  final ApiService _api;

  MesajlarViewModel({ApiService? api}) : _api = api ?? ApiService();

  List<Mesaj> _mesajlar = [];
  List<Mesaj> get mesajlar => _mesajlar;

  String? _hataMesaji;
  String? get hataMesaji => _hataMesaji;

  Timer? _mesajGuncelleTimer;

  /// Kotlin: mesajlariYuklePeriyodik(gonderenId, aliciId)
  void mesajlariYuklePeriyodik({
    required int gonderenId,
    required int aliciId,
  }) {
    _mesajGuncelleTimer?.cancel();

    // İlk yükleme hemen
    _yukleMesajlar(gonderenId: gonderenId, aliciId: aliciId);

    _mesajGuncelleTimer = Timer.periodic(const Duration(seconds: 15), (_) {
      _yukleMesajlar(gonderenId: gonderenId, aliciId: aliciId);
    });
  }

  Future<void> _yukleMesajlar({
    required int gonderenId,
    required int aliciId,
  }) async {
    try {
      final response = await _api.mesajlariGetir(
        gonderenId: gonderenId,
        aliciId: aliciId,
      );

      if (response.success) {
        _mesajlar = response.mesajlar;
        _hataMesaji = null;
      } else {
        _hataMesaji = 'Mesajlar yüklenemedi';
      }
    } catch (e) {
      _hataMesaji = 'Hata: $e';
    }
    notifyListeners();
  }

  /// Kotlin: mesajGonder(...)
  Future<void> mesajGonder({
    required int gonderenId,
    required int aliciId,
    required String mesajText,
    String? base64Image,
  }) async {
    final resimVar = (base64Image == null || base64Image.isEmpty) ? 0 : 1;

    try {
      final response = await _api.mesajGonder(
        gonderenId: gonderenId,
        aliciId: aliciId,
        mesajText: mesajText,
        resimVar: resimVar,
        base64Img: base64Image,
      );

      if (response.success) {
        await _yukleMesajlar(gonderenId: gonderenId, aliciId: aliciId);
      } else {
        _hataMesaji = response.error ?? 'Mesaj gönderilemedi';
        notifyListeners();
      }
    } catch (e) {
      _hataMesaji = 'Hata: $e';
      notifyListeners();
    }
  }

  void stop() {
    _mesajGuncelleTimer?.cancel();
    _mesajGuncelleTimer = null;
  }

  @override
  void dispose() {
    stop();
    super.dispose();
  }
}
