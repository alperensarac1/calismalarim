import 'package:path/path.dart' as p;
import 'package:sqflite/sqflite.dart';


import 'row_entity.dart';

class AppDb {
  static Database? _db;

  static Future<Database> get db async {
    if (_db != null) return _db!;
    final path = p.join(await getDatabasesPath(), 'csv_explorer_flutter.db');
    _db = await openDatabase(
      path,
      version: 1,
      onCreate: (d, v) async {
        await d.execute('''
          CREATE TABLE rows (
            local_id INTEGER PRIMARY KEY AUTOINCREMENT,
            external_id TEXT,
            data_json TEXT NOT NULL
          )
        ''');
      },
    );
    return _db!;
  }

  static Future<void> insertAll(List<RowEntity> items) async {
    final d = await db;
    final batch = d.batch();
    for (final it in items) {
      batch.insert('rows', it.toMap(), conflictAlgorithm: ConflictAlgorithm.replace);
    }
    await batch.commit(noResult: true);
  }

  static Future<List<RowEntity>> getAll() async {
    final d = await db;
    final rows = await d.query('rows', orderBy: 'local_id DESC');
    return rows.map(RowEntity.fromMap).toList();
  }

  static Future<void> clear() async {
    final d = await db;
    await d.delete('rows');
  }

  // Basit LIKE filtre: ALL_COLUMNS -> json string içinde arar
  static Future<List<RowEntity>> searchAll(String q) async {
    final d = await db;
    final rows = await d.query(
      'rows',
      where: 'data_json LIKE ?',
      whereArgs: ['%$q%'],
      orderBy: 'local_id DESC',
    );
    return rows.map(RowEntity.fromMap).toList();
  }

  // Kolon filtresi: JSON içinde "key":"...q..." arar (string tabanlı basit arama)
  static Future<List<RowEntity>> searchColumn(String key, String q) async {
    final d = await db;
    final pattern = '%"$key":"%$q%"%';
    final rows = await d.query(
      'rows',
      where: 'data_json LIKE ?',
      whereArgs: [pattern],
      orderBy: 'local_id DESC',
    );
    return rows.map(RowEntity.fromMap).toList();
  }
}
