import 'package:flutter/foundation.dart';

import '../model/kategori.dart';
import '../model/urun.dart';
import '../service/services.dart';
import '../service/services_impl.dart';
import '../utils/extension.dart';

class UrunViewModel extends ChangeNotifier {
  final Services _dao;

  UrunViewModel({Services? dao}) : _dao = dao ?? ServicesImpl.getInstance();

  List<Urun> urunler = const [];
  List<Kategori> kategoriler = const [];

  bool loadingUrun = false;
  bool loadingKategori = false;

  Future<void> urunleriYukle() async {
    loadingUrun = true;
    notifyListeners();
    try {
      urunler = await _dao.urunleriGetir();
    } catch (e, st) {
      logE('UrunVM', e, st);
    } finally {
      loadingUrun = false;
      notifyListeners();
    }
  }

  Future<void> kategorileriYukle() async {
    loadingKategori = true;
    notifyListeners();
    try {
      kategoriler = await _dao.kategorileriGetir();
    } catch (e, st) {
      logE('UrunVM', e, st);
    } finally {
      loadingKategori = false;
      notifyListeners();
    }
  }

  Future<void> urunEkle({
    required String ad,
    required double fiyat,
    required int kategoriId,
    required String base64,
  }) async {
    try {
      await _dao.urunEkleAdmin(ad, fiyat, kategoriId, 0, base64);
      await urunleriYukle();
    } catch (e, st) {
      logE('UrunVM', e, st);
    }
  }

  Future<void> urunSil(String urunAd) async {
    try {
      await _dao.urunSil(urunAd);
      await urunleriYukle();
    } catch (e, st) {
      logE('UrunVM', e, st);
    }
  }

  Future<void> kategoriEkle(String ad) async {
    try {
      await _dao.kategoriEkle(ad);
      await kategorileriYukle();
    } catch (e, st) {
      logE('UrunVM', e, st);
    }
  }

  Future<void> kategoriSil(int kategoriId) async {
    try {
      await _dao.kategoriSil(kategoriId);
      await kategorileriYukle();
    } catch (e, st) {
      logE('UrunVM', e, st);
    }
  }
}
