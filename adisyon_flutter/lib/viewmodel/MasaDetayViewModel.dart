import 'package:flutter/foundation.dart';
import '../model/Masa.dart';
import '../model/MasaUrun.dart';
import '../model/Urun.dart';
import '../service/AdisyonService.dart';

class MasaDetayViewModel extends ChangeNotifier {
  MasaDetayViewModel(this.masaId, {AdisyonService? service})
      : _service = service ?? AdisyonService(baseUrl: 'https://alperensaracdeneme.com/adisyon/');

  final int masaId;
  final AdisyonService _service;

  // ───────── private state ─────────
  List<MasaUrun> _masaUrunler = [];
  double _toplamFiyat = 0;
  bool _isLoading = false;
  Masa? _masa;
  bool _odemeTamamlandi = false;

  // ───────── getters (public) ─────────
  List<MasaUrun> get masaUrunler => _masaUrunler;
  double get toplamFiyat => _toplamFiyat;
  bool get isLoading => _isLoading;
  Masa? get masa => _masa;
  bool get odemeTamamlandi => _odemeTamamlandi;

  // ───────── high‑level helpers ─────────
  Future<void> yukleTumVeriler() async {
    _isLoading = true;
    notifyListeners();
    try {
      _masa = await _service.masaGetir(masaId);
      _masaUrunler = await _service.getMasaUrunleri(masaId);
      _toplamFiyat = _masaUrunler.fold<double>(0, (sum, e) => sum + e.toplamFiyat);
    } catch (e) {
      debugPrint('Veri yuklenemedi: $e');
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> urunEkle(int urunId, {int adet = 1}) async {
    try {
      await _service.masaUrunEkle(masaId, urunId, adet);
      await yukleTumVeriler();
    } catch (e) {
      debugPrint('Urun ekleme hatasi: $e');
    }
  }

  Future<void> urunCikar(int urunId) async {
    try {
      await _service.urunCikar(masaId, urunId);
      await yukleTumVeriler();
    } catch (e) {
      debugPrint('Urun cikar hatasi: $e');
    }
  }

  Future<void> odemeAl(VoidCallback onSuccess) async {
    try {
      await _service.masaOde(masaId);
      _odemeTamamlandi = true;
      _toplamFiyat = 0;
      notifyListeners();
      onSuccess();
    } catch (e) {
      debugPrint('Odeme alma hatasi: $e');
    }
  }
}
