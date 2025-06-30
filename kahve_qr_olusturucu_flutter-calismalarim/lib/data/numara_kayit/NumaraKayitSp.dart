
import 'package:shared_preferences/shared_preferences.dart';

class NumaraKayitSp {
  final SharedPreferences sp;

  NumaraKayitSp(this.sp);

  void numaraKaydedildiMiSet() {
    sp.setBool("kullaniciNumarasiKayitliMi", true);
  }

  bool numaraKaydedildiMi() {
    return sp.getBool("kullaniciNumarasiKayitliMi") ?? false;
  }

  String numaraGetir() {
    return sp.getString("numara") ?? "";
  }

  void numaraKaydet(String kullaniciNumarasi) {
    sp.setString("numara", kullaniciNumarasi);
    numaraKaydedildiMiSet();
  }
}
