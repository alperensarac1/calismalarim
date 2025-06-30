

import '../model/CRUDCevap.dart';
import '../model/KodUretCevap.dart';
import '../model/UrunCevap.dart';
import '../model/UrunKategori.dart';

abstract class Services {
  Future<CRUDCevap> kullaniciEkle(String kisiTel);
  Future<UrunCevap> tumKahveler();
  Future<KodUretCevap> kodUret(String dogrulamaKodu, String kisiTel);
  Future<UrunCevap> kahveWithKategoriId(UrunKategori urunKategori);
  Future<KodUretCevap> kodSil(String dogrulamaKodu);
}
