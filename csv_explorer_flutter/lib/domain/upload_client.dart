import 'dart:convert';
import 'dart:io';

import 'package:http/http.dart' as http;

class UploadClient {
  static Future<String> uploadCsv(String endpoint, File file) async {
    final req = http.MultipartRequest('POST', Uri.parse(endpoint));

    req.files.add(await http.MultipartFile.fromPath('csv', file.path, filename: file.uri.pathSegments.last));

    final res = await req.send();
    final body = await res.stream.bytesToString();

    if (res.statusCode < 200 || res.statusCode >= 300) {
      throw Exception('Upload failed (${res.statusCode}): $body');
    }

    final urlFromJson = _tryParseUrlFromJson(body);
    if (urlFromJson != null && urlFromJson.isNotEmpty) return urlFromJson;

    final m = RegExp(r'https?://\S+').firstMatch(body)?.group(0);
    if (m != null && m.isNotEmpty) return m;

    throw Exception('No download URL found. Response: $body');
  }

  static String? _tryParseUrlFromJson(String body) {
    try {
      final o = jsonDecode(body);
      if (o is Map) {
        String pick(String k) => (o[k]?.toString() ?? '').trim();
        final a = pick('download_url');
        if (a.isNotEmpty) return a;
        final b = pick('url');
        if (b.isNotEmpty) return b;
        final c = pick('file_url');
        if (c.isNotEmpty) return c;
        final d = pick('link');
        if (d.isNotEmpty) return d;
      }
    } catch (_) {}
    return null;
  }
}
