import 'package:flutter/foundation.dart';
import '../dao/sozluk_dao.dart';
import '../model/entry.dart';
import '../model/simple_response.dart';
import '../util/debouncer.dart';
import '../util/states.dart';

class ProfilViewModel extends ChangeNotifier {
  final SozlukDao _dao;
  final Debouncer _debouncer = Debouncer(milliseconds: 300);

  ProfilViewModel({SozlukDao? dao}) : _dao = dao ?? SozlukDao();

  EntriesUiState ui = const EntriesUiState();
  List<Entry> _all = [];
  String _searchQuery = '';

  SimpleResponse? deleteResult;

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

  Future<void> loadUserEntries(int userId) async {
    ui = ui.copyWith(loading: true, error: null);
    notifyListeners();

    try {
      _all = await _dao.getEntriesByUser(userId);
      ui = ui.copyWith(loading: false, error: null);
    } catch (_) {
      ui = ui.copyWith(loading: false, error: 'Bağlantı hatası');
    }

    notifyListeners();
  }

  Future<void> deleteEntry({
    required int entryId,
    required int userId,
  }) async {
    ui = ui.copyWith(loading: true, error: null);
    deleteResult = null;
    notifyListeners();

    try {
      final res = await _dao.deleteEntry(entryId);
      deleteResult = res;
      ui = ui.copyWith(loading: false);

      if (res.success) {
        await loadUserEntries(userId);
      }
    } catch (_) {
      ui = ui.copyWith(loading: false, error: 'Bağlantı hatası');
      deleteResult = SimpleResponse(success: false, message: 'Bağlantı hatası');
    }

    notifyListeners();
  }

  @override
  void dispose() {
    _debouncer.dispose();
    super.dispose();
  }
}
