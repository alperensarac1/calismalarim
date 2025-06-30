import 'package:flutter/material.dart';
import 'package:kahve_qr_olusturucu/model/UrunKategori.dart';
import 'package:kahve_qr_olusturucu/services/http_service/KahveHttpServisDao.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized(); // async kod için gerekli

  final servis = KahveHTTPServisDao();

  final kategori = UrunKategori.ICECEKLER; // Denemek istediğin kategori
  try {
    final cevap = await servis.kahveWithKategoriId(kategori);
    print(" ${cevap.kahveUrun.length} ürün bulundu");

    for (var urun in cevap.kahveUrun) {
      print("🔹 ${urun.urunAd} (${urun.urunKategoriId}) - ${urun.urunFiyat} TL");
    }
  } catch (e) {
    print("API çağrısında hata oluştu: $e");
  }
}
