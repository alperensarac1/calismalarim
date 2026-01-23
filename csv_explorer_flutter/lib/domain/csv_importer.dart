import 'dart:convert';
import 'dart:io';
import 'package:csv/csv.dart';

import '../data/row_entity.dart';

class CsvImportResult {
  final List<String> headers;
  final List<RowEntity> rows;
  CsvImportResult({required this.headers, required this.rows});
}

class DynamicCsvImporter {
  static CsvImportResult importFile(File file) {
    final text = file.readAsStringSync(encoding: utf8);

    final converter = const CsvToListConverter(
      fieldDelimiter: ',',
      eol: '\n',
      shouldParseNumbers: false,
    );

    final table = converter.convert(text);
    if (table.isEmpty) return CsvImportResult(headers: [], rows: []);

    final headers = table.first.map((e) => (e?.toString() ?? '').trim()).where((e) => e.isNotEmpty).toList();

    final rows = <RowEntity>[];
    for (int r = 1; r < table.length; r++) {
      final line = table[r];
      if (line.isEmpty) continue;

      final map = <String, String>{};
      for (int c = 0; c < headers.length; c++) {
        final key = headers[c];
        final val = (c < line.length ? (line[c]?.toString() ?? '') : '').trim();
        if (val.isNotEmpty) map[key] = val;
      }

      final externalId = _guessExternalId(headers, map);
      rows.add(RowEntity(externalId: externalId, dataJson: jsonEncode(map)));
    }

    return CsvImportResult(headers: headers, rows: rows);
  }

  static String? _guessExternalId(List<String> headers, Map<String, String> m) {
    const candidates = ['id', 'ID', 'Id', 'user_id', 'uid', 'pk'];
    for (final c in candidates) {
      if (headers.contains(c) && (m[c]?.trim().isNotEmpty ?? false)) return m[c]!.trim();
    }
    return null;
  }
}
