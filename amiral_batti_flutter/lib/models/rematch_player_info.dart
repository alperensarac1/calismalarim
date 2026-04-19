class RematchPlayerInfo {
  final String id;
  final String name;
  final bool wantsRematch;

  RematchPlayerInfo({
    required this.id,
    required this.name,
    required this.wantsRematch,
  });

  factory RematchPlayerInfo.fromJson(Map<String, dynamic> json) {
    return RematchPlayerInfo(
      id: json['id'] ?? '',
      name: json['name'] ?? '',
      wantsRematch: json['wantsRematch'] ?? false,
    );
  }
}
