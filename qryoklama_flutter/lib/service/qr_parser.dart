import 'dart:convert';

class QRParser {
  Map<String, dynamic> parsePayload(String raw) {
    var s = (raw).trim();

    // "...." veya “....” tırnaklarını kırp
    if ((s.startsWith('"') && s.endsWith('"')) ||
        (s.startsWith('“') && s.endsWith('”'))) {
      s = s.substring(1, s.length - 1);
    }

    // Android: s.replace("\\\"", "\"").replace("\\\\", "\\");
    s = s.replaceAll(r'\"', '"').replaceAll(r'\\', r'\');

    // URL ise ?qr=... parametresini çek
    if (s.toLowerCase().startsWith("http")) {
      final uri = Uri.tryParse(s);
      final q = uri?.queryParameters["qr"];
      if (q != null && q.trim().startsWith("{")) {
        s = q.trim();
      }
    }

    // JSON değil ama base64'e benziyorsa decode dene
    if (!s.startsWith("{") && _looksLikeBase64(s)) {
      try {
        final decodedBytes = base64.decode(s);
        final decoded = utf8.decode(decodedBytes).trim();
        if (decoded.startsWith("{")) {
          s = decoded;
        }
      } catch (_) {
        // ignore
      }
    }

    if (!s.startsWith("{")) {
      final preview = s.length > 60 ? "${s.substring(0, 60)}..." : s;
      throw FormatException("Geçersiz QR: $preview");
    }

    final obj = jsonDecode(s);
    if (obj is Map<String, dynamic>) return obj;
    throw FormatException("QR JSON obje değil");
  }

  bool _looksLikeBase64(String s) {
    // Android regex: ^[A-Za-z0-9+/=\s]+$
    final re = RegExp(r'^[A-Za-z0-9+/=\s]+$');
    return re.hasMatch(s);
  }
}
