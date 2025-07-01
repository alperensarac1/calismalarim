import 'package:flutter/cupertino.dart';

import '../model/Masa.dart';
import '../service/AdisyonService.dart';

class MasalarViewModel extends ChangeNotifier {
  final AdisyonService _service = AdisyonService(
    baseUrl: 'https://alperensaracdeneme.com/adisyon/',
  );

  List<Masa> _masalar = [];
  bool _birlesmeSonucu = false;

  List<Masa> get masalar => _masalar;
  bool get birlesmeSonucu => _birlesmeSonucu;

  Future<void> masalariYukle() async {
    try {
      _masalar = await _service.getMasalar();
      notifyListeners();
    } catch (e) {
      debugPrint('Masalari yuklerken hata: $e');
    }
  }

  void guncelleMasa(Masa masa) {
    final index = _masalar.indexWhere((m) => m.id == masa.id);
    if (index != -1) {
      _masalar[index] = masa;
      notifyListeners();
    }
  }

  Future<void> masaEkle({VoidCallback? onSuccess}) async {
    try {
      await _service.masaEkle();
      if (onSuccess != null) onSuccess();
      await masalariYukle();
    } catch (e) {
      debugPrint('Masa eklerken hata: $e');
    }
  }

  Future<void> masaSil(int masaId) async {
    try {
      await _service.masaSil(masaId);
      await masalariYukle();
    } catch (e) {
      debugPrint('Masa silerken hata: $e');
    }
  }

  Future<void> masaBirlestir(int anaId, int bId) async {
    try {
      await _service.masaBirlestir(anaId, bId);
      _birlesmeSonucu = true;
      notifyListeners();
    } catch (e) {
      debugPrint('Masa birlestirirken hata: $e');
    }
  }
}
