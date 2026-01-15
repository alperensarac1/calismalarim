import 'dart:async';

import 'package:flutter/material.dart';

import '../model/konusulan_kisi.dart';
import '../service/api_service.dart';

class SohbetListesiViewModel extends ChangeNotifier {
  final ApiService _api;

  SohbetListesiViewModel({ApiService? api}) : _api = api ?? ApiService();

  List<KonusulanKisi> _konusulanKisiler = [];
  List<KonusulanKisi> get konusulanKisiler => _konusulanKisiler;

  String? _hataMesaji;
  String? get hataMesaji => _hataMesaji;

  Timer? _yenilemeTimer;

  /// Kotlin: sohbetListesiniBaslat(kullaniciId)
  void sohbetListesiniBaslat({required int kullaniciId}) {
    _yenilemeTimer?.cancel();

    // İlk yükleme hemen
    _fetch(kullaniciId);

    _yenilemeTimer = Timer.periodic(const Duration(seconds: 15), (_) {
      _fetch(kullaniciId);
    });
  }

  Future<void> _fetch(int kullaniciId) async {
    try {
      final response = await _api.konusulanKisiler(kullaniciId: kullaniciId);

      if (response.success) {
        _konusulanKisiler = response.kisiler;
        _hataMesaji = null;
      } else {
        _hataMesaji = 'Liste alınamadı';
      }
    } catch (e) {
      _hataMesaji = 'Sunucu hatası: $e';
    }
    notifyListeners();
  }

  void stop() {
    _yenilemeTimer?.cancel();
    _yenilemeTimer = null;
  }

  @override
  void dispose() {
    stop();
    super.dispose();
  }
}
