import 'package:flutter/material.dart';
import 'Not.dart';
import 'NoteDatabase.dart';

class NotDefteriViewModel extends ChangeNotifier {
  final NoteDatabase _db = NoteDatabase.instance;
  List<Note> _notlar = [];

  List<Note> get notlar => _notlar;

  NotDefteriViewModel() {
    getNotlarim();
  }

  Future<void> getNotlarim() async {
    _notlar = await _db.getNotlarim();
    notifyListeners();
  }

  Future<void> notAra(String query) async {
    if (query.isEmpty) {
      await getNotlarim();
    } else {
      _notlar = await _db.notAra(query);
      notifyListeners();
    }
  }

  Future<Note?> getNotById(int id) async {
    return await _db.getNotById(id);
  }

  Future<void> yeniNot(Note not) async {
    if (not.id == 0) {
      await _db.yeniNot(not);
    } else {
      await _db.notGuncelle(not);
    }
    await getNotlarim();
  }

  Future<void> notSil(int notId) async {
    await _db.notSil(notId);
    await getNotlarim();
  }
}
