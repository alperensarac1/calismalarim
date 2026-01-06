import 'dart:convert';
import 'package:http/http.dart' as http;

import '../model/comment.dart';
import '../model/entry.dart';
import '../model/simple_response.dart';
import '../model/vote_request.dart';



class SozlukApiService {
  static const String baseUrl =
      'https://alperensaracdeneme.com/sozluk/';

  /* ---------------- AUTH ---------------- */

  static Future<SimpleResponse> registerUser({
    required String username,
    required String email,
    required String password,
  }) async {
    final response = await http.post(
      Uri.parse('${baseUrl}sozluk_register.php'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({
        'username': username,
        'email': email,
        'password': password,
      }),
    );

    return SimpleResponse.fromJson(jsonDecode(response.body));
  }

  static Future<SimpleResponse> loginUser({
    required String username,
    required String password,
  }) async {
    final response = await http.post(
      Uri.parse('${baseUrl}sozluk_login.php'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({
        'username': username,
        'password': password,
      }),
    );

    return SimpleResponse.fromJson(jsonDecode(response.body));
  }

  /* ---------------- ENTRY ---------------- */

  static Future<SimpleResponse> addEntry({
    required int userId,
    required String title,
    required String content,
  }) async {
    final response = await http.post(
      Uri.parse('${baseUrl}sozluk_entry_insert.php'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({
        'user_id': userId.toString(),
        'title': title,
        'content': content,
      }),
    );

    return SimpleResponse.fromJson(jsonDecode(response.body));
  }

  static Future<List<Entry>> getAllEntries() async {
    final response = await http.get(
      Uri.parse('${baseUrl}sozluk_entry_list.php'),
    );

    final List list = jsonDecode(response.body);
    return list.map((e) => Entry.fromJson(e)).toList();
  }

  static Future<List<Entry>> getEntriesByUser(int userId) async {
    final response = await http.get(
      Uri.parse(
        '${baseUrl}sozluk_entry_by_user.php?user_id=$userId',
      ),
    );

    final List list = jsonDecode(response.body);
    return list.map((e) => Entry.fromJson(e)).toList();
  }

  static Future<Entry?> getEntryById(int entryId) async {
    final response = await http.get(
      Uri.parse(
        '${baseUrl}sozluk_entry_get.php?entry_id=$entryId',
      ),
    );

    if (response.body.isEmpty) return null;
    return Entry.fromJson(jsonDecode(response.body));
  }

  static Future<SimpleResponse> deleteEntry({
    required int entryId,
  }) async {
    final response = await http.post(
      Uri.parse('${baseUrl}sozluk_entry_delete.php'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({
        'entry_id': entryId,
      }),
    );

    return SimpleResponse.fromJson(jsonDecode(response.body));
  }

  /* ---------------- COMMENT ---------------- */

  static Future<SimpleResponse> addComment({
    required int entryId,
    required int userId,
    required String commentText,
  }) async {
    final response = await http.post(
      Uri.parse('${baseUrl}sozluk_comment_insert.php'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({
        'entry_id': entryId.toString(),
        'user_id': userId.toString(),
        'comment_text': commentText,
      }),
    );

    return SimpleResponse.fromJson(jsonDecode(response.body));
  }

  static Future<List<Comment>> getCommentsByEntry(int entryId) async {
    final response = await http.get(
      Uri.parse(
        '${baseUrl}sozluk_comments_by_entry.php?entry_id=$entryId',
      ),
    );

    final List list = jsonDecode(response.body);
    return list.map((e) => Comment.fromJson(e)).toList();
  }

  /* ---------------- LIKE / DISLIKE ---------------- */

  static Future<SimpleResponse> likeOrDislikeComment(
      VoteRequest request) async {
    final response = await http.post(
      Uri.parse('${baseUrl}sozluk_like_comment.php'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode(request.toJson()),
    );

    return SimpleResponse.fromJson(jsonDecode(response.body));
  }
}
