// urun_view_model.dart
import 'package:flutter/foundation.dart';
import '../model/Kategori.dart';
import '../model/Urun.dart';
import '../service/AdisyonService.dart';



class UrunSilResponse {
  final bool success;
  final String? message;
  UrunSilResponse({required this.success, this.message});
  factory UrunSilResponse.fromJson(Map<String, dynamic> json) =>
      UrunSilResponse(success: json['success'] ?? false, message: json['message']);
}

class KategoriSilResponse {
  final bool success;
  final String? message;
  KategoriSilResponse({required this.success, this.message});
  factory KategoriSilResponse.fromJson(Map<String, dynamic> json) =>
      KategoriSilResponse(success: json['success'] ?? false, message: json['message']);
}


class UrunViewModel extends ChangeNotifier {
  final AdisyonService _service = AdisyonService(
    baseUrl: 'https://alperensaracdeneme.com/adisyon/',
  );

  /*── State alanları ──*/
  List<Urun> _urunler = [];
  List<Kategori> _kategoriler = [];
  bool _isLoading = false;
  String? _error;

  /*── CRUD sonuç flag’i ──*/
  bool _sonuc = false;

  /*── Response objeleri ──*/
  UrunSilResponse? _silmeSonucu;
  KategoriSilResponse? _kategoriSilSonuc;

  /*── Getters ──*/
  List<Urun> get urunler => _urunler;
  List<Kategori> get kategoriler => _kategoriler;
  bool get sonuc => _sonuc;
  bool get isLoading => _isLoading;
  String? get error => _error;
  UrunSilResponse? get silmeSonucu => _silmeSonucu;
  KategoriSilResponse? get kategoriSilSonuc => _kategoriSilSonuc;

  /*──────────────── Helper: hata ayarla ─────────────*/
  void _setError(String? message) {
    _error = message;
    notifyListeners();
  }

  Future<void> hazirla() async {
    debugPrint("▶️ UrunViewModel hazirla başladı");
    _isLoading = true;
    _setError(null);
    notifyListeners();

    try {
      await kategorileriYukle();
      debugPrint("✅ Kategoriler yüklendi");

      await urunleriYukle();
      debugPrint("✅ Ürünler yüklendi");
    } catch (e) {
      debugPrint("❌ Hazırlama hatası: $e");
    } finally {
      _isLoading = false;
      debugPrint("🏁 hazirla bitti");
      notifyListeners();
    }
  }


  Future<void> urunleriYukle() async {
    try {
      _urunler = await _service.getUrunler();
      _setError(null);
    } catch (e) {
      _urunler = [];
      _setError('Ürün yükleme hatası: $e');
    }
  }

  Future<void> kategorileriYukle() async {
    try {
      _kategoriler = await _service.getKategoriler();
      _setError(null);
    } catch (e) {
      _kategoriler = [];
      _setError('Kategori yükleme hatası: $e');
    }
  }

  /*──────────────── Ürün CRUD ───────────────────────*/
  Future<void> urunEkle(
      String ad,
      double fiyat,
      String base64,
      int kategoriId,
      ) async {
    try {
      await _service.urunEkle(ad, fiyat, kategoriId, 1, base64);
      _sonuc = true;
      await urunleriYukle();           // listeyi tazele
    } catch (e) {
      _sonuc = false;
      _setError('Ürün ekleme hatası: $e');
    }
  }

  Future<void> urunSil(String ad) async {
    try {
      final json = await _service.urunSil(ad);
      _silmeSonucu = UrunSilResponse.fromJson(json);
      _sonuc = _silmeSonucu?.success ?? false;
      if (_sonuc) await urunleriYukle();
    } catch (e) {
      _sonuc = false;
      _silmeSonucu = UrunSilResponse(success: false, message: e.toString());
      _setError('Ürün silme hatası: $e');
    }
  }

  /*──────────────── Kategori CRUD ───────────────────*/
  Future<void> kategoriEkle(String ad) async {
    try {
      await _service.kategoriEkle(ad);
      _sonuc = true;
      await kategorileriYukle();
    } catch (e) {
      _sonuc = false;
      _setError('Kategori ekleme hatası: $e');
    }
  }

  Future<void> kategoriSil(int id) async {
    try {
      final json = await _service.kategoriSil(id);
      _kategoriSilSonuc = KategoriSilResponse.fromJson(json);
      _sonuc = _kategoriSilSonuc?.success ?? false;
      if (_sonuc) await kategorileriYukle();
    } catch (e) {
      _sonuc = false;
      _kategoriSilSonuc = KategoriSilResponse(success: false, message: e.toString());
      _setError('Kategori silme hatası: $e');
    }
  }
}
