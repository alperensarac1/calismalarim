import 'package:shared_preferences/shared_preferences.dart';

import '../model/UygulamalarModel.dart';

class UygulamalarRepository {
  Future<void> copKutusunaTasi(UygulamalarModel uygulama) async {
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    await prefs.setBool(uygulama.uygulamaAdi.replaceAll(" ", "") + "Cop", true);
  }

  Future<void> copKutusundanCikar(UygulamalarModel uygulama) async {
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    await prefs.setBool(uygulama.uygulamaAdi.replaceAll(" ", "") + "Cop", false);
  }
}
