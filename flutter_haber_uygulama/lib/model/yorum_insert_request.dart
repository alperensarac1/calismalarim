// yorum_insert_request.dart
class YorumInsertRequest {
  final int haber_id;
  final String takma_ad;
  final String yorum_metni;

  const YorumInsertRequest({
    required this.haber_id,
    required this.takma_ad,
    required this.yorum_metni,
  });

  factory YorumInsertRequest.fromJson(Map<String, dynamic> json) {
    return YorumInsertRequest(
      haber_id: (json['haber_id'] ?? 0) as int,
      takma_ad: (json['takma_ad'] ?? '') as String,
      yorum_metni: (json['yorum_metni'] ?? '') as String,
    );
  }

  Map<String, dynamic> toJson() => {
    'haber_id': haber_id,
    'takma_ad': takma_ad,
    'yorum_metni': yorum_metni,
  };
}
