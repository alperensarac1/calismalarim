import '../model/comment.dart';
import '../model/entry.dart';
import '../model/simple_response.dart';
import '../model/vote_request.dart';
import '../service/sozluk_api_service.dart';

class SozlukDao {
  Future<SimpleResponse> register({
    required String username,
    required String password,
    required String email,
  }) {
    return SozlukApiService.registerUser(
      username: username,
      email: email,
      password: password,
    );
  }

  // Login
  Future<SimpleResponse> login({
    required String username,
    required String password,
  }) {
    return SozlukApiService.loginUser(
      username: username,
      password: password,
    );
  }

  // Entry Ekle
  Future<SimpleResponse> addEntry({
    required int userId,
    required String title,
    required String content,
  }) {
    return SozlukApiService.addEntry(
      userId: userId,
      title: title,
      content: content,
    );
  }

  Future<List<Entry>> getAllEntries() {
    return SozlukApiService.getAllEntries();
  }

  Future<List<Entry>> getEntriesByUser(int userId) {
    return SozlukApiService.getEntriesByUser(userId);
  }

  Future<SimpleResponse> addComment({
    required int entryId,
    required int userId,
    required String commentText,
  }) {
    return SozlukApiService.addComment(
      entryId: entryId,
      userId: userId,
      commentText: commentText,
    );
  }

  Future<List<Comment>> getCommentsByEntry(int entryId) {
    return SozlukApiService.getCommentsByEntry(entryId);
  }

  Future<SimpleResponse> voteComment({
    required int commentId,
    required int userId,
    required bool isLike,
  }) {
    final req = VoteRequest(
      commentId: commentId,
      userId: userId,
      isLike: isLike ? 1 : 0,
    );
    return SozlukApiService.likeOrDislikeComment(req);
  }

  Future<SimpleResponse> deleteEntry(int entryId) {
    return SozlukApiService.deleteEntry(entryId: entryId);
  }

  Future<Entry?> getEntryById(int entryId) {
    return SozlukApiService.getEntryById(entryId);
  }
}
