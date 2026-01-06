import 'package:flutter/foundation.dart';

import '../dao/sozluk_dao.dart';
import '../model/comment.dart';
import '../model/entry.dart';
import '../model/simple_response.dart';
import '../util/states.dart';


class EntryDetayViewModel extends ChangeNotifier {
  final SozlukDao _dao;

  EntryDetayViewModel({SozlukDao? dao}) : _dao = dao ?? SozlukDao();

  Entry? entry;
  List<Comment> comments = [];
  EntryDetailUiState ui = const EntryDetailUiState();

  Future<void> loadEntry(int entryId) async {
    ui = ui.copyWith(loadingEntry: true, error: null);
    notifyListeners();

    try {
      entry = await _dao.getEntryById(entryId);
      ui = ui.copyWith(loadingEntry: false, error: null);
    } catch (_) {
      ui = ui.copyWith(loadingEntry: false, error: 'Bağlantı hatası');
    }

    notifyListeners();
  }

  Future<void> loadComments(int entryId) async {
    ui = ui.copyWith(loadingComments: true, error: null);
    notifyListeners();

    try {
      comments = await _dao.getCommentsByEntry(entryId);
      ui = ui.copyWith(loadingComments: false, error: null);
    } catch (_) {
      ui = ui.copyWith(loadingComments: false, error: 'Bağlantı hatası');
    }

    notifyListeners();
  }

  Future<void> addComment({
    required int entryId,
    required int userId,
    required String text,
  }) async {
    ui = ui.copyWith(posting: true, error: null);
    notifyListeners();

    try {
      final res = await _dao.addComment(
        entryId: entryId,
        userId: userId,
        commentText: text,
      );

      ui = ui.copyWith(posting: false);

      if (res.success) {
        await loadComments(entryId);
      } else {
        ui = ui.copyWith(error: res.message ?? 'Yorum eklenemedi');
        notifyListeners();
      }
    } catch (_) {
      ui = ui.copyWith(posting: false, error: 'Bağlantı hatası');
      notifyListeners();
    }
  }

  Future<void> voteComment({
    required int entryId,
    required int commentId,
    required int userId,
    required bool isLike,
  }) async {
    try {
      final SimpleResponse res = await _dao.voteComment(
        commentId: commentId,
        userId: userId,
        isLike: isLike,
      );
      if (res.success) {
        await loadComments(entryId);
      }
    } catch (_) {
      // Kotlin'de de sessiz geçiyordu
    }
  }
}
