import 'dart:convert';
import 'dart:io';

import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:fluttertoast/fluttertoast.dart';
import 'package:url_launcher/url_launcher.dart';

import 'data/db.dart';
import 'data/row_entity.dart';
import 'domain/csv_importer.dart';
import 'domain/upload_client.dart';

void main() {
  runApp(const CsvExplorerApp());
}

class CsvExplorerApp extends StatelessWidget {
  const CsvExplorerApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'CSV Explorer',
      theme: ThemeData(useMaterial3: true),
      home: const MainPage(),
    );
  }
}

class MainPage extends StatefulWidget {
  const MainPage({super.key});

  @override
  State<MainPage> createState() => _MainPageState();
}

class _MainPageState extends State<MainPage> {
  static const uploadEndpoint = 'https://alperensaracdeneme.com/deneme/upload_csv.php';

  bool loading = false;
  String infoText = '0 records';
  String selectedColumn = 'ALL_COLUMNS';
  String query = '';
  String? error;

  List<String> headers = [];
  List<RowEntity> records = [];

  File? lastPickedFile;

  @override
  void initState() {
    super.initState();
    _refreshAll();
  }

  Future<void> _refreshAll() async {
    final list = await AppDb.getAll();
    setState(() {
      records = list;
      infoText = '${list.length} records';
    });
  }

  Future<void> _pickCsv() async {
    final res = await FilePicker.platform.pickFiles(
      type: FileType.custom,
      allowedExtensions: ['csv', 'txt'],
      withData: false,
    );
    if (res == null || res.files.isEmpty) return;

    final path = res.files.single.path;
    if (path == null) return;

    final file = File(path);
    setState(() {
      loading = true;
      error = null;
      infoText = 'Importing...';
      lastPickedFile = file;
    });

    try {
      final import = DynamicCsvImporter.importFile(file);
      await AppDb.insertAll(import.rows);
      final list = await AppDb.getAll();

      setState(() {
        headers = import.headers;
        records = list;
        loading = false;
        infoText = 'Imported: ${import.rows.length} rows';
      });

      Fluttertoast.showToast(msg: 'Import OK');
    } catch (e) {
      setState(() {
        loading = false;
        error = 'Import error: $e';
        infoText = 'Import failed';
      });
    }
  }

  Future<void> _applyFilter() async {
    setState(() {
      loading = true;
      error = null;
      infoText = 'Filtering...';
    });

    try {
      final q = query.trim();
      List<RowEntity> list;
      if (q.isEmpty) {
        list = await AppDb.getAll();
      } else if (selectedColumn == 'ALL_COLUMNS') {
        list = await AppDb.searchAll(q);
      } else {
        list = await AppDb.searchColumn(selectedColumn, q);
      }

      setState(() {
        records = list;
        loading = false;
        infoText = '${list.length} records (filter: $selectedColumn)';
      });
    } catch (e) {
      setState(() {
        loading = false;
        error = 'Filter error: $e';
      });
    }
  }

  Future<void> _clearFilter() async {
    setState(() {
      query = '';
      selectedColumn = 'ALL_COLUMNS';
    });
    await _refreshAll();
  }

  Future<void> _clearDb() async {
    setState(() {
      loading = true;
      error = null;
      infoText = 'Clearing DB...';
    });
    try {
      await AppDb.clear();
      setState(() {
        loading = false;
        headers = [];
        records = [];
        lastPickedFile = null;
        query = '';
        selectedColumn = 'ALL_COLUMNS';
        infoText = 'Database cleared';
      });
    } catch (e) {
      setState(() {
        loading = false;
        error = 'Clear error: $e';
      });
    }
  }

  Future<void> _uploadAndOpenXls() async {
    if (lastPickedFile == null) {
      Fluttertoast.showToast(msg: 'Önce CSV seçmelisin');
      return;
    }

    setState(() {
      loading = true;
      error = null;
      infoText = 'Uploading...';
    });

    try {
      final url = await UploadClient.uploadCsv(uploadEndpoint, lastPickedFile!);

      setState(() {
        loading = false;
        infoText = 'Upload done. Opening...';
      });

      final uri = Uri.parse(url);
      if (!await launchUrl(uri, mode: LaunchMode.externalApplication)) {
        throw Exception('Cannot open: $url');
      }
    } catch (e) {
      setState(() {
        loading = false;
        error = 'Upload error: $e';
        infoText = 'Upload failed';
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final cols = ['ALL_COLUMNS', ...headers];

    return Scaffold(
      appBar: AppBar(title: const Text('CSV Explorer (Flutter)')),
      body: Padding(
        padding: const EdgeInsets.all(14),
        child: Column(
          children: [
            Wrap(
              spacing: 10,
              runSpacing: 10,
              children: [
                FilledButton(
                  onPressed: loading ? null : _pickCsv,
                  child: const Text('Select CSV'),
                ),
                OutlinedButton(
                  onPressed: (loading || lastPickedFile == null) ? null : _uploadAndOpenXls,
                  child: const Text('Get .xls'),
                ),
                OutlinedButton(
                  onPressed: loading ? null : _applyFilter,
                  child: const Text('Filter'),
                ),
                OutlinedButton(
                  onPressed: loading ? null : _clearFilter,
                  child: const Text('Clear'),
                ),
                TextButton(
                  onPressed: loading ? null : _clearDb,
                  child: const Text('Clear DB'),
                ),
              ],
            ),
            const SizedBox(height: 10),
            Align(
              alignment: Alignment.centerLeft,
              child: Text(infoText, style: const TextStyle(fontWeight: FontWeight.bold)),
            ),
            const SizedBox(height: 10),

            // Column dropdown
            DropdownButtonFormField<String>(
              value: selectedColumn,
              items: cols.map((c) => DropdownMenuItem(value: c, child: Text(c))).toList(),
              onChanged: loading
                  ? null
                  : (v) {
                if (v == null) return;
                setState(() => selectedColumn = v);
              },
              decoration: const InputDecoration(labelText: 'Column', border: OutlineInputBorder()),
            ),
            const SizedBox(height: 10),

            TextFormField(
              enabled: !loading,
              initialValue: query,
              onChanged: (v) => query = v,
              decoration: const InputDecoration(
                labelText: 'Search',
                border: OutlineInputBorder(),
              ),
            ),

            const SizedBox(height: 10),
            if (loading) const LinearProgressIndicator(),
            if (error != null) ...[
              const SizedBox(height: 8),
              Align(
                alignment: Alignment.centerLeft,
                child: Text(error!, style: TextStyle(color: Theme.of(context).colorScheme.error)),
              ),
            ],
            const SizedBox(height: 10),

            Expanded(
              child: ListView.separated(
                itemCount: records.length,
                separatorBuilder: (_, __) => const SizedBox(height: 10),
                itemBuilder: (ctx, i) {
                  final it = records[i];
                  final obj = _safeJson(it.dataJson);

                  final id = (obj['id'] ?? '').toString();
                  final first = (obj['first_name'] ?? obj['firstname'] ?? '').toString();
                  final last = (obj['last_name'] ?? obj['lastname'] ?? '').toString();

                  final title = (id.isNotEmpty && (first.isNotEmpty || last.isNotEmpty))
                      ? '#$id  ${(first + ' ' + last).trim()}'
                      : (id.isNotEmpty)
                      ? '#$id'
                      : (first.isNotEmpty || last.isNotEmpty)
                      ? (first + ' ' + last).trim()
                      : 'Row';

                  final subtitle = _subtitle(obj);

                  return Card(
                    child: ListTile(
                      title: Text(title),
                      subtitle: Text(subtitle),
                      onTap: () {
                        Navigator.of(context).push(
                          MaterialPageRoute(
                            builder: (_) => DetailsPage(jsonStr: it.dataJson, headers: headers),
                          ),
                        );
                      },
                    ),
                  );
                },
              ),
            ),
          ],
        ),
      ),
    );
  }

  Map<String, dynamic> _safeJson(String s) {
    try {
      final o = jsonDecode(s);
      if (o is Map<String, dynamic>) return o;
    } catch (_) {}
    return {};
  }

  String _subtitle(Map<String, dynamic> obj) {
    final lastSeen = (obj['last_seen'] ?? '').toString();
    final country = (obj['country_title'] ?? '').toString();
    final city = (obj['city_title'] ?? '').toString();

    final parts = <String>[];
    if (lastSeen.isNotEmpty) parts.add('Last seen: $lastSeen');
    final loc = [country, city].where((e) => e.trim().isNotEmpty).join(' / ');
    if (loc.isNotEmpty) parts.add(loc);

    return parts.isEmpty ? 'Tap to view details' : parts.join(' • ');
  }
}

class DetailsPage extends StatefulWidget {
  final String jsonStr;
  final List<String> headers;

  const DetailsPage({super.key, required this.jsonStr, required this.headers});

  @override
  State<DetailsPage> createState() => _DetailsPageState();
}

class _DetailsPageState extends State<DetailsPage> {
  String q = '';

  @override
  Widget build(BuildContext context) {
    final obj = _safeJson(widget.jsonStr);
    final fields = _buildFields(widget.headers, obj);
    final filtered = q.trim().isEmpty
        ? fields
        : fields.where((e) => e.key.toLowerCase().contains(q.toLowerCase()) || e.value.toLowerCase().contains(q.toLowerCase())).toList();

    return Scaffold(
      appBar: AppBar(title: const Text('Row Details')),
      body: Padding(
        padding: const EdgeInsets.all(14),
        child: Column(
          children: [
            TextFormField(
              onChanged: (v) => setState(() => q = v),
              decoration: const InputDecoration(labelText: 'Search in fields', border: OutlineInputBorder()),
            ),
            const SizedBox(height: 10),

            Wrap(
              spacing: 10,
              runSpacing: 10,
              children: [
                FilledButton(
                  onPressed: () {
                    Clipboard.setData(ClipboardData(text: widget.jsonStr));
                    Fluttertoast.showToast(msg: 'Copied JSON');
                  },
                  child: const Text('Copy JSON'),
                ),
                OutlinedButton(
                  onPressed: () {
                    final csv = _buildCsv(widget.headers, obj);
                    Clipboard.setData(ClipboardData(text: csv));
                    Fluttertoast.showToast(msg: 'Copied CSV row');
                  },
                  child: const Text('Copy CSV'),
                ),
              ],
            ),

            const SizedBox(height: 10),
            Align(
              alignment: Alignment.centerLeft,
              child: Text('${filtered.length} fields', style: const TextStyle(fontWeight: FontWeight.bold)),
            ),
            const SizedBox(height: 10),

            Expanded(
              child: ListView.separated(
                itemCount: filtered.length,
                separatorBuilder: (_, __) => const SizedBox(height: 10),
                itemBuilder: (_, i) {
                  final it = filtered[i];
                  return Card(
                    child: Padding(
                      padding: const EdgeInsets.all(12),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(it.key, style: const TextStyle(fontWeight: FontWeight.bold)),
                          const SizedBox(height: 6),
                          Text(it.value),
                        ],
                      ),
                    ),
                  );
                },
              ),
            ),
          ],
        ),
      ),
    );
  }

  Map<String, dynamic> _safeJson(String s) {
    try {
      final o = jsonDecode(s);
      if (o is Map<String, dynamic>) return o;
    } catch (_) {}
    return {};
  }

  List<_FieldItem> _buildFields(List<String> headers, Map<String, dynamic> obj) {
    final out = <_FieldItem>[];

    if (headers.isNotEmpty) {
      for (final h in headers) {
        final v = (obj[h] ?? '').toString();
        out.add(_FieldItem(h, v.isEmpty ? '-' : v));
      }

      final extraKeys = obj.keys.where((k) => !headers.contains(k)).toList()..sort();
      for (final k in extraKeys) {
        final v = (obj[k] ?? '').toString();
        out.add(_FieldItem(k, v.isEmpty ? '-' : v));
      }
    } else {
      final keys = obj.keys.toList()..sort();
      for (final k in keys) {
        final v = (obj[k] ?? '').toString();
        out.add(_FieldItem(k, v.isEmpty ? '-' : v));
      }
    }

    return out;
  }

  String _buildCsv(List<String> headers, Map<String, dynamic> obj) {
    if (headers.isEmpty) return jsonEncode(obj);
    final headerLine = headers.join(',');
    final rowLine = headers.map((h) => _esc((obj[h] ?? '').toString())).join(',');
    return '$headerLine\n$rowLine';
  }

  String _esc(String value0) {
    var value = value0;
    final needsQuotes = value.contains(',') || value.contains('"') || value.contains('\n') || value.contains('\r');
    value = value.replaceAll('"', '""');
    if (needsQuotes) value = '"$value"';
    return value;
  }
}

class _FieldItem {
  final String key;
  final String value;
  _FieldItem(this.key, this.value);
}
