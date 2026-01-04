import 'package:shared_preferences/shared_preferences.dart';

class Prefs {
  static const _kStudentNo = "student_no";
  static const _kDeviceId = "device_id";

  Future<String?> getStudentNo() async {
    final sp = await SharedPreferences.getInstance();
    final v = sp.getString(_kStudentNo);
    return (v == null || v.trim().isEmpty) ? null : v.trim();
  }

  Future<void> setStudentNo(String no) async {
    final sp = await SharedPreferences.getInstance();
    await sp.setString(_kStudentNo, no.trim());
  }

  Future<String?> getDeviceId() async {
    final sp = await SharedPreferences.getInstance();
    return sp.getString(_kDeviceId);
  }

  Future<void> setDeviceId(String id) async {
    final sp = await SharedPreferences.getInstance();
    await sp.setString(_kDeviceId, id);
  }
}
