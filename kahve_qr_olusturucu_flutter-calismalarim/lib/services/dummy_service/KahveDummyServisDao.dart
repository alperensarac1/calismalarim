import 'dart:async';
import 'package:kahve_qr_olusturucu/model/CRUDCevap.dart';
import 'package:kahve_qr_olusturucu/model/UrunCevap.dart';
import 'package:uuid/uuid.dart';
import 'package:kahve_qr_olusturucu/services/dummy_service/Urunler.dart';
import '../../model/KodUretCevap.dart';
import '../../model/UrunKategori.dart';
import '../Services.dart';

class KahveDummyServisDao implements Services {
  @override
  Future<CRUDCevap> kullaniciEkle(String kisiTel) async {
    return CRUDCevap(
      success: 1,
      message: "Ekleme başarılı",
      kahveSayisi: 0,
      hediyeKahve: 0,
    );
  }

  @override
  Future<UrunCevap> tumKahveler() async {
    return UrunCevap(kahveUrun: Urunler.dummyUrunler);
  }

  @override
  Future<KodUretCevap> kodUret(String dogrulamaKodu, String kisiTel) async {
    final uuid = Uuid();
    return KodUretCevap(
      success: 1,
      message: uuid.v4(),
    );
  }

  @override
  Future<UrunCevap> kahveWithKategoriId(UrunKategori urunKategori) async {
    final kod = urunKategori.kategoriKodu;
    final urunler = Urunler.dummyUrunler.where((u) => u.urunKategoriId == kod).toList();
    return UrunCevap(kahveUrun: urunler);
  }

  @override
  Future<KodUretCevap> kodSil(String dogrulamaKodu) async {
    return KodUretCevap(success: 1, message: "Kod başarıyla silindi");
  }
}
