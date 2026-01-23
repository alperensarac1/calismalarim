class RowEntity {
  final int? localId;
  final String? externalId;
  final String dataJson;

  RowEntity({this.localId, this.externalId, required this.dataJson});

  Map<String, Object?> toMap() => {
    'local_id': localId,
    'external_id': externalId,
    'data_json': dataJson,
  };

  static RowEntity fromMap(Map<String, Object?> m) => RowEntity(
    localId: m['local_id'] as int?,
    externalId: m['external_id'] as String?,
    dataJson: (m['data_json'] as String?) ?? '{}',
  );
}
