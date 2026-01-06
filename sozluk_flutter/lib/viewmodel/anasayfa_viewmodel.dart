import 'package:flutter/foundation.dart';

import '../dao/sozluk_dao.dart';
import '../model/entry.dart';
import '../util/debouncer.dart';
import '../util/states.dart';

class AnaSayfaViewModel extends ChangeNotifier {
  final SozlukDao _dao;
  final Debouncer _debouncer = Debouncer(milliseconds: 300);

  AnaSayfaViewModel({SozlukDao? dao}) : _dao = dao ?? SozlukDao();

  EntriesUiState ui = const EntriesUiState();
  List<Entry> _all = [];
  String _searchQuery = '';

  String get searchQuery => _searchQuery;

  List<Entry> get entries {
    if (_searchQuery.trim().isEmpty) return _all;
    final q = _searchQuery.toLowerCase();
    return _all.where((e) => e.title.toLowerCase().contains(q)).toList();
  }

  void setSearchQuery(String query) {
    _searchQuery = query;
    _debouncer.run(() => notifyListeners());
  }

  Future<void> loadMostCommentedEntriesToday() async {
    ui = ui.copyWith(loading: true, error: null);
    notifyListeners();

    try {
      final list = await _dao.getAllEntries();
      _all = [...list]..sort((a, b) => b.id.compareTo(a.id));
      ui = ui.copyWith(loading: false, error: null);
    } catch (_) {
      ui = ui.copyWith(loading: false, error: 'Bağlantı hatası');
    }

    notifyListeners();
  }

  @override
  void dispose() {
    _debouncer.dispose();
    super.dispose();
  }
}
