

import 'package:dio/dio.dart';
import 'package:kahve_qr_olusturucu/services/Services.dart';

import '../../model/CRUDCevap.dart';
import '../../model/KodUretCevap.dart';
import '../../model/UrunCevap.dart';
import '../../model/UrunKategori.dart';

class KahveHTTPServisDao implements Services {
  final Dio _dio = Dio(BaseOptions(baseUrl: "https://alperensaracdeneme.com/kahveservis/")); // Burayı ayarla

  Future<CRUDCevap> kullaniciEkle(String kisiTel) async {
    try {
      final response = await _dio.post("kullanici_ekle.php", data: {"telefon_no": kisiTel});
      print(response.data.toString());
      return CRUDCevap.fromJson(response.data);
    } catch (e) {
      return CRUDCevap(success: 0, message: "Yükleme Başarısız", kahveSayisi: 0, hediyeKahve: 0);
    }
  }

  Future<UrunCevap> tumKahveler() async {
    try {
      final response = await _dio.get("tum_kahveler.php");
      print(response.data.toString());
      return UrunCevap.fromJson(response.data);
    } catch (e) {
      return UrunCevap(kahveUrun: []);
    }
  }

  Future<KodUretCevap> kodUret(String dogrulamaKodu, String kisiTel) async {
    try {
      final response = await _dio.post("kod_uret.php", data: {
        "dogrulama_kodu": dogrulamaKodu,
        "telefon_no": kisiTel,
      });
      return KodUretCevap.fromJson(response.data);
    } catch (e) {
      return KodUretCevap(success: 0, message: "Kod üretimi başarısız: ${e.toString()}");
    }
  }

  @override
  Future<UrunCevap> kahveWithKategoriId(UrunKategori kategori) async {
    try {
      final response = await _dio.get("kahve_with_kategori_id.php", queryParameters: {
        "id": kategori.kategoriKodu
      });

      print("Kategori ${kategori.kategoriKodu} için gelen veri: ${response.data}");

      return UrunCevap.fromJson(response.data);
    } catch (e) {
      print(" kahveWithKategoriId hata: $e");
      return UrunCevap(kahveUrun: []);
    }
  }


  Future<KodUretCevap> kodSil(String dogrulamaKodu) async {
    try {
      final response = await _dio.post("kod_sil.php", data: {
        "dogrulama_kodu": dogrulamaKodu,
      });
      return KodUretCevap.fromJson(response.data);
    } catch (e) {
      return KodUretCevap(success: 0, message: "Kod silme başarısız");
    }
  }
}
